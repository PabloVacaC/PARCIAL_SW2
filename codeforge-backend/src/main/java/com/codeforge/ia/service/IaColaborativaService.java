package com.codeforge.ia.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeforge.auth.security.AuthenticationUtil;
import com.codeforge.ia.dto.IaChatMensajeDTO;
import com.codeforge.ia.dto.IaDecisionRequestDTO;
import com.codeforge.ia.dto.IaPreguntaRequestDTO;
import com.codeforge.ia.dto.IaPropuestaDTO;
import com.codeforge.ia.dto.IaUmlFindingDTO;
import com.codeforge.ia.entity.IaConversacion;
import com.codeforge.ia.entity.IaMensaje;
import com.codeforge.ia.entity.IaMetrica;
import com.codeforge.ia.entity.IaPropuesta;
import com.codeforge.ia.repository.IaConversacionRepository;
import com.codeforge.ia.repository.IaMensajeRepository;
import com.codeforge.ia.repository.IaMetricaRepository;
import com.codeforge.ia.repository.IaPropuestaRepository;
import com.codeforge.modelo.entity.Modelo;
import com.codeforge.modelo.repository.ModeloRepository;
import com.codeforge.proyecto.entity.Proyecto;
import com.codeforge.proyecto.entity.ProyectoColaborador;
import com.codeforge.proyecto.repository.ProyectoColaboradorRepository;
import com.codeforge.proyecto.repository.ProyectoRepository;
import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

@Service
public class IaColaborativaService {

    private final IaConversacionRepository conversacionRepository;
    private final IaMensajeRepository mensajeRepository;
    private final IaPropuestaRepository propuestaRepository;
    private final IaMetricaRepository metricaRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProyectoColaboradorRepository colaboradorRepository;
    private final ModeloRepository modeloRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final IaLocalService iaLocalService;
    private final String proveedor;

    private final String apiKey;
    private final String model;

    public IaColaborativaService(
            IaConversacionRepository conversacionRepository,
            IaMensajeRepository mensajeRepository,
            IaPropuestaRepository propuestaRepository,
            IaMetricaRepository metricaRepository,
            ProyectoRepository proyectoRepository,
            ProyectoColaboradorRepository colaboradorRepository,
            ModeloRepository modeloRepository,
            UsuarioRepository usuarioRepository,
            SimpMessagingTemplate messagingTemplate,
            IaLocalService iaLocalService,
            @Value("${codeforge.ai.gemini.api-key:}") String apiKey,
            @Value("${codeforge.ai.gemini.model:gemini-3.5-flash}") String model,
            @Value("${codeforge.ai.proveedor:HIBRIDO}") String proveedor
    ) {
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.propuestaRepository = propuestaRepository;
        this.metricaRepository = metricaRepository;
        this.proyectoRepository = proyectoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.modeloRepository = modeloRepository;
        this.usuarioRepository = usuarioRepository;
        this.messagingTemplate = messagingTemplate;
        this.iaLocalService = iaLocalService;
        this.proveedor = (proveedor != null && !proveedor.isBlank()) ? proveedor.trim().toUpperCase() : "HIBRIDO";
        this.objectMapper = new ObjectMapper();

        if (apiKey == null) {
            apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null) {
                apiKey = System.getProperty("GEMINI_API_KEY");
            }
        }
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = (model != null && !model.isBlank()) ? model.trim() : "gemini-3.5-flash";
    }

    // ============================================================
    // VALIDACIÓN DE AISLAMIENTO Y PERMISOS DEL PROYECTO
    // ============================================================

    public Usuario validarAccesoProyecto(Long idProyecto) {
        String username = AuthenticationUtil.obtenerUsernameAutenticado();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));

        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe"));

        boolean esPropietario = proyecto.getPropietario().getIdUsuario().equals(usuario.getIdUsuario());
        boolean esColaborador = colaboradorRepository.existsByProyecto_IdProyectoAndUsuario_IdUsuario(
                idProyecto, usuario.getIdUsuario());

        if (!esPropietario && !esColaborador) {
            throw new AccessDeniedException("No tienes permiso para acceder a este proyecto ni a su IA colaborativa.");
        }

        return usuario;
    }

    public void validarPermisoModificacion(Long idProyecto, Usuario usuario) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe"));

        if (proyecto.getPropietario().getIdUsuario().equals(usuario.getIdUsuario())) {
            return; // Propietario tiene control total
        }

        Optional<ProyectoColaborador> colaboradorOpt = colaboradorRepository
                .findByProyecto_IdProyecto(idProyecto).stream()
                .filter(c -> c.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
                .findFirst();

        if (colaboradorOpt.isEmpty()) {
            throw new AccessDeniedException("No perteneces al proyecto.");
        }

        String rol = colaboradorOpt.get().getRolProyecto();
        if ("LECTOR".equalsIgnoreCase(rol)) {
            throw new AccessDeniedException("Los colaboradores con rol LECTOR no pueden aprobar ni aplicar cambios.");
        }
    }

    // ============================================================
    // OBTENER O CREAR CONVERSACIÓN COMPARTIDA
    // ============================================================

    @Transactional
    public IaConversacion obtenerOCrearConversacion(Long idProyecto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("El proyecto no existe"));

        return conversacionRepository.findByProyecto_IdProyecto(idProyecto)
                .orElseGet(() -> {
                    IaConversacion nueva = new IaConversacion(
                            proyecto,
                            "Sala de Desarrollo - " + proyecto.getNombre()
                    );
                    IaConversacion guardada = conversacionRepository.save(nueva);

                    // Mensaje de bienvenida inicial de la IA
                    IaMensaje bienvenida = new IaMensaje();
                    bienvenida.setConversacion(guardada);
                    bienvenida.setNombreEmisor("CodeForge AI");
                    bienvenida.setTipoEmisor("IA");
                    bienvenida.setModoIa("GENERAL");
                    bienvenida.setContenido("👋 ¡Hola equipo de **" + proyecto.getNombre() + "**! Soy su **IA Desarrolladora Colaborativa**.\n\n"
                            + "Estoy lista para participar como un miembro técnico más de su equipo. Puedo:\n"
                            + "- 📐 **Modelar y diagnosticar** su diagrama UML (detectar clases faltantes, inconsistencias de relaciones, multiplicidades).\n"
                            + "- 📋 **Consultar y auditar requisitos** funcionales y no funcionales.\n"
                            + "- 🏛️ **Evaluar la arquitectura** y diseñar la separación por capas.\n"
                            + "- 💻 **Generar y revisar código** Spring Boot alineado con el proyecto.\n"
                            + "- 🧪 **Proponer pruebas automatizadas** y detectar vulnerabilidades.\n"
                            + "- ✨ **Proponer modificaciones al diagrama**, las cuales **SOLO se aplicarán tras su aprobación explícita**.\n\n"
                            + "¿En qué podemos trabajar hoy?");
                    bienvenida.setFechaEnvio(LocalDateTime.now());
                    mensajeRepository.save(bienvenida);

                    return guardada;
                });
    }

    // ============================================================
    // CONSULTAR HISTORIAL DE MENSAJES DEL PROYECTO
    // ============================================================

    @Transactional(readOnly = true)
    public List<IaChatMensajeDTO> obtenerHistorial(Long idProyecto) {
        validarAccesoProyecto(idProyecto);
        IaConversacion conversacion = obtenerOCrearConversacion(idProyecto);

        List<IaMensaje> mensajes = mensajeRepository
                .findByConversacion_IdConversacionOrderByFechaEnvioAsc(conversacion.getIdConversacion());

        return mensajes.stream()
                .map(this::convertirAMensajeDTO)
                .toList();
    }

    // ============================================================
    // ENVIAR MENSAJE Y GENERAR RESPUESTA IA COLABORATIVA
    // ============================================================

    @Transactional
    public List<IaChatMensajeDTO> enviarMensajeYProcesar(Long idProyecto, IaPreguntaRequestDTO dto) {
        Usuario usuario = validarAccesoProyecto(idProyecto);
        IaConversacion conversacion = obtenerOCrearConversacion(idProyecto);

        // 1. Guardar mensaje del usuario humano
        IaMensaje mensajeUsuario = new IaMensaje();
        mensajeUsuario.setConversacion(conversacion);
        mensajeUsuario.setUsuario(usuario);
        mensajeUsuario.setNombreEmisor(usuario.getNombre() + " " + usuario.getApellido());
        mensajeUsuario.setTipoEmisor("USUARIO");
        mensajeUsuario.setModoIa(dto.getModoIa() != null ? dto.getModoIa() : "GENERAL");
        mensajeUsuario.setContenido(dto.getMensaje().trim());
        mensajeUsuario.setFechaEnvio(LocalDateTime.now());
        IaMensaje usuarioGuardado = mensajeRepository.save(mensajeUsuario);

        // Broadcast usuario mensaje vía WebSocket en tiempo real
        IaChatMensajeDTO dtoUsuario = convertirAMensajeDTO(usuarioGuardado);
        broadcastMensaje(idProyecto, dtoUsuario);

        // 2. Preparar contexto del proyecto y del modelo UML vigente
        Proyecto proyecto = proyectoRepository.findById(idProyecto).orElseThrow();
        Modelo modeloActual = obtenerModeloProyecto(idProyecto, dto.getIdModelo());

        String modo = (dto.getModoIa() != null && !dto.getModoIa().isBlank()) ? dto.getModoIa() : "GENERAL";
        String contexto = construirContexto(proyecto, modeloActual, dto.getMensaje());
        String systemPrompt = construirSystemPrompt(modo, proyecto, modeloActual);

        // 3. Invocar a IA (Estrategia Híbrida: Local / Gemini con Fallback)
        long inicioMs = System.currentTimeMillis();
        String respuestaTexto = null;
        boolean exito = true;
        String errorMsg = null;
        Integer promptTokens = null;
        Integer completionTokens = null;

        boolean forzarLocal = "LOCAL".equalsIgnoreCase(proveedor) || (apiKey == null || apiKey.isBlank());

        if (forzarLocal) {
            try {
                if (iaLocalService.estaDisponible()) {
                    respuestaTexto = iaLocalService.generarRespuesta(modo, systemPrompt, contexto, usuario.getNombre(), dto.getMensaje());
                } else {
                    throw new java.io.IOException("El motor local Ollama no está activo en " + iaLocalService.getLocalUrl());
                }
            } catch (Exception e) {
                System.err.println("CODEFORGE IA LOCAL: Error consultando IA local: " + e.getMessage());
                // Si la local falla pero hay Gemini configurado, intentar Gemini
                if (apiKey != null && !apiKey.isBlank()) {
                    try {
                        respuestaTexto = invocarGemini(systemPrompt, contexto, usuario.getNombre(), dto.getMensaje());
                    } catch (Exception exGem) {
                        exito = false;
                        errorMsg = exGem.getMessage();
                        respuestaTexto = generarRespuestaLocalDeRespaldo(modo, dto.getMensaje(), modeloActual);
                    }
                } else {
                    exito = false;
                    errorMsg = e.getMessage();
                    respuestaTexto = "⚠️ **Aviso de IA (Offline)**: No se pudo conectar al motor local (" + e.getMessage() + "). Se activó la respuesta asistida de respaldo.\n\n"
                            + generarRespuestaLocalDeRespaldo(modo, dto.getMensaje(), modeloActual);
                }
            }
        } else {
            // Modo HÍBRIDO o GEMINI: Intentar Gemini primero, con fallback automático a IA Local
            try {
                respuestaTexto = invocarGemini(systemPrompt, contexto, usuario.getNombre(), dto.getMensaje());
            } catch (Exception e) {
                System.err.println("CODEFORGE IA: Falló Gemini (" + e.getMessage() + "). Conmutando automáticamente a IA Local (Ollama)...");
                if (iaLocalService.estaDisponible()) {
                    try {
                        respuestaTexto = iaLocalService.generarRespuesta(modo, systemPrompt, contexto, usuario.getNombre(), dto.getMensaje());
                        exito = true;
                        errorMsg = null;
                    } catch (Exception exLocal) {
                        exito = false;
                        errorMsg = exLocal.getMessage();
                        respuestaTexto = "⚠️ **Aviso de IA**: Ocurrió un error tanto en la nube como en la IA local. Se activó la respuesta de respaldo.\n\n"
                                + generarRespuestaLocalDeRespaldo(modo, dto.getMensaje(), modeloActual);
                    }
                } else {
                    exito = false;
                    errorMsg = e.getMessage();
                    respuestaTexto = "⚠️ **Aviso de IA**: Falló el servicio en la nube y el motor local no está activo. Se activó la respuesta de respaldo.\n\n"
                            + generarRespuestaLocalDeRespaldo(modo, dto.getMensaje(), modeloActual);
                }
            }
        }

        long duracionMs = System.currentTimeMillis() - inicioMs;

        // Registrar métrica
        registrarMetrica(proyecto, usuario, modo, duracionMs, exito, errorMsg, promptTokens, completionTokens);

        // 4. Analizar si la respuesta de la IA contiene una propuesta formal estructurada
        ExtraccionPropuesta resultadoPropuesta = extraerYCrearPropuestaSiAplica(
                idProyecto, modeloActual, conversacion, usuario, respuestaTexto, dto.getGenerarPropuesta()
        );

        // 5. Guardar respuesta de la IA
        IaMensaje mensajeIa = new IaMensaje();
        mensajeIa.setConversacion(conversacion);
        mensajeIa.setNombreEmisor("CodeForge AI (" + traducirModo(modo) + ")");
        mensajeIa.setTipoEmisor("IA");
        mensajeIa.setModoIa(modo);
        mensajeIa.setContenido(resultadoPropuesta.textoLimpio);

        if (resultadoPropuesta.propuesta != null) {
            mensajeIa.setIdPropuestaAsociada(resultadoPropuesta.propuesta.getIdPropuesta());
            mensajeIa.setMetadatosJson(resultadoPropuesta.metadatosJson);
        }

        mensajeIa.setFechaEnvio(LocalDateTime.now());
        IaMensaje iaGuardado = mensajeRepository.save(mensajeIa);

        conversacion.setFechaActualizacion(LocalDateTime.now());
        conversacionRepository.save(conversacion);

        // Broadcast IA mensaje vía WebSocket en tiempo real
        IaChatMensajeDTO dtoIa = convertirAMensajeDTO(iaGuardado);
        if (resultadoPropuesta.propuesta != null) {
            dtoIa.setPropuestaAsociada(convertirAPropuestaDTO(resultadoPropuesta.propuesta));
            // Broadcast también la propuesta
            broadcastPropuesta(idProyecto, dtoIa.getPropuestaAsociada());
        }
        broadcastMensaje(idProyecto, dtoIa);

        return List.of(dtoUsuario, dtoIa);
    }

    // ============================================================
    // GESTIÓN DE PROPUESTAS DE MODIFICACIÓN
    // ============================================================

    @Transactional(readOnly = true)
    public List<IaPropuestaDTO> listarPropuestas(Long idProyecto) {
        validarAccesoProyecto(idProyecto);
        return propuestaRepository.findByProyecto_IdProyectoOrderByFechaCreacionDesc(idProyecto)
                .stream()
                .map(this::convertirAPropuestaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public IaPropuestaDTO obtenerPropuesta(Long idProyecto, Long idPropuesta) {
        validarAccesoProyecto(idProyecto);
        IaPropuesta propuesta = propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(idPropuesta, idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada"));
        return convertirAPropuestaDTO(propuesta);
    }

    @Transactional
    public IaPropuestaDTO decidirPropuesta(Long idProyecto, Long idPropuesta, IaDecisionRequestDTO dto) {
        Usuario usuario = validarAccesoProyecto(idProyecto);
        validarPermisoModificacion(idProyecto, usuario);

        IaPropuesta propuesta = propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(idPropuesta, idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada"));

        if (!"PENDIENTE".equalsIgnoreCase(propuesta.getEstado())) {
            throw new IllegalStateException("La propuesta no se encuentra pendiente de decisión (Estado actual: " + propuesta.getEstado() + ").");
        }

        String decision = dto.getDecision() != null ? dto.getDecision().toUpperCase().trim() : "";
        if ("APROBAR".equals(decision)) {
            propuesta.setEstado("APROBADA");
            propuesta.setAprobador(usuario);
            propuesta.setFechaDecision(LocalDateTime.now());

            publicarNotificacionSistema(
                    idProyecto,
                    propuesta.getConversacion(),
                    "✅ **Propuesta #" + propuesta.getIdPropuesta() + " APROBADA** por @" + usuario.getUsername() + ".\n"
                            + "Título: *" + propuesta.getTitulo() + "*\n"
                            + (dto.getComentario() != null && !dto.getComentario().isBlank() ? "Nota: " + dto.getComentario() + "\n" : "")
                            + "Lista para ser aplicada al modelo UML oficial."
            );
        } else if ("RECHAZAR".equals(decision)) {
            propuesta.setEstado("RECHAZADA");
            propuesta.setAprobador(usuario);
            propuesta.setFechaDecision(LocalDateTime.now());

            publicarNotificacionSistema(
                    idProyecto,
                    propuesta.getConversacion(),
                    "❌ **Propuesta #" + propuesta.getIdPropuesta() + " RECHAZADA** por @" + usuario.getUsername() + ".\n"
                            + "Título: *" + propuesta.getTitulo() + "*\n"
                            + (dto.getComentario() != null && !dto.getComentario().isBlank() ? "Motivo: " + dto.getComentario() : "El artefacto original se mantiene intacto.")
            );
        } else {
            throw new IllegalArgumentException("Decisión inválida. Use 'APROBAR' o 'RECHAZAR'.");
        }

        IaPropuesta guardada = propuestaRepository.save(propuesta);
        IaPropuestaDTO dtoResultado = convertirAPropuestaDTO(guardada);
        broadcastPropuesta(idProyecto, dtoResultado);
        return dtoResultado;
    }

    // ============================================================
    // APLICACIÓN CONTROLADA Y TRANSACCIONAL (FASE 8 Y 9)
    // ============================================================

    @Transactional
    public IaPropuestaDTO aplicarPropuesta(Long idProyecto, Long idPropuesta) {
        Usuario usuario = validarAccesoProyecto(idProyecto);
        validarPermisoModificacion(idProyecto, usuario);

        IaPropuesta propuesta = propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(idPropuesta, idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada"));

        if ("APLICADA".equalsIgnoreCase(propuesta.getEstado())) {
            throw new IllegalStateException("Esta propuesta ya ha sido aplicada previamente (Idempotencia garantizada).");
        }

        if ("RECHAZADA".equalsIgnoreCase(propuesta.getEstado())) {
            throw new IllegalStateException("No se puede aplicar una propuesta que ha sido rechazada.");
        }

        Modelo modelo = propuesta.getModelo();
        if (modelo != null && modelo.getIdModelo() != null) {
            modelo = modeloRepository.findById(modelo.getIdModelo()).orElse(modelo);
        } else {
            modelo = modeloRepository.findByProyecto_IdProyecto(idProyecto).stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No existe modelo UML asociado al proyecto"));
        }

        // CONTROL DE CONCURRENCIA Y VERSIONADO (Requisito fundamental 8):
        // Si otro usuario modificó el diagrama antes de aplicar esta propuesta, la versión habrá cambiado.
        if (!modelo.getVersion().equals(propuesta.getVersionOrigen())) {
            propuesta.setEstado("OBSOLETA");
            propuestaRepository.save(propuesta);

            String mensajeConflicto = "⚠️ **Conflicto de Concurrencia detectado**:\n"
                    + "La propuesta #" + propuesta.getIdPropuesta() + " fue generada sobre la versión `" + propuesta.getVersionOrigen() + "`,\n"
                    + "pero el modelo UML actual ya se encuentra en la versión `" + modelo.getVersion() + "` (modificado por otro desarrollador).\n\n"
                    + "Se bloqueó la aplicación para proteger el trabajo concurrente. Por favor solicite a la IA una reconciliación de cambios.";

            publicarNotificacionSistema(idProyecto, propuesta.getConversacion(), mensajeConflicto);
            throw new IllegalStateException("Conflicto de versión: La versión del modelo cambió de " + propuesta.getVersionOrigen() + " a " + modelo.getVersion() + ". Debe re-evaluar la propuesta.");
        }

        // Aplicar las operaciones al JSON del modelo
        Map<String, Object> contenidoJson = modelo.getContenidoJson();
        if (contenidoJson == null) {
            contenidoJson = new LinkedHashMap<>();
            contenidoJson.put("versionUML", "2.5");
            contenidoJson.put("elementos", new ArrayList<>());
            contenidoJson.put("relaciones", new ArrayList<>());
        }

        // Sanear previamente el contenido JSON por si contenía relaciones huérfanas de ediciones anteriores
        sanearRelacionesContenido(contenidoJson);

        // Guardar snapshot previo exacto del modelo antes de aplicar cambios
        try {
            String snapshotPrevio = objectMapper.writeValueAsString(contenidoJson);
            propuesta.setSnapshotPrevioJson(snapshotPrevio);
        } catch (Exception exSnap) {
            System.err.println("CODEFORGE: Advertencia al serializar snapshot previo: " + exSnap.getMessage());
        }

        int operacionesEjecutadas = 0;
        StringBuilder logCambios = new StringBuilder();

        try {
            JsonNode operacionesNode = objectMapper.readTree(propuesta.getOperacionesJson());
            if (operacionesNode.isArray()) {
                for (JsonNode op : operacionesNode) {
                    ejecutarOperacionUml(contenidoJson, op, logCambios);
                    operacionesEjecutadas++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error aplicando operaciones en el modelo UML: " + e.getMessage(), e);
        }

        // Sanear relaciones asegurando que origen y destino apunten a los UUIDs correspondientes
        sanearRelacionesContenido(contenidoJson);

        // Validar integridad estructural tras la modificación
        validarIntegridadUml(contenidoJson);

        // Incrementar versión del modelo
        String versionNueva = calcularSiguienteVersion(modelo.getVersion());
        modelo.setVersion(versionNueva);
        modelo.setContenidoJson(contenidoJson);
        modelo.setFechaActualizacion(LocalDateTime.now());
        modeloRepository.save(modelo);

        // Actualizar propuesta
        propuesta.setEstado("APLICADA");
        propuesta.setAprobador(usuario);
        propuesta.setFechaAplicacion(LocalDateTime.now());
        propuesta.setVersionResultante(versionNueva);
        propuesta.setResultadoAplicacion("Aplicadas con éxito " + operacionesEjecutadas + " operaciones.\n" + logCambios.toString());
        IaPropuesta guardada = propuestaRepository.save(propuesta);

        // Notificación oficial en el chat compartido para todo el equipo
        String resumenMensaje = "🚀 **¡CAMBIO APLICADO CON ÉXITO!**\n\n"
                + "- **Propuesta:** #" + propuesta.getIdPropuesta() + " - *" + propuesta.getTitulo() + "*\n"
                + "- **Solicitado por:** @" + propuesta.getSolicitante().getUsername() + "\n"
                + "- **Aprobado y aplicado por:** @" + usuario.getUsername() + "\n"
                + "- **Transición de versión:** `" + propuesta.getVersionOrigen() + "` ➔ `" + versionNueva + "`\n"
                + "- **Operaciones ejecutadas:** " + operacionesEjecutadas + "\n"
                + "- **Detalle:**\n" + logCambios.toString() + "\n\n"
                + "✨ El diagrama UML ha sido actualizado y validado correctamente.";

        publicarNotificacionSistema(idProyecto, propuesta.getConversacion(), resumenMensaje);

        IaPropuestaDTO dtoResultado = convertirAPropuestaDTO(guardada);
        broadcastPropuesta(idProyecto, dtoResultado);

        // Notificar al canvas que el modelo fue actualizado para auto-recarga
        messagingTemplate.convertAndSend("/topic/proyecto/" + idProyecto + "/modelo-actualizado", (Object) Map.of(
                "idModelo", modelo.getIdModelo(),
                "version", versionNueva,
                "timestamp", System.currentTimeMillis()
        ));

        return dtoResultado;
    }

    // ============================================================
    // REVERTIR PROPUESTA (VOLVER ATRÁS ANTES DE SU APLICACIÓN)
    // ============================================================

    @Transactional
    public IaPropuestaDTO revertirPropuesta(Long idProyecto, Long idPropuesta) {
        Usuario usuario = validarAccesoProyecto(idProyecto);
        validarPermisoModificacion(idProyecto, usuario);

        IaPropuesta propuesta = propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(idPropuesta, idProyecto)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada"));

        if (!"APLICADA".equalsIgnoreCase(propuesta.getEstado())) {
            throw new IllegalStateException("Solo se pueden revertir propuestas que se encuentren en estado APLICADA.");
        }

        Modelo modelo = propuesta.getModelo();
        if (modelo != null && modelo.getIdModelo() != null) {
            modelo = modeloRepository.findById(modelo.getIdModelo()).orElse(modelo);
        } else {
            modelo = modeloRepository.findByProyecto_IdProyecto(idProyecto).stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No existe modelo UML asociado al proyecto"));
        }

        Map<String, Object> contenidoRestaurado;
        if (propuesta.getSnapshotPrevioJson() != null && !propuesta.getSnapshotPrevioJson().isBlank()) {
            try {
                contenidoRestaurado = objectMapper.readValue(propuesta.getSnapshotPrevioJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                System.err.println("CODEFORGE: Error deserializando snapshot previo, usando fallback inverso: " + e.getMessage());
                contenidoRestaurado = revertirOperacionesUml(modelo.getContenidoJson(), propuesta.getOperacionesJson());
            }
        } else {
            contenidoRestaurado = revertirOperacionesUml(modelo.getContenidoJson(), propuesta.getOperacionesJson());
        }

        sanearRelacionesContenido(contenidoRestaurado);

        String versionRestaurada = (propuesta.getVersionOrigen() != null && !propuesta.getVersionOrigen().isBlank())
                ? propuesta.getVersionOrigen()
                : modelo.getVersion();

        modelo.setContenidoJson(contenidoRestaurado);
        modelo.setVersion(versionRestaurada);
        modelo.setFechaActualizacion(LocalDateTime.now());
        modeloRepository.save(modelo);

        propuesta.setEstado("REVERTIDA");
        propuesta.setResultadoAplicacion("Revertida por @" + usuario.getUsername() + " el " + LocalDateTime.now());
        IaPropuesta guardada = propuestaRepository.save(propuesta);

        String resumenMensaje = "⏪ **¡PROPUESTA REVERTIDA!**\n\n"
                + "- **Propuesta:** #" + propuesta.getIdPropuesta() + " - *" + propuesta.getTitulo() + "*\n"
                + "- **Revertida por:** @" + usuario.getUsername() + "\n"
                + "- **Versión restaurada:** `" + versionRestaurada + "`\n\n"
                + "✨ El diagrama UML ha vuelto al estado anterior a la aplicación de esta propuesta.";

        publicarNotificacionSistema(idProyecto, propuesta.getConversacion(), resumenMensaje);

        IaPropuestaDTO dtoResultado = convertirAPropuestaDTO(guardada);
        broadcastPropuesta(idProyecto, dtoResultado);

        messagingTemplate.convertAndSend("/topic/proyecto/" + idProyecto + "/modelo-actualizado", (Object) Map.of(
                "idModelo", modelo.getIdModelo(),
                "version", versionRestaurada,
                "timestamp", System.currentTimeMillis()
        ));

        return dtoResultado;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> revertirOperacionesUml(Map<String, Object> contenidoActual, String operacionesJson) {
        Map<String, Object> copia;
        try {
            String jsonStr = objectMapper.writeValueAsString(contenidoActual != null ? contenidoActual : Map.of());
            copia = objectMapper.readValue(jsonStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            copia = new LinkedHashMap<>(contenidoActual != null ? contenidoActual : Map.of());
        }

        List<Map<String, Object>> elementos = (List<Map<String, Object>>) copia.get("elementos");
        List<Map<String, Object>> relaciones = (List<Map<String, Object>>) copia.get("relaciones");
        if (elementos == null) {
            elementos = new ArrayList<>();
            copia.put("elementos", elementos);
        }
        if (relaciones == null) {
            relaciones = new ArrayList<>();
            copia.put("relaciones", relaciones);
        }

        try {
            JsonNode operacionesNode = objectMapper.readTree(operacionesJson);
            if (operacionesNode.isArray()) {
                List<JsonNode> ops = new ArrayList<>();
                operacionesNode.forEach(ops::add);
                java.util.Collections.reverse(ops);

                for (JsonNode op : ops) {
                    String tipoOp = op.path("tipo").asText().toUpperCase();
                    switch (tipoOp) {
                        case "CREAR_CLASE":
                        case "CREAR_ELEMENTO": {
                            String nom = op.path("elemento").path("nombre").asText().trim();
                            String idElem = resolverIdElemento(elementos, nom);
                            elementos.removeIf(e -> nom.equalsIgnoreCase(String.valueOf(e.get("nombre"))));
                            if (idElem != null) {
                                relaciones.removeIf(r -> idElem.equals(String.valueOf(r.get("origen"))) || idElem.equals(String.valueOf(r.get("destino"))));
                            }
                            break;
                        }
                        case "CREAR_RELACION": {
                            JsonNode rNode = op.path("relacion");
                            String orig = rNode.path("origen").asText().trim();
                            String dest = rNode.path("destino").asText().trim();
                            String tipo = rNode.path("tipo").asText().trim();
                            String idOrig = resolverIdElemento(elementos, orig);
                            String idDest = resolverIdElemento(elementos, dest);
                            relaciones.removeIf(r -> {
                                String o = String.valueOf(r.get("origen")).trim();
                                String d = String.valueOf(r.get("destino")).trim();
                                String t = String.valueOf(r.get("tipo")).trim();
                                return (t.equalsIgnoreCase(tipo) || tipo.isBlank()) &&
                                        ((o.equalsIgnoreCase(idOrig) || o.equalsIgnoreCase(orig)) &&
                                         (d.equalsIgnoreCase(idDest) || d.equalsIgnoreCase(dest)));
                            });
                            break;
                        }
                        case "AGREGAR_ATRIBUTO": {
                            String clase = op.path("clase").asText().trim();
                            String nomAtr = op.path("atributo").path("nombre").asText().trim();
                            elementos.stream()
                                    .filter(e -> clase.equalsIgnoreCase(String.valueOf(e.get("nombre"))) || clase.equalsIgnoreCase(String.valueOf(e.get("id"))))
                                    .findFirst()
                                    .ifPresent(e -> {
                                        List<Map<String, Object>> atrs = (List<Map<String, Object>>) e.get("atributos");
                                        if (atrs != null) {
                                            atrs.removeIf(a -> nomAtr.equalsIgnoreCase(String.valueOf(a.get("nombre"))));
                                        }
                                    });
                            break;
                        }
                        case "AGREGAR_OPERACION": {
                            String clase = op.path("clase").asText().trim();
                            String nomOp = op.path("operacion").path("nombre").asText().trim();
                            elementos.stream()
                                    .filter(e -> clase.equalsIgnoreCase(String.valueOf(e.get("nombre"))) || clase.equalsIgnoreCase(String.valueOf(e.get("id"))))
                                    .findFirst()
                                    .ifPresent(e -> {
                                        List<Map<String, Object>> opers = (List<Map<String, Object>>) e.get("operaciones");
                                        if (opers != null) {
                                            opers.removeIf(o -> nomOp.equalsIgnoreCase(String.valueOf(o.get("nombre"))));
                                        }
                                    });
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("CODEFORGE: Error en revertirOperacionesUml: " + ex.getMessage());
        }

        return copia;
    }

    // ============================================================
    // EJECUCIÓN SEGURA DE OPERACIONES SOBRE CONTENIDO UML
    // ============================================================

    @SuppressWarnings("unchecked")
    private void ejecutarOperacionUml(Map<String, Object> contenidoJson, JsonNode op, StringBuilder log) {
        String tipoOp = op.path("tipo").asText().toUpperCase();

        List<Map<String, Object>> elementos = (List<Map<String, Object>>) contenidoJson.get("elementos");
        if (elementos == null) {
            elementos = new ArrayList<>();
            contenidoJson.put("elementos", elementos);
        }

        List<Map<String, Object>> relaciones = (List<Map<String, Object>>) contenidoJson.get("relaciones");
        if (relaciones == null) {
            relaciones = new ArrayList<>();
            contenidoJson.put("relaciones", relaciones);
        }

        switch (tipoOp) {
            case "CREAR_CLASE":
            case "CREAR_ELEMENTO": {
                JsonNode elementoNode = op.path("elemento");
                String nombre = elementoNode.path("nombre").asText().trim();
                String tipo = elementoNode.path("tipo").asText("CLASE").toUpperCase();
                boolean existe = elementos.stream().anyMatch(e -> nombre.equalsIgnoreCase(String.valueOf(e.get("nombre"))));
                if (!existe && !nombre.isBlank()) {
                    Map<String, Object> nuevo = new LinkedHashMap<>();
                    String nuevoId = UUID.randomUUID().toString();
                    nuevo.put("id", nuevoId);
                    nuevo.put("tipo", tipo);
                    nuevo.put("nombre", nombre);
                    nuevo.put("visibilidad", elementoNode.path("visibilidad").asText("PUBLIC"));
                    nuevo.put("abstracto", elementoNode.path("abstracto").asBoolean(false));

                    // Posicionamiento inteligente en el lienzo para evitar solapamientos
                    int idx = elementos.size();
                    nuevo.put("posicion", Map.of(
                            "x", 80 + (idx % 3) * 340,
                            "y", 80 + (idx / 3) * 280
                    ));

                    // Atributos
                    List<Map<String, Object>> atributos = new ArrayList<>();
                    if (elementoNode.has("atributos") && elementoNode.path("atributos").isArray()) {
                        for (JsonNode atr : elementoNode.path("atributos")) {
                            Map<String, Object> a = new LinkedHashMap<>();
                            a.put("id", UUID.randomUUID().toString());
                            a.put("nombre", atr.path("nombre").asText());
                            a.put("tipo", atr.path("tipo").asText("String"));
                            a.put("visibilidad", atr.path("visibilidad").asText("PRIVATE"));
                            atributos.add(a);
                        }
                    }
                    nuevo.put("atributos", atributos);

                    // Operaciones
                    List<Map<String, Object>> operaciones = new ArrayList<>();
                    if (elementoNode.has("operaciones") && elementoNode.path("operaciones").isArray()) {
                        for (JsonNode o : elementoNode.path("operaciones")) {
                            Map<String, Object> opMap = new LinkedHashMap<>();
                            opMap.put("id", UUID.randomUUID().toString());
                            opMap.put("nombre", o.path("nombre").asText());
                            opMap.put("tipoRetorno", o.path("tipoRetorno").asText("void"));
                            opMap.put("visibilidad", o.path("visibilidad").asText("PUBLIC"));
                            opMap.put("parametros", new ArrayList<>());
                            operaciones.add(opMap);
                        }
                    }
                    nuevo.put("operaciones", operaciones);

                    elementos.add(nuevo);
                    log.append("• Creado elemento: ").append(tipo).append(" ").append(nombre).append("\n");
                }
                break;
            }

            case "AGREGAR_ATRIBUTO": {
                String clase = op.path("clase").asText().trim();
                JsonNode atr = op.path("atributo");
                elementos.stream()
                        .filter(e -> clase.equalsIgnoreCase(String.valueOf(e.get("nombre"))) || clase.equalsIgnoreCase(String.valueOf(e.get("id"))))
                        .findFirst()
                        .ifPresent(e -> {
                            List<Map<String, Object>> atrs = (List<Map<String, Object>>) e.computeIfAbsent("atributos", k -> new ArrayList<>());
                            String nomAtr = atr.path("nombre").asText();
                            boolean existeAtr = atrs.stream().anyMatch(a -> nomAtr.equalsIgnoreCase(String.valueOf(a.get("nombre"))));
                            if (!existeAtr && !nomAtr.isBlank()) {
                                Map<String, Object> a = new LinkedHashMap<>();
                                a.put("id", UUID.randomUUID().toString());
                                a.put("nombre", nomAtr);
                                a.put("tipo", atr.path("tipo").asText("String"));
                                a.put("visibilidad", atr.path("visibilidad").asText("PRIVATE"));
                                atrs.add(a);
                                log.append("• Agregado atributo '").append(nomAtr).append("' a clase ").append(clase).append("\n");
                            }
                        });
                break;
            }

            case "AGREGAR_OPERACION": {
                String clase = op.path("clase").asText().trim();
                JsonNode oper = op.path("operacion");
                elementos.stream()
                        .filter(e -> clase.equalsIgnoreCase(String.valueOf(e.get("nombre"))) || clase.equalsIgnoreCase(String.valueOf(e.get("id"))))
                        .findFirst()
                        .ifPresent(e -> {
                            List<Map<String, Object>> opers = (List<Map<String, Object>>) e.computeIfAbsent("operaciones", k -> new ArrayList<>());
                            String nomOp = oper.path("nombre").asText();
                            boolean existeOp = opers.stream().anyMatch(o -> nomOp.equalsIgnoreCase(String.valueOf(o.get("nombre"))));
                            if (!existeOp && !nomOp.isBlank()) {
                                Map<String, Object> o = new LinkedHashMap<>();
                                o.put("id", UUID.randomUUID().toString());
                                o.put("nombre", nomOp);
                                o.put("tipoRetorno", oper.path("tipoRetorno").asText("void"));
                                o.put("visibilidad", oper.path("visibilidad").asText("PUBLIC"));
                                o.put("parametros", new ArrayList<>());
                                opers.add(o);
                                log.append("• Agregada operación '").append(nomOp).append("()' a clase ").append(clase).append("\n");
                            }
                        });
                break;
            }

            case "CREAR_RELACION": {
                JsonNode relNode = op.path("relacion");
                String origenRaw = relNode.path("origen").asText().trim();
                String destinoRaw = relNode.path("destino").asText().trim();
                String tipo = relNode.path("tipo").asText("ASOCIACION").toUpperCase();

                // Resolver nombres de clase o IDs a sus UUIDs oficiales de elemento
                String origenId = resolverIdElemento(elementos, origenRaw);
                String destinoId = resolverIdElemento(elementos, destinoRaw);

                if (origenId == null || origenId.isBlank() || destinoId == null || destinoId.isBlank()) {
                    log.append("• Advertencia: No se pudo resolver la clase origen ('")
                       .append(origenRaw).append("') o destino ('").append(destinoRaw).append("') para la relación.\n");
                    break;
                }

                String nombreOrigen = obtenerNombreElemento(elementos, origenId, origenRaw);
                String nombreDestino = obtenerNombreElemento(elementos, destinoId, destinoRaw);

                boolean existeRel = relaciones.stream().anyMatch(r -> {
                    String rOrigen = String.valueOf(r.get("origen")).trim();
                    String rDestino = String.valueOf(r.get("destino")).trim();
                    String rTipo = String.valueOf(r.get("tipo")).trim();
                    return rTipo.equalsIgnoreCase(tipo) && (
                            (rOrigen.equalsIgnoreCase(origenId) || rOrigen.equalsIgnoreCase(nombreOrigen)) &&
                            (rDestino.equalsIgnoreCase(destinoId) || rDestino.equalsIgnoreCase(nombreDestino))
                    );
                });

                if (!existeRel) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("id", UUID.randomUUID().toString());
                    r.put("tipo", tipo);
                    r.put("origen", origenId); // ID UUID para que JointJS lo dibuje en el canvas
                    r.put("destino", destinoId); // ID UUID para que JointJS lo dibuje en el canvas
                    r.put("claseOrigen", nombreOrigen);
                    r.put("claseDestino", nombreDestino);
                    r.put("rolOrigen", relNode.path("rolOrigen").asText(""));
                    r.put("rolDestino", relNode.path("rolDestino").asText(""));
                    r.put("multiplicidadOrigen", relNode.path("multiplicidadOrigen").asText("1"));
                    r.put("multiplicidadDestino", relNode.path("multiplicidadDestino").asText("1"));
                    relaciones.add(r);
                    log.append("• Creada relación ").append(tipo).append(" entre ").append(nombreOrigen).append(" y ").append(nombreDestino).append("\n");
                }
                break;
            }

            case "MODIFICAR_MULTIPLICIDAD": {
                String origenRaw = op.path("origen").asText().trim();
                String destinoRaw = op.path("destino").asText().trim();
                String origenId = resolverIdElemento(elementos, origenRaw);
                String destinoId = resolverIdElemento(elementos, destinoRaw);
                String multDestino = op.path("multiplicidadDestino").asText();
                String multOrigen = op.path("multiplicidadOrigen").asText();

                for (Map<String, Object> r : relaciones) {
                    String rOrigen = String.valueOf(r.get("origen")).trim();
                    String rDestino = String.valueOf(r.get("destino")).trim();
                    if ((rOrigen.equalsIgnoreCase(origenId) || rOrigen.equalsIgnoreCase(origenRaw)) &&
                        (rDestino.equalsIgnoreCase(destinoId) || rDestino.equalsIgnoreCase(destinoRaw))) {
                        if (!multDestino.isBlank()) r.put("multiplicidadDestino", multDestino);
                        if (!multOrigen.isBlank()) r.put("multiplicidadOrigen", multOrigen);
                        log.append("• Actualizada multiplicidad entre ").append(origenRaw).append(" y ").append(destinoRaw).append("\n");
                        break;
                    }
                }
                break;
            }

            case "ELIMINAR_CLASE": {
                String nombre = op.path("nombre").asText().trim();
                String elemId = resolverIdElemento(elementos, nombre);
                boolean removido = elementos.removeIf(e -> nombre.equalsIgnoreCase(String.valueOf(e.get("nombre"))) ||
                                                          (elemId != null && elemId.equalsIgnoreCase(String.valueOf(e.get("id")))));
                if (removido) {
                    relaciones.removeIf(r -> {
                        String ro = String.valueOf(r.get("origen"));
                        String rd = String.valueOf(r.get("destino"));
                        return nombre.equalsIgnoreCase(ro) || nombre.equalsIgnoreCase(rd) ||
                               (elemId != null && (elemId.equalsIgnoreCase(ro) || elemId.equalsIgnoreCase(rd)));
                    });
                    log.append("• Eliminada clase ").append(nombre).append(" y sus relaciones asociadas\n");
                }
                break;
            }

            case "ELIMINAR_RELACION": {
                String origenRaw = op.path("origen").asText().trim();
                String destinoRaw = op.path("destino").asText().trim();
                String origenId = resolverIdElemento(elementos, origenRaw);
                String destinoId = resolverIdElemento(elementos, destinoRaw);
                relaciones.removeIf(r -> {
                    String ro = String.valueOf(r.get("origen")).trim();
                    String rd = String.valueOf(r.get("destino")).trim();
                    return (ro.equalsIgnoreCase(origenId) || ro.equalsIgnoreCase(origenRaw)) &&
                           (rd.equalsIgnoreCase(destinoId) || rd.equalsIgnoreCase(destinoRaw));
                });
                log.append("• Eliminada relación entre ").append(origenRaw).append(" y ").append(destinoRaw).append("\n");
                break;
            }

            default:
                log.append("• Operación desconocida o no soportada: ").append(tipoOp).append("\n");
        }
    }

    private String resolverIdElemento(List<Map<String, Object>> elementos, String referencia) {
        if (referencia == null || referencia.isBlank() || elementos == null) {
            return null;
        }
        String ref = referencia.trim();
        // 1. Coincidencia exacta con ID
        for (Map<String, Object> elem : elementos) {
            String id = String.valueOf(elem.getOrDefault("id", "")).trim();
            if (ref.equalsIgnoreCase(id)) {
                return id;
            }
        }
        // 2. Coincidencia exacta con Nombre (case-insensitive)
        for (Map<String, Object> elem : elementos) {
            String nombre = String.valueOf(elem.getOrDefault("nombre", "")).trim();
            if (ref.equalsIgnoreCase(nombre)) {
                return String.valueOf(elem.get("id")).trim();
            }
        }
        // 3. Coincidencia alfanumérica limpia (por ejemplo si contiene comillas o prefijos)
        String limpia = ref.replaceAll("[^a-zA-Z0-9_]", "");
        if (!limpia.isBlank()) {
            for (Map<String, Object> elem : elementos) {
                String nombre = String.valueOf(elem.getOrDefault("nombre", "")).trim();
                if (limpia.equalsIgnoreCase(nombre)) {
                    return String.valueOf(elem.get("id")).trim();
                }
            }
        }
        return null;
    }

    private String obtenerNombreElemento(List<Map<String, Object>> elementos, String id, String fallback) {
        if (elementos == null) return fallback;
        for (Map<String, Object> elem : elementos) {
            String elemId = String.valueOf(elem.getOrDefault("id", "")).trim();
            if (id != null && id.trim().equalsIgnoreCase(elemId)) {
                return String.valueOf(elem.getOrDefault("nombre", fallback)).trim();
            }
            String nombre = String.valueOf(elem.getOrDefault("nombre", "")).trim();
            if (fallback != null && fallback.trim().equalsIgnoreCase(nombre)) {
                return nombre;
            }
        }
        return fallback != null ? fallback : id;
    }

    @SuppressWarnings("unchecked")
    private void sanearRelacionesContenido(Map<String, Object> contenido) {
        if (contenido == null) return;
        List<Map<String, Object>> elementos = (List<Map<String, Object>>) contenido.get("elementos");
        List<Map<String, Object>> relaciones = (List<Map<String, Object>>) contenido.get("relaciones");
        if (elementos == null || relaciones == null) return;

        // 1. Resolver y normalizar IDs y nombres de origen y destino
        for (Map<String, Object> rel : relaciones) {
            String origen = String.valueOf(rel.getOrDefault("origen", "")).trim();
            String claseOrigen = String.valueOf(rel.getOrDefault("claseOrigen", "")).trim();
            String destino = String.valueOf(rel.getOrDefault("destino", "")).trim();
            String claseDestino = String.valueOf(rel.getOrDefault("claseDestino", "")).trim();

            String origenId = resolverIdElemento(elementos, origen);
            if (origenId == null && !claseOrigen.isBlank()) {
                origenId = resolverIdElemento(elementos, claseOrigen);
            }

            String destinoId = resolverIdElemento(elementos, destino);
            if (destinoId == null && !claseDestino.isBlank()) {
                destinoId = resolverIdElemento(elementos, claseDestino);
            }

            if (origenId != null && !origenId.isBlank()) {
                rel.put("origen", origenId);
                rel.put("claseOrigen", obtenerNombreElemento(elementos, origenId, claseOrigen.isBlank() ? origen : claseOrigen));
            }
            if (destinoId != null && !destinoId.isBlank()) {
                rel.put("destino", destinoId);
                rel.put("claseDestino", obtenerNombreElemento(elementos, destinoId, claseDestino.isBlank() ? destino : claseDestino));
            }
        }

        // 2. Purgar relaciones huérfanas cuyos extremos no existan en el modelo
        relaciones.removeIf(rel -> {
            String origen = String.valueOf(rel.getOrDefault("origen", "")).trim();
            String destino = String.valueOf(rel.getOrDefault("destino", "")).trim();
            boolean origenValido = elementos.stream().anyMatch(e ->
                    origen.equalsIgnoreCase(String.valueOf(e.get("id")).trim()) ||
                    origen.equalsIgnoreCase(String.valueOf(e.get("nombre")).trim())
            );
            boolean destinoValido = elementos.stream().anyMatch(e ->
                    destino.equalsIgnoreCase(String.valueOf(e.get("id")).trim()) ||
                    destino.equalsIgnoreCase(String.valueOf(e.get("nombre")).trim())
            );
            return !origenValido || !destinoValido;
        });
    }

    @Transactional(readOnly = true)
    public List<IaUmlFindingDTO> diagnosticarModelo(Long idProyecto, Long idModelo) {
        validarAccesoProyecto(idProyecto);
        Modelo modelo = obtenerModeloProyecto(idProyecto, idModelo);
        if (modelo == null) {
            return Collections.emptyList();
        }
        return ejecutarDiagnosticoProgramatico(modelo);
    }

    @SuppressWarnings("unchecked")
    public List<IaUmlFindingDTO> ejecutarDiagnosticoProgramatico(Modelo modelo) {
        List<IaUmlFindingDTO> hallazgos = new ArrayList<>();
        if (modelo == null || modelo.getContenidoJson() == null) {
            return hallazgos;
        }

        Map<String, Object> contenido = modelo.getContenidoJson();
        List<Map<String, Object>> elementos = (List<Map<String, Object>>) contenido.get("elementos");
        List<Map<String, Object>> relaciones = (List<Map<String, Object>>) contenido.get("relaciones");

        if (elementos == null) elementos = Collections.emptyList();
        if (relaciones == null) relaciones = Collections.emptyList();

        Map<String, Long> conteoNombres = new LinkedHashMap<>();
        Map<String, String> idANombre = new LinkedHashMap<>();

        // 1. Validar elementos (clases, interfaces, enums)
        int index = 1;
        for (Map<String, Object> elem : elementos) {
            String id = String.valueOf(elem.getOrDefault("id", ""));
            String nombre = String.valueOf(elem.getOrDefault("nombre", "")).trim();
            String tipo = String.valueOf(elem.getOrDefault("tipo", "CLASE")).toUpperCase();

            if (nombre.isBlank()) {
                IaUmlFindingDTO f = new IaUmlFindingDTO();
                f.setId("ERR-" + (index++));
                f.setElementoAfectado(id.isBlank() ? "Elemento sin ID" : id);
                f.setTipoProblema("ERROR_ESTRUCTURAL");
                f.setSeveridad("ALTA");
                f.setDescripcion("Elemento UML de tipo " + tipo + " sin nombre definido.");
                f.setEvidencia("El atributo 'nombre' está vacío o nulo.");
                f.setPropuestaSolucion("Asignar un nombre válido en PascalCase al elemento.");
                f.setPosiblesEfectosSecundarios("Fallo al generar código backend y persistir.");
                f.setRequiereConfirmacion(false);
                hallazgos.add(f);
            } else {
                idANombre.put(id, nombre);
                idANombre.put(nombre.toLowerCase(), nombre);
                conteoNombres.put(nombre.toLowerCase(), conteoNombres.getOrDefault(nombre.toLowerCase(), 0L) + 1);
            }

            // Validar atributos y operaciones
            List<Map<String, Object>> atributos = (List<Map<String, Object>>) elem.get("atributos");
            List<Map<String, Object>> operaciones = (List<Map<String, Object>>) elem.get("operaciones");

            if ((atributos == null || atributos.isEmpty()) && (operaciones == null || operaciones.isEmpty()) && !"ENUM".equals(tipo)) {
                IaUmlFindingDTO f = new IaUmlFindingDTO();
                f.setId("WAR-" + (index++));
                f.setElementoAfectado(nombre);
                f.setTipoProblema("RECOMENDACION_DISENO");
                f.setSeveridad("BAJA");
                f.setDescripcion("La clase '" + nombre + "' no tiene atributos ni operaciones definidas.");
                f.setEvidencia("Listas de atributos y operaciones vacías.");
                f.setPropuestaSolucion("Definir atributos clave o métodos de negocio para dar propósito a la clase.");
                f.setPosiblesEfectosSecundarios("Generación de una entidad vacía sin columnas en la base de datos.");
                f.setRequiereConfirmacion(true);
                hallazgos.add(f);
            }

            if (atributos != null) {
                Map<String, Integer> conteoAtributos = new LinkedHashMap<>();
                for (Map<String, Object> atr : atributos) {
                    String nomAtr = String.valueOf(atr.getOrDefault("nombre", "")).trim();
                    String tipoAtr = String.valueOf(atr.getOrDefault("tipo", "")).trim();

                    if (!nomAtr.isBlank()) {
                        conteoAtributos.put(nomAtr.toLowerCase(), conteoAtributos.getOrDefault(nomAtr.toLowerCase(), 0) + 1);
                    }
                    if (tipoAtr.isBlank()) {
                        IaUmlFindingDTO f = new IaUmlFindingDTO();
                        f.setId("INC-" + (index++));
                        f.setElementoAfectado(nombre + "." + (nomAtr.isBlank() ? "sin_nombre" : nomAtr));
                        f.setTipoProblema("INCONSISTENCIA");
                        f.setSeveridad("MEDIA");
                        f.setDescripcion("El atributo '" + nomAtr + "' de la clase '" + nombre + "' carece de tipo de dato.");
                        f.setEvidencia("Campo 'tipo' vacío o ausente.");
                        f.setPropuestaSolucion("Especificar un tipo (ej. Long, String, Integer, LocalDateTime, Double).");
                        f.setPosiblesEfectosSecundarios("Fallo al inferir tipo de columna SQL en Spring Boot.");
                        f.setRequiereConfirmacion(false);
                        hallazgos.add(f);
                    }
                }

                for (Map.Entry<String, Integer> entry : conteoAtributos.entrySet()) {
                    if (entry.getValue() > 1) {
                        IaUmlFindingDTO f = new IaUmlFindingDTO();
                        f.setId("ERR-" + (index++));
                        f.setElementoAfectado(nombre + "." + entry.getKey());
                        f.setTipoProblema("ERROR_ESTRUCTURAL");
                        f.setSeveridad("ALTA");
                        f.setDescripcion("Atributo duplicado '" + entry.getKey() + "' en la clase '" + nombre + "'.");
                        f.setEvidencia("El atributo aparece " + entry.getValue() + " veces.");
                        f.setPropuestaSolucion("Eliminar o renombrar los atributos duplicados.");
                        f.setPosiblesEfectosSecundarios("Error de compilación en Java por nombres de campo duplicados.");
                        f.setRequiereConfirmacion(false);
                        hallazgos.add(f);
                    }
                }
            }
        }

        // Clases con nombres duplicados
        for (Map.Entry<String, Long> entry : conteoNombres.entrySet()) {
            if (entry.getValue() > 1) {
                IaUmlFindingDTO f = new IaUmlFindingDTO();
                f.setId("ERR-" + (index++));
                f.setElementoAfectado(entry.getKey());
                f.setTipoProblema("ERROR_ESTRUCTURAL");
                f.setSeveridad("ALTA");
                f.setDescripcion("Existe más de un elemento con el nombre '" + entry.getKey() + "'.");
                f.setEvidencia("Nombre repetido " + entry.getValue() + " veces en el modelo.");
                f.setPropuestaSolucion("Unificar las clases duplicadas o renombrar con nombres únicos y descriptivos.");
                f.setPosiblesEfectosSecundarios("Conflicto de nombres al generar entidades JPA en el mismo paquete.");
                f.setRequiereConfirmacion(false);
                hallazgos.add(f);
            }
        }

        // 2. Validar relaciones
        for (Map<String, Object> rel : relaciones) {
            String origen = String.valueOf(rel.getOrDefault("origen", "")).trim();
            String destino = String.valueOf(rel.getOrDefault("destino", "")).trim();
            String tipoRel = String.valueOf(rel.getOrDefault("tipo", "ASOCIACION")).toUpperCase();
            String multO = String.valueOf(rel.getOrDefault("multiplicidadOrigen", "1")).trim();

            boolean origenExiste = idANombre.containsKey(origen) || idANombre.containsKey(origen.toLowerCase());
            boolean destinoExiste = idANombre.containsKey(destino) || idANombre.containsKey(destino.toLowerCase());

            if (!origenExiste || !destinoExiste) {
                IaUmlFindingDTO f = new IaUmlFindingDTO();
                f.setId("ERR-" + (index++));
                f.setElementoAfectado("Relación " + tipoRel + " (" + origen + " ➔ " + destino + ")");
                f.setTipoProblema("ERROR_ESTRUCTURAL");
                f.setSeveridad("ALTA");
                f.setDescripcion("La relación referencia a un elemento que no existe en el diagrama ("
                        + (!origenExiste ? "Origen inexistente: '" + origen + "'. " : "")
                        + (!destinoExiste ? "Destino inexistente: '" + destino + "'." : "") + ")");
                f.setEvidencia("ID o nombre no encontrado en la lista de elementos del modelo.");
                f.setPropuestaSolucion("Reasignar los extremos a clases existentes o eliminar la relación huérfana.");
                f.setPosiblesEfectosSecundarios("Fallo al establecer claves foráneas o relaciones JPA @ManyToOne/@OneToMany.");
                f.setRequiereConfirmacion(false);
                hallazgos.add(f);
            }

            // Regla UML: Composición fuerte
            if ("COMPOSICION".equals(tipoRel)) {
                if (!"1".equals(multO) && !"0..1".equals(multO)) {
                    IaUmlFindingDTO f = new IaUmlFindingDTO();
                    f.setId("INC-" + (index++));
                    f.setElementoAfectado("Composición " + origen + " ➔ " + destino);
                    f.setTipoProblema("INCONSISTENCIA");
                    f.setSeveridad("MEDIA");
                    f.setDescripcion("En una relación de Composición, el contenedor (origen) debe tener multiplicidad 1 o 0..1 (valor actual: '" + multO + "').");
                    f.setEvidencia("La semántica de composición UML exige que la parte dependa exclusivamente de una única instancia del todo.");
                    f.setPropuestaSolucion("Cambiar la multiplicidad en el origen a '1' o sustituir por Agregación si la parte puede compartirse.");
                    f.setPosiblesEfectosSecundarios("Violación de la regla de propiedad exclusiva en cascada de JPA.");
                    f.setRequiereConfirmacion(true);
                    hallazgos.add(f);
                }
            }

            // Relación con sí misma que no está marcada como recursiva
            if (origen.equalsIgnoreCase(destino) && !"RECURSIVA".equals(tipoRel)) {
                IaUmlFindingDTO f = new IaUmlFindingDTO();
                f.setId("INC-" + (index++));
                f.setElementoAfectado("Relación en " + origen);
                f.setTipoProblema("INCONSISTENCIA");
                f.setSeveridad("MEDIA");
                f.setDescripcion("Relación autoreferencial entre '" + origen + "' y sí misma no está clasificada como RECURSIVA.");
                f.setEvidencia("Origen y destino coinciden pero tipo es '" + tipoRel + "'.");
                f.setPropuestaSolucion("Clasificar el tipo como RECURSIVA y verificar multiplicidades jerárquicas.");
                f.setPosiblesEfectosSecundarios("Renderizado visual incorrecto y problemas en relaciones jerárquicas auto-asociadas.");
                f.setRequiereConfirmacion(false);
                hallazgos.add(f);
            }
        }

        return hallazgos;
    }

    private void validarIntegridadUml(Map<String, Object> contenido) {
        if (!contenido.containsKey("elementos") || !contenido.containsKey("relaciones")) {
            throw new IllegalStateException("El formato UML resultante es inválido.");
        }
        Modelo temp = new Modelo();
        temp.setContenidoJson(contenido);
        List<IaUmlFindingDTO> fallos = ejecutarDiagnosticoProgramatico(temp);
        for (IaUmlFindingDTO f : fallos) {
            if ("ALTA".equalsIgnoreCase(f.getSeveridad())) {
                throw new IllegalStateException("Error de integridad UML tras la operación: " + f.getDescripcion() + " (" + f.getEvidencia() + ")");
            }
        }
    }

    private String calcularSiguienteVersion(String versionActual) {
        if (versionActual == null || versionActual.isBlank()) {
            return "1.1";
        }
        try {
            String[] parts = versionActual.split("\\.");
            int mayor = Integer.parseInt(parts[0]);
            int menor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return mayor + "." + (menor + 1);
        } catch (Exception e) {
            return versionActual + ".1";
        }
    }

    // ============================================================
    // DETECCIÓN Y EXTRACCIÓN DE PROPUESTAS
    // ============================================================

    private static class ExtraccionPropuesta {
        String textoLimpio;
        IaPropuesta propuesta;
        String metadatosJson;
    }

    private ExtraccionPropuesta extraerYCrearPropuestaSiAplica(
            Long idProyecto, Modelo modelo, IaConversacion conversacion, Usuario solicitante,
            String respuestaTexto, Boolean forzarGeneracion
    ) {
        ExtraccionPropuesta resultado = new ExtraccionPropuesta();
        resultado.textoLimpio = respuestaTexto;

        // Buscar bloque json ```json ... ```
        Pattern pattern = Pattern.compile("```json\\s*(\\{[\\s\\S]*?\"propuesta\"[\\s\\S]*?\\})\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(respuestaTexto);

        if (matcher.find()) {
            String jsonRaw = matcher.group(1);
            try {
                JsonNode root = objectMapper.readTree(jsonRaw);
                JsonNode propNode = root.path("propuesta");
                if (!propNode.isMissingNode()) {
                    String titulo = propNode.path("titulo").asText("Propuesta de modificación UML");
                    String problema = propNode.path("descripcionProblema").asText("");
                    String solucion = propNode.path("solucionPropuesta").asText("");
                    String riesgos = propNode.path("riesgosEfectos").asText("Ningún efecto adverso detectado.");

                    JsonNode ops = propNode.path("operaciones");
                    String opsJson = ops.isMissingNode() ? "[]" : objectMapper.writeValueAsString(ops);

                    IaPropuesta p = new IaPropuesta();
                    p.setProyecto(conversacion.getProyecto());
                    p.setModelo(modelo);
                    p.setConversacion(conversacion);
                    p.setVersionOrigen(modelo != null ? modelo.getVersion() : "1.0");
                    p.setTitulo(titulo);
                    p.setDescripcionProblema(problema);
                    p.setSolucionPropuesta(solucion);
                    p.setRiesgosEfectos(riesgos);
                    p.setOperacionesJson(opsJson);
                    p.setEstado("PENDIENTE");
                    p.setSolicitante(solicitante);
                    p.setFechaCreacion(LocalDateTime.now());

                    IaPropuesta guardada = propuestaRepository.save(p);
                    resultado.propuesta = guardada != null ? guardada : p;

                    // Dejar el texto limpio sin el bloque crudo o con una referencia limpia
                    Long idProp = (resultado.propuesta.getIdPropuesta() != null) ? resultado.propuesta.getIdPropuesta() : 1L;
                    resultado.textoLimpio = matcher.replaceFirst("*(Se ha generado la **Propuesta de Cambio #" + idProp + "** pendiente de revisión y aprobación por el equipo)*");
                    resultado.metadatosJson = jsonRaw;
                }
            } catch (Exception e) {
                System.err.println("Error procesando propuesta JSON: " + e.getMessage());
            }
        }

        return resultado;
    }

    // ============================================================
    // CONSTRUCCIÓN DE PROMPTS Y CONTEXTOS
    // ============================================================

    private Modelo obtenerModeloProyecto(Long idProyecto, Long idModelo) {
        Modelo m;
        if (idModelo != null) {
            m = modeloRepository.findById(idModelo).orElse(null);
        } else {
            m = modeloRepository.findByProyecto_IdProyecto(idProyecto).stream().findFirst().orElse(null);
        }
        if (m != null && m.getContenidoJson() != null) {
            sanearRelacionesContenido(m.getContenidoJson());
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private String construirContexto(Proyecto proyecto, Modelo modelo, String mensajeUsuario) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Proyecto: ").append(proyecto.getNombre()).append(" (ID: ").append(proyecto.getIdProyecto()).append(")\n");
        if (proyecto.getDescripcion() != null && !proyecto.getDescripcion().isBlank()) {
            sb.append("- Descripción: ").append(proyecto.getDescripcion()).append("\n");
        }

        if (modelo != null) {
            sb.append("- Modelo UML activo: '").append(modelo.getNombre()).append("' | Versión: ").append(modelo.getVersion()).append("\n");
            Map<String, Object> c = modelo.getContenidoJson();
            if (c != null) {
                sanearRelacionesContenido(c);
                List<Map<String, Object>> elems = (List<Map<String, Object>>) c.get("elementos");
                List<Map<String, Object>> rels = (List<Map<String, Object>>) c.get("relaciones");
                sb.append("  • Total clases/elementos: ").append(elems != null ? elems.size() : 0).append("\n");
                sb.append("  • Total relaciones: ").append(rels != null ? rels.size() : 0).append("\n");

                if (elems != null && !elems.isEmpty()) {
                    sb.append("  • Clases/Entidades presentes en el diagrama:\n");
                    for (Map<String, Object> elem : elems) {
                        String id = String.valueOf(elem.getOrDefault("id", ""));
                        String nombre = String.valueOf(elem.getOrDefault("nombre", ""));
                        sb.append("    - Entidad: '").append(nombre).append("' (ID: ").append(id).append(")\n");
                    }
                }

                if (rels != null && !rels.isEmpty()) {
                    sb.append("  • Relaciones actuales entre clases:\n");
                    for (Map<String, Object> rel : rels) {
                        String ro = String.valueOf(rel.getOrDefault("claseOrigen", rel.getOrDefault("origen", "")));
                        String rd = String.valueOf(rel.getOrDefault("claseDestino", rel.getOrDefault("destino", "")));
                        String t = String.valueOf(rel.getOrDefault("tipo", "ASOCIACION"));
                        String mo = String.valueOf(rel.getOrDefault("multiplicidadOrigen", "1"));
                        String md = String.valueOf(rel.getOrDefault("multiplicidadDestino", "1"));
                        sb.append("    - ").append(ro).append(" (").append(mo).append(") ── ").append(t).append(" ── (").append(md).append(") ").append(rd).append("\n");
                    }
                }

                try {
                    String jsonResumen = objectMapper.writeValueAsString(c);
                    // Si no es excesivamente largo, incluir estructura completa
                    if (jsonResumen.length() < 12000) {
                        sb.append("  • Estructura UML completa en JSON:\n").append(jsonResumen).append("\n");
                    } else {
                        sb.append("  • Estructura resumida:\n").append(jsonResumen.substring(0, 10000)).append("...\n");
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            sb.append("- Sin modelo UML activo actualmente.\n");
        }

        return sb.toString();
    }

    private String construirSystemPrompt(String modo, Proyecto proyecto, Modelo modelo) {
        String base = """
                Eres la IA Desarrolladora Colaborativa oficial de CodeForge, integrada como un miembro técnico senior en el equipo del proyecto "%s".
                
                No eres un simple chatbot aislado: eres un colaborador compartido por todos los desarrolladores del equipo.
                Tus respuestas se publican en la sala de chat colaborativa en tiempo real.
                
                REGLAS CRÍTICAS DE CODEFORGE:
                1. NO apliques cambios de forma unilateral. El flujo obligatorio es: ANALIZAR -> INFORMAR -> PROPONER -> ESPERAR APROBACIÓN HUMANA EXPLÍCITA -> APLICAR.
                2. Basa tus análisis en el modelo UML real y los requisitos documentados del proyecto. Distingue entre:
                   - ERRORES OBJETIVOS (referencias a clases inexistentes, errores de multiplicidad, tipos inválidos).
                   - POSIBLES INCONSISTENCIAS (ambigüedades, falta de trazabilidad).
                   - RECOMENDACIONES DE DISEÑO (patrones, buenas prácticas).
                3. REGLA OBLIGATORIA: RELACIONAR SIEMPRE LAS CLASES (CERO CLASES AISLADAS O HUÉRFANAS):
                   - Al crear, proponer o incorporar una nueva clase o entidad al diagrama (ejemplo: 'Asistencia'), es ESTRICTAMENTE OBLIGATORIO que identifiques con qué otras clases del modelo debe interactuar y formules TANTO la operación "CREAR_CLASE" COMO las operaciones "CREAR_RELACION" necesarias para dejarla conectada.
                   - ¡NUNCA crees o dejes una clase aislada o desconectada! En un diagrama UML de negocio, toda entidad (como Asistencia) debe estar vinculada a las entidades que le dan sentido (por ejemplo: Asistencia se relaciona con Alumno y con Grupo).
                   - Si el desarrollador te dice "debería relacionar la clase", "relaciona la clase X" o "conecta la entidad", identifica de inmediato las entidades pertinentes del modelo actual y formula una PROPUESTA FORMAL con las operaciones "CREAR_RELACION" para conectarlas.
                   - En las operaciones "CREAR_RELACION", puedes colocar en "origen" y "destino" el nombre de la clase (ej. "Alumno", "Asistencia") o su ID. El motor de CodeForge las mapeará automáticamente a los identificadores UUID para dibujarlas en el lienzo.
                   - Selecciona el tipo de relación UML adecuado: ASOCIACION, AGREGACION, COMPOSICION o GENERALIZACION, y define multiplicidades precisas (1, 0..1, 1..*, 0..*).
                4. Si el usuario solicita modificar, corregir, agregar o relacionar elementos en el diagrama UML, formula una PROPUESTA ESTRUCTURADA incluyendo un bloque JSON con el formato:
                ```json
                {
                  "propuesta": {
                    "titulo": "Título descriptivo del cambio",
                    "descripcionProblema": "Qué problema o deficiencia se identificó",
                    "solucionPropuesta": "Explicación de la solución técnica",
                    "riesgosEfectos": "Posibles efectos secundarios o consideraciones",
                    "operaciones": [
                      {
                        "tipo": "CREAR_CLASE",
                        "elemento": {
                          "tipo": "CLASE",
                          "nombre": "NombreClase",
                          "visibilidad": "PUBLIC",
                          "atributos": [
                            { "nombre": "id", "tipo": "Long", "visibilidad": "PRIVATE" }
                          ],
                          "operaciones": []
                        }
                      },
                      {
                        "tipo": "CREAR_RELACION",
                        "relacion": {
                          "tipo": "ASOCIACION",
                          "origen": "ClaseA",
                          "destino": "ClaseB",
                          "multiplicidadOrigen": "1",
                          "multiplicidadDestino": "0..*"
                        }
                      }
                    ]
                  }
                }
                ```
                Operaciones soportadas: CREAR_CLASE, AGREGAR_ATRIBUTO, AGREGAR_OPERACION, CREAR_RELACION, MODIFICAR_MULTIPLICIDAD, ELIMINAR_CLASE, ELIMINAR_RELACION.
                5. Comunícate siempre en español profesional, con formato Markdown legible, viñetas y tablas cuando aporte claridad.
                """.formatted(proyecto.getNombre());

        String rolEspecializado = switch (modo.toUpperCase()) {
            case "MODELADOR_UML" -> """
                    MODO ACTIVO: MODELADOR UML SENIOR.
                    Tu enfoque:
                    - Analiza exhaustivamente las clases, atributos, tipos de datos, operaciones y visibilidades.
                    - Evalúa las relaciones: Asociaciones, Agregaciones, Composiciones, Generalización/Herencia, Dependencias.
                    - Verifica consistencia en multiplicidades (1..1, 0..*, 1..*), claves foráneas implícitas y referencias cruzadas.
                    - Asegura que todas las clases del modelo estén debidamente conectadas según las reglas del negocio, sin dejar clases aisladas.
                    - Si una clase no está relacionada o el desarrollador pide relacionarla, formula inmediatamente la propuesta estructurada con las operaciones CREAR_RELACION.
                    - Produce informes de diagnóstico detallados indicando Elemento Afectado, Tipo de Problema, Severidad y Solución.
                    """;
            case "REQUISITOS" -> """
                    MODO ACTIVO: CONSULTOR DE REQUISITOS.
                    Tu enfoque:
                    - Analiza requisitos funcionales y no funcionales.
                    - Identifica requisitos ambiguos o contradictorios.
                    - Compara los requisitos con las entidades y métodos del diagrama UML.
                    - Si falta información de reglas de negocio, formula preguntas directas al equipo antes de hacer suposiciones.
                    """;
            case "ARQUITECTO" -> """
                    MODO ACTIVO: ARQUITECTO DE SOFTWARE.
                    Tu enfoque:
                    - Evalúa la separación por capas (Controlador -> Servicio -> Repositorio -> Entidad).
                    - Asegura el principio de Responsabilidad Única y patrones de diseño (Repository, DTO, Service Layer).
                    - Analiza el impacto de cualquier cambio propuesto en el backend de Spring Boot y la base de datos PostgreSQL.
                    - Justifica las decisiones arquitectónicas explicando ventajas y desventajas.
                    """;
            case "DESARROLLADOR" -> """
                    MODO ACTIVO: DESARROLLADOR SPRING BOOT SENIOR.
                    Tu enfoque:
                    - Diseña y revisa entidades JPA (@Entity, @Table, @ManyToOne, etc.).
                    - Genera repositorios Spring Data JPA, DTOs de petición y respuesta, y controladores RESTful.
                    - Garantiza el uso de validaciones (@NotNull, @NotBlank, etc.) y manejo de excepciones.
                    - Alinea el código con la arquitectura oficial de CodeForge.
                    """;
            case "REVISOR_PRUEBAS" -> """
                    MODO ACTIVO: REVISOR DE CÓDIGO E INGENIERO DE PRUEBAS.
                    Tu enfoque:
                    - Identifica errores lógicos, problemas de concurrencia y posibles vulnerabilidades (inyecciones, validaciones ausentes).
                    - Diseña planes de pruebas unitarias con JUnit 5 y Mockito.
                    - Distingue con precisión entre análisis estático e hipótesis de ejecución real.
                    """;
            case "DOCUMENTADOR" -> """
                    MODO ACTIVO: DOCUMENTADOR TÉCNICO.
                    Tu enfoque:
                    - Redacta registros de decisiones de arquitectura (ADR).
                    - Genera resúmenes ejecutivos de cambios y notas de versión del diagrama.
                    - Documenta endpoints REST y modelos de datos de forma clara y estructurada.
                    """;
            case "ANALISTA_ERRORES" -> """
                    MODO ACTIVO: ANALISTA DE ERRORES UML Y DE DISEÑO.
                    Tu enfoque:
                    - Clasifica y diagnostica fallos estructurales (referencias rotas, duplicados, multiplicidades).
                    - Distingue con rigor entre errores objetivos comprobados y recomendaciones.
                    - Explica la causa raíz y las operaciones exactas para subsanarlo.
                    """;
            case "IMPACTO" -> """
                    MODO ACTIVO: ASISTENTE DE ANÁLISIS DE IMPACTO DE CAMBIOS.
                    Tu enfoque:
                    - Evalúa las consecuencias de agregar, modificar o eliminar clases y relaciones en el backend y BD.
                    - Identifica dependencias afectadas en repositorios, servicios, controladores y migraciones SQL.
                    - Alerta sobre incompatibilidades hacia atrás o riesgos de concurrencia.
                    """;
            case "DECISIONES" -> """
                    MODO ACTIVO: ASISTENTE DE EXPLICACIÓN DE DECISIONES TÉCNICAS.
                    Tu enfoque:
                    - Explica los fundamentos de diseño y arquitectura del proyecto a los desarrolladores.
                    - Justifica por qué se modeló una clase, relación o patrón de cierta manera basándote en el contexto y decisiones previas.
                    - Ayuda a nuevos integrantes del equipo a comprender la evolución técnica del sistema.
                    """;
            default -> """
                    MODO ACTIVO: COLABORADOR TÉCNICO GENERAL.
                    Participa activamente en la discusión del equipo, integrando conocimientos de UML, arquitectura y desarrollo en CodeForge.
                    Si te piden crear o relacionar clases, formula siempre propuestas completas con CREAR_CLASE y CREAR_RELACION para mantener el diagrama integrado.
                    """;
        };

        return base + "\n" + rolEspecializado;
    }

    private String generarRespuestaLocalDeRespaldo(String modo, String pregunta, Modelo modelo) {
        return "Hola equipo. Como IA colaborativa, analicé su consulta: *\"" + pregunta + "\"* en modo **" + traducirModo(modo) + "**.\n\n"
                + "Actualmente el proyecto cuenta con el modelo `" + (modelo != null ? modelo.getNombre() : "Sin modelo") + "` (versión " + (modelo != null ? modelo.getVersion() : "1.0") + ").\n\n"
                + "📋 **Recomendación preliminar:**\n"
                + "- Revisen la coherencia de las entidades y relaciones existentes.\n"
                + "- Si desean proponer una nueva entidad o relación, pueden solicitarlo explícitamente y prepararé una propuesta de cambio para aprobación del equipo.";
    }

    private void registrarMetrica(
            Proyecto proyecto, Usuario usuario, String modo, long duracionMs,
            boolean exitoso, String error, Integer promptTokens, Integer completionTokens
    ) {
        try {
            IaMetrica m = new IaMetrica();
            m.setProyecto(proyecto);
            m.setUsuario(usuario);
            m.setModeloIa(model);
            m.setModoIa(modo);
            m.setDuracionMs(duracionMs);
            m.setExitoso(exitoso);
            m.setMensajeError(error);
            m.setPromptTokens(promptTokens);
            m.setCompletionTokens(completionTokens);
            m.setTotalTokens((promptTokens != null ? promptTokens : 0) + (completionTokens != null ? completionTokens : 0));
            metricaRepository.save(m);
        } catch (Exception ignored) {
        }
    }

    private void publicarNotificacionSistema(Long idProyecto, IaConversacion conversacion, String contenido) {
        IaMensaje sistema = new IaMensaje();
        sistema.setConversacion(conversacion);
        sistema.setNombreEmisor("Sistema CodeForge");
        sistema.setTipoEmisor("SISTEMA");
        sistema.setModoIa("SISTEMA");
        sistema.setContenido(contenido);
        sistema.setFechaEnvio(LocalDateTime.now());
        IaMensaje guardado = mensajeRepository.save(sistema);

        IaChatMensajeDTO dto = convertirAMensajeDTO(guardado);
        broadcastMensaje(idProyecto, dto);
    }

    private void broadcastMensaje(Long idProyecto, IaChatMensajeDTO dto) {
        try {
            messagingTemplate.convertAndSend("/topic/proyecto/" + idProyecto + "/ia-chat", dto);
        } catch (Exception e) {
            System.err.println("Error enviando WebSocket ia-chat: " + e.getMessage());
        }
    }

    private void broadcastPropuesta(Long idProyecto, IaPropuestaDTO dto) {
        try {
            messagingTemplate.convertAndSend("/topic/proyecto/" + idProyecto + "/ia-propuestas", dto);
        } catch (Exception e) {
            System.err.println("Error enviando WebSocket ia-propuestas: " + e.getMessage());
        }
    }

    private String traducirModo(String modo) {
        if (modo == null) return "Colaborador General";
        return switch (modo.toUpperCase()) {
            case "MODELADOR_UML" -> "Modelador UML";
            case "REQUISITOS" -> "Consultor de Requisitos";
            case "ARQUITECTO" -> "Arquitecto de Software";
            case "DESARROLLADOR" -> "Desarrollador";
            case "REVISOR_PRUEBAS" -> "Revisor y Pruebas";
            case "DOCUMENTADOR" -> "Documentador";
            case "ANALISTA_ERRORES" -> "Analista de Errores";
            case "IMPACTO" -> "Análisis de Impacto";
            case "DECISIONES" -> "Explicación de Decisiones";
            default -> "Colaborador General";
        };
    }

    private IaChatMensajeDTO convertirAMensajeDTO(IaMensaje m) {
        IaChatMensajeDTO dto = new IaChatMensajeDTO();
        dto.setIdMensaje(m.getIdMensaje());
        dto.setIdConversacion(m.getConversacion().getIdConversacion());
        if (m.getUsuario() != null) {
            dto.setIdUsuario(m.getUsuario().getIdUsuario());
            dto.setUsernameEmisor(m.getUsuario().getUsername());
        }
        dto.setNombreEmisor(m.getNombreEmisor());
        dto.setTipoEmisor(m.getTipoEmisor());
        dto.setModoIa(m.getModoIa());
        dto.setContenido(m.getContenido());
        dto.setMetadatosJson(m.getMetadatosJson());
        dto.setIdPropuestaAsociada(m.getIdPropuestaAsociada());
        dto.setFechaEnvio(m.getFechaEnvio());

        if (m.getIdPropuestaAsociada() != null) {
            propuestaRepository.findById(m.getIdPropuestaAsociada())
                    .ifPresent(p -> dto.setPropuestaAsociada(convertirAPropuestaDTO(p)));
        }

        return dto;
    }

    private IaPropuestaDTO convertirAPropuestaDTO(IaPropuesta p) {
        IaPropuestaDTO dto = new IaPropuestaDTO();
        dto.setIdPropuesta(p.getIdPropuesta());
        dto.setIdProyecto(p.getProyecto().getIdProyecto());
        dto.setIdModelo(p.getModelo().getIdModelo());
        dto.setIdConversacion(p.getConversacion().getIdConversacion());
        dto.setVersionOrigen(p.getVersionOrigen());
        dto.setTitulo(p.getTitulo());
        dto.setDescripcionProblema(p.getDescripcionProblema());
        dto.setSolucionPropuesta(p.getSolucionPropuesta());
        dto.setRiesgosEfectos(p.getRiesgosEfectos());
        dto.setOperacionesJson(p.getOperacionesJson());
        dto.setEstado(p.getEstado());
        dto.setIdSolicitante(p.getSolicitante().getIdUsuario());
        dto.setNombreSolicitante(p.getSolicitante().getNombre() + " " + p.getSolicitante().getApellido());

        if (p.getAprobador() != null) {
            dto.setIdAprobador(p.getAprobador().getIdUsuario());
            dto.setNombreAprobador(p.getAprobador().getNombre() + " " + p.getAprobador().getApellido());
        }

        dto.setFechaCreacion(p.getFechaCreacion());
        dto.setFechaDecision(p.getFechaDecision());
        dto.setFechaAplicacion(p.getFechaAplicacion());
        dto.setVersionResultante(p.getVersionResultante());
        dto.setResultadoAplicacion(p.getResultadoAplicacion());

        try {
            JsonNode ops = objectMapper.readTree(p.getOperacionesJson());
            dto.setTotalOperaciones(ops.isArray() ? ops.size() : 0);
        } catch (Exception e) {
            dto.setTotalOperaciones(0);
        }

        return dto;
    }

    private String invocarGemini(String systemPrompt, String contexto, String nombreUsuario, String mensajeUsuario) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY no está configurada.");
        }

        String promptCompleto = contexto + "\n\n" + nombreUsuario + ": " + mensajeUsuario;

        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(Content.builder().parts(List.of(Part.fromText(systemPrompt))).build())
                .temperature(0.3f)
                .build();

        String[] modelosCandidatos = {
                (model != null && !model.isBlank()) ? model : "gemini-3.5-flash",
                "gemini-3.5-flash-lite",
                "gemini-3.8-flash"
        };

        Exception ultimoError = null;
        for (String modeloIntento : modelosCandidatos) {
            try {
                GenerateContentResponse response = client.models.generateContent(
                        modeloIntento,
                        promptCompleto,
                        config
                );
                String texto = response.text();
                if (texto != null && !texto.isBlank()) {
                    return texto.trim();
                }
            } catch (Exception e) {
                ultimoError = e;
                System.err.println("CODEFORGE IA COLABORATIVA: Falló modelo " + modeloIntento + " (" + e.getMessage() + ").");
            }
        }

        throw new IllegalStateException("Error al consultar Gemini en todos los modelos: "
                + (ultimoError != null ? ultimoError.getMessage() : "sin respuesta"), ultimoError);
    }
}

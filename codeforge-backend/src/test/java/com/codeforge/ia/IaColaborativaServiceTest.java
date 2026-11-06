package com.codeforge.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.codeforge.ia.dto.IaDecisionRequestDTO;
import com.codeforge.ia.dto.IaPreguntaRequestDTO;
import com.codeforge.ia.dto.IaPropuestaDTO;
import com.codeforge.ia.dto.IaUmlFindingDTO;
import com.codeforge.ia.entity.IaConversacion;
import com.codeforge.ia.entity.IaMensaje;
import com.codeforge.ia.entity.IaPropuesta;
import com.codeforge.ia.repository.IaConversacionRepository;
import com.codeforge.ia.repository.IaMensajeRepository;
import com.codeforge.ia.repository.IaMetricaRepository;
import com.codeforge.ia.repository.IaPropuestaRepository;
import com.codeforge.ia.service.IaColaborativaService;
import com.codeforge.ia.service.IaLocalService;
import com.codeforge.modelo.entity.Modelo;
import com.codeforge.modelo.repository.ModeloRepository;
import com.codeforge.proyecto.entity.Proyecto;
import com.codeforge.proyecto.entity.ProyectoColaborador;
import com.codeforge.proyecto.repository.ProyectoColaboradorRepository;
import com.codeforge.proyecto.repository.ProyectoRepository;
import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class IaColaborativaServiceTest {

    @Mock
    private IaConversacionRepository conversacionRepository;

    @Mock
    private IaMensajeRepository mensajeRepository;

    @Mock
    private IaPropuestaRepository propuestaRepository;

    @Mock
    private IaMetricaRepository metricaRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoColaboradorRepository colaboradorRepository;

    @Mock
    private ModeloRepository modeloRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private IaLocalService iaLocalService;

    private IaColaborativaService service;

    private Usuario usuarioPropietario;
    private Usuario usuarioColaborador;
    private Usuario usuarioLector;
    private Usuario usuarioExterno;
    private Proyecto proyecto;
    private Modelo modelo;
    private IaConversacion conversacion;

    @BeforeEach
    void setUp() {
        service = new IaColaborativaService(
                conversacionRepository,
                mensajeRepository,
                propuestaRepository,
                metricaRepository,
                proyectoRepository,
                colaboradorRepository,
                modeloRepository,
                usuarioRepository,
                messagingTemplate,
                iaLocalService,
                "", // sin API key para forzar fallback local seguro en tests
                "gemini-3.5-flash",
                "HIBRIDO"
        );

        usuarioPropietario = new Usuario();
        usuarioPropietario.setIdUsuario(1L);
        usuarioPropietario.setUsername("propietario");
        usuarioPropietario.setNombre("Juan");
        usuarioPropietario.setApellido("Perez");

        usuarioColaborador = new Usuario();
        usuarioColaborador.setIdUsuario(2L);
        usuarioColaborador.setUsername("colaborador");
        usuarioColaborador.setNombre("Ana");
        usuarioColaborador.setApellido("Gomez");

        usuarioLector = new Usuario();
        usuarioLector.setIdUsuario(3L);
        usuarioLector.setUsername("lector");
        usuarioLector.setNombre("Carlos");
        usuarioLector.setApellido("Lopez");

        usuarioExterno = new Usuario();
        usuarioExterno.setIdUsuario(99L);
        usuarioExterno.setUsername("externo");
        usuarioExterno.setNombre("Hacker");
        usuarioExterno.setApellido("Anon");

        proyecto = new Proyecto();
        proyecto.setIdProyecto(10L);
        proyecto.setNombre("Sistema de Ventas");
        proyecto.setPropietario(usuarioPropietario);

        modelo = new Modelo();
        modelo.setIdModelo(100L);
        modelo.setProyecto(proyecto);
        modelo.setNombre("Modelo Principal");
        modelo.setVersion("1.0");

        Map<String, Object> contenidoJson = new LinkedHashMap<>();
        contenidoJson.put("versionUML", "2.5");
        contenidoJson.put("elementos", new ArrayList<Map<String, Object>>());
        contenidoJson.put("relaciones", new ArrayList<Map<String, Object>>());
        modelo.setContenidoJson(contenidoJson);

        conversacion = new IaConversacion(proyecto, "Sala de Desarrollo");
        conversacion.setIdConversacion(50L);

        // Configurar contexto de seguridad autenticado
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "propietario",
                        "password",
                        Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        org.mockito.Mockito.lenient().when(mensajeRepository.save(any(IaMensaje.class)))
                .thenAnswer(inv -> {
                    IaMensaje m = inv.getArgument(0);
                    if (m.getIdMensaje() == null) {
                        m.setIdMensaje(12345L);
                    }
                    return m;
                });
    }

    @Test
    @DisplayName("Caso B: Aislamiento - Usuario no miembro del proyecto recibe AccessDeniedException")
    void testAislamientoAccesoDenegado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "externo",
                        "password",
                        Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        when(usuarioRepository.findByUsername("externo")).thenReturn(Optional.of(usuarioExterno));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(colaboradorRepository.existsByProyecto_IdProyectoAndUsuario_IdUsuario(10L, 99L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.validarAccesoProyecto(10L));
    }

    @Test
    @DisplayName("Caso I: Seguridad - Colaborador con rol LECTOR no puede aprobar ni aplicar cambios")
    void testLectorNoPuedeAprobar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "lector",
                        "password",
                        Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        when(usuarioRepository.findByUsername("lector")).thenReturn(Optional.of(usuarioLector));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(colaboradorRepository.existsByProyecto_IdProyectoAndUsuario_IdUsuario(10L, 3L)).thenReturn(true);

        ProyectoColaborador colabLector = new ProyectoColaborador();
        colabLector.setProyecto(proyecto);
        colabLector.setUsuario(usuarioLector);
        colabLector.setRolProyecto("LECTOR");

        when(colaboradorRepository.findByProyecto_IdProyecto(10L)).thenReturn(List.of(colabLector));

        IaDecisionRequestDTO decisionDTO = new IaDecisionRequestDTO();
        decisionDTO.setDecision("APROBAR");

        assertThrows(AccessDeniedException.class, () ->
                service.decidirPropuesta(10L, 500L, decisionDTO));
    }

    @Test
    @DisplayName("Caso C: Diagnóstico UML - Detección programática de clases duplicadas y relaciones rotas")
    void testDiagnosticoUmlProgramatico() {
        Map<String, Object> json = new LinkedHashMap<>();
        List<Map<String, Object>> elementos = new ArrayList<>();

        Map<String, Object> elem1 = new LinkedHashMap<>();
        elem1.put("id", "c1");
        elem1.put("nombre", "Cliente");
        elem1.put("tipo", "CLASE");
        elem1.put("atributos", List.of(
                Map.of("id", "a1", "nombre", "nombre", "tipo", "String"),
                Map.of("id", "a2", "nombre", "nombre", "tipo", "String") // Atributo duplicado
        ));
        elementos.add(elem1);

        Map<String, Object> elem2 = new LinkedHashMap<>();
        elem2.put("id", "c2");
        elem2.put("nombre", "Cliente"); // Clase duplicada
        elem2.put("tipo", "CLASE");
        elementos.add(elem2);

        json.put("elementos", elementos);

        List<Map<String, Object>> relaciones = new ArrayList<>();
        Map<String, Object> rel1 = new LinkedHashMap<>();
        rel1.put("id", "r1");
        rel1.put("tipo", "ASOCIACION");
        rel1.put("origen", "c1");
        rel1.put("destino", "c_fantasma"); // Elemento inexistente
        relaciones.add(rel1);

        json.put("relaciones", relaciones);
        modelo.setContenidoJson(json);

        List<IaUmlFindingDTO> hallazgos = service.ejecutarDiagnosticoProgramatico(modelo);

        assertNotNull(hallazgos);
        assertFalse(hallazgos.isEmpty());

        boolean encontroClaseDuplicada = hallazgos.stream()
                .anyMatch(h -> h.getDescripcion().contains("Existe más de un elemento con el nombre"));
        boolean encontroAtributoDuplicado = hallazgos.stream()
                .anyMatch(h -> h.getDescripcion().contains("Atributo duplicado"));
        boolean encontroRelacionInexistente = hallazgos.stream()
                .anyMatch(h -> h.getDescripcion().contains("referencia a un elemento que no existe"));

        assertTrue(encontroClaseDuplicada, "Debe detectar clases con nombre duplicado");
        assertTrue(encontroAtributoDuplicado, "Debe detectar atributos duplicados en la misma clase");
        assertTrue(encontroRelacionInexistente, "Debe detectar relaciones con extremos inexistentes");
    }

    @Test
    @DisplayName("Caso E: Rechazo de propuesta - El artefacto oficial permanece intacto")
    void testRechazoPropuesta() {
        when(usuarioRepository.findByUsername("propietario")).thenReturn(Optional.of(usuarioPropietario));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));

        IaPropuesta propuesta = new IaPropuesta();
        propuesta.setIdPropuesta(101L);
        propuesta.setProyecto(proyecto);
        propuesta.setModelo(modelo);
        propuesta.setConversacion(conversacion);
        propuesta.setVersionOrigen("1.0");
        propuesta.setTitulo("Crear clase Factura");
        propuesta.setOperacionesJson("[]");
        propuesta.setEstado("PENDIENTE");
        propuesta.setSolicitante(usuarioPropietario);
        propuesta.setFechaCreacion(LocalDateTime.now());

        when(propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(101L, 10L))
                .thenReturn(Optional.of(propuesta));
        when(propuestaRepository.save(any(IaPropuesta.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IaDecisionRequestDTO decision = new IaDecisionRequestDTO();
        decision.setDecision("RECHAZAR");
        decision.setComentario("No requerimos esa entidad en este sprint.");

        IaPropuestaDTO resultado = service.decidirPropuesta(10L, 101L, decision);

        assertEquals("RECHAZADA", resultado.getEstado());
        assertEquals("1.0", modelo.getVersion(), "La versión del modelo UML debe permanecer intacta tras rechazo");
    }

    @Test
    @DisplayName("Caso F: Aprobación y Aplicación - Aplica operaciones, valida e incrementa versión a 1.1")
    void testAprobacionYAplicacionExitosa() {
        when(usuarioRepository.findByUsername("propietario")).thenReturn(Optional.of(usuarioPropietario));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));

        String operacionesJson = """
                [
                  {
                    "tipo": "CREAR_CLASE",
                    "elemento": {
                      "tipo": "CLASE",
                      "nombre": "Producto",
                      "visibilidad": "PUBLIC",
                      "atributos": [
                        { "nombre": "precio", "tipo": "Double", "visibilidad": "PRIVATE" }
                      ]
                    }
                  }
                ]
                """;

        IaPropuesta propuesta = new IaPropuesta();
        propuesta.setIdPropuesta(202L);
        propuesta.setProyecto(proyecto);
        propuesta.setModelo(modelo);
        propuesta.setConversacion(conversacion);
        propuesta.setVersionOrigen("1.0");
        propuesta.setTitulo("Agregar entidad Producto");
        propuesta.setOperacionesJson(operacionesJson);
        propuesta.setEstado("APROBADA");
        propuesta.setSolicitante(usuarioPropietario);
        propuesta.setFechaCreacion(LocalDateTime.now());

        when(propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(202L, 10L))
                .thenReturn(Optional.of(propuesta));
        when(modeloRepository.findById(100L)).thenReturn(Optional.of(modelo));
        when(propuestaRepository.save(any(IaPropuesta.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(modeloRepository.save(any(Modelo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IaPropuestaDTO resultado = service.aplicarPropuesta(10L, 202L);

        assertEquals("APLICADA", resultado.getEstado());
        assertEquals("1.1", resultado.getVersionResultante());
        assertEquals("1.1", modelo.getVersion());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elementos = (List<Map<String, Object>>) modelo.getContenidoJson().get("elementos");
        assertNotNull(elementos);
        assertEquals(1, elementos.size());
        assertEquals("Producto", elementos.get(0).get("nombre"));
    }

    @Test
    @DisplayName("Caso G: Concurrencia - Bloquea aplicación si la versión del modelo cambió y marca OBSOLETA")
    void testControlDeConcurrenciaVersionMismatch() {
        when(usuarioRepository.findByUsername("propietario")).thenReturn(Optional.of(usuarioPropietario));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));

        // El modelo en base de datos ya fue modificado por otro usuario y pasó a versión 1.2
        modelo.setVersion("1.2");

        IaPropuesta propuestaDesactualizada = new IaPropuesta();
        propuestaDesactualizada.setIdPropuesta(303L);
        propuestaDesactualizada.setProyecto(proyecto);
        propuestaDesactualizada.setModelo(modelo);
        propuestaDesactualizada.setConversacion(conversacion);
        propuestaDesactualizada.setVersionOrigen("1.0"); // Basada en versión vieja
        propuestaDesactualizada.setTitulo("Propuesta vieja");
        propuestaDesactualizada.setOperacionesJson("[]");
        propuestaDesactualizada.setEstado("APROBADA");
        propuestaDesactualizada.setSolicitante(usuarioPropietario);

        when(propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(303L, 10L))
                .thenReturn(Optional.of(propuestaDesactualizada));
        when(modeloRepository.findById(100L)).thenReturn(Optional.of(modelo));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.aplicarPropuesta(10L, 303L));

        assertTrue(ex.getMessage().contains("Conflicto de versión"));
        assertEquals("OBSOLETA", propuestaDesactualizada.getEstado(), "La propuesta debe ser marcada como OBSOLETA al detectar discrepancia");
        verify(propuestaRepository).save(propuestaDesactualizada);
    }

    @Test
    @DisplayName("Caso H: Idempotencia - No permite aplicar dos veces una propuesta ya APLICADA")
    void testIdempotenciaAplicacion() {
        when(usuarioRepository.findByUsername("propietario")).thenReturn(Optional.of(usuarioPropietario));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));

        IaPropuesta propuesta = new IaPropuesta();
        propuesta.setIdPropuesta(404L);
        propuesta.setProyecto(proyecto);
        propuesta.setEstado("APLICADA"); // Ya fue aplicada

        when(propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(404L, 10L))
                .thenReturn(Optional.of(propuesta));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.aplicarPropuesta(10L, 404L));

        assertTrue(ex.getMessage().contains("ya ha sido aplicada"));
    }

    @Test
    @DisplayName("Caso K: Fallback Asistido - Si la IA no tiene API key, genera respuesta local sin perder mensajes")
    void testFallbackLocalDeIa() {
        when(usuarioRepository.findByUsername("propietario")).thenReturn(Optional.of(usuarioPropietario));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(conversacionRepository.findByProyecto_IdProyecto(10L)).thenReturn(Optional.of(conversacion));
        when(modeloRepository.findByProyecto_IdProyecto(10L)).thenReturn(List.of(modelo));
        when(mensajeRepository.save(any(IaMensaje.class))).thenAnswer(inv -> inv.getArgument(0));

        IaPreguntaRequestDTO pregunta = new IaPreguntaRequestDTO();
        pregunta.setMensaje("¿Cómo diseño la relación entre Pedido y Producto?");
        pregunta.setModoIa("MODELADOR_UML");

        List<com.codeforge.ia.dto.IaChatMensajeDTO> resultado = service.enviarMensajeYProcesar(10L, pregunta);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("USUARIO", resultado.get(0).getTipoEmisor());
        assertEquals("IA", resultado.get(1).getTipoEmisor());
        assertTrue(resultado.get(1).getContenido().contains("Como IA colaborativa"));
        verify(metricaRepository).save(any());
    }

    @Test
    @DisplayName("Caso R: Relaciones UML - Resuelve nombres de clases a UUIDs reales al aplicar relaciones")
    void testAprobacionYAplicacionRelacionResuelveNombreAUuid() {
        when(usuarioRepository.findByUsername("propietario")).thenReturn(Optional.of(usuarioPropietario));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));

        // Preparar modelo con una clase existente "Alumno" con UUID conocido
        String uuidAlumno = UUID.randomUUID().toString();
        Map<String, Object> claseAlumno = new LinkedHashMap<>();
        claseAlumno.put("id", uuidAlumno);
        claseAlumno.put("tipo", "CLASE");
        claseAlumno.put("nombre", "Alumno");
        claseAlumno.put("visibilidad", "PUBLIC");
        claseAlumno.put("atributos", new ArrayList<>());
        claseAlumno.put("operaciones", new ArrayList<>());

        List<Map<String, Object>> elementos = new ArrayList<>();
        elementos.add(claseAlumno);
        modelo.getContenidoJson().put("elementos", elementos);
        modelo.getContenidoJson().put("relaciones", new ArrayList<Map<String, Object>>());

        // Operaciones para crear "Asistencia" y relacionar "Alumno" con "Asistencia" usando nombres
        String operacionesJson = """
                [
                  {
                    "tipo": "CREAR_CLASE",
                    "elemento": {
                      "tipo": "CLASE",
                      "nombre": "Asistencia",
                      "visibilidad": "PUBLIC",
                      "atributos": [
                        { "nombre": "fecha", "tipo": "LocalDate", "visibilidad": "PRIVATE" }
                      ]
                    }
                  },
                  {
                    "tipo": "CREAR_RELACION",
                    "relacion": {
                      "tipo": "ASOCIACION",
                      "origen": "Alumno",
                      "destino": "Asistencia",
                      "multiplicidadOrigen": "1",
                      "multiplicidadDestino": "0..*"
                    }
                  }
                ]
                """;

        IaPropuesta propuesta = new IaPropuesta();
        propuesta.setIdPropuesta(505L);
        propuesta.setProyecto(proyecto);
        propuesta.setModelo(modelo);
        propuesta.setConversacion(conversacion);
        propuesta.setVersionOrigen("1.0");
        propuesta.setTitulo("Crear y relacionar Asistencia con Alumno");
        propuesta.setOperacionesJson(operacionesJson);
        propuesta.setEstado("APROBADA");
        propuesta.setSolicitante(usuarioPropietario);
        propuesta.setFechaCreacion(LocalDateTime.now());

        when(propuestaRepository.findByIdPropuestaAndProyecto_IdProyecto(505L, 10L))
                .thenReturn(Optional.of(propuesta));
        when(modeloRepository.findById(100L)).thenReturn(Optional.of(modelo));
        when(propuestaRepository.save(any(IaPropuesta.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(modeloRepository.save(any(Modelo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IaPropuestaDTO resultado = service.aplicarPropuesta(10L, 505L);

        assertEquals("APLICADA", resultado.getEstado());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elemsResultantes = (List<Map<String, Object>>) modelo.getContenidoJson().get("elementos");
        assertEquals(2, elemsResultantes.size());

        Map<String, Object> asistenciaElem = elemsResultantes.stream()
                .filter(e -> "Asistencia".equalsIgnoreCase(String.valueOf(e.get("nombre"))))
                .findFirst().orElseThrow();
        String uuidAsistencia = String.valueOf(asistenciaElem.get("id"));
        assertNotNull(uuidAsistencia);
        assertFalse(uuidAsistencia.isBlank());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> relsResultantes = (List<Map<String, Object>>) modelo.getContenidoJson().get("relaciones");
        assertEquals(1, relsResultantes.size());

        Map<String, Object> rel = relsResultantes.get(0);
        assertEquals(uuidAlumno, rel.get("origen"), "El origen de la relación DEBE ser el UUID del elemento Alumno");
        assertEquals(uuidAsistencia, rel.get("destino"), "El destino de la relación DEBE ser el UUID del elemento Asistencia");
        assertEquals("Alumno", rel.get("claseOrigen"));
        assertEquals("Asistencia", rel.get("claseDestino"));
        assertEquals("1", rel.get("multiplicidadOrigen"));
        assertEquals("0..*", rel.get("multiplicidadDestino"));
    }
}

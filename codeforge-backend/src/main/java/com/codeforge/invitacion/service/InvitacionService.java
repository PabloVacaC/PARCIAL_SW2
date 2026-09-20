package com.codeforge.invitacion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeforge.auth.security.AuthenticationUtil;
import com.codeforge.invitacion.dto.InvitacionCreateDTO;
import com.codeforge.invitacion.dto.InvitacionResponseDTO;
import com.codeforge.invitacion.entity.Invitacion;
import com.codeforge.invitacion.repository.InvitacionRepository;
import com.codeforge.proyecto.entity.Proyecto;
import com.codeforge.proyecto.entity.ProyectoColaborador;
import com.codeforge.proyecto.repository.ProyectoColaboradorRepository;
import com.codeforge.proyecto.repository.ProyectoRepository;
import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;

@Service
public class InvitacionService {

    private final InvitacionRepository invitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProyectoColaboradorRepository proyectoColaboradorRepository;

    public InvitacionService(
            InvitacionRepository invitacionRepository,
            UsuarioRepository usuarioRepository,
            ProyectoRepository proyectoRepository,
            ProyectoColaboradorRepository proyectoColaboradorRepository) {

        this.invitacionRepository = invitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.proyectoRepository = proyectoRepository;
        this.proyectoColaboradorRepository = proyectoColaboradorRepository;
    }

    // =========================================================
    // CREAR INVITACIÓN
    // =========================================================

    @Transactional
    public InvitacionResponseDTO crearInvitacion(
            InvitacionCreateDTO dto) {

        // Usuario realmente autenticado mediante JWT
        String usernameAutenticado =
                AuthenticationUtil.obtenerUsernameAutenticado();

        Usuario emisor =
                usuarioRepository
                        .findByUsername(usernameAutenticado)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El usuario autenticado no existe"));

        // El idEmisor del JSON debe corresponder
        // al usuario autenticado
        if (!emisor.getIdUsuario()
                .equals(dto.getIdEmisor())) {

            throw new RuntimeException(
                    "El usuario autenticado no puede enviar invitaciones en nombre de otro usuario");
        }

        // Buscar receptor
        Usuario receptor =
                usuarioRepository
                        .findById(dto.getIdReceptor())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El usuario receptor no existe"));

        // Buscar proyecto
        Proyecto proyecto =
                proyectoRepository
                        .findById(dto.getIdProyecto())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El proyecto no existe"));

        // No puede invitarse a sí mismo
        if (emisor.getIdUsuario()
                .equals(receptor.getIdUsuario())) {

            throw new RuntimeException(
                    "Un usuario no puede invitarse a sí mismo");
        }

        // Verificar si ya pertenece al proyecto
        boolean yaEsColaborador =
                proyectoColaboradorRepository
                        .existsByProyecto_IdProyectoAndUsuario_IdUsuario(
                                dto.getIdProyecto(),
                                dto.getIdReceptor()
                        );

        if (yaEsColaborador) {
            throw new RuntimeException(
                    "El usuario ya pertenece al proyecto");
        }

        // Verificar invitación pendiente
        List<Invitacion> invitaciones =
                invitacionRepository
                        .findByReceptor_IdUsuarioAndEstado(
                                dto.getIdReceptor(),
                                "PENDIENTE"
                        );

        boolean invitacionExistente =
                invitaciones.stream()
                        .anyMatch(inv ->
                                inv.getProyecto()
                                        .getIdProyecto()
                                        .equals(dto.getIdProyecto()));

        if (invitacionExistente) {

            throw new RuntimeException(
                    "Ya existe una invitación pendiente para este proyecto");
        }

        // Verificar permisos del emisor
        boolean puedeInvitar =
                puedeEnviarInvitacion(
                        proyecto,
                        emisor.getIdUsuario()
                );

        if (!puedeInvitar) {

            throw new RuntimeException(
                    "El usuario no tiene permisos para enviar invitaciones");
        }

        // Crear invitación
        Invitacion invitacion =
                new Invitacion();

        invitacion.setProyecto(proyecto);
        invitacion.setEmisor(emisor);
        invitacion.setReceptor(receptor);
        invitacion.setRolPropuesto(
                dto.getRolPropuesto());
        invitacion.setEstado("PENDIENTE");
        invitacion.setFechaEnvio(
                LocalDateTime.now());

        Invitacion guardada =
                invitacionRepository.save(invitacion);

        return convertirAResponseDTO(guardada);
    }

    // =========================================================
    // PERMISOS PARA ENVIAR
    // =========================================================

    private boolean puedeEnviarInvitacion(
            Proyecto proyecto,
            Long idUsuario) {

        // El propietario puede invitar
        if (proyecto.getPropietario()
                .getIdUsuario()
                .equals(idUsuario)) {

            return true;
        }

        // Administradores activos
        return proyectoColaboradorRepository
                .findByProyecto_IdProyecto(
                        proyecto.getIdProyecto())
                .stream()
                .anyMatch(colaborador ->

                        colaborador.getUsuario()
                                .getIdUsuario()
                                .equals(idUsuario)

                        &&

                        (
                            colaborador.getRolProyecto()
                                    .equals("ADMINISTRADOR")

                            ||

                            colaborador.getRolProyecto()
                                    .equals("PROPIETARIO")
                        )

                        &&

                        colaborador.getEstado()
                                .equals("ACTIVO")
                );
    }

    // =========================================================
    // INVITACIONES RECIBIDAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<InvitacionResponseDTO> obtenerRecibidas(
            Long idUsuario) {

        String usernameAutenticado =
                AuthenticationUtil.obtenerUsernameAutenticado();

        Usuario usuario =
                usuarioRepository
                        .findByUsername(usernameAutenticado)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El usuario autenticado no existe"));

        // Un usuario solamente puede consultar
        // sus propias invitaciones
        if (!usuario.getIdUsuario().equals(idUsuario)) {

            throw new RuntimeException(
                    "No tiene permiso para consultar las invitaciones de otro usuario");
        }

        return invitacionRepository
                .findByReceptor_IdUsuario(idUsuario)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    // =========================================================
    // INVITACIONES ENVIADAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<InvitacionResponseDTO> obtenerEnviadas(
            Long idUsuario) {

        String usernameAutenticado =
                AuthenticationUtil.obtenerUsernameAutenticado();

        Usuario usuario =
                usuarioRepository
                        .findByUsername(usernameAutenticado)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "El usuario autenticado no existe"));

        // Un usuario solamente puede consultar
        // las invitaciones que él mismo envió
        if (!usuario.getIdUsuario().equals(idUsuario)) {

            throw new RuntimeException(
                    "No tiene permiso para consultar las invitaciones de otro usuario");
        }

        return invitacionRepository
                .findByEmisor_IdUsuario(idUsuario)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    // =========================================================
    // ACEPTAR INVITACIÓN
    // =========================================================

    @Transactional
    public InvitacionResponseDTO aceptarInvitacion(
            Long idInvitacion) {

        String usernameAutenticado =
                AuthenticationUtil.obtenerUsernameAutenticado();

        Invitacion invitacion =
                invitacionRepository
                        .findById(idInvitacion)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "La invitación no existe"));

        // La persona autenticada debe ser
        // el receptor de la invitación
        if (!invitacion.getReceptor()
                .getUsername()
                .equals(usernameAutenticado)) {

            throw new RuntimeException(
                    "No tiene permiso para aceptar esta invitación");
        }

        // Verificar estado
        if (!invitacion.getEstado()
                .equals("PENDIENTE")) {

            throw new RuntimeException(
                    "La invitación ya fue procesada");
        }

        // Verificar si ya es colaborador
        boolean yaEsColaborador =
                proyectoColaboradorRepository
                        .existsByProyecto_IdProyectoAndUsuario_IdUsuario(
                                invitacion.getProyecto()
                                        .getIdProyecto(),

                                invitacion.getReceptor()
                                        .getIdUsuario()
                        );

        if (yaEsColaborador) {

            throw new RuntimeException(
                    "El usuario ya pertenece al proyecto");
        }

        // Crear colaborador
        ProyectoColaborador colaborador =
                new ProyectoColaborador();

        colaborador.setProyecto(
                invitacion.getProyecto());

        colaborador.setUsuario(
                invitacion.getReceptor());

        colaborador.setRolProyecto(
                invitacion.getRolPropuesto());

        colaborador.setEstado("ACTIVO");

        colaborador.setFechaIngreso(
                LocalDateTime.now());

        proyectoColaboradorRepository
                .save(colaborador);

        // Actualizar invitación
        invitacion.setEstado("ACEPTADA");

        invitacion.setFechaRespuesta(
                LocalDateTime.now());

        Invitacion actualizada =
                invitacionRepository
                        .save(invitacion);

        return convertirAResponseDTO(actualizada);
    }

    // =========================================================
    // RECHAZAR INVITACIÓN
    // =========================================================

    @Transactional
    public InvitacionResponseDTO rechazarInvitacion(
            Long idInvitacion) {

        String usernameAutenticado =
                AuthenticationUtil.obtenerUsernameAutenticado();

        Invitacion invitacion =
                invitacionRepository
                        .findById(idInvitacion)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "La invitación no existe"));

        // Solo el receptor puede rechazar
        if (!invitacion.getReceptor()
                .getUsername()
                .equals(usernameAutenticado)) {

            throw new RuntimeException(
                    "No tiene permiso para rechazar esta invitación");
        }

        // Verificar estado
        if (!invitacion.getEstado()
                .equals("PENDIENTE")) {

            throw new RuntimeException(
                    "La invitación ya fue procesada");
        }

        invitacion.setEstado("RECHAZADA");

        invitacion.setFechaRespuesta(
                LocalDateTime.now());

        Invitacion actualizada =
                invitacionRepository
                        .save(invitacion);

        return convertirAResponseDTO(actualizada);
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private InvitacionResponseDTO convertirAResponseDTO(
            Invitacion invitacion) {

        InvitacionResponseDTO dto =
                new InvitacionResponseDTO();

        dto.setIdInvitacion(
                invitacion.getIdInvitacion());

        dto.setIdProyecto(
                invitacion.getProyecto()
                        .getIdProyecto());

        dto.setNombreProyecto(
                invitacion.getProyecto()
                        .getNombre());

        dto.setIdEmisor(
                invitacion.getEmisor()
                        .getIdUsuario());

        dto.setNombreEmisor(
                invitacion.getEmisor()
                        .getNombre()
                + " "
                + invitacion.getEmisor()
                        .getApellido());

        dto.setIdReceptor(
                invitacion.getReceptor()
                        .getIdUsuario());

        dto.setNombreReceptor(
                invitacion.getReceptor()
                        .getNombre()
                + " "
                + invitacion.getReceptor()
                        .getApellido());

        dto.setRolPropuesto(
                invitacion.getRolPropuesto());

        dto.setEstado(
                invitacion.getEstado());

        dto.setFechaEnvio(
                invitacion.getFechaEnvio());

        dto.setFechaRespuesta(
                invitacion.getFechaRespuesta());

        return dto;
    }
}
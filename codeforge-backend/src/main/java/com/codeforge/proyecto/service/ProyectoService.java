package com.codeforge.proyecto.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeforge.auth.security.AuthenticationUtil;
import com.codeforge.proyecto.dto.ColaboradorResponseDTO;
import com.codeforge.proyecto.dto.ProyectoCreateDTO;
import com.codeforge.proyecto.dto.ProyectoResponseDTO;
import com.codeforge.proyecto.entity.Proyecto;
import com.codeforge.proyecto.entity.ProyectoColaborador;
import com.codeforge.proyecto.repository.ProyectoColaboradorRepository;
import com.codeforge.proyecto.repository.ProyectoRepository;
import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;


@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    private final ProyectoColaboradorRepository colaboradorRepository;

    private final UsuarioRepository usuarioRepository;


    public ProyectoService(
            ProyectoRepository proyectoRepository,
            ProyectoColaboradorRepository colaboradorRepository,
            UsuarioRepository usuarioRepository
    ) {

        this.proyectoRepository =
                proyectoRepository;

        this.colaboradorRepository =
                colaboradorRepository;

        this.usuarioRepository =
                usuarioRepository;
    }


    // ============================================================
    // CREAR PROYECTO
    // ============================================================

    @Transactional
    public ProyectoResponseDTO crearProyecto(
            ProyectoCreateDTO dto
    ) {

        // Obtener el usuario autenticado desde el JWT
        String username =
                AuthenticationUtil.obtenerUsernameAutenticado();


        // Buscar usuario
        Usuario propietario =
                usuarioRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario autenticado no encontrado"
                                )
                        );


        // Limpiar nombre
        String nombreProyecto =
                dto.getNombre().trim();


        // Evitar proyectos duplicados
        // para el mismo propietario
        if (
                proyectoRepository
                        .existsByNombreAndPropietario_IdUsuario(
                                nombreProyecto,
                                propietario.getIdUsuario()
                        )
        ) {

            throw new RuntimeException(
                    "Ya existe un proyecto con ese nombre"
            );

        }


        // Crear entidad Proyecto
        Proyecto proyecto =
                new Proyecto();


        proyecto.setNombre(
                nombreProyecto
        );


        proyecto.setDescripcion(
                dto.getDescripcion() == null
                        ? null
                        : dto.getDescripcion().trim()
        );


        proyecto.setPropietario(
                propietario
        );


        proyecto.setEstado(
                "ACTIVO"
        );


        // Fechas
        LocalDateTime ahora =
                LocalDateTime.now();


        proyecto.setFechaCreacion(
                ahora
        );


        proyecto.setFechaActualizacion(
                ahora
        );


        // Guardar proyecto
        Proyecto proyectoGuardado =
                proyectoRepository.save(
                        proyecto
                );


        // ========================================================
        // REGISTRAR AL PROPIETARIO COMO COLABORADOR
        // ========================================================

        ProyectoColaborador propietarioColaborador =
                new ProyectoColaborador();


        propietarioColaborador.setProyecto(
                proyectoGuardado
        );


        propietarioColaborador.setUsuario(
                propietario
        );


        propietarioColaborador.setRolProyecto(
                "PROPIETARIO"
        );


        propietarioColaborador.setEstado(
                "ACTIVO"
        );


        propietarioColaborador.setFechaIngreso(
                ahora
        );


        colaboradorRepository.save(
                propietarioColaborador
        );


        return convertirDTO(
                proyectoGuardado
        );
    }


    // ============================================================
    // OBTENER TODOS LOS PROYECTOS DEL USUARIO
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProyectoResponseDTO> obtenerMisProyectos() {

        // Usuario autenticado
        String username =
                AuthenticationUtil.obtenerUsernameAutenticado();


        Usuario usuario =
                usuarioRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario autenticado no encontrado"
                                )
                        );


        /*
         * Buscamos todos los proyectos donde el usuario
         * tenga una relación activa en ProyectoColaborador.
         *
         * Esto permite mostrar:
         *
         * PROPIETARIO
         * ADMINISTRADOR
         * COLABORADOR
         * LECTOR
         */

        return colaboradorRepository

                .findByUsuario_IdUsuario(
                        usuario.getIdUsuario()
                )

                .stream()

                // Solo relaciones activas
                .filter(colaborador ->
                        "ACTIVO".equals(
                                colaborador.getEstado()
                        )
                )

                // Obtener proyecto
                .map(
                        ProyectoColaborador::getProyecto
                )

                // Convertir a DTO
                .map(
                        this::convertirDTO
                )

                // Evitar duplicados
                .distinct()

                .toList();
    }


    // ============================================================
    // OBTENER UN PROYECTO
    // ============================================================

    @Transactional(readOnly = true)
    public ProyectoResponseDTO obtenerProyecto(
            Long idProyecto
    ) {

        // Buscar proyecto
        Proyecto proyecto =
                proyectoRepository.findById(
                        idProyecto
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Proyecto no encontrado"
                        )
                );


        // Usuario autenticado
        String username =
                AuthenticationUtil.obtenerUsernameAutenticado();


        Usuario usuario =
                usuarioRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario autenticado no encontrado"
                                )
                        );


        // Verificar si es propietario
        boolean esPropietario =
                proyecto.getPropietario()
                        .getIdUsuario()
                        .equals(
                                usuario.getIdUsuario()
                        );


        // Verificar si es colaborador
        boolean esColaborador =
                colaboradorRepository
                        .existsByProyecto_IdProyectoAndUsuario_IdUsuario(
                                idProyecto,
                                usuario.getIdUsuario()
                        );


        // Verificar acceso
        if (
                !esPropietario &&
                !esColaborador
        ) {

            throw new RuntimeException(
                    "No tiene acceso a este proyecto"
            );

        }


        return convertirDTO(
                proyecto
        );
    }


    // ============================================================
    // OBTENER COLABORADORES DE UN PROYECTO
    // ============================================================

    @Transactional(readOnly = true)
    public List<ColaboradorResponseDTO> obtenerColaboradores(
            Long idProyecto
    ) {

        // --------------------------------------------------------
        // 1. Verificar que el proyecto exista
        // --------------------------------------------------------

        Proyecto proyecto =
                proyectoRepository.findById(
                        idProyecto
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Proyecto no encontrado"
                        )
                );


        // --------------------------------------------------------
        // 2. Obtener usuario autenticado
        // --------------------------------------------------------

        String username =
                AuthenticationUtil.obtenerUsernameAutenticado();


        Usuario usuario =
                usuarioRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario autenticado no encontrado"
                                )
                        );


        // --------------------------------------------------------
        // 3. Verificar que tenga acceso al proyecto
        // --------------------------------------------------------

        boolean esPropietario =
                proyecto.getPropietario()
                        .getIdUsuario()
                        .equals(
                                usuario.getIdUsuario()
                        );


        boolean esColaborador =
                colaboradorRepository
                        .existsByProyecto_IdProyectoAndUsuario_IdUsuario(
                                idProyecto,
                                usuario.getIdUsuario()
                        );


        if (
                !esPropietario &&
                !esColaborador
        ) {

            throw new RuntimeException(
                    "No tiene acceso a este proyecto"
            );

        }


        // --------------------------------------------------------
        // 4. Obtener colaboradores activos
        // --------------------------------------------------------

        return colaboradorRepository
                .findByProyecto_IdProyecto(
                        idProyecto
                )

                .stream()

                .filter(colaborador ->
                        "ACTIVO".equals(
                                colaborador.getEstado()
                        )
                )

                .map(colaborador -> {

                    Usuario usuarioColaborador =
                            colaborador.getUsuario();


                    return new ColaboradorResponseDTO(

                            usuarioColaborador
                                    .getIdUsuario(),

                            usuarioColaborador
                                    .getNombre(),

                            usuarioColaborador
                                    .getApellido(),

                            usuarioColaborador
                                    .getUsername(),

                            usuarioColaborador
                                    .getCorreo(),

                            colaborador
                                    .getRolProyecto(),

                            colaborador
                                    .getEstado()
                    );

                })

                .toList();
    }


    // ============================================================
    // CONVERTIR ENTIDAD → DTO
    // ============================================================

    private ProyectoResponseDTO convertirDTO(
            Proyecto proyecto
    ) {

        Usuario propietario =
                proyecto.getPropietario();


        String nombrePropietario =
                propietario.getNombre()
                        + " "
                        + propietario.getApellido();


        return new ProyectoResponseDTO(

                proyecto.getIdProyecto(),

                proyecto.getNombre(),

                proyecto.getDescripcion(),

                proyecto.getEstado(),

                propietario.getIdUsuario(),

                propietario.getUsername(),

                nombrePropietario,

                proyecto.getFechaCreacion(),

                proyecto.getFechaActualizacion()
        );
    }

}
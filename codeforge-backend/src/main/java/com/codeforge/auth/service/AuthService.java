package com.codeforge.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codeforge.auth.dto.LoginResponseDTO;
import com.codeforge.auth.dto.RegistroRequestDTO;
import com.codeforge.auth.dto.RegistroResponseDTO;
import com.codeforge.auth.security.JwtService;
import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================================================
    // REGISTRO
    // =========================================================

    public RegistroResponseDTO registrarUsuario(
            RegistroRequestDTO dto) {

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException(
                    "El username ya está registrado");
        }

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException(
                    "El correo ya está registrado");
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setUsername(dto.getUsername());
        usuario.setCorreo(dto.getCorreo());

        usuario.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        usuario.setEstado("ACTIVO");
        usuario.setFechaCreacion(LocalDateTime.now());

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        RegistroResponseDTO respuesta =
                new RegistroResponseDTO();

        respuesta.setIdUsuario(
                usuarioGuardado.getIdUsuario());

        respuesta.setNombre(
                usuarioGuardado.getNombre());

        respuesta.setApellido(
                usuarioGuardado.getApellido());

        respuesta.setUsername(
                usuarioGuardado.getUsername());

        respuesta.setCorreo(
                usuarioGuardado.getCorreo());

        respuesta.setEstado(
                usuarioGuardado.getEstado());

        return respuesta;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResponseDTO iniciarSesion(
            String username,
            String password) {

        Usuario usuario = usuarioRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario o contraseña incorrectos"));

        boolean passwordCorrecta =
                passwordEncoder.matches(
                        password,
                        usuario.getPassword()
                );

        if (!passwordCorrecta) {
            throw new RuntimeException(
                    "Usuario o contraseña incorrectos");
        }

        if (!usuario.getEstado().equals("ACTIVO")) {
            throw new RuntimeException(
                    "El usuario está inactivo");
        }

        usuario.setUltimoAcceso(
                LocalDateTime.now());

        usuarioRepository.save(usuario);

        // Generar JWT
        String token = jwtService.generarToken(usuario);

        LoginResponseDTO respuesta =
                new LoginResponseDTO();

        respuesta.setIdUsuario(
                usuario.getIdUsuario());

        respuesta.setNombre(
                usuario.getNombre());

        respuesta.setApellido(
                usuario.getApellido());

        respuesta.setUsername(
                usuario.getUsername());

        respuesta.setCorreo(
                usuario.getCorreo());

        respuesta.setEstado(
                usuario.getEstado());

        respuesta.setMensaje(
                "Inicio de sesión exitoso");

        respuesta.setToken(token);

        return respuesta;
    }
}
package com.codeforge.usuario.service;

import org.springframework.stereotype.Service;

import com.codeforge.usuario.dto.UsuarioBusquedaDTO;
import com.codeforge.usuario.entity.Usuario;
import com.codeforge.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioBusquedaDTO buscarPorUsername(
            String username
    ) {

        String usernameLimpio =
                username.trim();

        if (usernameLimpio.isBlank()) {
            throw new RuntimeException(
                    "Debe proporcionar un username"
            );
        }

        Usuario usuario =
                usuarioRepository
                        .findByUsername(usernameLimpio)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado"
                                )
                        );

        return new UsuarioBusquedaDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getUsername(),
                usuario.getCorreo()
        );
    }
}
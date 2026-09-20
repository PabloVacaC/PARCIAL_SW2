package com.codeforge.usuario.dto;

public record UsuarioBusquedaDTO(
        Long idUsuario,
        String nombre,
        String apellido,
        String username,
        String correo
) {
}
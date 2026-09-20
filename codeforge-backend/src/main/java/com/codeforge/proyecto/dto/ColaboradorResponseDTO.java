package com.codeforge.proyecto.dto;

public record ColaboradorResponseDTO(
        Long idUsuario,
        String nombre,
        String apellido,
        String username,
        String correo,
        String rolProyecto,
        String estado
) {
}
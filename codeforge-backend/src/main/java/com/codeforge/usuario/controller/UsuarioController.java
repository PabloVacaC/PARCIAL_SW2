package com.codeforge.usuario.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.usuario.dto.UsuarioBusquedaDTO;
import com.codeforge.usuario.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(
            UsuarioService usuarioService
    ) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<UsuarioBusquedaDTO> buscarUsuario(
            @RequestParam String username
    ) {

        return ResponseEntity.ok(
                usuarioService.buscarPorUsername(username)
        );
    }
}
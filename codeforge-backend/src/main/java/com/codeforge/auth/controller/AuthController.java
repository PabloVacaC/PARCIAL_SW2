package com.codeforge.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.auth.dto.LoginRequestDTO;
import com.codeforge.auth.dto.LoginResponseDTO;
import com.codeforge.auth.dto.RegistroRequestDTO;
import com.codeforge.auth.dto.RegistroResponseDTO;
import com.codeforge.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // REGISTRO
    // =========================================================

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponseDTO> registrarUsuario(
            @Valid @RequestBody RegistroRequestDTO dto) {

        RegistroResponseDTO respuesta =
                authService.registrarUsuario(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO respuesta =
                authService.iniciarSesion(
                        dto.getUsername(),
                        dto.getPassword()
                );

        return ResponseEntity.ok(respuesta);
    }
}
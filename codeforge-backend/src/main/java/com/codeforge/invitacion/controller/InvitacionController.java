package com.codeforge.invitacion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.invitacion.dto.InvitacionCreateDTO;
import com.codeforge.invitacion.dto.InvitacionResponseDTO;
import com.codeforge.invitacion.service.InvitacionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invitaciones")
public class InvitacionController {

    private final InvitacionService invitacionService;

    public InvitacionController(
            InvitacionService invitacionService) {

        this.invitacionService = invitacionService;
    }

    // =========================================================
    // CREAR INVITACIÓN
    // =========================================================

    @PostMapping
    public ResponseEntity<InvitacionResponseDTO> crearInvitacion(
            @Valid @RequestBody InvitacionCreateDTO dto) {

        InvitacionResponseDTO invitacion =
                invitacionService.crearInvitacion(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitacion);
    }

    // =========================================================
    // INVITACIONES RECIBIDAS
    // =========================================================

    @GetMapping("/recibidas/{idUsuario}")
    public ResponseEntity<List<InvitacionResponseDTO>>
            obtenerRecibidas(
                    @PathVariable Long idUsuario) {

        return ResponseEntity.ok(
                invitacionService
                        .obtenerRecibidas(idUsuario)
        );
    }

    // =========================================================
    // INVITACIONES ENVIADAS
    // =========================================================

    @GetMapping("/enviadas/{idUsuario}")
    public ResponseEntity<List<InvitacionResponseDTO>>
            obtenerEnviadas(
                    @PathVariable Long idUsuario) {

        return ResponseEntity.ok(
                invitacionService
                        .obtenerEnviadas(idUsuario)
        );
    }

    // =========================================================
    // ACEPTAR INVITACIÓN
    // =========================================================

    @PutMapping("/{idInvitacion}/aceptar")
    public ResponseEntity<InvitacionResponseDTO>
            aceptarInvitacion(
                    @PathVariable Long idInvitacion) {

        return ResponseEntity.ok(
                invitacionService
                        .aceptarInvitacion(idInvitacion)
        );
    }

    // =========================================================
    // RECHAZAR INVITACIÓN
    // =========================================================

    @PutMapping("/{idInvitacion}/rechazar")
    public ResponseEntity<InvitacionResponseDTO>
            rechazarInvitacion(
                    @PathVariable Long idInvitacion) {

        return ResponseEntity.ok(
                invitacionService
                        .rechazarInvitacion(idInvitacion)
        );
    }
}
package com.codeforge.proyecto.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.proyecto.dto.ColaboradorResponseDTO;
import com.codeforge.proyecto.dto.ProyectoCreateDTO;
import com.codeforge.proyecto.dto.ProyectoResponseDTO;
import com.codeforge.proyecto.service.ProyectoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(
            ProyectoService proyectoService
    ) {
        this.proyectoService = proyectoService;
    }


    // =========================================================
    // CREAR PROYECTO
    // =========================================================

    @PostMapping
    public ResponseEntity<ProyectoResponseDTO> crearProyecto(
            @Valid @RequestBody ProyectoCreateDTO dto
    ) {

        ProyectoResponseDTO proyecto =
                proyectoService.crearProyecto(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(proyecto);
    }


    // =========================================================
    // MIS PROYECTOS
    // =========================================================

    @GetMapping("/mis-proyectos")
    public ResponseEntity<List<ProyectoResponseDTO>> misProyectos() {

        return ResponseEntity.ok(
                proyectoService.obtenerMisProyectos()
        );
    }


    // =========================================================
    // OBTENER UN PROYECTO
    // =========================================================

    @GetMapping("/{idProyecto}")
    public ResponseEntity<ProyectoResponseDTO> obtenerProyecto(
            @PathVariable Long idProyecto
    ) {

        return ResponseEntity.ok(
                proyectoService.obtenerProyecto(idProyecto)
        );
    }


    // =========================================================
    // OBTENER COLABORADORES
    // =========================================================

    @GetMapping("/{idProyecto}/colaboradores")
    public ResponseEntity<List<ColaboradorResponseDTO>> obtenerColaboradores(
            @PathVariable Long idProyecto
    ) {

        return ResponseEntity.ok(
                proyectoService.obtenerColaboradores(idProyecto)
        );
    }

}
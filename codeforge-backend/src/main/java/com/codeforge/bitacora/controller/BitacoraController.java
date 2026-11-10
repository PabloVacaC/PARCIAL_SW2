package com.codeforge.bitacora.controller;

import com.codeforge.bitacora.dto.BitacoraEventoCreateDTO;
import com.codeforge.bitacora.dto.BitacoraEventoDTO;
import com.codeforge.bitacora.service.BitacoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/proyectos/{idProyecto}/bitacora")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping
    public ResponseEntity<List<BitacoraEventoDTO>> listarBitacora(@PathVariable("idProyecto") Long idProyecto) {
        return ResponseEntity.ok(bitacoraService.listarPorProyecto(idProyecto));
    }

    @PostMapping
    public ResponseEntity<BitacoraEventoDTO> registrarEvento(
            @PathVariable("idProyecto") Long idProyecto,
            @RequestBody BitacoraEventoCreateDTO dto
    ) {
        dto.setIdProyecto(idProyecto);
        return ResponseEntity.ok(bitacoraService.registrarEvento(dto));
    }
}

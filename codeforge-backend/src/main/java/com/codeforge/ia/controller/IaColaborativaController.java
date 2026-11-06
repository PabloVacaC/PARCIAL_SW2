package com.codeforge.ia.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeforge.ia.dto.IaChatMensajeDTO;
import com.codeforge.ia.dto.IaDecisionRequestDTO;
import com.codeforge.ia.dto.IaPreguntaRequestDTO;
import com.codeforge.ia.dto.IaPropuestaDTO;
import com.codeforge.ia.dto.IaUmlFindingDTO;
import com.codeforge.ia.entity.IaConversacion;
import com.codeforge.ia.service.IaColaborativaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ia/colaborativa")
@CrossOrigin(origins = "http://localhost:4200")
public class IaColaborativaController {

    private final IaColaborativaService iaColaborativaService;

    public IaColaborativaController(IaColaborativaService iaColaborativaService) {
        this.iaColaborativaService = iaColaborativaService;
    }

    // ============================================================
    // OBTENER CONVERSACIÓN E HISTORIAL DE MENSAJES
    // ============================================================

    @GetMapping("/proyecto/{idProyecto}/conversacion")
    public ResponseEntity<Map<String, Object>> obtenerConversacion(
            @PathVariable Long idProyecto
    ) {
        IaConversacion conv = iaColaborativaService.obtenerOCrearConversacion(idProyecto);
        List<IaChatMensajeDTO> historial = iaColaborativaService.obtenerHistorial(idProyecto);

        return ResponseEntity.ok(Map.of(
                "idConversacion", conv.getIdConversacion(),
                "titulo", conv.getTitulo(),
                "idProyecto", idProyecto,
                "mensajes", historial
        ));
    }

    // ============================================================
    // ENVIAR MENSAJE HUMANO Y GENERAR RESPUESTA IA
    // ============================================================

    @PostMapping("/proyecto/{idProyecto}/mensaje")
    public ResponseEntity<List<IaChatMensajeDTO>> enviarMensaje(
            @PathVariable Long idProyecto,
            @Valid @RequestBody IaPreguntaRequestDTO dto
    ) {
        List<IaChatMensajeDTO> nuevosMensajes = iaColaborativaService.enviarMensajeYProcesar(idProyecto, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevosMensajes);
    }

    // ============================================================
    // LISTAR PROPUESTAS DE CAMBIO
    // ============================================================

    @GetMapping("/proyecto/{idProyecto}/propuestas")
    public ResponseEntity<List<IaPropuestaDTO>> listarPropuestas(
            @PathVariable Long idProyecto
    ) {
        List<IaPropuestaDTO> propuestas = iaColaborativaService.listarPropuestas(idProyecto);
        return ResponseEntity.ok(propuestas);
    }

    // ============================================================
    // DETALLE DE PROPUESTA
    // ============================================================

    @GetMapping("/proyecto/{idProyecto}/propuestas/{idPropuesta}")
    public ResponseEntity<IaPropuestaDTO> obtenerPropuesta(
            @PathVariable Long idProyecto,
            @PathVariable Long idPropuesta
    ) {
        IaPropuestaDTO propuesta = iaColaborativaService.obtenerPropuesta(idProyecto, idPropuesta);
        return ResponseEntity.ok(propuesta);
    }

    // ============================================================
    // APROBAR O RECHAZAR PROPUESTA
    // ============================================================

    @PostMapping("/proyecto/{idProyecto}/propuestas/{idPropuesta}/decision")
    public ResponseEntity<IaPropuestaDTO> decidirPropuesta(
            @PathVariable Long idProyecto,
            @PathVariable Long idPropuesta,
            @Valid @RequestBody IaDecisionRequestDTO dto
    ) {
        IaPropuestaDTO resultado = iaColaborativaService.decidirPropuesta(idProyecto, idPropuesta, dto);
        return ResponseEntity.ok(resultado);
    }

    // ============================================================
    // APLICAR PROPUESTA (CONTROL DE CONCURRENCIA Y VERSIONADO)
    // ============================================================

    @PostMapping("/proyecto/{idProyecto}/propuestas/{idPropuesta}/aplicar")
    public ResponseEntity<IaPropuestaDTO> aplicarPropuesta(
            @PathVariable Long idProyecto,
            @PathVariable Long idPropuesta
    ) {
        IaPropuestaDTO resultado = iaColaborativaService.aplicarPropuesta(idProyecto, idPropuesta);
        return ResponseEntity.ok(resultado);
    }

    // ============================================================
    // REVERTIR PROPUESTA (VOLVER ATRÁS ANTES DE SU APLICACIÓN)
    // ============================================================

    @PostMapping("/proyecto/{idProyecto}/propuestas/{idPropuesta}/revertir")
    public ResponseEntity<IaPropuestaDTO> revertirPropuesta(
            @PathVariable Long idProyecto,
            @PathVariable Long idPropuesta
    ) {
        IaPropuestaDTO resultado = iaColaborativaService.revertirPropuesta(idProyecto, idPropuesta);
        return ResponseEntity.ok(resultado);
    }

    // ============================================================
    // DIAGNÓSTICO Y ANÁLISIS UML PROGRAMÁTICO
    // ============================================================

    @GetMapping("/proyecto/{idProyecto}/diagnostico")
    public ResponseEntity<List<IaUmlFindingDTO>> obtenerDiagnostico(
            @PathVariable Long idProyecto,
            @RequestParam(required = false) Long idModelo
    ) {
        List<IaUmlFindingDTO> hallazgos = iaColaborativaService.diagnosticarModelo(idProyecto, idModelo);
        return ResponseEntity.ok(hallazgos);
    }
}

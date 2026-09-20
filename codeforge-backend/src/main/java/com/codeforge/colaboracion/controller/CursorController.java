package com.codeforge.colaboracion.controller;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.codeforge.colaboracion.config.WebSocketEventListener;
import com.codeforge.colaboracion.dto.CursorMessage;

@Controller
public class CursorController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketEventListener eventListener;

    public CursorController(SimpMessagingTemplate messagingTemplate, WebSocketEventListener eventListener) {
        this.messagingTemplate = messagingTemplate;
        this.eventListener = eventListener;
    }

    /**
     * Recibe la posición del cursor de un usuario y la retransmite
     * a todos los suscriptores del proyecto:
     * Destino: /topic/proyecto/{idProyecto}/cursores
     */
    @MessageMapping("/cursor")
    public void actualizarCursor(
            @Payload CursorMessage mensaje,
            @Header(value = "simpSessionId", required = false) String sessionId
    ) {
        if (mensaje != null && mensaje.getProyectoId() != null) {
            if (sessionId != null && mensaje.getUsuarioId() != null) {
                eventListener.registrarSesion(sessionId, mensaje.getProyectoId(), mensaje.getUsuarioId());
            }

            String destino = "/topic/proyecto/" + mensaje.getProyectoId() + "/cursores";
            messagingTemplate.convertAndSend(destino, mensaje);
        }
    }
}

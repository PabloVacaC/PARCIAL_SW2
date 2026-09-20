package com.codeforge.colaboracion.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.codeforge.colaboracion.dto.CursorMessage;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, CursorSession> sesiones = new ConcurrentHashMap<>();

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Asocia una sesión de WebSocket a un usuario y proyecto.
     */
    public void registrarSesion(String sessionId, Long proyectoId, Long usuarioId) {
        if (sessionId != null && proyectoId != null && usuarioId != null) {
            sesiones.put(sessionId, new CursorSession(proyectoId, usuarioId));
        }
    }

    /**
     * Captura el evento de desconexión STOMP/WebSocket y notifica
     * al canal del proyecto que el usuario salió para limpiar su cursor.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        CursorSession session = sesiones.remove(sessionId);

        if (session != null && session.proyectoId != null && session.usuarioId != null) {
            log.info("WebSocket: Usuario {} desconectado del proyecto {}", session.usuarioId, session.proyectoId);

            CursorMessage salida = new CursorMessage(
                    session.proyectoId,
                    session.usuarioId,
                    0.0,
                    0.0,
                    "",
                    "LEAVE"
            );

            String destino = "/topic/proyecto/" + session.proyectoId + "/cursores";
            messagingTemplate.convertAndSend(destino, salida);
        }
    }

    public static class CursorSession {
        private final Long proyectoId;
        private final Long usuarioId;

        public CursorSession(Long proyectoId, Long usuarioId) {
            this.proyectoId = proyectoId;
            this.usuarioId = usuarioId;
        }

        public Long getProyectoId() {
            return proyectoId;
        }

        public Long getUsuarioId() {
            return usuarioId;
        }
    }
}

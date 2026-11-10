package com.codeforge.bitacora.service;

import com.codeforge.bitacora.dto.BitacoraEventoCreateDTO;
import com.codeforge.bitacora.dto.BitacoraEventoDTO;
import com.codeforge.bitacora.entity.BitacoraEvento;
import com.codeforge.bitacora.repository.BitacoraEventoRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BitacoraService {

    private final BitacoraEventoRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public BitacoraService(BitacoraEventoRepository repository, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public BitacoraEventoDTO registrarEvento(BitacoraEventoCreateDTO dto) {
        if (dto.getIdProyecto() == null) {
            throw new IllegalArgumentException("El id del proyecto es obligatorio para la bitácora.");
        }

        BitacoraEvento evento = new BitacoraEvento(
                dto.getIdProyecto(),
                dto.getUsuarioId(),
                dto.getUsuarioNombre() != null ? dto.getUsuarioNombre() : (dto.isEsIa() ? "IA CodeForge" : "Usuario"),
                dto.getUsuarioUsername(),
                dto.isEsIa(),
                dto.getTipoAccion() != null ? dto.getTipoAccion() : "ACCION",
                dto.getOrigen() != null ? dto.getOrigen() : (dto.isEsIa() ? "CHAT_COLABORATIVO" : "LIENZO_MANUAL"),
                dto.getDescripcion() != null ? dto.getDescripcion() : "Cambio realizado en el modelo UML",
                dto.getDetallesJson(),
                LocalDateTime.now()
        );

        BitacoraEvento guardado = repository.save(evento);
        BitacoraEventoDTO eventoDTO = convertirADTO(guardado);

        // Notificar en tiempo real a todos los clientes suscritos al proyecto vía WebSocket STOMP
        try {
            messagingTemplate.convertAndSend("/topic/proyecto/" + dto.getIdProyecto() + "/bitacora", eventoDTO);
        } catch (Exception e) {
            System.err.println("CODEFORGE BITACORA: Error enviando evento por WebSocket: " + e.getMessage());
        }

        return eventoDTO;
    }

    @Transactional(readOnly = true)
    public List<BitacoraEventoDTO> listarPorProyecto(Long idProyecto) {
        return repository.findByIdProyectoOrderByFechaHoraDesc(idProyecto)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private BitacoraEventoDTO convertirADTO(BitacoraEvento e) {
        return new BitacoraEventoDTO(
                e.getId(),
                e.getIdProyecto(),
                e.getUsuarioId(),
                e.getUsuarioNombre(),
                e.getUsuarioUsername(),
                e.isEsIa(),
                e.getTipoAccion(),
                e.getOrigen(),
                e.getDescripcion(),
                e.getDetallesJson(),
                e.getFechaHora()
        );
    }
}

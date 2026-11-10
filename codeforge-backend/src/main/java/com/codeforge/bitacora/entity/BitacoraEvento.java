package com.codeforge.bitacora.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_evento")
public class BitacoraEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_proyecto", nullable = false)
    private Long idProyecto;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nombre", nullable = false, length = 150)
    private String usuarioNombre;

    @Column(name = "usuario_username", length = 100)
    private String usuarioUsername;

    @Column(name = "es_ia", nullable = false)
    private boolean esIa;

    @Column(name = "tipo_accion", nullable = false, length = 60)
    private String tipoAccion;

    @Column(name = "origen", nullable = false, length = 60)
    private String origen; // LIENZO_MANUAL, CHAT_COLABORATIVO

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "detalles_json", columnDefinition = "TEXT")
    private String detallesJson;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public BitacoraEvento() {
    }

    public BitacoraEvento(Long idProyecto, Long usuarioId, String usuarioNombre, String usuarioUsername,
                          boolean esIa, String tipoAccion, String origen, String descripcion,
                          String detallesJson, LocalDateTime fechaHora) {
        this.idProyecto = idProyecto;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.usuarioUsername = usuarioUsername;
        this.esIa = esIa;
        this.tipoAccion = tipoAccion;
        this.origen = origen;
        this.descripcion = descripcion;
        this.detallesJson = detallesJson;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }

    public boolean isEsIa() {
        return esIa;
    }

    public void setEsIa(boolean esIa) {
        this.esIa = esIa;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDetallesJson() {
        return detallesJson;
    }

    public void setDetallesJson(String detallesJson) {
        this.detallesJson = detallesJson;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}

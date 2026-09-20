package com.codeforge.invitacion.dto;

import java.time.LocalDateTime;

public class InvitacionResponseDTO {

    private Long idInvitacion;

    private Long idProyecto;
    private String nombreProyecto;

    private Long idEmisor;
    private String nombreEmisor;

    private Long idReceptor;
    private String nombreReceptor;

    private String rolPropuesto;
    private String estado;

    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRespuesta;

    public InvitacionResponseDTO() {
    }

    public Long getIdInvitacion() {
        return idInvitacion;
    }

    public void setIdInvitacion(Long idInvitacion) {
        this.idInvitacion = idInvitacion;
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public Long getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(Long idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    public Long getIdReceptor() {
        return idReceptor;
    }

    public void setIdReceptor(Long idReceptor) {
        this.idReceptor = idReceptor;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public void setNombreReceptor(String nombreReceptor) {
        this.nombreReceptor = nombreReceptor;
    }

    public String getRolPropuesto() {
        return rolPropuesto;
    }

    public void setRolPropuesto(String rolPropuesto) {
        this.rolPropuesto = rolPropuesto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}
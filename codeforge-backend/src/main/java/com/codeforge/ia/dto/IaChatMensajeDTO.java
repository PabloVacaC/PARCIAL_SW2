package com.codeforge.ia.dto;

import java.time.LocalDateTime;

public class IaChatMensajeDTO {

    private Long idMensaje;
    private Long idConversacion;
    private Long idUsuario;
    private String nombreEmisor;
    private String usernameEmisor;
    private String tipoEmisor; // "USUARIO", "IA", "SISTEMA"
    private String modoIa; // "GENERAL", "MODELADOR_UML", "REQUISITOS", "ARQUITECTO", "DESARROLLADOR", "REVISOR_PRUEBAS", "DOCUMENTADOR"
    private String contenido;
    private String metadatosJson;
    private Long idPropuestaAsociada;
    private IaPropuestaDTO propuestaAsociada;
    private LocalDateTime fechaEnvio;

    public IaChatMensajeDTO() {
    }

    public Long getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Long idMensaje) {
        this.idMensaje = idMensaje;
    }

    public Long getIdConversacion() {
        return idConversacion;
    }

    public void setIdConversacion(Long idConversacion) {
        this.idConversacion = idConversacion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    public String getUsernameEmisor() {
        return usernameEmisor;
    }

    public void setUsernameEmisor(String usernameEmisor) {
        this.usernameEmisor = usernameEmisor;
    }

    public String getTipoEmisor() {
        return tipoEmisor;
    }

    public void setTipoEmisor(String tipoEmisor) {
        this.tipoEmisor = tipoEmisor;
    }

    public String getModoIa() {
        return modoIa;
    }

    public void setModoIa(String modoIa) {
        this.modoIa = modoIa;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getMetadatosJson() {
        return metadatosJson;
    }

    public void setMetadatosJson(String metadatosJson) {
        this.metadatosJson = metadatosJson;
    }

    public Long getIdPropuestaAsociada() {
        return idPropuestaAsociada;
    }

    public void setIdPropuestaAsociada(Long idPropuestaAsociada) {
        this.idPropuestaAsociada = idPropuestaAsociada;
    }

    public IaPropuestaDTO getPropuestaAsociada() {
        return propuestaAsociada;
    }

    public void setPropuestaAsociada(IaPropuestaDTO propuestaAsociada) {
        this.propuestaAsociada = propuestaAsociada;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}

package com.codeforge.ia.dto;

import java.time.LocalDateTime;

public class IaPropuestaDTO {

    private Long idPropuesta;
    private Long idProyecto;
    private Long idModelo;
    private Long idConversacion;
    private String versionOrigen;
    private String titulo;
    private String descripcionProblema;
    private String solucionPropuesta;
    private String riesgosEfectos;
    private String operacionesJson;
    private String estado; // "PENDIENTE", "APROBADA", "RECHAZADA", "APLICADA", "OBSOLETA"
    private Long idSolicitante;
    private String nombreSolicitante;
    private Long idAprobador;
    private String nombreAprobador;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaDecision;
    private LocalDateTime fechaAplicacion;
    private String versionResultante;
    private String resultadoAplicacion;
    private Integer totalOperaciones;

    public IaPropuestaDTO() {
    }

    public Long getIdPropuesta() {
        return idPropuesta;
    }

    public void setIdPropuesta(Long idPropuesta) {
        this.idPropuesta = idPropuesta;
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Long getIdModelo() {
        return idModelo;
    }

    public void setIdModelo(Long idModelo) {
        this.idModelo = idModelo;
    }

    public Long getIdConversacion() {
        return idConversacion;
    }

    public void setIdConversacion(Long idConversacion) {
        this.idConversacion = idConversacion;
    }

    public String getVersionOrigen() {
        return versionOrigen;
    }

    public void setVersionOrigen(String versionOrigen) {
        this.versionOrigen = versionOrigen;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcionProblema() {
        return descripcionProblema;
    }

    public void setDescripcionProblema(String descripcionProblema) {
        this.descripcionProblema = descripcionProblema;
    }

    public String getSolucionPropuesta() {
        return solucionPropuesta;
    }

    public void setSolucionPropuesta(String solucionPropuesta) {
        this.solucionPropuesta = solucionPropuesta;
    }

    public String getRiesgosEfectos() {
        return riesgosEfectos;
    }

    public void setRiesgosEfectos(String riesgosEfectos) {
        this.riesgosEfectos = riesgosEfectos;
    }

    public String getOperacionesJson() {
        return operacionesJson;
    }

    public void setOperacionesJson(String operacionesJson) {
        this.operacionesJson = operacionesJson;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(Long idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public Long getIdAprobador() {
        return idAprobador;
    }

    public void setIdAprobador(Long idAprobador) {
        this.idAprobador = idAprobador;
    }

    public String getNombreAprobador() {
        return nombreAprobador;
    }

    public void setNombreAprobador(String nombreAprobador) {
        this.nombreAprobador = nombreAprobador;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaDecision() {
        return fechaDecision;
    }

    public void setFechaDecision(LocalDateTime fechaDecision) {
        this.fechaDecision = fechaDecision;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public String getVersionResultante() {
        return versionResultante;
    }

    public void setVersionResultante(String versionResultante) {
        this.versionResultante = versionResultante;
    }

    public String getResultadoAplicacion() {
        return resultadoAplicacion;
    }

    public void setResultadoAplicacion(String resultadoAplicacion) {
        this.resultadoAplicacion = resultadoAplicacion;
    }

    public Integer getTotalOperaciones() {
        return totalOperaciones;
    }

    public void setTotalOperaciones(Integer totalOperaciones) {
        this.totalOperaciones = totalOperaciones;
    }
}

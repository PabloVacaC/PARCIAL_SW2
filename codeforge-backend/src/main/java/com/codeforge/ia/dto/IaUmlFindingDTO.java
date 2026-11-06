package com.codeforge.ia.dto;

public class IaUmlFindingDTO {

    private String id;
    private String elementoAfectado;
    private String tipoProblema; // "ERROR_ESTRUCTURAL", "INCONSISTENCIA", "REGLA_NEGOCIO", "RECOMENDACION_DISENO"
    private String severidad; // "ALTA", "MEDIA", "BAJA", "INFO"
    private String descripcion;
    private String evidencia;
    private String propuestaSolucion;
    private String posiblesEfectosSecundarios;
    private Boolean requiereConfirmacion;

    public IaUmlFindingDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getElementoAfectado() {
        return elementoAfectado;
    }

    public void setElementoAfectado(String elementoAfectado) {
        this.elementoAfectado = elementoAfectado;
    }

    public String getTipoProblema() {
        return tipoProblema;
    }

    public void setTipoProblema(String tipoProblema) {
        this.tipoProblema = tipoProblema;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getPropuestaSolucion() {
        return propuestaSolucion;
    }

    public void setPropuestaSolucion(String propuestaSolucion) {
        this.propuestaSolucion = propuestaSolucion;
    }

    public String getPosiblesEfectosSecundarios() {
        return posiblesEfectosSecundarios;
    }

    public void setPosiblesEfectosSecundarios(String posiblesEfectosSecundarios) {
        this.posiblesEfectosSecundarios = posiblesEfectosSecundarios;
    }

    public Boolean getRequiereConfirmacion() {
        return requiereConfirmacion;
    }

    public void setRequiereConfirmacion(Boolean requiereConfirmacion) {
        this.requiereConfirmacion = requiereConfirmacion;
    }
}

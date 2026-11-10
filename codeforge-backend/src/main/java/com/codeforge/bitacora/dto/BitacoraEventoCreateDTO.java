package com.codeforge.bitacora.dto;

public class BitacoraEventoCreateDTO {

    private Long idProyecto;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioUsername;
    private boolean esIa;
    private String tipoAccion;
    private String origen;
    private String descripcion;
    private String detallesJson;

    public BitacoraEventoCreateDTO() {
    }

    public BitacoraEventoCreateDTO(Long idProyecto, Long usuarioId, String usuarioNombre,
                                   String usuarioUsername, boolean esIa, String tipoAccion,
                                   String origen, String descripcion, String detallesJson) {
        this.idProyecto = idProyecto;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.usuarioUsername = usuarioUsername;
        this.esIa = esIa;
        this.tipoAccion = tipoAccion;
        this.origen = origen;
        this.descripcion = descripcion;
        this.detallesJson = detallesJson;
    }

    // Getters y Setters

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
}

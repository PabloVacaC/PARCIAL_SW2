package com.codeforge.ia.dto;

import jakarta.validation.constraints.NotBlank;

public class IaPreguntaRequestDTO {

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    private String modoIa = "GENERAL"; // "GENERAL", "MODELADOR_UML", "REQUISITOS", "ARQUITECTO", "DESARROLLADOR", "REVISOR_PRUEBAS", "DOCUMENTADOR"

    private Long idModelo;

    private Boolean generarPropuesta = false;

    public IaPreguntaRequestDTO() {
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getModoIa() {
        return modoIa;
    }

    public void setModoIa(String modoIa) {
        this.modoIa = modoIa;
    }

    public Long getIdModelo() {
        return idModelo;
    }

    public void setIdModelo(Long idModelo) {
        this.idModelo = idModelo;
    }

    public Boolean getGenerarPropuesta() {
        return generarPropuesta;
    }

    public void setGenerarPropuesta(Boolean generarPropuesta) {
        this.generarPropuesta = generarPropuesta;
    }
}

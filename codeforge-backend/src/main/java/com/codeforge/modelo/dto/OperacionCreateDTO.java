package com.codeforge.modelo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OperacionCreateDTO {

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @Size(max = 20)
    private String visibilidad;

    @Size(max = 100)
    private String tipoRetorno;

    private Boolean estatica;

    private Boolean abstracta;

    public OperacionCreateDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(String visibilidad) {
        this.visibilidad = visibilidad;
    }

    public String getTipoRetorno() {
        return tipoRetorno;
    }

    public void setTipoRetorno(String tipoRetorno) {
        this.tipoRetorno = tipoRetorno;
    }

    public Boolean getEstatica() {
        return estatica;
    }

    public void setEstatica(Boolean estatica) {
        this.estatica = estatica;
    }

    public Boolean getAbstracta() {
        return abstracta;
    }

    public void setAbstracta(Boolean abstracta) {
        this.abstracta = abstracta;
    }
}
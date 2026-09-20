package com.codeforge.modelo.dto;

import jakarta.validation.constraints.Size;

public class ElementoUpdateDTO {

    @Size(max = 150)
    private String nombre;

    @Size(max = 20)
    private String visibilidad;

    @Size(max = 100)
    private String estereotipo;

    private Boolean abstracto;

    private Boolean finalElemento;

    private Double posicionX;

    private Double posicionY;

    private Double ancho;

    private Double alto;

    public ElementoUpdateDTO() {
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

    public String getEstereotipo() {
        return estereotipo;
    }

    public void setEstereotipo(String estereotipo) {
        this.estereotipo = estereotipo;
    }

    public Boolean getAbstracto() {
        return abstracto;
    }

    public void setAbstracto(Boolean abstracto) {
        this.abstracto = abstracto;
    }

    public Boolean getFinalElemento() {
        return finalElemento;
    }

    public void setFinalElemento(Boolean finalElemento) {
        this.finalElemento = finalElemento;
    }

    public Double getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(Double posicionX) {
        this.posicionX = posicionX;
    }

    public Double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(Double posicionY) {
        this.posicionY = posicionY;
    }

    public Double getAncho() {
        return ancho;
    }

    public void setAncho(Double ancho) {
        this.ancho = ancho;
    }

    public Double getAlto() {
        return alto;
    }

    public void setAlto(Double alto) {
        this.alto = alto;
    }
}
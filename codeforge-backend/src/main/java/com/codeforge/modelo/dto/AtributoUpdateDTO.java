package com.codeforge.modelo.dto;

import jakarta.validation.constraints.Size;

public class AtributoUpdateDTO {

    @Size(max = 150)
    private String nombre;

    @Size(max = 100)
    private String tipo;

    @Size(max = 20)
    private String visibilidad;

    @Size(max = 30)
    private String multiplicidad;

    private Boolean derivado;

    private Boolean estatico;

    private String valorInicial;

    public AtributoUpdateDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(String visibilidad) {
        this.visibilidad = visibilidad;
    }

    public String getMultiplicidad() {
        return multiplicidad;
    }

    public void setMultiplicidad(String multiplicidad) {
        this.multiplicidad = multiplicidad;
    }

    public Boolean getDerivado() {
        return derivado;
    }

    public void setDerivado(Boolean derivado) {
        this.derivado = derivado;
    }

    public Boolean getEstatico() {
        return estatico;
    }

    public void setEstatico(Boolean estatico) {
        this.estatico = estatico;
    }

    public String getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(String valorInicial) {
        this.valorInicial = valorInicial;
    }
}
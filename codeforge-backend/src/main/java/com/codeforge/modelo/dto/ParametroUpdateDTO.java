package com.codeforge.modelo.dto;

import jakarta.validation.constraints.Size;

public class ParametroUpdateDTO {

    @Size(max = 150)
    private String nombre;

    @Size(max = 100)
    private String tipo;

    @Size(max = 20)
    private String direccion;

    private String valorInicial;

    public ParametroUpdateDTO() {
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(String valorInicial) {
        this.valorInicial = valorInicial;
    }
}
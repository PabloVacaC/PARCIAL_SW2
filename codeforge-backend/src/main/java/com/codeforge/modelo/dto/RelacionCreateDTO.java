package com.codeforge.modelo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RelacionCreateDTO {

    @NotBlank
    @Size(max = 30)
    private String tipo;

    @NotBlank
    private String origen;

    @NotBlank
    private String destino;

    @Size(max = 100)
    private String rolOrigen;

    @Size(max = 100)
    private String rolDestino;

    @Size(max = 30)
    private String multiplicidadOrigen;

    @Size(max = 30)
    private String multiplicidadDestino;

    public RelacionCreateDTO() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getRolOrigen() {
        return rolOrigen;
    }

    public void setRolOrigen(String rolOrigen) {
        this.rolOrigen = rolOrigen;
    }

    public String getRolDestino() {
        return rolDestino;
    }

    public void setRolDestino(String rolDestino) {
        this.rolDestino = rolDestino;
    }

    public String getMultiplicidadOrigen() {
        return multiplicidadOrigen;
    }

    public void setMultiplicidadOrigen(String multiplicidadOrigen) {
        this.multiplicidadOrigen = multiplicidadOrigen;
    }

    public String getMultiplicidadDestino() {
        return multiplicidadDestino;
    }

    public void setMultiplicidadDestino(String multiplicidadDestino) {
        this.multiplicidadDestino = multiplicidadDestino;
    }
}
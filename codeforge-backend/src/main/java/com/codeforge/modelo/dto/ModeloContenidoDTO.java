package com.codeforge.modelo.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

public class ModeloContenidoDTO {

    @NotNull
    private Map<String, Object> contenidoJson;


    public ModeloContenidoDTO() {
    }


    public Map<String, Object> getContenidoJson() {
        return contenidoJson;
    }


    public void setContenidoJson(
            Map<String, Object> contenidoJson
    ) {
        this.contenidoJson = contenidoJson;
    }

}
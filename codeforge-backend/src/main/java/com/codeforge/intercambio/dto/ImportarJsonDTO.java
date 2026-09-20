package com.codeforge.intercambio.dto;

import java.util.Map;

public class ImportarJsonDTO {

    private Map<String, Object> contenidoJson;

    public ImportarJsonDTO() {
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
package com.codeforge.modelo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ModeloResponseDTO {

    private Long idModelo;

    private Long idProyecto;

    private String nombre;

    private String descripcion;

    private String version;

    private Map<String, Object> contenidoJson;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;


    public ModeloResponseDTO() {
    }


    public ModeloResponseDTO(
            Long idModelo,
            Long idProyecto,
            String nombre,
            String descripcion,
            String version,
            Map<String, Object> contenidoJson,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion
    ) {

        this.idModelo = idModelo;
        this.idProyecto = idProyecto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.version = version;
        this.contenidoJson = contenidoJson;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion =
                fechaActualizacion;
    }


    public Long getIdModelo() {
        return idModelo;
    }

    public void setIdModelo(Long idModelo) {
        this.idModelo = idModelo;
    }


    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public Map<String, Object> getContenidoJson() {
        return contenidoJson;
    }

    public void setContenidoJson(
            Map<String, Object> contenidoJson
    ) {
        this.contenidoJson = contenidoJson;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {
        this.fechaCreacion = fechaCreacion;
    }


    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {
        this.fechaActualizacion =
                fechaActualizacion;
    }

}
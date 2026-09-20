package com.codeforge.proyecto.dto;

import java.time.LocalDateTime;

public class ProyectoResponseDTO {

    private Long idProyecto;
    private String nombre;
    private String descripcion;
    private String estado;

    private Long idPropietario;
    private String usernamePropietario;
    private String nombrePropietario;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public ProyectoResponseDTO() {
    }

    public ProyectoResponseDTO(
            Long idProyecto,
            String nombre,
            String descripcion,
            String estado,
            Long idPropietario,
            String usernamePropietario,
            String nombrePropietario,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion
    ) {
        this.idProyecto = idProyecto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.idPropietario = idPropietario;
        this.usernamePropietario = usernamePropietario;
        this.nombrePropietario = nombrePropietario;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
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

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(Long idPropietario) {
        this.idPropietario = idPropietario;
    }

    public String getUsernamePropietario() {
        return usernamePropietario;
    }

    public void setUsernamePropietario(String usernamePropietario) {
        this.usernamePropietario = usernamePropietario;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
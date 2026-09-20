package com.codeforge.modelo.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.codeforge.proyecto.entity.Proyecto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "modelo")
public class Modelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modelo")
    private Long idModelo;

    @Column(
        name = "nombre",
        nullable = false,
        length = 150
    )
    private String nombre;

    @Column(
        name = "descripcion",
        length = 500
    )
    private String descripcion;

    @Column(
        name = "version",
        nullable = false,
        length = 30
    )
    private String version = "1.0";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "contenido_json",
        nullable = false,
        columnDefinition = "jsonb"
    )
    private Map<String, Object> contenidoJson;

    @Column(
        name = "fecha_creacion",
        nullable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "id_proyecto",
        nullable = false
    )
    private Proyecto proyecto;


    public Modelo() {
    }


    public Long getIdModelo() {
        return idModelo;
    }

    public void setIdModelo(Long idModelo) {
        this.idModelo = idModelo;
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


    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(
            Proyecto proyecto
    ) {
        this.proyecto = proyecto;
    }

}
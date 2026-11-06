package com.codeforge.ia.entity;

import java.time.LocalDateTime;

import com.codeforge.modelo.entity.Modelo;
import com.codeforge.proyecto.entity.Proyecto;
import com.codeforge.usuario.entity.Usuario;

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
@Table(name = "ia_propuesta")
public class IaPropuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_propuesta")
    private Long idPropuesta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modelo", nullable = false)
    private Modelo modelo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conversacion", nullable = false)
    private IaConversacion conversacion;

    @Column(name = "version_origen", nullable = false, length = 30)
    private String versionOrigen;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion_problema", columnDefinition = "TEXT")
    private String descripcionProblema;

    @Column(name = "solucion_propuesta", columnDefinition = "TEXT")
    private String solucionPropuesta;

    @Column(name = "riesgos_efectos", columnDefinition = "TEXT")
    private String riesgosEfectos;

    @Column(name = "operaciones_json", nullable = false, columnDefinition = "TEXT")
    private String operacionesJson;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE"; // "PENDIENTE", "APROBADA", "RECHAZADA", "APLICADA", "OBSOLETA"

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aprobador")
    private Usuario aprobador;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    @Column(name = "fecha_aplicacion")
    private LocalDateTime fechaAplicacion;

    @Column(name = "version_resultante", length = 30)
    private String versionResultante;

    @Column(name = "resultado_aplicacion", columnDefinition = "TEXT")
    private String resultadoAplicacion;

    @Column(name = "snapshot_previo_json", columnDefinition = "TEXT")
    private String snapshotPrevioJson;

    public IaPropuesta() {
    }

    public Long getIdPropuesta() {
        return idPropuesta;
    }

    public void setIdPropuesta(Long idPropuesta) {
        this.idPropuesta = idPropuesta;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public IaConversacion getConversacion() {
        return conversacion;
    }

    public void setConversacion(IaConversacion conversacion) {
        this.conversacion = conversacion;
    }

    public String getVersionOrigen() {
        return versionOrigen;
    }

    public void setVersionOrigen(String versionOrigen) {
        this.versionOrigen = versionOrigen;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcionProblema() {
        return descripcionProblema;
    }

    public void setDescripcionProblema(String descripcionProblema) {
        this.descripcionProblema = descripcionProblema;
    }

    public String getSolucionPropuesta() {
        return solucionPropuesta;
    }

    public void setSolucionPropuesta(String solucionPropuesta) {
        this.solucionPropuesta = solucionPropuesta;
    }

    public String getRiesgosEfectos() {
        return riesgosEfectos;
    }

    public void setRiesgosEfectos(String riesgosEfectos) {
        this.riesgosEfectos = riesgosEfectos;
    }

    public String getOperacionesJson() {
        return operacionesJson;
    }

    public void setOperacionesJson(String operacionesJson) {
        this.operacionesJson = operacionesJson;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Usuario getAprobador() {
        return aprobador;
    }

    public void setAprobador(Usuario aprobador) {
        this.aprobador = aprobador;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaDecision() {
        return fechaDecision;
    }

    public void setFechaDecision(LocalDateTime fechaDecision) {
        this.fechaDecision = fechaDecision;
    }

    public LocalDateTime getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDateTime fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public String getVersionResultante() {
        return versionResultante;
    }

    public void setVersionResultante(String versionResultante) {
        this.versionResultante = versionResultante;
    }

    public String getResultadoAplicacion() {
        return resultadoAplicacion;
    }

    public void setResultadoAplicacion(String resultadoAplicacion) {
        this.resultadoAplicacion = resultadoAplicacion;
    }

    public String getSnapshotPrevioJson() {
        return snapshotPrevioJson;
    }

    public void setSnapshotPrevioJson(String snapshotPrevioJson) {
        this.snapshotPrevioJson = snapshotPrevioJson;
    }
}

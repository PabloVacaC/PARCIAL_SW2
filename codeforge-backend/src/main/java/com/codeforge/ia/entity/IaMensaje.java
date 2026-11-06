package com.codeforge.ia.entity;

import java.time.LocalDateTime;

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
@Table(name = "ia_mensaje")
public class IaMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long idMensaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conversacion", nullable = false)
    private IaConversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre_emisor", nullable = false, length = 120)
    private String nombreEmisor;

    @Column(name = "tipo_emisor", nullable = false, length = 30)
    private String tipoEmisor; // "USUARIO", "IA", "SISTEMA"

    @Column(name = "modo_ia", length = 40)
    private String modoIa; // "GENERAL", "MODELADOR_UML", "REQUISITOS", "ARQUITECTO", "DESARROLLADOR", "REVISOR_PRUEBAS", "DOCUMENTADOR"

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "metadatos_json", columnDefinition = "TEXT")
    private String metadatosJson;

    @Column(name = "id_propuesta_asociada")
    private Long idPropuestaAsociada;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    public IaMensaje() {
    }

    public Long getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(Long idMensaje) {
        this.idMensaje = idMensaje;
    }

    public IaConversacion getConversacion() {
        return conversacion;
    }

    public void setConversacion(IaConversacion conversacion) {
        this.conversacion = conversacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    public String getTipoEmisor() {
        return tipoEmisor;
    }

    public void setTipoEmisor(String tipoEmisor) {
        this.tipoEmisor = tipoEmisor;
    }

    public String getModoIa() {
        return modoIa;
    }

    public void setModoIa(String modoIa) {
        this.modoIa = modoIa;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getMetadatosJson() {
        return metadatosJson;
    }

    public void setMetadatosJson(String metadatosJson) {
        this.metadatosJson = metadatosJson;
    }

    public Long getIdPropuestaAsociada() {
        return idPropuestaAsociada;
    }

    public void setIdPropuestaAsociada(Long idPropuestaAsociada) {
        this.idPropuestaAsociada = idPropuestaAsociada;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}

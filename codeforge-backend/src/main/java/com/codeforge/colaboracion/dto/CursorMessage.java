package com.codeforge.colaboracion.dto;

public class CursorMessage {

    private Long proyectoId;
    private Long usuarioId;
    private Double x;
    private Double y;
    private String color;
    private String tipo; // "MOVE", "LEAVE", "JOIN"

    public CursorMessage() {
    }

    public CursorMessage(Long proyectoId, Long usuarioId, Double x, Double y, String color) {
        this.proyectoId = proyectoId;
        this.usuarioId = usuarioId;
        this.x = x;
        this.y = y;
        this.color = color;
        this.tipo = "MOVE";
    }

    public CursorMessage(Long proyectoId, Long usuarioId, Double x, Double y, String color, String tipo) {
        this.proyectoId = proyectoId;
        this.usuarioId = usuarioId;
        this.x = x;
        this.y = y;
        this.color = color;
        this.tipo = tipo;
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}

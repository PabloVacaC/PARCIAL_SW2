package com.codeforge.invitacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InvitacionCreateDTO {

    @NotNull
    private Long idProyecto;

    @NotNull
    private Long idEmisor;

    @NotNull
    private Long idReceptor;

    @NotBlank
    @Size(max = 30)
    private String rolPropuesto;

    public InvitacionCreateDTO() {
    }

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Long getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(Long idEmisor) {
        this.idEmisor = idEmisor;
    }

    public Long getIdReceptor() {
        return idReceptor;
    }

    public void setIdReceptor(Long idReceptor) {
        this.idReceptor = idReceptor;
    }

    public String getRolPropuesto() {
        return rolPropuesto;
    }

    public void setRolPropuesto(String rolPropuesto) {
        this.rolPropuesto = rolPropuesto;
    }
}
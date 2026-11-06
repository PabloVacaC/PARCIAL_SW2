package com.codeforge.ia.dto;

import jakarta.validation.constraints.NotBlank;

public class IaDecisionRequestDTO {

    @NotBlank(message = "La decisión es obligatoria (APROBAR o RECHAZAR)")
    private String decision; // "APROBAR", "RECHAZAR", "SOLICITAR_AJUSTE"

    private String comentario;

    public IaDecisionRequestDTO() {
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}

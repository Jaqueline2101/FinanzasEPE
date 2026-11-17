package com.pe.finanzas.solicitud_anulacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolucionRequest {

    /** ID del supervisor/admin que resuelve */
    @NotNull(message = "El ID del autorizador es obligatorio")
    private Long idAutorizador;

    /**
     * Motivo de rechazo — SOLO obligatorio cuando se rechaza.
     * El controller valida esto según el tipo de acción.
     */
    @Size(max = 500, message = "El motivo de rechazo no puede exceder 500 caracteres")
    private String motivoRechazo;
}

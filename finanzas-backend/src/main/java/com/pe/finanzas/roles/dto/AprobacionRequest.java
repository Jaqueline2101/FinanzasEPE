package com.pe.finanzas.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AprobacionRequest {

    @NotNull(message = "El ID del solicitante es obligatorio")
    private Long solicitanteId;

    private Long aprobadorId;

    @NotNull(message = "El tipo de acción es obligatorio")
    private String tipoAccion;

    private String descripcion;

    private BigDecimal monto;

    private String datosContexto;
}

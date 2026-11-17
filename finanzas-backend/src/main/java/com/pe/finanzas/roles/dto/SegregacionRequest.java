package com.pe.finanzas.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SegregacionRequest {

    @NotNull(message = "El ID del rol origen es obligatorio")
    private Long rolOrigenId;

    @NotNull(message = "El ID del rol conflicto es obligatorio")
    private Long rolConflictoId;

    private String descripcion;

    private String razonNegocio;

    @Builder.Default
    private Boolean active = true;
}

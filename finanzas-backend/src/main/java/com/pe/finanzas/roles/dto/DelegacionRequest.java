package com.pe.finanzas.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelegacionRequest {

    @NotNull(message = "El ID del usuario origen es obligatorio")
    private Long usuarioOrigenId;

    @NotNull(message = "El ID del usuario delegado es obligatorio")
    private Long usuarioDelegadoId;

    @NotNull(message = "El ID del rol es obligatorio")
    private Long roleId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    private String motivo;

    @Builder.Default
    private Boolean active = true;
}

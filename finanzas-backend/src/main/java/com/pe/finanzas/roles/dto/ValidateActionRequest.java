package com.pe.finanzas.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateActionRequest {

    @NotNull(message = "El ID del rol es obligatorio")
    private Long roleId;

    @NotNull(message = "El ID del permiso es obligatorio")
    private Long permissionId;

    private BigDecimal monto;

    private LocalDateTime fechaAccion;

    private String contexto;
}

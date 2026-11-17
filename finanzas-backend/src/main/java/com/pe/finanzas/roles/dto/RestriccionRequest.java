package com.pe.finanzas.roles.dto;

import com.pe.finanzas.roles.entity.TipoRestriccion;
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
public class RestriccionRequest {

    @NotNull(message = "El ID del rol es obligatorio")
    private Long roleId;

    @NotNull(message = "El ID del permiso es obligatorio")
    private Long permissionId;

    @NotNull(message = "El tipo de restricción es obligatorio")
    private TipoRestriccion tipoRestriccion;

    private BigDecimal valorNumerico;

    private String valorTexto;

    private String descripcion;

    @Builder.Default
    private Boolean active = true;
}

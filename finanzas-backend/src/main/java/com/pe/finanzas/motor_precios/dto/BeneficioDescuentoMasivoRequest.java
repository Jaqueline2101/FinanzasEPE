package com.pe.finanzas.motor_precios.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficioDescuentoMasivoRequest {

    @NotEmpty(message = "Debe seleccionar al menos un nivel")
    private List<Long> idNiveles;

    private BigDecimal descuentoPorcentaje;

    private BigDecimal descuentoFijo;

    private BigDecimal precioFijo;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private String tipoDescuento;
}

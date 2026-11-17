package com.pe.finanzas.descuentos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class DescuentoMasivoDto {

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor a 100%")
    private BigDecimal porcentajeDescuento;

    @NotEmpty(message = "Debe seleccionar al menos un servicio")
    private List<String> serviciosIds;

    private BigDecimal cobertura;
}

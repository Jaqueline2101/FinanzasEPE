package com.pe.finanzas.catalogo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AplicarDescuentoDto {

    @NotNull(message = "El ID del plan es obligatorio")
    private Long idPlan;

    private BigDecimal descuentoFijo;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal precioFijo;

    @NotNull(message = "El tipo de aplicación es obligatorio")
    private String tipoAplicacion;

    private Integer idNivel;
    private Long idServicio;
    private Long idTipoAtencion;

    private String tipoDescuento;
}

package com.pe.finanzas.motor_precios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoPrecioDto {

    private BigDecimal precioBase;

    private BigDecimal montoDescuento;

    private BigDecimal porcentajeDescuento;

    private BigDecimal valorVenta;

    private BigDecimal igv;

    private BigDecimal precioVenta;

    private String tipoAtencion;

    private Integer nivelServicio;

    private String descripcionDescuento;

    private Long idPlan;

    private String nombrePlan;
}

package com.pe.finanzas.descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioConDescuentoDTO {

    private Long idServicio;
    private String codigoServicio;
    private String nombreServicio;
    private BigDecimal precioBase;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoMonto;
    private BigDecimal precioFinal;
    private String descripcionDescuento;
    private Long idTipoPaciente;
    private String nombreTipoPaciente;
}

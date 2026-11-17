package com.pe.finanzas.rep_pago_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGraficoDto {
    private String etiqueta;
    private BigDecimal valor;
    private Long cantidad;
}

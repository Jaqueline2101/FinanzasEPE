package com.pe.finanzas.valorizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValorizacionDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private String lote;
    private BigDecimal stock;
    private BigDecimal stockMinimo;
    private BigDecimal costoUnitario;
    private BigDecimal precioVenta;
    private BigDecimal valorTotal;
    private LocalDate fechaVencimiento;
    private String estado; // NORMAL, BAJO_STOCK, PROXIMO_VENCER, VENCIDO
}

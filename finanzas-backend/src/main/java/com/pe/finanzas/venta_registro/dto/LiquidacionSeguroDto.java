package com.pe.finanzas.venta_registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa el desglose completo de una liquidación con seguro médico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionSeguroDto {
    private BigDecimal totalCuenta;
    private BigDecimal copago;
    private BigDecimal deducible;
    private BigDecimal subtotalGravable; // Total - Copago - Deducible
    private BigDecimal montoCoaseguroPaciente;
    private BigDecimal montoPagoSeguro;
    private BigDecimal totalPaciente; // Copago + Deducible + CoaseguroPaciente
}

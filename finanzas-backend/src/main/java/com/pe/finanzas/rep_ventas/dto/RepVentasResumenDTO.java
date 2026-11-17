package com.pe.finanzas.rep_ventas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepVentasResumenDTO {
    private BigDecimal totalGeneral;
    private BigDecimal totalEfectivo;
    private BigDecimal totalTarjeta;
    private BigDecimal totalAnulado;

    // Lista paginada de ventas
    private com.pe.finanzas.shared.dto.PageResponse<RepVentasResponseDTO> detalleVentas;
}

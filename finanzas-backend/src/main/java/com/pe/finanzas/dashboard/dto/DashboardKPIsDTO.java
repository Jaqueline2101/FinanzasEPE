package com.pe.finanzas.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKPIsDTO {
    private BigDecimal ventasTotales;
    private BigDecimal comprasTotales;
    private Long stockActual;
    private BigDecimal gananciaNeta;
    private Long comprobantesEmitidos;
    private Long comprobantesAnulados;
    private Long pacientesAtendidos;
    private Long aseguradorasActivas;
    
    // Variaciones porcentuales vs periodo anterior
    private Double variacionVentas;
    private Double variacionCompras;
    private Double variacionStock;
    private Double variacionGanancia;
}

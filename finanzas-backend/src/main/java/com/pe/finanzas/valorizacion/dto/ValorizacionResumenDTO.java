package com.pe.finanzas.valorizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValorizacionResumenDTO {
    private Long totalProductos;
    private BigDecimal valorTotalInventario;
    private Long productosSinStock;
    private Long productosStockBajo;
    private Long productosProximosVencer;
    
    // Para los gráficos
    private Map<String, BigDecimal> valorizacionPorCategoria;
    private Map<String, BigDecimal> topProductosValorizados;
}

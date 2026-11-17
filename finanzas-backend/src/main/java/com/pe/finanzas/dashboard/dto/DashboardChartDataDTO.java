package com.pe.finanzas.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartDataDTO {
    // Ventas vs Compras (Líneas)
    private List<String> labelsMeses;
    private List<BigDecimal> dataVentas;
    private List<BigDecimal> dataCompras;

    // Reporte de Descuentos (Donut)
    private Map<String, BigDecimal> descuentosPorTipo;

    // Comprobantes Anulados (Barras)
    private Map<String, Long> anuladosPorMes;
    private Map<String, Long> usuariosAnuladores;

    // Compañías Aseguradoras (Top)
    private Map<String, Long> topAseguradoras;

    // Kardex / Valorización
    private Map<String, BigDecimal> valorizacionPorCategoria;
    private Map<String, BigDecimal> topProductosValorizados;

    // Heatmap del Kardex
    private List<Map<String, Object>> heatmapKardex;
}

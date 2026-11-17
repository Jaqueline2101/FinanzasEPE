package com.pe.finanzas.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private DashboardKPIsDTO kpis;
    private DashboardChartDataDTO graficos;
    
    // Tablas Recientes
    private List<VentaRecienteDTO> ventasRecientes;
    private List<CompraRecienteDTO> comprasRecientes;

    @Data
    @Builder
    public static class VentaRecienteDTO {
        private String fecha;
        private String cliente;
        private java.math.BigDecimal monto;
        private String estado;
    }

    @Data
    @Builder
    public static class CompraRecienteDTO {
        private String fecha;
        private String proveedor;
        private String producto;
        private java.math.BigDecimal total;
    }
}

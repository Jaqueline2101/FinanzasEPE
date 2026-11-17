package com.pe.finanzas.rep_pago_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPagoPersonalDto {
    private ReporteResumenDto resumen;
    private List<ReporteGraficoDto> mensual;
    private List<ReporteGraficoDto> area;
}

package com.pe.finanzas.rep_pago_personal.service.impl;

import com.pe.finanzas.rep_pago_personal.repository.ReportePagoPersonalRepository;
import com.pe.finanzas.rep_pago_personal.dto.DashboardPagoPersonalDto;
import com.pe.finanzas.rep_pago_personal.dto.ReporteGraficoDto;
import com.pe.finanzas.rep_pago_personal.dto.ReporteResumenDto;
import com.pe.finanzas.rep_pago_personal.service.ReportePagoPersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportePagoPersonalServiceImpl implements ReportePagoPersonalService {

    private final ReportePagoPersonalRepository repository;

    @Override
    public DashboardPagoPersonalDto obtenerDashboard(Integer anio) {
        // 1. Obtener Resumen
        Object[] resumenRaw = repository.getResumenPersonal(anio);
        
        // Manejo ultra-seguro por si el query devuelve null o un array de nulos (típico cuando no hay datos)
        ReporteResumenDto.ReporteResumenDtoBuilder resumenBuilder = ReporteResumenDto.builder()
                .totalBruto(BigDecimal.ZERO)
                .totalNeto(BigDecimal.ZERO)
                .totalDescuentos(BigDecimal.ZERO)
                .cantidadPagos(0L);

        if (resumenRaw != null && resumenRaw.length > 0) {
            resumenBuilder
                    .totalBruto(resumenRaw[0] != null ? (BigDecimal) resumenRaw[0] : BigDecimal.ZERO)
                    .totalNeto(resumenRaw[1] != null ? (BigDecimal) resumenRaw[1] : BigDecimal.ZERO)
                    .totalDescuentos(resumenRaw[2] != null ? (BigDecimal) resumenRaw[2] : BigDecimal.ZERO)
                    .cantidadPagos(resumenRaw[3] != null ? (Long) resumenRaw[3] : 0L);
        }
        
        ReporteResumenDto resumen = resumenBuilder.build();

        // 2. Obtener Reporte Mensual
        List<Object[]> mensualRaw = repository.getReporteMensualPersonal(anio);
        List<ReporteGraficoDto> mensual = new ArrayList<>();
        if (mensualRaw != null) {
            for (Object[] row : mensualRaw) {
                mensual.add(ReporteGraficoDto.builder()
                        .etiqueta(row[0] != null ? String.valueOf(row[0]) : "S/M")
                        .valor(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                        .cantidad(row[2] != null ? (Long) row[2] : 0L)
                        .build());
            }
        }

        // 3. Obtener Reporte por Área
        List<Object[]> areaRaw = repository.getReporteAreaPersonal(anio);
        List<ReporteGraficoDto> area = new ArrayList<>();
        if (areaRaw != null) {
            for (Object[] row : areaRaw) {
                area.add(ReporteGraficoDto.builder()
                        .etiqueta(row[0] != null ? String.valueOf(row[0]) : "SIN ÁREA")
                        .valor(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                        .cantidad(row[2] != null ? (Long) row[2] : 0L)
                        .build());
            }
        }

        return DashboardPagoPersonalDto.builder()
                .resumen(resumen)
                .mensual(mensual)
                .area(area)
                .build();
    }
}

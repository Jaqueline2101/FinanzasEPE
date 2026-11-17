package com.pe.finanzas.rep_honorarios.service;

import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import com.pe.finanzas.pagorec_honorarios.repository.PagoComprobanteRepository;
import com.pe.finanzas.rep_honorarios.dto.RepHonorariosRequestDTO;
import com.pe.finanzas.rep_honorarios.dto.RepHonorariosResponseDTO;
import com.pe.finanzas.rep_honorarios.dto.RepHonorariosResumenDTO;
import com.pe.finanzas.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepHonorariosService {

    private final PagoComprobanteRepository repository;

    public RepHonorariosResumenDTO generarReporte(RepHonorariosRequestDTO request, Pageable pageable) {
        // 1. Obtener página de registros
        Page<PagoComprobante> pagosPage = repository.findReporteHonorarios(
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getTipoPago(),
                request.getTipoBeneficiario(),
                request.getEstadoPago(),
                request.getQuery(),
                pageable
        );

        Page<RepHonorariosResponseDTO> responsePage = pagosPage.map(this::mapToDTO);
        PageResponse<RepHonorariosResponseDTO> paginatedResponse = PageResponse.of(responsePage);

        // 2. Calcular totales sobre todo el universo filtrado
        List<PagoComprobante> pagosCompletos = repository.findResumenReporteHonorarios(
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getTipoPago(),
                request.getTipoBeneficiario(),
                request.getEstadoPago(),
                request.getQuery()
        );

        BigDecimal brutoTotal = BigDecimal.ZERO;
        BigDecimal retencionesTotal = BigDecimal.ZERO;
        BigDecimal netoTotal = BigDecimal.ZERO;

        for (PagoComprobante p : pagosCompletos) {
            if (!"ANULADO".equalsIgnoreCase(p.getEstadoPago())) {
                brutoTotal = brutoTotal.add(p.getMontoBruto() != null ? p.getMontoBruto() : BigDecimal.ZERO);
                retencionesTotal = retencionesTotal.add(p.getTotalRetenciones() != null ? p.getTotalRetenciones() : BigDecimal.ZERO);
                netoTotal = netoTotal.add(p.getMontoNeto() != null ? p.getMontoNeto() : BigDecimal.ZERO);
            }
        }

        return RepHonorariosResumenDTO.builder()
                .totalBruto(brutoTotal)
                .totalRetenciones(retencionesTotal)
                .totalNeto(netoTotal)
                .totalRegistros(pagosCompletos.size())
                .detallePagos(paginatedResponse)
                .build();
    }

    private RepHonorariosResponseDTO mapToDTO(PagoComprobante p) {
        return RepHonorariosResponseDTO.builder()
                .idPago(p.getIdPago())
                .fechaEmision(p.getFechaEmision())
                .comprobante((p.getSerieComprobante() != null ? p.getSerieComprobante() : "") + "-" + 
                             (p.getNumeroRecibo() != null ? p.getNumeroRecibo() : ""))
                .beneficiario(p.getNombreRazonSocial())
                .documento(p.getNumeroDocumento())
                .tipoPago(p.getTipoPago())
                .concepto(p.getConceptoPago())
                .montoBruto(p.getMontoBruto())
                .retencion(p.getTotalRetenciones())
                .montoNeto(p.getMontoNeto())
                .moneda(p.getMoneda())
                .medioPago(p.getMedioPago())
                .estado(p.getEstadoPago())
                .build();
    }
}

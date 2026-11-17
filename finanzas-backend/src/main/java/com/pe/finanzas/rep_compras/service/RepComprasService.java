package com.pe.finanzas.rep_compras.service;

import com.pe.finanzas.compra.entity.Compra;
import com.pe.finanzas.compra.repository.CompraRepository;
import com.pe.finanzas.rep_compras.dto.RepComprasRequestDTO;
import com.pe.finanzas.rep_compras.dto.RepComprasResponseDTO;
import com.pe.finanzas.rep_compras.dto.RepComprasResumenDTO;
import com.pe.finanzas.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepComprasService {

    private final CompraRepository compraRepository;

    @Transactional(readOnly = true)
    public RepComprasResumenDTO generarReporte(RepComprasRequestDTO request, Pageable pageable) {
        // 1. Obtener la página de compras
        Page<Compra> comprasPage = compraRepository.findReporteCompras(
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getIdSucursal(),
                request.getIdProveedor(),
                request.getEstado(),
                pageable);

        Page<RepComprasResponseDTO> responsePage = comprasPage.map(this::mapToDTO);
        PageResponse<RepComprasResponseDTO> paginatedResponse = PageResponse.of(responsePage);

        // 2. Calcular totales (resumen)
        List<Compra> comprasCompletas = compraRepository.findResumenReporteCompras(
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getIdSucursal(),
                request.getIdProveedor(),
                request.getEstado());

        BigDecimal totalGravado = BigDecimal.ZERO;
        BigDecimal totalInafecto = BigDecimal.ZERO;
        BigDecimal totalExonerado = BigDecimal.ZERO;
        BigDecimal totalIGV = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (Compra c : comprasCompletas) {
            if (c.getEstado() != null && c.getEstado() != 0) {
                totalGravado = totalGravado.add(c.getValorVentaGravado() != null ? c.getValorVentaGravado() : BigDecimal.ZERO);
                totalInafecto = totalInafecto.add(c.getValorVentaInafecto() != null ? c.getValorVentaInafecto() : BigDecimal.ZERO);
                totalIGV = totalIGV.add(c.getIgv() != null ? c.getIgv() : BigDecimal.ZERO);
                totalGeneral = totalGeneral.add(c.getTotalPagar() != null ? c.getTotalPagar() : BigDecimal.ZERO);
            }
        }

        return RepComprasResumenDTO.builder()
                .totalGravado(totalGravado)
                .totalInafecto(totalInafecto)
                .totalExonerado(totalExonerado)
                .totalIGV(totalIGV)
                .totalGeneral(totalGeneral)
                .detalleCompras(paginatedResponse)
                .build();
    }

    private RepComprasResponseDTO mapToDTO(Compra c) {
        return RepComprasResponseDTO.builder()
                .idCompra(c.getId())
                .fechaEmision(c.getFechaEmision())
                .fechaRegistro(c.getFechaRegistro())
                .tipoComprobante(c.getTipoComprobante())
                .serie(c.getSerie())
                .correlativo(c.getCorrelativo())
                .proveedor(c.getProveedor() != null ? c.getProveedor().getRazonSocial() : "-")
                .rucProveedor(c.getProveedor() != null ? c.getProveedor().getNumDocIdent() : "-")
                .moneda(c.getMoneda())
                .valorVentaGravado(c.getValorVentaGravado())
                .valorVentaInafecto(c.getValorVentaInafecto())
                .igv(c.getIgv())
                .total(c.getTotalPagar())
                .estado(c.getEstado())
                .build();
    }
}

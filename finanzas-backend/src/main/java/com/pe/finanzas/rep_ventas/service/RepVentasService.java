package com.pe.finanzas.rep_ventas.service;

import com.pe.finanzas.rep_ventas.dto.RepVentasRequestDTO;
import com.pe.finanzas.rep_ventas.dto.RepVentasResponseDTO;
import com.pe.finanzas.rep_ventas.dto.RepVentasResumenDTO;
import com.pe.finanzas.shared.dto.PageResponse;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepVentasService {

    private final VentaRegistroRepository ventaRegistroRepository;

    public RepVentasResumenDTO generarReporte(RepVentasRequestDTO request, int page, int size, String sortStr) {
        // Build pageable
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "fecha")
                .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "idVenta"));
        if ("fechaAsc".equalsIgnoreCase(sortStr))
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "fecha")
                    .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
                            "idVenta"));
        else if ("montoDesc".equalsIgnoreCase(sortStr))
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                    "total");
        else if ("montoAsc".equalsIgnoreCase(sortStr))
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "total");

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                sort);
        Long idSucursal = request.getIdSucursal();
        Long idPersonal = request.getIdPersonal();

        // 1. Obtener la página de ventas
        Page<VentaRegistro> ventasPage = ventaRegistroRepository.findReporteVentas(
                request.getFechaInicio(),
                request.getFechaFin(),
                idSucursal,
                idPersonal,
                request.getEstado(),
                request.getTipoAtencion(),
                request.getQuery(),
                pageable);

        Page<RepVentasResponseDTO> responsePage = ventasPage.map(this::mapToDTO);
        PageResponse<RepVentasResponseDTO> paginatedResponse = PageResponse.of(responsePage);

        // 2. Calcular totales
        List<VentaRegistro> ventasCompletas = ventaRegistroRepository.findResumenReporteVentas(
                request.getFechaInicio(),
                request.getFechaFin(),
                idSucursal,
                idPersonal,
                request.getEstado(),
                request.getTipoAtencion(),
                request.getQuery());

        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjeta = BigDecimal.ZERO;
        BigDecimal totalAnulado = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (VentaRegistro v : ventasCompletas) {
            BigDecimal totalVenta = v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO;
            if (Integer.valueOf(10).equals(v.getEstado())) {
                totalAnulado = totalAnulado.add(totalVenta);
            } else {
                totalGeneral = totalGeneral.add(totalVenta);
                String mp = v.getMetodoPago() != null ? String.valueOf(v.getMetodoPago()) : "";
                if ("1".equals(mp))
                    totalEfectivo = totalEfectivo.add(totalVenta);
                else if ("2".equals(mp) || "3".equals(mp) || "4".equals(mp))
                    totalTarjeta = totalTarjeta.add(totalVenta);
            }
        }

        return RepVentasResumenDTO.builder()
                .totalGeneral(totalGeneral)
                .totalEfectivo(totalEfectivo)
                .totalTarjeta(totalTarjeta)
                .totalAnulado(totalAnulado)
                .detalleVentas(paginatedResponse)
                .build();
    }

    private RepVentasResponseDTO mapToDTO(VentaRegistro v) {
        String metodoLabel = "—";
        if (v.getMetodoPago() != null) {
            long m = v.getMetodoPago();
            if (m == 1)
                metodoLabel = "EFECTIVO";
            else if (m == 2)
                metodoLabel = "TARJETA";
            else if (m == 3)
                metodoLabel = "YAPE/PLIN";
            else if (m == 4)
                metodoLabel = "TRANSFERENCIA";
        } else if (v.getRefMetodoPago() != null && !v.getRefMetodoPago().isBlank()) {
            metodoLabel = v.getRefMetodoPago().toUpperCase().trim();
        }

        String estadoLabel = "PENDIENTE";
        if (v.getEstado() != null) {
            int e = v.getEstado();
            if (e == 16)
                estadoLabel = "PAGADO";
            else if (e == 10)
                estadoLabel = "ANULADO";
            else if (e == 1)
                estadoLabel = "PENDIENTE";
        }

        return RepVentasResponseDTO.builder()
                .idVenta(v.getIdVenta())
                .fecha(v.getFecha())
                .comprobante((v.getTipoDoc() != null ? v.getTipoDoc() : "") + " "
                        + (v.getSerie() != null ? v.getSerie() : "") + "-"
                        + (v.getNumdoc() != null ? v.getNumdoc() : ""))
                .tipoDoc(v.getTipoDoc())
                .paciente(v.getNombrePac())
                .tipoAtencion(v.getTipoAtencion() != null ? v.getTipoAtencion() : "PARTICULAR")
                .metodoPago(metodoLabel)
                .total(v.getTotal())
                .estado(estadoLabel)
                .cajeroId(null)
                .build();
    }
}

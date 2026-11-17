package com.pe.finanzas.rep_comprobantes_anulados.service.impl;

import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosRequestDTO;
import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosResponseDTO;
import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosResumenDTO;
import com.pe.finanzas.rep_comprobantes_anulados.service.RepAnuladosService;
import com.pe.finanzas.shared.dto.PageResponse;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepAnuladosServiceImpl implements RepAnuladosService {

    private final VentaRegistroRepository ventaRegistroRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    public RepAnuladosResumenDTO generarReporte(RepAnuladosRequestDTO request, Pageable pageable) {
        // Estado 10 es ANULADO
        Integer estadoAnulado = 10;
        Long idSucursal = request.getIdSucursal();

        // 1. Obtener la página de ventas anuladas
        Page<VentaRegistro> ventasPage = ventaRegistroRepository.findReporteVentas(
                request.getFechaInicio(),
                request.getFechaFin(),
                idSucursal,
                null, // idPersonal
                estadoAnulado,
                null, // tipoAtencion
                request.getQuery(),
                pageable);

        // Mapear de Entity a DTO
        Page<RepAnuladosResponseDTO> responsePage = ventasPage.map(this::mapToDTO);
        PageResponse<RepAnuladosResponseDTO> paginatedResponse = PageResponse.of(responsePage);

        // 2. Calcular totales del resumen (basado en el filtro actual para todos los registros)
        List<VentaRegistro> ventasCompletas = ventaRegistroRepository.findResumenReporteVentas(
                request.getFechaInicio(),
                request.getFechaFin(),
                idSucursal,
                null, // idPersonalUser
                estadoAnulado,
                null, // tipoAtencion
                request.getQuery());

        BigDecimal totalAnulado = ventasCompletas.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RepAnuladosResumenDTO.builder()
                .totalAnulado(totalAnulado)
                .cantidadAnulados(ventasCompletas.size())
                .detalleAnulados(paginatedResponse)
                .build();
    }

    private RepAnuladosResponseDTO mapToDTO(VentaRegistro v) {
        String usuarioNombre = "—";
        if (v.getIdUserAnul() != null) {
            usuarioNombre = datosPersonalesRepository.findById(Long.valueOf(v.getIdUserAnul()))
                    .map(u -> u.getVerNombre() != null ? u.getVerNombre() : u.getNombre())
                    .orElse("ID: " + v.getIdUserAnul());
        }

        return RepAnuladosResponseDTO.builder()
                .idVenta(v.getIdVenta())
                .fecha(v.getFecha())
                .comprobante((v.getTipoDoc() != null ? v.getTipoDoc() : "") + " "
                        + (v.getSerie() != null ? v.getSerie() : "") + "-"
                        + (v.getNumdoc() != null ? v.getNumdoc() : ""))
                .tipoDoc(v.getTipoDoc())
                .paciente(v.getNombrePac())
                .metodoPago(v.getMetodoPago() != null ? String.valueOf(v.getMetodoPago()) : "—")
                .total(v.getTotal())
                .motivoAnulacion(v.getDetalle() != null ? v.getDetalle() : "Sin motivo")
                .usuarioAnulacion(usuarioNombre)
                .build();
    }
}

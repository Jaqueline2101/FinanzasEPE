package com.pe.finanzas.nota_credito.service.impl;

import com.pe.finanzas.nota_credito.dto.NotaCreditoDto;
import com.pe.finanzas.nota_credito.dto.NotaCreditoRequest;
import com.pe.finanzas.nota_credito.entity.NotaCredito;
import com.pe.finanzas.nota_credito.repository.NotaCreditoRepository;
import com.pe.finanzas.nota_credito.service.NotaCreditoService;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotaCreditoServiceImpl implements NotaCreditoService {

    private final NotaCreditoRepository ncRepo;
    private final VentaRegistroRepository ventaRepo;

    /** Catálogo de motivos SUNAT — código → descripción */
    private static final Map<String, String> MOTIVOS_SUNAT = Map.of(
            "01", "Anulación de la operación",
            "02", "Anulación por error en el RUC",
            "03", "Corrección por error en la descripción",
            "04", "Descuento global",
            "05", "Descuento por ítem",
            "06", "Devolución total",
            "07", "Devolución por ítem",
            "13", "Ajuste en el monto del comprobante");

    // ─── Emitir ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotaCreditoDto emitir(NotaCreditoRequest request) {
        log.info("Emitiendo NC para venta ID: {}", request.getIdVentaRef());

        // 1. Verificar que la venta existe
        VentaRegistro venta = ventaRepo.findByIdVenta(request.getIdVentaRef())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Venta no encontrada con ID: " + request.getIdVentaRef()));

        // 2. Solo Boletas (B) y Facturas (F) admiten NC
        String tipoDoc = venta.getTipoDoc() != null ? venta.getTipoDoc().trim().toUpperCase() : "";
        boolean isB = "B".equals(tipoDoc) || "BOLETA".equals(tipoDoc);
        boolean isF = "F".equals(tipoDoc) || "FACTURA".equals(tipoDoc);

        if (!isB && !isF) {
            throw new IllegalStateException(
                    "Solo se pueden emitir Notas de Crédito para Boletas (B) o Facturas (F). " +
                            "El tipo actual es: " + venta.getTipoDoc());
        }

        // 3. Verificar que la venta no esté anulada
        if ("ANULADO".equals(venta.getEstado())) {
            throw new IllegalStateException(
                    "No se puede emitir una NC sobre una venta ya anulada.");
        }

        // 4. Verificar que el monto NC no excede el total original
        if (venta.getTotal() != null &&
                request.getMontoNc().compareTo(venta.getTotal()) > 0) {
            throw new IllegalArgumentException(
                    String.format("El monto de la NC (%.2f) no puede superar el total original (%.2f).",
                            request.getMontoNc(), venta.getTotal()));
        }

        // 5. Determinar la serie según tipo de comprobante
        String serieNc = isB ? "NC-B" : "NC-F";

        // 6. Generar el número correlativo de la serie
        String numdocNc = generarCorrelativo(serieNc);

        // 7. Snapshot del comprobante original
        String comprobanteRef = String.format("%s %s-%s",
                venta.getTipoDoc() != null ? venta.getTipoDoc() : "",
                venta.getSerie() != null ? venta.getSerie() : "",
                venta.getNumdoc() != null ? venta.getNumdoc() : "").trim();

        // 8. Descripción del motivo desde el catálogo SUNAT
        String motivoDesc = MOTIVOS_SUNAT.getOrDefault(request.getMotivoCodigo(),
                "Motivo código: " + request.getMotivoCodigo());

        // 9. Persistir la NC
        NotaCredito nc = NotaCredito.builder()
                .serieNc(serieNc)
                .numdocNc(numdocNc)
                .idVentaRef(request.getIdVentaRef())
                .comprobanteRef(comprobanteRef)
                .tipoComprobante(isB ? "BOLETA" : "FACTURA")
                .motivoCodigo(request.getMotivoCodigo())
                .motivoDescripcion(motivoDesc)
                .detalle(request.getDetalle())
                .montoNc(request.getMontoNc())
                .montoOriginal(venta.getTotal())
                .paciente(null) // VentaRegistro no expone nombre del paciente directamente
                .idSucursal(request.getIdSucursal())
                .idUsuario(request.getIdUsuario())
                .fechaEmision(LocalDate.now())
                .estado("EMITIDA")
                .build();

        NotaCredito guardada = ncRepo.save(nc);
        log.info("NC emitida: {} {} para venta ID: {}", serieNc, numdocNc, request.getIdVentaRef());
        return toDto(guardada);
    }

    // ─── Listar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<NotaCreditoDto> listarPorSucursal(Long idSucursal, String estado, Pageable pageable) {
        Page<NotaCredito> page = (estado != null && !estado.isBlank())
                ? ncRepo.findByIdSucursalAndEstadoOrderByFechaCreacionDesc(idSucursal, estado, pageable)
                : ncRepo.findByIdSucursalOrderByFechaCreacionDesc(idSucursal, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotaCreditoDto> listarPorVenta(Long idVentaRef, Pageable pageable) {
        return ncRepo.findByIdVentaRefOrderByFechaCreacionDesc(idVentaRef, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public NotaCreditoDto obtenerPorId(Long idNc) {
        return toDto(buscarPorId(idNc));
    }

    // ─── Anular ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotaCreditoDto anular(Long idNc, Long idUsuario) {
        log.info("Anulando NC ID: {} por usuario ID: {}", idNc, idUsuario);

        NotaCredito nc = buscarPorId(idNc);

        if ("ANULADA".equals(nc.getEstado())) {
            throw new IllegalStateException("La Nota de Crédito ya está anulada.");
        }

        nc.setEstado("ANULADA");
        NotaCredito guardada = ncRepo.save(nc);
        log.info("NC ID: {} anulada.", idNc);
        return toDto(guardada);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private String generarCorrelativo(String serieNc) {
        int ultimoNum = ncRepo.findMaxNumdocBySerie(serieNc).orElse(0);
        return String.format("%08d", ultimoNum + 1); // → "00000001"
    }

    private NotaCredito buscarPorId(Long idNc) {
        return ncRepo.findById(idNc)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nota de Crédito no encontrada con ID: " + idNc));
    }

    private String buildNumeroNc(NotaCredito nc) {
        return nc.getSerieNc() + " " + nc.getNumdocNc();
    }

    private NotaCreditoDto toDto(NotaCredito nc) {
        return NotaCreditoDto.builder()
                .idNc(nc.getIdNc())
                .serieNc(nc.getSerieNc())
                .numdocNc(nc.getNumdocNc())
                .numeroNc(buildNumeroNc(nc))
                .idVentaRef(nc.getIdVentaRef())
                .comprobanteRef(nc.getComprobanteRef())
                .tipoComprobante(nc.getTipoComprobante())
                .paciente(nc.getPaciente())
                .motivoCodigo(nc.getMotivoCodigo())
                .motivoDescripcion(nc.getMotivoDescripcion())
                .detalle(nc.getDetalle())
                .montoNc(nc.getMontoNc())
                .montoOriginal(nc.getMontoOriginal())
                .idSucursal(nc.getIdSucursal())
                .idUsuario(nc.getIdUsuario())
                .nombreUsuario(nc.getNombreUsuario())
                .estado(nc.getEstado())
                .fechaEmision(nc.getFechaEmision())
                .fechaCreacion(nc.getFechaCreacion())
                .build();
    }
}

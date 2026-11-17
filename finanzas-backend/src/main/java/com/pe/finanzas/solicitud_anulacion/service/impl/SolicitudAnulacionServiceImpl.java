package com.pe.finanzas.solicitud_anulacion.service.impl;

import com.pe.finanzas.solicitud_anulacion.dto.ResolucionRequest;
import com.pe.finanzas.solicitud_anulacion.dto.SolicitudAnulacionDto;
import com.pe.finanzas.solicitud_anulacion.dto.SolicitudAnulacionRequest;
import com.pe.finanzas.solicitud_anulacion.entity.SolicitudAnulacion;
import com.pe.finanzas.solicitud_anulacion.repository.SolicitudAnulacionRepository;
import com.pe.finanzas.solicitud_anulacion.service.SolicitudAnulacionService;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import com.pe.finanzas.venta_registro.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudAnulacionServiceImpl implements SolicitudAnulacionService {

    private final SolicitudAnulacionRepository solicitudRepo;
    private final VentaRegistroRepository ventaRepo;
    private final VentaService ventaService;

    // ─── Crear solicitud ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public SolicitudAnulacionDto crearSolicitud(SolicitudAnulacionRequest request) {
        log.info("Creando solicitud de anulación para venta ID: {}", request.getIdVenta());

        // 1. Verificar que la venta existe
        VentaRegistro venta = ventaRepo.findByIdVenta(request.getIdVenta())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Venta no encontrada con ID: " + request.getIdVenta()));

        // 2. Verificar que la venta no está ya anulada
        if ("ANULADO".equals(venta.getEstado())) {
            throw new IllegalStateException("La venta ya se encuentra anulada.");
        }

        // 3. Verificar que no sea del día de hoy (regla de negocio)
        if (venta.getFecha().equals(java.time.LocalDate.now())) {
            throw new IllegalStateException(
                    "Esta venta es del día de hoy. Use 'Anular Comprobante' directamente.");
        }

        // 4. Verificar que no exista ya una solicitud PENDIENTE para esta venta
        solicitudRepo.findByIdVentaAndEstado(request.getIdVenta(), "PENDIENTE")
                .ifPresent(s -> {
                    throw new IllegalStateException(
                            "Ya existe una solicitud PENDIENTE (ID: " + s.getIdSolicitud() +
                                    ") para esta venta. Espere la resolución del supervisor.");
                });

        // 5. Crear el comprobante snapshot
        String comprobante = String.format("%s %s-%s",
                venta.getTipoDoc() != null ? venta.getTipoDoc() : "",
                venta.getSerie() != null ? venta.getSerie() : "",
                venta.getNumdoc() != null ? venta.getNumdoc() : "").trim();

        // 6. Persistir la solicitud
        SolicitudAnulacion solicitud = SolicitudAnulacion.builder()
                .idVenta(request.getIdVenta())
                .idSucursal(request.getIdSucursal())
                .comprobante(comprobante)
                .fechaVenta(venta.getFecha())
                .montoVenta(venta.getTotal())
                .motivoSolicitud(request.getMotivoSolicitud())
                .estado("PENDIENTE")
                .idSolicitante(request.getIdSolicitante())
                .build();

        SolicitudAnulacion guardada = solicitudRepo.save(solicitud);
        log.info("Solicitud de anulación creada. ID: {}", guardada.getIdSolicitud());
        return toDto(guardada);
    }

    // ─── Listar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudAnulacionDto> listarPorSucursal(Long idSucursal, String estado, Pageable pageable) {
        Page<SolicitudAnulacion> page = (estado != null && !estado.isBlank())
                ? solicitudRepo.findByIdSucursalAndEstadoOrderByFechaCreacionDesc(idSucursal, estado, pageable)
                : solicitudRepo.findByIdSucursalOrderByFechaCreacionDesc(idSucursal, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudAnulacionDto obtenerPorId(Long idSolicitud) {
        return toDto(buscarPorId(idSolicitud));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientes(Long idSucursal) {
        return solicitudRepo.countPendientesBySucursal(idSucursal);
    }

    // ─── Aprobar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SolicitudAnulacionDto aprobar(Long idSolicitud, ResolucionRequest request) {
        log.info("Aprobando solicitud ID: {} por autorizador ID: {}", idSolicitud, request.getIdAutorizador());

        SolicitudAnulacion solicitud = buscarPorId(idSolicitud);
        validarEstadoPendiente(solicitud);

        // Ejecutar la anulación real de la venta
        ventaService.anularVenta(solicitud.getIdVenta(), solicitud.getMotivoSolicitud());

        // Actualizar la solicitud
        solicitud.setEstado("APROBADO");
        solicitud.setIdAutorizador(request.getIdAutorizador());
        solicitud.setFechaResolucion(LocalDateTime.now());

        SolicitudAnulacion guardada = solicitudRepo.save(solicitud);
        log.info("Solicitud ID: {} aprobada. Venta ID: {} anulada.", idSolicitud, solicitud.getIdVenta());
        return toDto(guardada);
    }

    // ─── Rechazar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SolicitudAnulacionDto rechazar(Long idSolicitud, ResolucionRequest request) {
        log.info("Rechazando solicitud ID: {} por autorizador ID: {}", idSolicitud, request.getIdAutorizador());

        SolicitudAnulacion solicitud = buscarPorId(idSolicitud);
        validarEstadoPendiente(solicitud);

        if (request.getMotivoRechazo() == null || request.getMotivoRechazo().isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio.");
        }

        solicitud.setEstado("RECHAZADO");
        solicitud.setIdAutorizador(request.getIdAutorizador());
        solicitud.setMotivoRechazo(request.getMotivoRechazo());
        solicitud.setFechaResolucion(LocalDateTime.now());

        SolicitudAnulacion guardada = solicitudRepo.save(solicitud);
        log.info("Solicitud ID: {} rechazada.", idSolicitud);
        return toDto(guardada);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private SolicitudAnulacion buscarPorId(Long idSolicitud) {
        return solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitud de anulación no encontrada con ID: " + idSolicitud));
    }

    private void validarEstadoPendiente(SolicitudAnulacion s) {
        if (!"PENDIENTE".equals(s.getEstado())) {
            throw new IllegalStateException(
                    "La solicitud ya fue resuelta con estado: " + s.getEstado());
        }
    }

    private SolicitudAnulacionDto toDto(SolicitudAnulacion s) {
        return SolicitudAnulacionDto.builder()
                .idSolicitud(s.getIdSolicitud())
                .idVenta(s.getIdVenta())
                .idSucursal(s.getIdSucursal())
                .comprobante(s.getComprobante())
                .fechaVenta(s.getFechaVenta())
                .montoVenta(s.getMontoVenta())
                .motivoSolicitud(s.getMotivoSolicitud())
                .estado(s.getEstado())
                .idSolicitante(s.getIdSolicitante())
                .nombreSolicitante(s.getNombreSolicitante())
                .idAutorizador(s.getIdAutorizador())
                .nombreAutorizador(s.getNombreAutorizador())
                .motivoRechazo(s.getMotivoRechazo())
                .fechaResolucion(s.getFechaResolucion())
                .fechaCreacion(s.getFechaCreacion())
                .build();
    }
}

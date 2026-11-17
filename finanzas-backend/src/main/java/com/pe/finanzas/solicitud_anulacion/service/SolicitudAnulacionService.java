package com.pe.finanzas.solicitud_anulacion.service;

import com.pe.finanzas.solicitud_anulacion.dto.ResolucionRequest;
import com.pe.finanzas.solicitud_anulacion.dto.SolicitudAnulacionDto;
import com.pe.finanzas.solicitud_anulacion.dto.SolicitudAnulacionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SolicitudAnulacionService {

    /**
     * El cajero crea una solicitud de anulación para una venta de fecha anterior.
     * Valida que no exista ya una solicitud PENDIENTE para la misma venta.
     */
    SolicitudAnulacionDto crearSolicitud(SolicitudAnulacionRequest request);

    /**
     * Listar solicitudes con filtro opcional por estado (PENDIENTE, APROBADO,
     * RECHAZADO).
     * Usado por el supervisor en su bandeja de trabajo.
     */
    Page<SolicitudAnulacionDto> listarPorSucursal(Long idSucursal, String estado, Pageable pageable);

    /**
     * El supervisor aprueba la solicitud.
     * Al aprobar → se ejecuta la anulación real de la venta (llama a VentaService).
     */
    SolicitudAnulacionDto aprobar(Long idSolicitud, ResolucionRequest request);

    /**
     * El supervisor rechaza la solicitud con un motivo obligatorio.
     * La venta permanece en estado PAGADO.
     */
    SolicitudAnulacionDto rechazar(Long idSolicitud, ResolucionRequest request);

    /** Obtener el detalle de una solicitud por ID */
    SolicitudAnulacionDto obtenerPorId(Long idSolicitud);

    /** Cantidad de solicitudes PENDIENTES (para badge en sidebar) */
    long contarPendientes(Long idSucursal);
}

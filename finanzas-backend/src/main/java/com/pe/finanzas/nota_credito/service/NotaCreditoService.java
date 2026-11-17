package com.pe.finanzas.nota_credito.service;

import com.pe.finanzas.nota_credito.dto.NotaCreditoDto;
import com.pe.finanzas.nota_credito.dto.NotaCreditoRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotaCreditoService {

    /**
     * Emitir una nueva Nota de Crédito para un comprobante existente.
     * Solo aplica a Boletas (B) y Facturas (F).
     * Genera automáticamente el número correlativo de la serie.
     */
    NotaCreditoDto emitir(NotaCreditoRequest request);

    /**
     * Listar NCs por sucursal con filtro opcional de estado.
     * Usado en el historial/pantalla principal.
     */
    Page<NotaCreditoDto> listarPorSucursal(Long idSucursal, String estado, Pageable pageable);

    /**
     * Listar NCs emitidas para una venta específica.
     */
    Page<NotaCreditoDto> listarPorVenta(Long idVentaRef, Pageable pageable);

    /**
     * Obtener el detalle de una NC por su ID.
     */
    NotaCreditoDto obtenerPorId(Long idNc);

    /**
     * Anular una NC previamente emitida.
     * Solo el estado cambia a ANULADA — no revierte el movimiento financiero
     * (ese flujo es más complejo y se maneja con otra NC).
     */
    NotaCreditoDto anular(Long idNc, Long idUsuario);
}

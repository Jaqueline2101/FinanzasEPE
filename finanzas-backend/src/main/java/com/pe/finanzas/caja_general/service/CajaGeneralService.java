package com.pe.finanzas.caja_general.service;

import com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoResponse;
import com.pe.finanzas.caja_general.dto.CajaGeneralResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CajaGeneralService {
    CajaGeneralResponse obtenerPorSucursal(Long idSucursal);

    CajaGeneralMovimientoResponse registrarMovimiento(CajaGeneralMovimientoRequest request);

    Page<CajaGeneralMovimientoResponse> listarMovimientos(Long idSucursal, String metodoPago, Pageable pageable);

    CajaGeneralMovimientoResponse actualizarMovimiento(Long id, CajaGeneralMovimientoRequest request);

    void eliminarMovimiento(Long id);
}

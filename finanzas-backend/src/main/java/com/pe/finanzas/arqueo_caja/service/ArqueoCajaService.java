package com.pe.finanzas.arqueo_caja.service;

import com.pe.finanzas.arqueo_caja.dto.ArqueoCajaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ArqueoCajaService {

    // Calcula el monto esperado en caja (desde el ultimo cierre hasta ahora)
    BigDecimal obtenerSaldoEsperado(Long idUsuario, Long idSucursal, Long idPunto);

    // Registra el cierre de caja y guarda la diferencia
    ArqueoCajaDTO registrarArqueo(ArqueoCajaDTO dto);

    // Lista el historial de arqueos
    Page<ArqueoCajaDTO> listarArqueos(Long idUsuario, Long idSucursal, Long idPunto, LocalDateTime start, LocalDateTime end, Pageable pageable);

}

package com.pe.finanzas.caja_general.dto;

import com.pe.finanzas.caja_general.entity.CajaGeneralMovimiento;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CajaGeneralMovimientoResponse {
    private Long id;
    private CajaGeneralMovimiento.TipoMovimiento tipo;
    private BigDecimal monto;
    private String descripcion;
    private String referencia;
    private String metodoPago;
    private LocalDateTime fecha;
    private String usuario;
    private String metodoPagoOrigen;
    private String metodoPagoDestino;
}

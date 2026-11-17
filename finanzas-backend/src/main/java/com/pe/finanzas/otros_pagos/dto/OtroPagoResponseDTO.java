package com.pe.finanzas.otros_pagos.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OtroPagoResponseDTO {
    private Long id;
    private String tipo;
    private BigDecimal monto;
    private String concepto;
    private Long idUsuario;
    private String nombreUsuario;   // resuelto en batch, sin N+1
    private Long idSucursal;
    private Long idCuenta;
    private LocalDateTime fechaMovimiento;
    private String origen;
    private String tipoComprobante;
    private String numeroComprobante;
    private String metodoPago;
    private String beneficiario;
    private String dni;
    private String tipoPersona;
    private String telefono;
    private String email;
    private String nombreCampana;
    private Boolean esAnonimo;
    private Boolean anulado;
}
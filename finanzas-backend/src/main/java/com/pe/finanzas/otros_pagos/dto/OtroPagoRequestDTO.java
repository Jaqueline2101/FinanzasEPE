package com.pe.finanzas.otros_pagos.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OtroPagoRequestDTO {
    private LocalDate fecha;
    private String beneficiario;
    private String tipoPersona; // TRABAJADOR, MEDICO, EXTERNO
    private String dni;
    private BigDecimal monto;
    private String metodoPago; // EFECTIVO, TRANSFERENCIA, YAPE
    private String tipo; // INGRESO, EGRESO
    private String concepto;
    private Long idCuenta;
    private Long idUsuario;
    private Long idSucursal;

    // Extended fields for 'Otro Pago' (Donations)
    private String telefono;
    private String email;
    private String nombreCampana;
    private Boolean esAnonimo;
}

package com.pe.finanzas.arqueo_caja.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArqueoCajaDTO {
    private Long id;
    private LocalDateTime fechaHora;
    private Long idUsuario;
    private String nombreUsuario; // A rellenar despachando la peticion
    private Long idSucursal;
    private String nombreSucursal;
    private Long idPunto;
    private String nombrePunto;
    private BigDecimal saldoEsperado;
    private BigDecimal saldoFisico;
    private BigDecimal diferencia;
    private String observaciones;
    private String estado;
}

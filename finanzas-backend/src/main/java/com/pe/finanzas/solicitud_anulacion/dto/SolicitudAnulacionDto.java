package com.pe.finanzas.solicitud_anulacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAnulacionDto {

    private Long idSolicitud;
    private Long idVenta;
    private Long idSucursal;

    private String comprobante;
    private LocalDate fechaVenta;
    private BigDecimal montoVenta;

    private String motivoSolicitud;

    /** PENDIENTE | APROBADO | RECHAZADO */
    private String estado;

    private Long idSolicitante;
    private String nombreSolicitante;

    private Long idAutorizador;
    private String nombreAutorizador;

    private String motivoRechazo;

    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaCreacion;
}

package com.pe.finanzas.rep_descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepDescuentoResponseDTO {
    private Long idAutorizacion;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    private String pacienteNombre;
    private String pacienteDni;
    private String doctorNombre;
    private String cajeroNombre;
    private String autorizadorNombre;
    private java.math.BigDecimal montoOriginal;
    private java.math.BigDecimal montoDescuento;
    private java.math.BigDecimal montoFinal;

    private String estado;
    private String motivoSolicitud;
    private String motivoRechazo;
}

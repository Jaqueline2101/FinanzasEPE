package com.pe.finanzas.autorizar_descuento.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AutorizacionResponseDto {
    private Long idAutorizacion;
    private Long idVenta;
    private BigDecimal montoOriginal;
    private BigDecimal montoDescuento;
    private BigDecimal montoFinal;
    private String motivoSolicitud;
    private String estado;
    private String pacienteNombre;
    private String pacienteDni;
    private String doctorNombre;



    // Nombres o DNI del cajero para la vista del gerente
    private String cajeroNombre;
    private String cajeroDni;

    private String autorizadorNombre;
    private String motivoRechazo;

    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
}

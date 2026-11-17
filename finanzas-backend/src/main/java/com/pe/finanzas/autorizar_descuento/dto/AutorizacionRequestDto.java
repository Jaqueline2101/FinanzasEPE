package com.pe.finanzas.autorizar_descuento.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AutorizacionRequestDto {
    private Long idVenta; // Opcional por si deseas amarrarlo a una venta
    private BigDecimal montoOriginal;
    private BigDecimal montoDescuento;
    private BigDecimal montoFinal;
    private String motivoSolicitud;
    private Long idCajero;
    
    // Patient identity for visibility in list
    private String pacienteNombre;
    private String pacienteDni;
    private String doctorNombre;
}



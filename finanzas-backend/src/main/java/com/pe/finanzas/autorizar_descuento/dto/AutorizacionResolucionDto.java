package com.pe.finanzas.autorizar_descuento.dto;

import lombok.Data;

@Data
public class AutorizacionResolucionDto {
    private Long idAutorizador;
    private String motivoRechazo; // Opcional, solo obligatorio si rechaza.
}

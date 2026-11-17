package com.pe.finanzas.puntos.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntosDocumentoDto {

    private Long idPuntoDocumento;

    // Información del punto
    private Long idPunto;
    private String nombrePunto;

    // Información del tipo de documento
    private Long idTipoDocumento;
    private String codigoTipoDocumento;
    private String nombreTipoDocumento;

    // Serie y número
    private String serie;
    private Integer numeroActual;
    private String siguienteNumero; // Formato: SERIE-00000001

    private Boolean activo;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.pe.finanzas.puntos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para obtener el siguiente número de serie de un documento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieCorrelativoDto {

    private Long idPuntoDocumento;
    private String serie;
    private Integer numeroActual;
    private String siguienteNumero; // Formato completo: SERIE-00000001
    private String tipoDocumento;
}

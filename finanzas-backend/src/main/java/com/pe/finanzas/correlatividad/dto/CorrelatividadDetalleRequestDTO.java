package com.pe.finanzas.correlatividad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelatividadDetalleRequestDTO {
    private Long idSucursal;
    private Integer anio;
    private Integer mes;
    private String tipoDoc;
    private String serie;
}

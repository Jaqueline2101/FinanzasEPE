package com.pe.finanzas.correlatividad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelatividadItemDTO {
    private String tipoDoc;
    private String serie;
    private Long cantidad;
    private String desde;
    private String hasta;
    private BigDecimal baseImp;
    private BigDecimal igv;
    private BigDecimal total;
    private Integer mes;
}

package com.pe.finanzas.correlatividad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelatividadResponseDTO {
    private List<CorrelatividadItemDTO> items;
    private BigDecimal totalBaseImp;
    private BigDecimal totalIgv;
    private BigDecimal totalGeneral;
}

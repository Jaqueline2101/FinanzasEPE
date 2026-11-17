package com.pe.finanzas.rep_comprobantes_anulados.dto;

import com.pe.finanzas.shared.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepAnuladosResumenDTO {
    private BigDecimal totalAnulado;
    private Integer cantidadAnulados;
    private PageResponse<RepAnuladosResponseDTO> detalleAnulados;
}

package com.pe.finanzas.rep_honorarios.dto;

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
public class RepHonorariosResumenDTO {
    private BigDecimal totalBruto;
    private BigDecimal totalRetenciones;
    private BigDecimal totalNeto;
    private Integer totalRegistros;

    private PageResponse<RepHonorariosResponseDTO> detallePagos;
}

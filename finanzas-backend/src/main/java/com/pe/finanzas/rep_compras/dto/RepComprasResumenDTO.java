package com.pe.finanzas.rep_compras.dto;

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
public class RepComprasResumenDTO {
    private BigDecimal totalGravado;
    private BigDecimal totalInafecto;
    private BigDecimal totalExonerado;
    private BigDecimal totalIGV;
    private BigDecimal totalGeneral;

    private PageResponse<RepComprasResponseDTO> detalleCompras;
}

package com.pe.finanzas.rep_empresas_atendidas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaAtendidaDTO {
    private Long idCompania;
    private String nombreCompania;
    private Long totalAtenciones;
    private BigDecimal totalMonto;
}

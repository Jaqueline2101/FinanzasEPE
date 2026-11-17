package com.pe.finanzas.valorizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValorizacionResponseDTO {
    private ValorizacionResumenDTO resumen;
    private Page<ValorizacionDTO> detalle;
}

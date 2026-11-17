package com.pe.finanzas.compania.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSuggestionDTO {
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String tipo;
}

package com.pe.finanzas.grupo_familiar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoFamiliarResponse {
    private Long id;
    private String idTitular;
    private String idFamiliar;
    private String parentesco;
    private String estado;
    private LocalDate fechaRegistro;
    private LocalDate fechaInactivacion;
    private String observaciones;

    // Campos adicionales con información de las personas
    private String nombreTitular;
    private String nombreFamiliar;
}

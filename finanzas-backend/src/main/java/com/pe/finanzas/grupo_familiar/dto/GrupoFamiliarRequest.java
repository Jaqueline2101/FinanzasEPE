package com.pe.finanzas.grupo_familiar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoFamiliarRequest {
    private String idTitular;
    private String idFamiliar;
    private String parentesco;
    private String observaciones;

    // Campos opcionales para creación de persona
    private String nombre;
    private String apepat;
    private String apemat;
    private java.time.LocalDate nacfec;
    private String sexo;
}

package com.pe.finanzas.grupo_familiar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionTrabajadorResponse {
    private boolean esValido;
    private String nombre;
    private Long idEmpresa;
    private String mensaje;
}

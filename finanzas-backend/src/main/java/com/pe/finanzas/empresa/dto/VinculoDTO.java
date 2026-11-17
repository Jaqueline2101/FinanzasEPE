package com.pe.finanzas.empresa.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinculoDTO {

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long idEmpresa;

    @NotNull(message = "El ID del personal es obligatorio")
    private Long idPersonal;

    private String cargo;

    private LocalDate fechaInicio;

    private String estado;
}

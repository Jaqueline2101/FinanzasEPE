package com.pe.finanzas.motor_precios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficioRequest {

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "El código debe tener formato 3 letras-3 números (Ej: GEN-231)")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de plan es obligatorio")
    @Pattern(regexp = "^(GENERAL|FAMILIAR)$", message = "El tipo debe ser GENERAL o FAMILIAR")
    private String tipoPlan;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String estado;
}

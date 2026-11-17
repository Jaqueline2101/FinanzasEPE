package com.pe.finanzas.descuentos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPacienteRequest {

    @NotBlank(message = "El código del tipo de paciente es obligatorio")
    @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "El código solo puede contener letras mayúsculas, números y guiones bajos")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor a 100%")
    private BigDecimal dsc;

    @DecimalMin(value = "0.00", message = "La cobertura no puede ser negativa")
    @DecimalMax(value = "100.00", message = "La cobertura no puede ser mayor a 100%")
    private BigDecimal cob;

    private Boolean esFrecuente;
    private Integer estado;

    private List<ServicioDescuentoRequest> servicios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioDescuentoRequest {

        @NotBlank(message = "El tipo de servicio es obligatorio")
        private String tipoServicio;

        @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor a 100%")
        private BigDecimal descuento;

        @DecimalMin(value = "0.00", message = "La cobertura no puede ser negativa")
        @DecimalMax(value = "100.00", message = "La cobertura no puede ser mayor a 100%")
        private BigDecimal cobertura;

        private Integer estado;
    }
}

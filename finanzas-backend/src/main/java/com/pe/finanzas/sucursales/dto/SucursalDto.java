package com.pe.finanzas.sucursales.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDto {

    private Long idSucursal;

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreSucursal;

    @NotBlank(message = "La dirección es obligatoria para reportes financieros")
    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    private String direccion;

    @Pattern(regexp = "^([0-9]{3,15})?$", message = "El teléfono debe contener entre 3 y 15 dígitos numéricos")
    private String telefono;

    @Pattern(regexp = "^([0-9]{3,15})?$", message = "El celular debe contener entre 3 y 15 dígitos numéricos")
    private String celular;

    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;

    @NotNull(message = "El estado (1=Activo/0=Inactivo) es obligatorio")
    @Min(value = 0, message = "El estado mínimo es 0")
    @Max(value = 1, message = "El estado máximo es 1")
    private Integer estado;

    // Campos de auditoría (solo lectura desde el cliente)
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Indicador de capacidad operativa
    @Min(value = 0, message = "La cantidad de personal no puede ser negativa")
    private Integer cantidadPersonal;

    /**
     * Helper para lógica de negocio: Verifica si la sucursal puede operar
     */
    public boolean isOperativa() {
        return this.estado != null && this.estado == 1;
    }
}
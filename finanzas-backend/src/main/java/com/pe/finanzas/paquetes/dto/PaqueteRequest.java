package com.pe.finanzas.paquetes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear/actualizar Paquete
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteRequest {

    private String codigo;

    @NotBlank(message = "El nombre del paquete es obligatorio")
    private String nombre;

    private String descripcion;

    private Long idEmpresa;
    private Long idTipoAtencion;
    private String tipoAtencion; // AMBULATORIO, HOSPITALARIO, EMERGENCIA

    private String categoria; // PAQUETE_REGULAR, CAMPAÑA

    @NotNull(message = "El precio del paquete es obligatorio")
    private BigDecimal precioPaquete;

    private BigDecimal descuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado; // "1" = activo, "0" = inactivo

    // Servicios incluidos en el paquete
    private List<ServicioRequest> servicios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioRequest {
        @NotNull(message = "El ID del servicio es obligatorio")
        private Long idServicio;

        private Integer cantidad; // Cantidad de veces que se incluye el servicio

        private BigDecimal descuentoPorcentaje; // Porcentaje de descuento (ej: 15.00 = 15%)
    }
}

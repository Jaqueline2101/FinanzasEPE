package com.pe.finanzas.paquetes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de Paquete
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteDto {

    private Long idPaquete;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Long idEmpresa;
    private Long idTipoAtencion;
    private String tipoAtencionNombre;
    private String tipoAtencion;
    private String categoria;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal precioPaquete;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal descuento;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private String estado;
    private Boolean activo;
    private Boolean vigente;
    private Boolean permanente;

    // Servicios incluidos en el paquete
    private List<ServicioPaqueteDto> servicios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioPaqueteDto {
        private Long idDetalle;
        private Long idServicio;
        private String nombreServicio;
        private Integer cantidad;

        // Campos para manejo de descuentos
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
        private BigDecimal precioUnitario; // Precio por unidad del servicio

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
        private BigDecimal descuentoPorcentaje; // Porcentaje de descuento (ej: 15.00)

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
        private BigDecimal descuentoMonto; // Monto del descuento en soles

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
        private BigDecimal precioFinal; // Precio final después del descuento
    }
}

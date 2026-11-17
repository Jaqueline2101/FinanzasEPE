package com.pe.finanzas.venta_registro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para respuestas de detalle de venta
 * Todos los campos monetarios se formatean con 4 decimales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalleDto {

    private Long idMovart;
    private Long idVenta;
    private Long idArticulo;
    private String glosa;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal cantidad;

    // Campos financieros con 4 decimales
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal valorAf;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal valorInaf;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal valorVenta;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal descuento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal descuentoEsp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal cobertura;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal baseImp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal igv;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal total;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal precioUnitario;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal porcIgv;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal porcDsc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal porcCob;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private Integer estado;
    private String tipoDesc;
}

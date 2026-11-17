package com.pe.finanzas.venta_registro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para respuestas de venta (cabecera)
 * Todos los campos monetarios se formatean con 4 decimales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRegistroDto {

    private Long idVenta;
    private Long idSucursal;
    private Long idCtacte;
    private Long idPersonal;
    private Long idAlmacen;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private String tipoDoc;
    private String serie;
    private String numdoc;

    // Campos financieros con 4 decimales
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal valorAfecto;

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
    private BigDecimal copago;

    private Integer estado;
    private String nombrePac;
    private String ruc;
    private String razon;
    private String metodoPago;
    private String refMetodoPago;
    private String tipoAtencion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal importeSoles;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal importeTarjeta;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal importeDolares;

    // Lista de detalles
    @Builder.Default
    private List<VentaDetalleDto> detalles = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

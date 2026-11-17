package com.pe.finanzas.orden_compra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraDetalleDto {

    private Long id;
    private String codigoItem;
    private String descripcion;
    private String unidadMedida;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.000")
    private BigDecimal cantidad;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal precioUnitario;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal descuento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal subtotal;

    private Boolean afectoIgv;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal igv;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal total;
}

package com.pe.finanzas.compra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraDTO {
    private Long id;
    private Long idProveedor;
    private String proveedorRazonSocial;
    private Long idSucursal;

    private String tipoComprobante;
    private String serie;
    private String correlativo;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String condicionPago;
    private String moneda;

    private BigDecimal impDolar;
    private BigDecimal tipoCambio;
    private BigDecimal impBruto;
    private BigDecimal descuento;
    private BigDecimal otrosTributos;

    private Boolean detraccion;
    private String retencion;
    private String medioPago;

    private BigDecimal valorVentaGravado;
    private BigDecimal valorVentaInafecto;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal percepcion;
    private BigDecimal totalPagar;
    private BigDecimal porcentajeIgv;

    private String estado;

    private Integer cuotas;
    private Integer diasFrecuencia;

    private List<DetalleCompraDTO> detalles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleCompraDTO {
        private Long id;
        private Long idProducto;
        private String productoNombre;
        private BigDecimal cantidad;
        private String unidadMedida;
        private Integer factorConversion;

        private String lote;
        private LocalDate fechaVencimiento;

        private BigDecimal precioUnitario;
        private BigDecimal porcentajeDescuento;
        private Boolean esBonificacion;
        private String tipoAfectacion;
        private String descripcion;
        private BigDecimal valorVenta;
    }
}

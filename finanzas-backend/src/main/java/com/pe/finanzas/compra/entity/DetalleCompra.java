package com.pe.finanzas.compra.entity;

import com.pe.finanzas.catalogo.entity.Catalogo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "detalle_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private Compra compra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Catalogo catalogo;

    @Column(name = "id_stock_producto")
    private Long idProductoStock;

    @Column(name = "producto_nombre", length = 255)
    private String productoNombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "id_unidad_medida")
    private Integer unidadMedida;

    @Column(name = "factor_conversion")
    @Builder.Default
    private Integer factorConversion = 1;

    @Column(length = 255)
    private String descripcion;

    // -- Datos de Farmacia --
    @Column(length = 50)
    private String lote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(length = 100)
    private String presentacion;

    // -- Precios y Descuentos --
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "porcentaje_descuento2", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento2 = BigDecimal.ZERO;

    @Column(name = "es_bonificacion")
    @Builder.Default
    private Boolean esBonificacion = false;

    @Column(name = "tipo_afectacion", length = 20)
    @Builder.Default
    private String tipoAfectacion = "GRAVADO_ONEROSO";

    @Column(name = "valor_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorVenta;
}

package com.pe.finanzas.compra.entity;

import com.pe.finanzas.proveedores.entity.Proveedor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private String tipoComprobante;

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(nullable = false, length = 20)
    private String correlativo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "condicion_pago", length = 50)
    private String condicionPago;

    @Column(nullable = false, length = 3)
    private String moneda; // PEN, USD

    @Column(name = "imp_dolar", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal impDolar = BigDecimal.ZERO;

    @Column(name = "tipo_cambio", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal tipoCambio = BigDecimal.ONE;

    @Column(name = "imp_bruto", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal impBruto = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "valor_venta_gravado", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorVentaGravado = BigDecimal.ZERO;

    @Column(name = "valor_venta_inafecto", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorVentaInafecto = BigDecimal.ZERO;

    @Column(name = "otros_tributos", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal otrosTributos = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal percepcion = BigDecimal.ZERO;

    @Column(name = "total_pagar", precision = 12, scale = 2)
    private BigDecimal totalPagar;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean detraccion = false;

    @Column(length = 20)
    private String retencion;

    @Column(name = "medio_pago", length = 50)
    private String medioPago;

    @Column(name = "porcentaje_igv", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeIgv = new BigDecimal("18.00");

    @Column(nullable = false, length = 20)
    @Builder.Default
    private Integer estado = 1;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<DetalleCompra> detalles;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CronogramaPago> cronograma;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
}

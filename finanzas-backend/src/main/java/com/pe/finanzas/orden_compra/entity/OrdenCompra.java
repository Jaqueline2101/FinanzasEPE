package com.pe.finanzas.orden_compra.entity;

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
@Table(name = "ordenes_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", unique = true, nullable = false, length = 20)
    private String numeroOrden;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_estimada_entrega")
    private LocalDate fechaEstimadaEntrega;

    @Column(name = "tipo_compra", length = 50)
    private String tipoCompra; // Insumos / Servicios / Equipos / Artículos

    @Column(name = "centro_costo", length = 100)
    private String centroCosto;

    @Column(name = "area_solicitante", length = 100)
    private String areaSolicitante;

    @Column(name = "responsable_compra", length = 100)
    private String responsableCompra;

    @Column(nullable = false, length = 10)
    private String moneda; // PEN, USD

    @Column(name = "tipo_cambio", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal tipoCambio = BigDecimal.ONE;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "BORRADOR"; // Borrador / Aprobado / Enviado / Recibido / Anulado

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // --- CAMPOS DE ADELANTO ---
    @Column(name = "permite_adelanto")
    @Builder.Default
    private Boolean permiteAdelanto = false;

    @Column(name = "tipo_adelanto", length = 20)
    private String tipoAdelanto; // Porcentaje / Monto fijo

    @Column(name = "porcentaje_adelanto", precision = 5, scale = 2)
    private BigDecimal porcentajeAdelanto;

    @Column(name = "monto_adelanto", precision = 12, scale = 2)
    private BigDecimal montoAdelanto;

    @Column(name = "fecha_adelanto")
    private LocalDate fechaAdelanto;

    @Column(name = "estado_adelanto", length = 20)
    private String estadoAdelanto; // Pendiente / Pagado / Parcial

    @Column(name = "saldo_pendiente_adelanto", precision = 12, scale = 2)
    private BigDecimal saldoPendienteAdelanto;

    // --- PROVEEDOR ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    // --- RESUMEN / TOTALES ---
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_descuento", precision = 12, scale = 2)
    private BigDecimal totalDescuento;

    @Column(name = "total_igv", precision = 12, scale = 2)
    private BigDecimal totalIgv;

    @Column(name = "total_general", precision = 12, scale = 2)
    private BigDecimal totalGeneral;

    // --- CONTROL FINANCIERO ---
    @Column(name = "total_adelantado", precision = 12, scale = 2)
    private BigDecimal totalAdelantado;

    @Column(name = "saldo_por_pagar", precision = 12, scale = 2)
    private BigDecimal saldoPorPagar;

    @Column(name = "estado_pago", length = 20)
    private String estadoPago; // Sin pago / Con adelanto / Pagado total

    @Column(name = "condicion_pago", length = 50)
    private String condicionPago;

    @Column(name = "plazo_dias")
    private Integer plazoDias;

    // --- INFORMACIÓN LOGÍSTICA ---
    @Column(name = "lugar_entrega", length = 255)
    private String lugarEntrega;

    @Column(name = "direccion_entrega", length = 500)
    private String direccionEntrega;

    @Column(name = "fecha_entrega_acordada")
    private LocalDate fechaEntregaAcordada;

    @Column(name = "responsable_recepcion", length = 100)
    private String responsableRecepcion;

    @Column(name = "observaciones_logisticas", columnDefinition = "TEXT")
    private String observacionesLogisticas;

    // --- CONTROL INTERNO ---
    @Column(name = "usuario_registro", length = 50)
    private String usuarioRegistro;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "usuario_aprobacion", length = 50)
    private String usuarioAprobacion;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "num_cotizacion_relacionada", length = 50)
    private String numCotizacionRelacionada;

    @Column(name = "num_requerimiento_interno", length = 50)
    private String numRequerimientoInterno;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "usuario_anulacion", length = 50)
    private String usuarioAnulacion;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenCompraDetalle> detalles;
}

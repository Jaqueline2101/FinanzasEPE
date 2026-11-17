package com.pe.finanzas.pagorec_honorarios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pago_concepto_detalle")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoConceptoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago")
    private PagoComprobante pagoComprobante;

    @Column(name = "nombre_concepto", length = 100)
    private String nombreConcepto;

    @Column(name = "tipo_concepto", length = 20)
    private String tipoConcepto; // INGRESO, DESCUENTO, APORTE

    @Column(name = "monto", precision = 12, scale = 4)
    private BigDecimal monto;

    @Column(name = "fecha_detalle")
    private LocalDate fechaDetalle;

    // 📌 Para Liquidaciones (si aplica)
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cese")
    private LocalDate fechaCese;

    @Column(name = "tiempo_trabajado", length = 100)
    private String tiempoTrabajado;

    @Column(name = "vacaciones_pendientes", precision = 12, scale = 4)
    private BigDecimal vacacionesPendientes;

    @Column(name = "cts", precision = 12, scale = 4)
    private BigDecimal cts;

    @Column(name = "gratificacion", precision = 12, scale = 4)
    private BigDecimal gratificacion;

    @Column(name = "indemnizacion", precision = 12, scale = 4)
    private BigDecimal indemnizacion;

    @Column(name = "total_liquidacion", precision = 12, scale = 4)
    private BigDecimal totalLiquidacion;
}

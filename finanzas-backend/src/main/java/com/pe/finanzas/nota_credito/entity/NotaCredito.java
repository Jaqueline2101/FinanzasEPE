package com.pe.finanzas.nota_credito.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nota_credito")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nc")
    private Long idNc;

    /** Serie de la NC, ej: NC-B, NC-F */
    @Column(name = "serie_nc", nullable = false, length = 10)
    private String serieNc;

    /** Número correlativo de la NC dentro de la serie */
    @Column(name = "numdoc_nc", nullable = false, length = 20)
    private String numdocNc;

    /** FK a la venta original referenciada */
    @Column(name = "id_venta_ref", nullable = false)
    private Long idVentaRef;

    /** Snapshot del número de comprobante original (ej: B 001-00000003) */
    @Column(name = "comprobante_ref", length = 50)
    private String comprobanteRef;

    /**
     * Tipo del comprobante referenciado: BOLETA | FACTURA
     * Los tickets internos (T) no generan NC
     */
    @Column(name = "tipo_comprobante", nullable = false, length = 10)
    private String tipoComprobante;

    /**
     * Código de motivo SUNAT:
     * 01 = Anulación de la operación
     * 02 = Anulación por error en el RUC
     * 03 = Corrección por error en la descripción
     * 04 = Descuento global
     * 05 = Descuento por ítem
     * 06 = Devolución total
     * 07 = Devolución por ítem
     * 13 = Ajuste en el monto
     */
    @Column(name = "motivo_codigo", nullable = false, length = 5)
    private String motivoCodigo;

    /** Descripción del motivo SUNAT (snapshot) */
    @Column(name = "motivo_descripcion", nullable = false, length = 200)
    private String motivoDescripcion;

    /** Detalle adicional del operador */
    @Column(name = "detalle", length = 500)
    private String detalle;

    /**
     * Monto que afecta la NC — puede ser parcial (descuento) o total (anulación)
     */
    @Column(name = "monto_nc", nullable = false, precision = 12, scale = 4)
    private BigDecimal montoNc;

    /** Monto total del comprobante original (snapshot) */
    @Column(name = "monto_original", precision = 12, scale = 4)
    private BigDecimal montoOriginal;

    /** Paciente / cliente referenciado (snapshot) */
    @Column(name = "paciente", length = 200)
    private String paciente;

    /** Sucursal donde se emite la NC */
    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    /** Usuario que emite la NC */
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    /** Nombre del usuario (snapshot) */
    @Column(name = "nombre_usuario", length = 150)
    private String nombreUsuario;

    /** Fecha de emisión de la NC */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /**
     * Estado de la NC: EMITIDA | ANULADA
     * (Una NC puede anularse emitiendo otra NC — raro pero posible)
     */
    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private String estado = "EMITIDA";

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

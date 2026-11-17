package com.pe.finanzas.solicitud_anulacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_anulacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAnulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    /** Venta que se quiere anular */
    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    /** Sucursal donde ocurrió la venta */
    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    /** Número de comprobante (ej.: B 001-00000003) — snapshot para auditoría */
    @Column(name = "comprobante", length = 50)
    private String comprobante;

    /** Fecha original de la venta */
    @Column(name = "fecha_venta")
    private LocalDate fechaVenta;

    /** Monto total de la venta a anular */
    @Column(name = "monto_venta")
    private java.math.BigDecimal montoVenta;

    /** Motivo que escribe el cajero al solicitar */
    @Column(name = "motivo_solicitud", nullable = false, length = 500)
    private String motivoSolicitud;

    /**
     * Estado del ciclo de vida:
     * PENDIENTE → APROBADO | RECHAZADO
     */
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private String estado = "PENDIENTE";

    /** Usuario que creó la solicitud (cajero/vendedor) */
    @Column(name = "id_solicitante", nullable = false)
    private Long idSolicitante;

    /** Nombre del solicitante — snapshot */
    @Column(name = "nombre_solicitante", length = 150)
    private String nombreSolicitante;

    /** Usuario que aprobó o rechazó (supervisor/admin) */
    @Column(name = "id_autorizador")
    private Long idAutorizador;

    /** Nombre del autorizador — snapshot */
    @Column(name = "nombre_autorizador", length = 150)
    private String nombreAutorizador;

    /** Motivo del rechazo (solo cuando estado = RECHAZADO) */
    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    /** Fecha en que el supervisor resolvió la solicitud */
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

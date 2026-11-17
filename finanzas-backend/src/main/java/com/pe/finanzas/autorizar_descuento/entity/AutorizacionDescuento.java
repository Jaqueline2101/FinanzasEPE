package com.pe.finanzas.autorizar_descuento.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.pe.finanzas.users.entity.DatosPersonales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "autorizacion_descuento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutorizacionDescuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autorizacion")
    private Long idAutorizacion;

    // Optional linkage to a specific receipt/sale if needed later.
    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "monto_original", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoOriginal;

    @Column(name = "monto_descuento", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoDescuento;

    @Column(name = "monto_final", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoFinal;

    @Column(name = "motivo_solicitud", length = 255)
    private String motivoSolicitud;

    // PENDIENTE, APROBADO, RECHAZADO
    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "paciente_nombre", length = 255)
    private String pacienteNombre;

    @Column(name = "paciente_dni", length = 20)
    private String pacienteDni;

    @Column(name = "doctor_nombre", length = 255)
    private String doctorNombre;

    @ManyToOne(fetch = FetchType.LAZY)


    @JoinColumn(name = "id_cajero", nullable = false)
    private DatosPersonales cajero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autorizador")
    private DatosPersonales autorizador;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}

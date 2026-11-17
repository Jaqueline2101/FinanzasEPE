package com.pe.finanzas.caja_chica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_chica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaChica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "saldo_actual", precision = 15, scale = 5)
    @Builder.Default
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @Column(name = "saldo_inicial", precision = 15, scale = 5)
    @Builder.Default
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Column(name = "saldo_cierre_real", precision = 15, scale = 5)
    private BigDecimal saldoCierreReal;

    @Column(name = "id_punto_venta")
    private Long idPuntoVenta;

    @Column(name = "id_usuario_cajero")
    private String idUsuarioCajero;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum EstadoCaja {
        ABIERTA, CERRADA
    }
}

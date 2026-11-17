package com.pe.finanzas.arqueo_caja.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "arqueo_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArqueoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(name = "id_punto")
    private Long idPunto;

    @Column(name = "saldo_esperado", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoEsperado; // Lo que dice el sistema

    @Column(name = "saldo_fisico", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoFisico; // Lo contado en billetes/monedas

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal diferencia; // Fisico - Esperado

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "CERRADO"; // ABIERTO, CERRADO, ANULADO

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

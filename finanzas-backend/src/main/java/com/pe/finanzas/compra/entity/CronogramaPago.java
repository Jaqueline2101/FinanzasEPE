package com.pe.finanzas.compra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cronograma_pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cronograma")
    private Long idCronograma;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_compra", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "cronograma", "hibernateLazyInitializer",
            "handler" })
    private Compra compra;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota; // 1, 2, 3...

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "monto_cuota", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCuota;

    @Column(nullable = false, length = 20)
    @Builder.Default
    @JsonIgnore
    private Integer estado = 1;

    @JsonGetter("estado")
    public String getEstadoDesc() {
        if (estado == null) return "PENDIENTE";
        switch (estado) {
            case 16: return "PAGADO";
            case 2: return "PENDIENTE";
            case 1: return "PENDIENTE";
            default: return "PENDIENTE";
        }
    }

    @Column(name = "fecha_pago_real")
    private LocalDateTime fechaPagoReal;

    @Column(name = "comprobante_pago")
    private String comprobantePago;
}

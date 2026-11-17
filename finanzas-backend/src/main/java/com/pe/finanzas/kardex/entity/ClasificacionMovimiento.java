package com.pe.finanzas.kardex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventario_clasificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificacionMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private Integer estado = 1;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, unique = true)
    private String nombre; // e.g., COMPRA, VENTA, AJUSTE_MERMA, TRASLADO

    @Column(nullable = false)
    private String signo; // "+" or "-"

    private String descripcion;
}

package com.pe.finanzas.venta_registro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "lab_orden_detalle")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOrdenDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_det")
    private Long idOrdenDet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    private LabOrden labOrden;

    @Column(name = "id_examen")
    private Long idExamen;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "precio", precision = 12, scale = 4)
    private BigDecimal precio;

    @Column(name = "estado", length = 20)
    private String estado;
}

package com.pe.finanzas.motor_precios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cred_planes_descuento", uniqueConstraints = @UniqueConstraint(columnNames = { "id_plan",
        "id_servicio" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredPlanDescuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan_descuento")
    private Long idPlanDescuento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan", nullable = false)
    private CredPlan plan;

    @Column(name = "id_servicio", nullable = true) // Temporary fix: allow null for legacy data
    private Long idServicio;

    @Column(name = "tipo_atencion", length = 20)
    private String tipoAtencion;

    @Column(name = "nivel_servicio")
    private Integer nivelServicio;

    @Column(name = "factor", precision = 10, scale = 4)
    private BigDecimal factor;

    @Column(name = "tipo_descuento", length = 20)
    private String tipoDescuento;

    @Column(name = "descuento", precision = 10, scale = 4, nullable = false)
    private BigDecimal descuento;

    @Column(name = "codigo_plan", length = 50)
    private String codigoPlan;

    @Column(name = "descuento_fijo", precision = 10, scale = 4)
    private BigDecimal descuentoFijo;

    @Column(name = "descuento_porcentaje", precision = 10, scale = 4)
    private BigDecimal descuentoPorcentaje;

    @Column(name = "precio_fijo", precision = 10, scale = 4)
    private BigDecimal precioFijo;

    @Column(name = "estado", length = 1)
    private String estado;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActivo() {
        return "1".equals(estado);
    }

    public boolean aplicaATodosLosNiveles() {
        return nivelServicio == null || nivelServicio == 0;
    }

    public boolean aplicaANivel(Integer nivel) {
        return aplicaATodosLosNiveles() || nivelServicio.equals(nivel);
    }

    public boolean aplicaATipoAtencion(String tipo) {
        return "TODOS".equalsIgnoreCase(tipoAtencion) || tipoAtencion.equalsIgnoreCase(tipo);
    }

    public boolean esPorcentaje() {
        return "PORCENTAJE".equalsIgnoreCase(tipoDescuento);
    }

    public boolean esMontoFijo() {
        return "MONTO_FIJO".equalsIgnoreCase(tipoDescuento);
    }
}

package com.pe.finanzas.activos_fijos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.finanzas.catalogo.entity.Catalogo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activos_fijos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_activo")
    private Long idActivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Catalogo catalogo;

    @Column(name = "codigo_patrimonial", unique = true, length = 50)
    private String codigoPatrimonial;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "estado_fisico", length = 50)
    private String estadoFisico;

    @Column(name = "situacion", length = 50)
    private String situacion;

    @Column(name = "valor_compra", precision = 12, scale = 2)
    private BigDecimal valorCompra;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_inicio_uso")
    private LocalDate fechaInicioUso;

    @Column(name = "vida_util_anios")
    private Integer vidaUtilAnios;

    @Column(name = "ubicacion_detallada", length = 200)
    private String ubicacionDetallada;

    @Column(name = "id_responsable")
    private Long idResponsable;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    @CreatedBy
    private String createdBy;

    @Column(name = "updated_by")
    @LastModifiedBy
    private String updatedBy;
}

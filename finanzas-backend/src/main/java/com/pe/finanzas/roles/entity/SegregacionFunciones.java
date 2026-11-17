package com.pe.finanzas.roles.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "segregacion_funciones")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SegregacionFunciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol_origen", nullable = false)
    private Role rolOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol_conflicto", nullable = false)
    private Role rolConflicto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "razon_negocio", length = 500)
    private String razonNegocio;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

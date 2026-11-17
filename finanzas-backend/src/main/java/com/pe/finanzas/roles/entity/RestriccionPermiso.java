package com.pe.finanzas.roles.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "restriccion_permiso")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestriccionPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permission", nullable = false)
    private com.pe.finanzas.permissions.entity.Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_restriccion", nullable = false, length = 30)
    private TipoRestriccion tipoRestriccion;

    @Column(name = "valor_numerico", precision = 12, scale = 2)
    private BigDecimal valorNumerico;

    @Column(name = "valor_texto", length = 100)
    private String valorTexto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

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

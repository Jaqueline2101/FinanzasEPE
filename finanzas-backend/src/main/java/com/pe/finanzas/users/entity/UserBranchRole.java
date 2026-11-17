package com.pe.finanzas.users.entity;

import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.sucursales.entity.Sucursal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_branch_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "branch_id", "role_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBranchRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private DatosPersonales user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @ToString.Exclude
    private Sucursal branch;

    @ManyToOne(fetch = FetchType.EAGER) // Eager para cargar el rol y sus permisos rápido
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

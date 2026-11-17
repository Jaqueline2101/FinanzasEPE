package com.pe.finanzas.users.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pe.finanzas.permissions.entity.Permission;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "permission_id" }, name = "uk_user_permission")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user") // Evita recursividad y fugas de memoria en logs
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el usuario (Lazy para no sobrecargar la memoria)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private DatosPersonales user;

    // Relación con el permiso (Eager porque si consultas el permiso especial,
    // quieres ver qué es)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    // Auditoría: Quién otorgó el privilegio extra
    @Column(name = "granted_by")
    private Long grantedBy;

    @CreationTimestamp
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    // Explicación legal/administrativa del permiso excepcional
    @Column(length = 500)
    private String reason;

    // Seguridad Temporal: El permiso puede ser solo por hoy o una semana
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Verifica si el permiso sigue siendo válido legalmente
     */
    public boolean isValid() {
        if (!active)
            return false;
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }
}
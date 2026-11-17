package com.pe.finanzas.roles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.pe.finanzas.permissions.entity.Permission;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.accesos.entity.AccesoMain;

@Entity
@Table(name = "roles")
@lombok.Getter
@lombok.Setter
@lombok.ToString(exclude = { "permissions", "users" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "alcance", length = 20)
    @Builder.Default
    private String alcance = "GLOBAL";

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<com.pe.finanzas.users.entity.UserBranchRole> userBranchRoles = new HashSet<>();

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<DatosPersonales> users = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Role parent;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<Role> children = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_acceso", nullable = false)
    private AccesoMain acceso;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Role))
            return false;
        Role role = (Role) o;
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
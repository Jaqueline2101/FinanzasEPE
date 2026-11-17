package com.pe.finanzas.users.dto;

import com.pe.finanzas.permissions.dto.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// ========================================
// Response principal de usuario con permisos
// ========================================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithPermissionsResponse {

    // Datos básicos del usuario
    private Long id;
    private String login;
    private String nombre;
    private String apepat;
    private String apemat;
    private String fullName;
    private String email;
    private Boolean active;

    // Información de roles
    private Set<String> roles;
    private List<PermissionResponse> rolePermissions;

    // Modo de permisos
    private Boolean useDirectPermissions;
    private String permissionMode; // "ROL" o "DIRECTO"

    // Permisos directos
    private List<UserDirectPermissionResponse> directPermissions;
    private Long activeDirectPermissionsCount;

    // Permisos efectivos (los que realmente tiene según su modo)
    private List<PermissionResponse> effectivePermissions;
    private Long effectivePermissionsCount;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

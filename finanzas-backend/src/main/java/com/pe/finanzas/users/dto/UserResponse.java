package com.pe.finanzas.users.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.pe.finanzas.permissions.dto.PermissionResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private Long idPersonal;
    private String username;
    private String email;
    private String firstName;
    private String paternalSurname;
    private String maternalSurname;
    private String lastName;
    private String phone;
    private Boolean active;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private Long roleId;
    private String roleName;
    private Boolean useDirectPermissions;
    private List<PermissionResponse> rolePermissions;
    private List<UserDirectPermissionResponse> directPermissions;
    private List<PermissionResponse> effectivePermissions;
    private Long idCompania;

    // Campos para identificación de familiar
    private Boolean esFamiliar;
    private String nombreTitular;

    // Campos solicitados en Mi Perfil
    private String numdoc;
    private java.time.LocalDate nacfec;
    private String direcc;
    private String nomOcupacion;
    private java.time.LocalDate fechaIng;
    private String sucursal;
    private String fotoPerfil;

}
// Force Recompile 2

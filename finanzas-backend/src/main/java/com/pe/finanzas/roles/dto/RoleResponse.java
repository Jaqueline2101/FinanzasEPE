package com.pe.finanzas.roles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

import com.pe.finanzas.permissions.dto.PermissionResponse;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private long userCount;
    private Set<PermissionResponse> permissions;
    private Long idAcceso;
    private String nombreAcceso;

    private String alcance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
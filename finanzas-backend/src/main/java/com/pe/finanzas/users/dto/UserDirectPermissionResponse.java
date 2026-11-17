package com.pe.finanzas.users.dto;

import java.time.LocalDateTime;

import com.pe.finanzas.permissions.dto.PermissionResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDirectPermissionResponse {

    private Long id;
    private PermissionResponse permission;

    // Información de quien otorgó el permiso
    private Long grantedBy;
    private String grantedByLogin;
    private String grantedByFullName;

    // Metadata del permiso
    private LocalDateTime grantedAt;
    private String reason;
    private LocalDateTime expiresAt;
    private Boolean active;
    private Boolean isExpired;

    // Días hasta expiración (si aplica)
    private Long daysUntilExpiration;
}

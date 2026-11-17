package com.pe.finanzas.permissions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String module;
    private boolean active;
    private java.time.LocalDateTime createdAt;
}
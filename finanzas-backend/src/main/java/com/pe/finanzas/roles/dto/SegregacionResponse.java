package com.pe.finanzas.roles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SegregacionResponse {

    private Long id;
    private Long rolOrigenId;
    private String rolOrigenName;
    private Long rolConflictoId;
    private String rolConflictoName;
    private String descripcion;
    private String razonNegocio;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

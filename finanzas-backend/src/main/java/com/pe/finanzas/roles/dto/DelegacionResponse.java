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
public class DelegacionResponse {

    private Long id;
    private Long usuarioOrigenId;
    private String usuarioOrigenNombre;
    private Long usuarioDelegadoId;
    private String usuarioDelegadoNombre;
    private Long roleId;
    private String roleName;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String motivo;
    private Boolean active;
    private Boolean isCurrentlyActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

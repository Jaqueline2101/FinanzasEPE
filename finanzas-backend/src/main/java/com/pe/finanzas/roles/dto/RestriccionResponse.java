package com.pe.finanzas.roles.dto;

import com.pe.finanzas.roles.entity.TipoRestriccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestriccionResponse {

    private Long id;
    private Long roleId;
    private String roleName;
    private Long permissionId;
    private String permissionName;
    private TipoRestriccion tipoRestriccion;
    private BigDecimal valorNumerico;
    private String valorTexto;
    private String descripcion;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

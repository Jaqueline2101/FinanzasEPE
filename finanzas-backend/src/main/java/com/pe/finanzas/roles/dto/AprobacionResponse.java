package com.pe.finanzas.roles.dto;

import com.pe.finanzas.roles.entity.EstadoAprobacion;
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
public class AprobacionResponse {

    private Long id;
    private Long solicitanteId;
    private String solicitanteNombre;
    private Long aprobadorId;
    private String aprobadorNombre;
    private String tipoAccion;
    private String descripcion;
    private BigDecimal monto;
    private String datosContexto;
    private EstadoAprobacion estado;
    private String comentarioAprobador;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private Boolean active;
}

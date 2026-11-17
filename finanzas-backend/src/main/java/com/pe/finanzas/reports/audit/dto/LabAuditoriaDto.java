package com.pe.finanzas.reports.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabAuditoriaDto {
    private Long idAuditoria;
    private String tabla;
    private String idRegistro;
    private String accion;
    private Long idPersonalUser;
    private String nombreUsuario;
    private String emailUsuario;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    private String datoAnterior;
    private String datoNuevo;
    private String descripcionCambio;
    private Integer cantidadCambios;
    private String ip;
    private String estado;
    private String userAgent;
    private String url;
    private String method;
}

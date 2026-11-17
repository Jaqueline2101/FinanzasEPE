package com.pe.finanzas.reports.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditFilterDto {
    private String tabla;
    private String idRegistro;
    private String accion;
    private Long idPersonalUser;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}

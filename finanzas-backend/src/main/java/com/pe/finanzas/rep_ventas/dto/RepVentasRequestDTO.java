package com.pe.finanzas.rep_ventas.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RepVentasRequestDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idSucursal;
    private Long idPersonal; // Filter by Cajero
    private Integer estado;
    private String query;
    private String tipoAtencion;
}

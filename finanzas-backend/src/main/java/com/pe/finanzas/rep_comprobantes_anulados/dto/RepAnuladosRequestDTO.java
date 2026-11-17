package com.pe.finanzas.rep_comprobantes_anulados.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RepAnuladosRequestDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idSucursal;
    private String query;
}

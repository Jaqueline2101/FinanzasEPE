package com.pe.finanzas.rep_honorarios.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RepHonorariosRequestDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipoPago;
    private String tipoBeneficiario;
    private String query; // Para buscar por nombre o documento
    private String estadoPago;
}

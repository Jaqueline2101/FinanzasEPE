package com.pe.finanzas.motor_precios.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class BulkVinculationRequest {
    private Long idCompania;
    private List<Long> idEmpresas;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String nombreBase;

    // Campos de seguro
    @jakarta.validation.constraints.Min(0)
    @com.fasterxml.jackson.annotation.JsonProperty("copago")
    private java.math.BigDecimal copago;

    @jakarta.validation.constraints.Min(0)
    @com.fasterxml.jackson.annotation.JsonProperty("deducible")
    private java.math.BigDecimal deducible;

    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(100)
    @com.fasterxml.jackson.annotation.JsonProperty("porcentajeCoaseguro")
    private java.math.BigDecimal porcentajeCoaseguro;
}

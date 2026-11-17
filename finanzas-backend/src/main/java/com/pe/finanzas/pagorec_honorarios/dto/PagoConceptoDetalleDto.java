package com.pe.finanzas.pagorec_honorarios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoConceptoDetalleDto {

    private Long idDetalle;
    private String nombreConcepto;
    private String tipoConcepto;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal monto;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDetalle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCese;

    private String tiempoTrabajado;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal vacacionesPendientes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal cts;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal gratificacion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal indemnizacion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal totalLiquidacion;
}

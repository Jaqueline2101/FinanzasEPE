package com.pe.finanzas.rep_honorarios.dto;

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
public class RepHonorariosResponseDTO {
    private Long idPago;
    private LocalDate fechaEmision;
    private String comprobante; // Serie-Numero
    private String beneficiario;
    private String documento;
    private String tipoPago;
    private String concepto;
    private BigDecimal montoBruto;
    private BigDecimal retencion;
    private BigDecimal montoNeto;
    private String moneda;
    private String medioPago;
    private String estado;
}

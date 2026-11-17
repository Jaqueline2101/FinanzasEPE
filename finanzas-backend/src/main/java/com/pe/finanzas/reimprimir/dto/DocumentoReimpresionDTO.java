package com.pe.finanzas.reimprimir.dto;

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
public class DocumentoReimpresionDTO {
    private Long id;
    private LocalDate fecha;
    private String tipoDocumento;
    private String serie;
    private String numero;
    private String clienteBeneficiario;
    private BigDecimal total;
    private String moneda;
    private String estado;
    private String usuario;
    private String moduloOrigen; // VENTAS, HONORARIOS, PAGO_PERSONAL
}

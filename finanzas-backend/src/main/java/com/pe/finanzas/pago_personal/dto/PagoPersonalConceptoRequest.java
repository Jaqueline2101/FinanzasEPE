package com.pe.finanzas.pago_personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoPersonalConceptoRequest {
    private String nombreConcepto;
    private String tipoConcepto; // INGRESO, DESCUENTO, APORTE
    private BigDecimal monto;
}

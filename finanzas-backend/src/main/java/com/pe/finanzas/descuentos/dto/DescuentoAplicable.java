package com.pe.finanzas.descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoAplicable {

    private String tipoDescuento;

    private BigDecimal porcentajeDescuento;

    private BigDecimal descuentoFijo;

    private BigDecimal precioFijo;

    private BigDecimal porcentajeCobertura;

    private Long idCampaña;

    private Long idPaquete;

    private Long idPlanDescuento;

    private String tipoPac;

    private String descripcion;

    private Integer prioridad;

    private Boolean requiereAutorizacion;

    public BigDecimal calcularBeneficioTotal() {
        BigDecimal descuento = porcentajeDescuento != null ? porcentajeDescuento : BigDecimal.ZERO;
        BigDecimal cobertura = porcentajeCobertura != null ? porcentajeCobertura : BigDecimal.ZERO;
        return descuento.add(cobertura);
    }
}

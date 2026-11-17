package com.pe.finanzas.venta_registro.service;

import com.pe.finanzas.venta_registro.dto.LiquidacionSeguroDto;
import java.math.BigDecimal;

/**
 * Interfaz para el motor de cálculo de liquidaciones de seguros médicos.
 */
public interface LiquidacionSeguroService {
    
    /**
     * Calcula el desglose detallado de una liquidación de seguro.
     * 
     * @param totalCuenta Total bruto de la atención.
     * @param copago Monto fijo de copago (pago inicial paciente).
     * @param deducible Monto de deducible a aplicar.
     * @param porcentajeCoaseguro Porcentaje que paga el PACIENTE (ej: 20.00 para 20%).
     * @return LiquidacionSeguroDto con el desglose calculado.
     */
    LiquidacionSeguroDto calcular(BigDecimal totalCuenta, BigDecimal copago, BigDecimal deducible, BigDecimal porcentajeCoaseguro);
}

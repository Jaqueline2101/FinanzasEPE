package com.pe.finanzas.venta_registro.service.impl;

import com.pe.finanzas.venta_registro.dto.LiquidacionSeguroDto;
import com.pe.finanzas.venta_registro.service.LiquidacionSeguroService;
import com.pe.finanzas.venta_registro.service.CalculoVentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquidacionSeguroServiceImpl implements LiquidacionSeguroService {

    private final CalculoVentaService calculoVentaService;
    private static final BigDecimal CIEN = new BigDecimal("100");

    @Override
    public LiquidacionSeguroDto calcular(BigDecimal totalCuenta, BigDecimal copago, BigDecimal deducible, BigDecimal porcentajeCoaseguro) {
        log.info("Iniciando cálculo de liquidación: Total={}, Copago={}, Ded={}, Coaseguro={}%", 
                 totalCuenta, copago, deducible, porcentajeCoaseguro);

        BigDecimal nTotal = calculoVentaService.normalizarDecimales(totalCuenta);
        BigDecimal nCopago = calculoVentaService.normalizarDecimales(copago);
        BigDecimal nDeducible = calculoVentaService.normalizarDecimales(deducible);
        BigDecimal nPorcCoaseguro = calculoVentaService.normalizarDecimales(porcentajeCoaseguro);

        // 1. Restar Copago
        BigDecimal subtotal = nTotal.subtract(nCopago);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) subtotal = BigDecimal.ZERO;

        // 2. Restar Deducible
        BigDecimal subtotalGravable = subtotal.subtract(nDeducible);
        if (subtotalGravable.compareTo(BigDecimal.ZERO) < 0) subtotalGravable = BigDecimal.ZERO;

        // 3. Calcular Coaseguro
        // Si el porcentaje es 20%, el paciente paga el 20% del subtotalGravable
        BigDecimal factorPaciente = nPorcCoaseguro.divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal montoCoaseguroPaciente = subtotalGravable.multiply(factorPaciente)
                .setScale(4, RoundingMode.HALF_UP);
        
        // 4. Calcular Pago Seguro
        BigDecimal montoPagoSeguro = subtotalGravable.subtract(montoCoaseguroPaciente)
                .setScale(4, RoundingMode.HALF_UP);

        // 5. Total Paciente
        BigDecimal totalPaciente = nCopago.add(nDeducible).add(montoCoaseguroPaciente);

        LiquidacionSeguroDto result = LiquidacionSeguroDto.builder()
                .totalCuenta(nTotal)
                .copago(nCopago)
                .deducible(nDeducible)
                .subtotalGravable(subtotalGravable)
                .montoCoaseguroPaciente(montoCoaseguroPaciente)
                .montoPagoSeguro(montoPagoSeguro)
                .totalPaciente(totalPaciente)
                .build();

        log.debug("Cálculo finalizado: {}", result);
        return result;
    }
}

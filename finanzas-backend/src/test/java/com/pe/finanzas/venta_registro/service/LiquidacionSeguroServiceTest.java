package com.pe.finanzas.venta_registro.service;

import com.pe.finanzas.venta_registro.dto.LiquidacionSeguroDto;
import com.pe.finanzas.venta_registro.service.impl.LiquidacionSeguroServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LiquidacionSeguroServiceTest {

    private LiquidacionSeguroService liquidacionSeguroService;

    @Mock
    private CalculoVentaService calculoVentaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        liquidacionSeguroService = new LiquidacionSeguroServiceImpl(calculoVentaService);
        
        // Mock normalizarDecimales to return the same value with 4 scale
        when(calculoVentaService.normalizarDecimales(any())).thenAnswer(invocation -> {
            BigDecimal val = invocation.getArgument(0);
            return val == null ? BigDecimal.ZERO.setScale(4) : val.setScale(4);
        });
    }

    @Test
    void testCalculoLiquidacionStandard() {
        // GIVEN
        // TOTAL CUENTA: 1000
        // COPAGO: 30
        // DEDUCIBLE: 200
        // COASEGURO PACIENTE (20%): 154
        // PAGO SEGURO (80%): 616
        
        BigDecimal totalCuenta = new BigDecimal("1000.00");
        BigDecimal copago = new BigDecimal("30.00");
        BigDecimal deducible = new BigDecimal("200.00");
        BigDecimal porcentajeCoaseguro = new BigDecimal("20.00");

        // WHEN
        LiquidacionSeguroDto result = liquidacionSeguroService.calcular(totalCuenta, copago, deducible, porcentajeCoaseguro);

        // THEN
        // Subtotal = 1000 - 30 = 970
        // SubtotalGravable = 970 - 200 = 770
        // Coaseguro Paciente = 770 * 0.20 = 154
        // Pago Seguro = 770 - 154 = 616
        // Total Paciente = 30 + 200 + 154 = 384
        
        assertEquals(0, new BigDecimal("770.0000").compareTo(result.getSubtotalGravable()));
        assertEquals(0, new BigDecimal("154.0000").compareTo(result.getMontoCoaseguroPaciente()));
        assertEquals(0, new BigDecimal("616.0000").compareTo(result.getMontoPagoSeguro()));
        assertEquals(0, new BigDecimal("384.0000").compareTo(result.getTotalPaciente()));
    }
}

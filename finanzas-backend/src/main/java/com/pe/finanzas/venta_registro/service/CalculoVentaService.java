package com.pe.finanzas.venta_registro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio de cálculo para ventas
 * 
 * CRÍTICO: Todos los cálculos financieros se realizan con 4 decimales
 * usando BigDecimal para garantizar precisión exacta
 * 
 * Tasa de IGV estándar: 18%
 */
@Service
@Slf4j
public class CalculoVentaService {

    // Constantes para cálculos
    private static final int ESCALA_DECIMALES = 4;
    private static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_UP;
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18"); // 18%
    private static final BigDecimal CIEN = new BigDecimal("100");

    /**
     * Calcula el monto de descuento aplicando un porcentaje sobre un precio base
     * 
     * @param precioBase          Precio base del servicio
     * @param porcentajeDescuento Porcentaje de descuento (ej: 15.00 para 15%)
     * @return Monto del descuento con 4 decimales
     */
    public BigDecimal calcularDescuento(BigDecimal precioBase, BigDecimal porcentajeDescuento) {
        validarNoNulo(precioBase, "Precio base");
        validarNoNulo(porcentajeDescuento, "Porcentaje de descuento");
        validarNoNegativo(precioBase, "Precio base");
        validarNoNegativo(porcentajeDescuento, "Porcentaje de descuento");

        if (porcentajeDescuento.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("El porcentaje de descuento no puede ser mayor a 100%");
        }

        // Fórmula: descuento = precioBase * (porcentajeDescuento / 100)
        BigDecimal descuento = precioBase
                .multiply(porcentajeDescuento)
                .divide(CIEN, ESCALA_DECIMALES, MODO_REDONDEO);

        log.debug("Descuento calculado: {} ({}% de {})", descuento, porcentajeDescuento, precioBase);
        return descuento;
    }

    /**
     * Calcula el IGV sobre una base imponible
     * 
     * @param baseImponible Base imponible (valor después de descuentos)
     * @return IGV calculado con 4 decimales
     */
    public BigDecimal calcularIGV(BigDecimal baseImponible) {
        validarNoNulo(baseImponible, "Base imponible");
        validarNoNegativo(baseImponible, "Base imponible");

        // Fórmula: IGV = baseImponible * 0.18
        BigDecimal igv = baseImponible
                .multiply(TASA_IGV)
                .setScale(ESCALA_DECIMALES, MODO_REDONDEO);

        log.debug("IGV calculado: {} (18% de {})", igv, baseImponible);
        return igv;
    }

    /**
     * Calcula la base imponible restando descuentos del valor de venta
     * 
     * @param valorVenta Valor de venta original
     * @param descuento  Descuento aplicado
     * @return Base imponible con 4 decimales
     */
    public BigDecimal calcularBaseImponible(BigDecimal valorVenta, BigDecimal descuento) {
        validarNoNulo(valorVenta, "Valor de venta");
        validarNoNulo(descuento, "Descuento");
        validarNoNegativo(valorVenta, "Valor de venta");
        validarNoNegativo(descuento, "Descuento");

        if (descuento.compareTo(valorVenta) > 0) {
            throw new IllegalArgumentException("El descuento no puede ser mayor al valor de venta");
        }

        // Fórmula: baseImponible = valorVenta - descuento
        BigDecimal baseImponible = valorVenta
                .subtract(descuento)
                .setScale(ESCALA_DECIMALES, MODO_REDONDEO);

        log.debug("Base imponible calculada: {} ({} - {})", baseImponible, valorVenta, descuento);
        return baseImponible;
    }

    /**
     * Calcula el total final sumando base imponible + IGV
     * 
     * @param baseImponible Base imponible
     * @param igv           IGV calculado
     * @return Total con 4 decimales
     */
    public BigDecimal calcularTotal(BigDecimal baseImponible, BigDecimal igv) {
        validarNoNulo(baseImponible, "Base imponible");
        validarNoNulo(igv, "IGV");
        validarNoNegativo(baseImponible, "Base imponible");
        validarNoNegativo(igv, "IGV");

        // Fórmula: total = baseImponible + igv
        BigDecimal total = baseImponible
                .add(igv)
                .setScale(ESCALA_DECIMALES, MODO_REDONDEO);

        log.debug("Total calculado: {} ({} + {})", total, baseImponible, igv);
        return total;
    }

    /**
     * Calcula el precio después de aplicar descuento
     * 
     * @param precioBase          Precio base
     * @param porcentajeDescuento Porcentaje de descuento
     * @return Precio con descuento aplicado (4 decimales)
     */
    public BigDecimal calcularPrecioConDescuento(BigDecimal precioBase, BigDecimal porcentajeDescuento) {
        BigDecimal descuento = calcularDescuento(precioBase, porcentajeDescuento);
        return precioBase.subtract(descuento).setScale(ESCALA_DECIMALES, MODO_REDONDEO);
    }

    /**
     * Calcula el valor de venta multiplicando precio unitario por cantidad
     * 
     * @param precioUnitario Precio unitario del servicio
     * @param cantidad       Cantidad de servicios
     * @return Valor de venta con 4 decimales
     */
    public BigDecimal calcularValorVenta(BigDecimal precioUnitario, BigDecimal cantidad) {
        validarNoNulo(precioUnitario, "Precio unitario");
        validarNoNulo(cantidad, "Cantidad");
        validarNoNegativo(precioUnitario, "Precio unitario");
        validarNoNegativo(cantidad, "Cantidad");

        BigDecimal valorVenta = precioUnitario
                .multiply(cantidad)
                .setScale(ESCALA_DECIMALES, MODO_REDONDEO);

        log.debug("Valor de venta calculado: {} ({} x {})", valorVenta, precioUnitario, cantidad);
        return valorVenta;
    }

    /**
     * Normaliza un BigDecimal a 4 decimales
     */
    public BigDecimal normalizarDecimales(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO.setScale(ESCALA_DECIMALES, MODO_REDONDEO);
        }
        return valor.setScale(ESCALA_DECIMALES, MODO_REDONDEO);
    }

    // Métodos de validación
    private void validarNoNulo(BigDecimal valor, String nombreCampo) {
        if (valor == null) {
            throw new IllegalArgumentException(nombreCampo + " no puede ser nulo");
        }
    }

    private void validarNoNegativo(BigDecimal valor, String nombreCampo) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(nombreCampo + " no puede ser negativo");
        }
    }
}

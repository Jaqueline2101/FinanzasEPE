package com.pe.finanzas.nota_credito.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NotaCreditoRequest {

    /** ID de la venta (Boleta o Factura) que se quiere referenciar */
    @NotNull(message = "El ID de la venta es obligatorio")
    private Long idVentaRef;

    /** Sucursal donde se emite */
    @NotNull(message = "El ID de sucursal es obligatorio")
    private Long idSucursal;

    /** Usuario que emite la NC */
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;

    /**
     * Código de motivo SUNAT.
     * Valores aceptados: 01, 02, 03, 04, 05, 06, 07, 13
     */
    @NotBlank(message = "El código de motivo es obligatorio")
    @Pattern(regexp = "^(01|02|03|04|05|06|07|13)$", message = "Código de motivo inválido. Use: 01,02,03,04,05,06,07,13")
    private String motivoCodigo;

    /**
     * Monto de la NC — debe ser > 0 y <= monto original.
     * El servicio valida el límite superior.
     */
    @NotNull(message = "El monto de la NC es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoNc;

    /** Detalle adicional (opcional pero recomendado para auditoría) */
    @Size(max = 500, message = "El detalle no puede exceder 500 caracteres")
    private String detalle;
}

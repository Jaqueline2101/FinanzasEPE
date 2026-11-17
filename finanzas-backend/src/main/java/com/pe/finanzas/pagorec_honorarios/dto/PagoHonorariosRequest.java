package com.pe.finanzas.pagorec_honorarios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoHonorariosRequest {

    @NotBlank(message = "El tipo de pago es obligatorio")
    private String tipoPago;

    private String numeroRecibo;
    private String serieComprobante;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEmision;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;

    private String periodoMes;
    private Integer periodoAnio;

    @NotBlank(message = "El concepto del pago es obligatorio")
    private String conceptoPago;

    private String estadoPago;
    private String estadoValidacion;
    private String descripcionAdicional;

    // Beneficiario
    @NotBlank(message = "El tipo de beneficiario es obligatorio")
    private String tipoBeneficiario;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "El nombre o razón social es obligatorio")
    private String nombreRazonSocial;

    private String areaServicio;
    private String centroCosto;

    // Montos
    @NotNull(message = "El monto bruto es obligatorio")
    private BigDecimal montoBruto;

    private BigDecimal descuento;
    private BigDecimal retencion;
    private BigDecimal aporteAfpOnp;
    private BigDecimal otrosDescuentos;
    private BigDecimal totalRetenciones;
    private BigDecimal montoNeto;

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;

    private BigDecimal tipoCambio;

    // Forma de Pago
    private String medioPago;
    private String bancoDestino;
    private String numeroCuenta;
    private String numeroOperacion;
    private String codigoTransferencia;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaTransferencia;

    private String observacionPago;

    // Contable
    private String cuentaContable;
    private String subcuenta;
    private String tipoGasto;
    private String numeroAsiento;
    private String glosaContable;
    private String periodoContable;

    // Detalle de Liquidación
    @Builder.Default
    private List<PagoConceptoDetalleDto> detalles = new ArrayList<>();
}

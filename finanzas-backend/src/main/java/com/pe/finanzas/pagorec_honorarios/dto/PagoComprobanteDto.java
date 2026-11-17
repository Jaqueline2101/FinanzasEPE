package com.pe.finanzas.pagorec_honorarios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoComprobanteDto {

    private Long idPago;
    private String tipoPago;
    private String numeroRecibo;
    private String serieComprobante;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEmision;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;

    private String periodoMes;
    private Integer periodoAnio;
    private String conceptoPago;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaProceso;
    private String descripcionAdicional;
    private String estadoPago;

    private String tipoBeneficiario;
    private String numeroDocumento;
    private String nombreRazonSocial;
    private String areaServicio;
    private String centroCosto;
    private String cargoTrabajador;
    private String areaTrabajador;
    private String tipoContrato;
    private String estadoLaboral;

    private String sistemaPensionario;
    private String afpNombre;
    private String tipoComision;
    private BigDecimal porcentajeComision;
    private BigDecimal porcentajeSeguro;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal montoBruto;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal descuento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal retencion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal aporteAfpOnp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal otrosDescuentos;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal totalRetenciones;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal montoNeto;

    private String moneda;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0000")
    private BigDecimal tipoCambio;

    private String medioPago;
    private String bancoDestino;
    private String numeroCuenta;
    private String numeroOperacion;
    private String codigoTransferencia;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaTransferencia;

    private String observacionPago;
    private String cuentaContable;
    private String subcuenta;
    private String tipoGasto;
    private String numeroAsiento;
    private String glosaContable;
    private String periodoContable;

    private String usuarioRegistro;
    private String usuarioAprobacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaAprobacion;

    private String estadoValidacion;
    private String motivoAnulacion;

    @Builder.Default
    private List<PagoConceptoDetalleDto> detalles = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

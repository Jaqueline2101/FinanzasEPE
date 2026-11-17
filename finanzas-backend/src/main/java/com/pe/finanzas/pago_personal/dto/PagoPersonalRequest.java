package com.pe.finanzas.pago_personal.dto;

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
public class PagoPersonalRequest {

    @NotBlank(message = "El tipo de pago es obligatorio")
    private String tipoPago; // Sueldo, Adelanto, Bono, Comisión, Reembolso, Otro

    private String periodoMes;
    private Integer periodoAnio;

    @NotNull(message = "La fecha de pago es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaProceso;

    @NotBlank(message = "El concepto del pago es obligatorio")
    private String conceptoPago;

    private String estadoPago; // Pendiente, Pagado, Anulado
    private String estadoValidacion;
    private String observacionPago;

    // Datos del Trabajador
    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombreRazonSocial;

    private String cargoTrabajador;
    private String areaTrabajador;
    private String centroCosto;
    private String tipoContrato;
    private String estadoLaboral;

    private String sistemaPensionario; // ONP, AFP
    private String afpNombre; // Habitat, Integra, Prima, Profuturo
    private String tipoComision; // Flujo, Mixta
    private BigDecimal porcentajeComision;
    private BigDecimal porcentajeSeguro;

    // Totales resumidos
    private BigDecimal montoBruto;
    private BigDecimal totalDescuentos;
    private BigDecimal montoNeto;

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;
    private BigDecimal tipoCambio;

    // Forma de Pago
    private String medioPago; // Depósito, Efectivo
    private String bancoDestino;
    private String numeroCuenta;
    private String numeroOperacion;
    private String codigoTransferencia;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeposito;

    // Información Contable
    private String cuentaContable;
    private String subcuenta;
    private String numeroAsiento;
    private String glosaContable;
    private String periodoContable;

    // Detalle de Ingresos y Descuentos
    @Builder.Default
    private List<PagoPersonalConceptoRequest> detalles = new ArrayList<>();

    // Cuenta bancaria origen del pago
    private Long idCuenta;
}

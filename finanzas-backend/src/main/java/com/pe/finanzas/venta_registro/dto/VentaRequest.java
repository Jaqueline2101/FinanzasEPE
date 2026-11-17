package com.pe.finanzas.venta_registro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class VentaRequest {

    // Datos de cabecera
    private Long idOrden; // Opcional: si es pago de orden médica
    @NotNull(message = "La sucursal es obligatoria")
    private Long idSucursal;

    @NotNull(message = "El cliente es obligatorio")
    private Long idCtacte;

    @NotNull(message = "El personal es obligatorio")
    private Long idPersonal;

    @NotNull(message = "El almacén es obligatorio")
    private Long idAlmacen;

    @NotNull(message = "La fecha es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 20)
    private String tipoDoc; // BOLETA, FACTURA, TICKET, etc. (Maps to tipo_documento.codigo)

    @NotBlank(message = "La serie es obligatoria")
    @Size(max = 10)
    private String serie;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20)
    private String numdoc;

    // Datos opcionales del cliente
    private String nombrePac;
    private String nroDni;
    private String tipoDni;
    private String ruc;
    private String razon;
    private String direcRuc;

    // Tipo de atención médica
    // Valores válidos: AMBULATORIO, HOSPITALARIO, EMERGENCIA
    private String tipoAtencion;

    // Método de pago (Legacy o principal)
    private String metodoPago;
    private String refMetodoPago;
    private BigDecimal vuelto;

    // Campos para Pagos Divididos (Sin nueva tabla)
    private BigDecimal montoEfectivo;
    private BigDecimal montoTarjeta;
    private BigDecimal montoTransferencia;
    private String refTarjeta;
    private String refTransferencia;

    // Campos para sistema de descuentos
    private String tipoPac; // Tipo de paciente (para descuentos automáticos)
    private Long idPlan; // Plan de convenio
    private Long idPaquete; // Si la venta es de un paquete completo
    private String codigoPromocional; // Código de campaña promocional
    private Long idAutorizacion; // ID de autorización aprobada (para descuentos >30%)
    private Long idAutorizacionDescuento; // ID de autorización de descuento
    private Boolean aplicarDescuentoAutomatico; // Si debe aplicar descuentos automáticamente

    // Campos de Seguro Médico (Liquidación)
    private BigDecimal copago;
    private BigDecimal deducible;

    // Items de la venta
    @NotEmpty(message = "Debe incluir al menos un item en la venta")
    @Valid
    @Builder.Default
    private List<VentaItemRequest> items = new ArrayList<>();

    /**
     * DTO para cada item de la venta
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VentaItemRequest {

        @NotNull(message = "El ID del servicio es obligatorio")
        private Long idArticulo;

        private String tipoArticulo; // 'SERVICIO' o 'PAQUETE'

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 200)
        private String glosa;

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        private BigDecimal cantidad;

        private String tipoDescuento;

        @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor a 100%")
        private BigDecimal porcentajeDescuento;

        @DecimalMin(value = "0.00", message = "La cobertura no puede ser negativa")
        @DecimalMax(value = "100.00", message = "La cobertura no puede ser mayor a 100%")
        private BigDecimal porcentajeCobertura;

        @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
        private BigDecimal precioUnitario; // Precio final enviado por el frontend (respeta ofertas)

        private Integer idMedicoSer; // Médico que atiende
        private String consultorio;
    }
}

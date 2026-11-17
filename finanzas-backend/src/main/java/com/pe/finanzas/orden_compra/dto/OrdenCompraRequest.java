package com.pe.finanzas.orden_compra.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraRequest {

    private String numeroOrden;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEmision;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEstimadaEntrega;

    private String tipoCompra;
    private String centroCosto;
    private String areaSolicitante;
    private String responsableCompra;
    private String moneda;
    private BigDecimal tipoCambio;
    private String estado;
    private String observaciones;

    // Adelanto
    private Boolean permiteAdelanto;
    private String tipoAdelanto;
    private BigDecimal porcentajeAdelanto;
    private BigDecimal montoAdelanto;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAdelanto;

    // Proveedor
    private Long idProveedor;
    private String condicionPago;
    private Integer plazoDias;

    // Totales
    private BigDecimal subtotal;
    private BigDecimal totalDescuento;
    private BigDecimal totalIgv;
    private BigDecimal totalGeneral;

    // Logística
    private String lugarEntrega;
    private String direccionEntrega;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEntregaAcordada;
    private String responsableRecepcion;
    private String observacionesLogisticas;

    // Control Interno
    private String usuarioRegistro;
    private String numCotizacionRelacionada;
    private String numRequerimientoInterno;

    private List<OrdenCompraDetalleDto> detalles;
}

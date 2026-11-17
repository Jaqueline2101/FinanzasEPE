package com.pe.finanzas.venta_registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagarOrdenRequest {
    private String metodoPago;
    private String refMetodoPago;
    private String tipoDoc;
    private String serie;
    private String numdoc;

    // Campos para pagos múltiples (NUEVO)
    private java.math.BigDecimal montoEfectivo;
    private java.math.BigDecimal montoTarjeta;
    private java.math.BigDecimal montoTransferencia;
    private String refTarjeta;
    private String refTransferencia;
    private java.math.BigDecimal vuelto;
    private String nroDni;
    private String tipoDni;
    private String tipoAtencion;

    // Datos opcionales si es factura:
    private String ruc;
    private String razon;
    private String direcRuc;

    private Long idSucursal;
    private Long idPersonal;

    // Items seleccionados (opcional, si no se envía se pagan todos los de la orden)
    private java.util.List<VentaRequest.VentaItemRequest> items;

    private Long idAutorizacionDescuento;
}


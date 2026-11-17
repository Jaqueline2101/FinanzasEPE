package com.pe.finanzas.rep_compras.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepComprasResponseDTO {
    private Long idCompra;
    private java.time.LocalDate fechaEmision;
    private java.time.LocalDateTime fechaRegistro;
    private String tipoComprobante;
    private String serie;
    private String correlativo;
    private String proveedor;
    private String rucProveedor;
    private String moneda;
    private BigDecimal valorVentaGravado;
    private BigDecimal valorVentaInafecto;
    private BigDecimal igv;
    private BigDecimal total;
    private Integer estado;
}

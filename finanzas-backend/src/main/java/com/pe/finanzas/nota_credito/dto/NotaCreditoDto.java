package com.pe.finanzas.nota_credito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaCreditoDto {

    private Long idNc;

    private String serieNc;
    private String numdocNc;

    /** Número completo de la NC, ej: NC-B 00000001 */
    private String numeroNc;

    private Long idVentaRef;
    private String comprobanteRef;
    private String tipoComprobante;
    private String paciente;

    private String motivoCodigo;
    private String motivoDescripcion;
    private String detalle;

    private BigDecimal montoNc;
    private BigDecimal montoOriginal;

    private Long idSucursal;
    private Long idUsuario;
    private String nombreUsuario;

    /** EMITIDA | ANULADA */
    private String estado;

    private LocalDate fechaEmision;
    private LocalDateTime fechaCreacion;
}

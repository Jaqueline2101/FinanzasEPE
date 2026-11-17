package com.pe.finanzas.motor_precios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoConvenioDto {

    private Long idPlanDescuento;
    private Long idServicio; // New field
    private String tipoAtencion; // AMBULATORIO, HOSPITALARIO, EMERGENCIA
    private Integer nivelServicio; // 1, 2, 3, 4

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal porcentajeDescuento;

    private String tipoDescuento; // PORCENTAJE, MONTO_FIJO
    private String estado;
}

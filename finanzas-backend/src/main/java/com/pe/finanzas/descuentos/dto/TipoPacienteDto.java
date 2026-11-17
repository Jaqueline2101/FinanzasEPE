package com.pe.finanzas.descuentos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPacienteDto {

    private Long idTipoPaciente;

    private String codigo;
    private String nombre;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal dsc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal cob;

    private Integer frq;
    private Integer estado;

    private Boolean activo;
    private Boolean tieneDescuento;
    private Boolean tieneCobertura;
    private Boolean esFrecuente;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal beneficioTotal;

    private List<TipoPacienteServicioDto> servicios;
}

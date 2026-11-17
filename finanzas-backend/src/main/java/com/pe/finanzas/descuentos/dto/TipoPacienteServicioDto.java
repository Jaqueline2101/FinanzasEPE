package com.pe.finanzas.descuentos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPacienteServicioDto {

    private Long idTipoPacienteServicio;

    private String tipoServicio;

    private String nombreServicio;

    private BigDecimal descuento;

    private BigDecimal cobertura;

    private BigDecimal beneficioTotal;

    private Integer estado;

    public String getNombreServicio() {
        if (nombreServicio != null) {
            return nombreServicio;
        }

        return switch (tipoServicio) {
            case "FARMACIA" -> "Farmacia";
            case "LABORATORIO" -> "Laboratorio";
            case "CONSULTA" -> "Consulta Médica";
            case "IMAGENES" -> "Imágenes";
            case "OTROS" -> "Otros Servicios";
            default -> tipoServicio;
        };
    }
}

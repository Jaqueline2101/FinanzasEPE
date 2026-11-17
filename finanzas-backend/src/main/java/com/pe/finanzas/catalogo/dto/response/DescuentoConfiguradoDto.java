package com.pe.finanzas.catalogo.dto.response;

import com.pe.finanzas.niveles.dto.NivelDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoConfiguradoDto {
    private Long idPlanDescuento;
    private Long idServicio;
    private CatalogoResponse servicio;

    private Integer idNivel;
    private NivelDto nivel;

    private String tipoAtencionId;
    private TipoAtencionSimpleDto tipoAtencion;

    private BigDecimal descuentoFijo;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal precioFijo;
    private String tipoDescuento;
    private String estado;
    private String codigoPlan;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoAtencionSimpleDto {
        private Long id;
        private String nombre;
        private String codigo;
    }
}

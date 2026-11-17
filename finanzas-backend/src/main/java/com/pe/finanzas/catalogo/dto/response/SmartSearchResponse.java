package com.pe.finanzas.catalogo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartSearchResponse {
    private List<SearchResultItem> results;
    private PlanMatch planMatch;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResultItem {
        private Long id;
        private String codigo;
        private String nombre;
        private String tipo; // SERVICIO, PAQUETE, PRODUCTO
        private java.math.BigDecimal precioReal;
        private java.math.BigDecimal precioFinal;
        private java.math.BigDecimal porcentajeDescuento;
        private boolean tieneDescuento;
        private boolean hasRule;
        private String nivelNombre;
        private String tipoCatalogo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanMatch {
        private Long idPlan;
        private String codigo;
        private String nombre;
        private String tipoPlan; // GENERAL, FAMILIAR, CONVENIO
    }
}

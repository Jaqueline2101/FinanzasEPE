package com.pe.finanzas.motor_precios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioRequest {

    @NotBlank(message = "El nombre del convenio es obligatorio")
    private String nombre;

    @NotNull(message = "El ID de la compañía es obligatorio")
    private Long idCompania;

    private Long idEmpresa;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    private String codigo;
    private String representante;
    private String codeps;
    private String ctacte;

    // Nuevos campos sincronizados con CredPlan
    private String poliza;
    private String controlPreex;
    private Long idPersonalUser;
    private LocalDate fechaIni;
    private String abrev;
    private Integer numCuotas;
    private Integer contador;
    private String detalle;
    private String correlativo;
    private String np;
    private String independiente;
    private String paquete;
    private String planNuevo;
    private String tipoFact;
    private Integer contador2;
    private String mostrarEmerg;
    private String segViaje;
    private String chequeo;
    private String ggss;
    private String codpro;

    // Campos de seguro
    @jakarta.validation.constraints.Min(0)
    @com.fasterxml.jackson.annotation.JsonProperty("copago")
    private java.math.BigDecimal copago;

    @jakarta.validation.constraints.Min(0)
    @com.fasterxml.jackson.annotation.JsonProperty("deducible")
    private java.math.BigDecimal deducible;

    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(100)
    @com.fasterxml.jackson.annotation.JsonProperty("porcentajeCoaseguro")
    private java.math.BigDecimal porcentajeCoaseguro;

    @jakarta.validation.constraints.Min(0)
    @com.fasterxml.jackson.annotation.JsonProperty("topeAnual")
    private java.math.BigDecimal topeAnual;
    @com.fasterxml.jackson.annotation.JsonProperty("tieneDeducible")
    private String tieneDeducible;

    private List<DescuentoRequest> descuentos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DescuentoRequest {

        @NotNull(message = "El servicio es obligatorio")
        private Long idServicio;

        private String tipoAtencion;
        private Integer nivelServicio;

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        private BigDecimal porcentajeDescuento;

        private String tipoDescuento;
        private String estado;
    }
}

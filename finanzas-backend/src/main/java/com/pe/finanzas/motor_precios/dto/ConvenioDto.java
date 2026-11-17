package com.pe.finanzas.motor_precios.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioDto {

    private Long idPlan;
    private String codigo;
    private String nombre;
    private Long idCompania;
    private String nombreEmpresa;
    private Long idEmpresa;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private String estado;
    private Boolean activo;
    private Boolean vigente;

    // Nuevos campos sincronizados con CredPlan
    private String poliza;
    private String controlPreex;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCre;

    private Long idPersonalUser;

    @JsonFormat(pattern = "yyyy-MM-dd")
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

    // =============================================
    // CAMPOS DE SEGURO: Deducible y Coaseguro
    // =============================================

    /** "1" si el plan tiene deducible activo, "0" en caso contrario */
    private String tieneDeducible;

    /** Monto deducible fijo (ej: 200.00) */
    @com.fasterxml.jackson.annotation.JsonProperty("deducible")
    private java.math.BigDecimal deducible;

    /** % que paga el paciente del monto restante tras el deducible (ej: 20.00 = 20%) */
    @com.fasterxml.jackson.annotation.JsonProperty("porcentajeCoaseguro")
    private java.math.BigDecimal porcentajeCoaseguro;

    /** Monto fijo de copago que el paciente paga por atención */
    @com.fasterxml.jackson.annotation.JsonProperty("copago")
    private java.math.BigDecimal copago;

    /** Moneto máximo anual cubierto por el seguro (null = sin tope) */
    private java.math.BigDecimal topeAnual;

    // Descuentos por tipo de atención y nivel
    private List<DescuentoConvenioDto> descuentos;
}

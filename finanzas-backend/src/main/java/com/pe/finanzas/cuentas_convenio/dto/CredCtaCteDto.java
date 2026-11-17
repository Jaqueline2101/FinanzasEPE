package com.pe.finanzas.cuentas_convenio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredCtaCteDto {
    private Long idCtacte;
    private String nroCtacte;
    private Long id; // Vínculo personal (datos_personales.id)
    private String dni;
    private Long idEmpresa;
    private Long idCompania;
    private Long idPlan;
    private String nroPoliza;
    private String codAsegurado;
    private String tipoAsegurado;
    private String parentesco;
    private String planSeguro;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String codigoPlan;
    private String nombrePlan;
    private String tipoCobertura;
    private BigDecimal porcentajeCobertura;
    private BigDecimal copago;
    private BigDecimal deducible;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private String estado;
    private Long idSucursal;
    private String observaciones;
    private String usuarioRegistro;

    // Nuevos campos sincronizados con CredPlan (Legacy 96 fields)
    private Long idEmpFact;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIni;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCierre;

    private String titular;
    private String documento;
    private Long idPersonalUser;
    private BigDecimal limite;
    private BigDecimal consumo;
    private String corteLimite;
    private String monedaLimite;
    private String tipoCta;
    private Integer idTipoAte;
    private Integer numSubctas;
    private Long idPaquete;
    private Long idEspecialidad;
    private String valesFarmacia;
    private Long idUserExp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaExp;

    private String control;
    private Long idPersonalControl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaControl;

    private String motivoControl;
    private String observacion;
    private Long idControl;
    private String nombreFact;
    private String nroPlaca;
    private String siniestro;
    private String tipoFac;
    private String motivoNoFact;
    private String punto;
    private String codAuto;
    private BigDecimal topeCarta;
    private BigDecimal cobeCarta;
    private String prefijo;
    private String estadoMi;
    private String subcta;
    private Long idCtacteOrigen;
    private String numCarne;
    private Long idAteGrupo;
    private String paquete;
    private Long idLiqUser;
    private Long idPersonalUpd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUpd;

    private Integer semanas;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaSemana;

    private String latencia;
    private String revAdm;
    private String obsEmer;
    private String mensaje;
    private Long idAteOdonto;
    private Integer idTipoAteEsp;
    private String depoTipoDoc;
    private String depoRazon;
    private String obsEstado;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime obsFecha;

    private Long obsIdPerObs;
    private Long idAteOrig;
    private String nroSoat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latenciaIni;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latenciaFin;

    private String empTemp;
    private String tipoFact;
    private String obsGarantia;
    private String otraFranq;
    private Long idPae;
    private String carencia;
    private String preexistencia;
    private String auditado;
    private String honoMedic;
    private String obsPreex;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEnvtrama;

    private BigDecimal dedHosp;
    private Long idUpd;
    private Long idTramaConcilia;
    private String tipoAfiliacion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAlta1;

    private String origen;
    private String controlEx;
    private String mixto;
    private Long idTitular;
    private String modEstado;
    private Long modIdPerUser;
    private String nroCarta;
    private String crearCta;
    private String idInterno;
    private String intMv;
    private String alojamientoConjunto;
}

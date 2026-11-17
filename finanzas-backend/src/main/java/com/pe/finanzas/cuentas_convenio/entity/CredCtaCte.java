package com.pe.finanzas.cuentas_convenio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pe.finanzas.compania.entity.Compania;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.users.entity.DatosPersonales;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cred_ctacte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredCtaCte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ctacte")
    private Long idCtacte;

    @Column(name = "nro_ctacte", unique = true, length = 50)
    private String nroCtacte;

    @ManyToOne
    @JoinColumn(name = "id", nullable = true) // Vínculo con datos_personales(id)
    private DatosPersonales paciente;

    @Column(name = "dni", length = 20)
    private String dni;

    @ManyToOne
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @JoinColumn(name = "id_compania", nullable = false)
    private Compania compania;

    @Column(name = "id_emp_fact")
    private Long idEmpFact;

    @Column(name = "fecha_ini")
    private LocalDate fechaIni;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "titular", length = 200)
    private String titular;

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "id_personal_user")
    private Long idPersonalUser;

    @Column(name = "id_plan")
    private Long idPlan;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "limite", precision = 12, scale = 2)
    private BigDecimal limite;

    @Column(name = "consumo", precision = 12, scale = 2)
    private BigDecimal consumo;

    @Column(name = "corte_limite", length = 1)
    private String corteLimite;

    @Column(name = "moneda_limite", length = 5)
    private String monedaLimite;

    @Column(name = "tipo_cta", length = 10)
    private String tipoCta;

    @Column(name = "id_tipo_ate")
    private Integer idTipoAte;

    @Column(name = "num_subctas")
    private Integer numSubctas;

    @Column(name = "id_paquete")
    private Long idPaquete;

    @Column(name = "id_especialidad")
    private Long idEspecialidad;

    @Column(name = "vales_farmacia", length = 1)
    private String valesFarmacia;

    @Column(name = "id_user_exp")
    private Long idUserExp;

    @Column(name = "fecha_exp")
    private LocalDateTime fechaExp;

    @Column(name = "control", length = 1)
    private String control;

    @Column(name = "id_personal_control")
    private Long idPersonalControl;

    @Column(name = "fecha_control")
    private LocalDateTime fechaControl;

    @Column(name = "motivo_control", length = 500)
    private String motivoControl;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "id_control")
    private Long idControl;

    @Column(name = "nombre_fact", length = 200)
    private String nombreFact;

    @Column(name = "nro_placa", length = 20)
    private String nroPlaca;

    @Column(name = "siniestro", length = 50)
    private String siniestro;

    @Column(name = "tipo_fac", length = 10)
    private String tipoFac;

    @Column(name = "motivo_no_fact", length = 500)
    private String motivoNoFact;

    @Column(name = "punto", length = 50)
    private String punto;

    @Column(name = "cod_auto", length = 50)
    private String codAuto;

    @Column(name = "tope_carta", precision = 12, scale = 2)
    private BigDecimal topeCarta;

    @Column(name = "cobe_carta", precision = 5, scale = 2)
    private BigDecimal cobeCarta;

    @Column(name = "prefijo", length = 10)
    private String prefijo;

    @Column(name = "estado_mi", length = 10)
    private String estadoMi;

    @Column(name = "subcta", length = 20)
    private String subcta;

    @Column(name = "id_ctacte_origen")
    private Long idCtacteOrigen;

    @Column(name = "num_carne", length = 50)
    private String numCarne;

    @Column(name = "id_ate_grupo")
    private Long idAteGrupo;

    @Column(name = "paquete", length = 1)
    private String paquete;

    @Column(name = "id_liq_user")
    private Long idLiqUser;

    @Column(name = "id_personal_upd")
    private Long idPersonalUpd;

    @Column(name = "fecha_upd")
    private LocalDateTime fechaUpd;

    @Column(name = "semanas")
    private Integer semanas;

    @Column(name = "fecha_semana")
    private LocalDate fechaSemana;

    @Column(name = "latencia", length = 1)
    private String latencia;

    @Column(name = "rev_adm", length = 1)
    private String revAdm;

    @Column(name = "obs_emer", columnDefinition = "TEXT")
    private String obsEmer;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "id_ate_odonto")
    private Long idAteOdonto;

    @Column(name = "id_tipo_ate_esp")
    private Integer idTipoAteEsp;

    @Column(name = "depo_tipo_doc", length = 10)
    private String depoTipoDoc;

    @Column(name = "depo_razon", length = 200)
    private String depoRazon;

    @Column(name = "obs_estado", length = 50)
    private String obsEstado;

    @Column(name = "obs_fecha")
    private LocalDateTime obsFecha;

    @Column(name = "obs_id_per_obs")
    private Long obsIdPerObs;

    @Column(name = "id_ate_orig")
    private Long idAteOrig;

    @Column(name = "nro_soat", length = 50)
    private String nroSoat;

    @Column(name = "latencia_ini")
    private LocalDate latenciaIni;

    @Column(name = "latencia_fin")
    private LocalDate latenciaFin;

    @Column(name = "emp_temp", length = 1)
    private String empTemp;

    @Column(name = "tipo_fact", length = 10)
    private String tipoFact;

    @Column(name = "obs_garantia", columnDefinition = "TEXT")
    private String obsGarantia;

    @Column(name = "otra_franq", length = 1)
    private String otraFranq;

    @Column(name = "id_pae")
    private Long idPae;

    @Column(name = "carencia", length = 1)
    private String carencia;

    @Column(name = "preexistencia", length = 1)
    private String preexistencia;

    @Column(name = "auditado", length = 1)
    private String auditado;

    @Column(name = "hono_medic", length = 1)
    private String honoMedic;

    @Column(name = "obs_preex", columnDefinition = "TEXT")
    private String obsPreex;

    @Column(name = "fecha_envtrama")
    private LocalDate fechaEnvtrama;

    @Column(name = "ded_hosp", precision = 12, scale = 2)
    private BigDecimal dedHosp;

    @Column(name = "id_upd")
    private Long idUpd;

    @Column(name = "id_trama_concilia")
    private Long idTramaConcilia;

    @Column(name = "tipo_afiliacion", length = 10)
    private String tipoAfiliacion;

    @Column(name = "fecha_int")
    private LocalDate fechaInt;

    @Column(name = "fecha_alta_1")
    private LocalDate fechaAlta1;

    @Column(name = "origen", length = 10)
    private String origen;

    @Column(name = "control_ex", length = 1)
    private String controlEx;

    @Column(name = "mixto", length = 1)
    private String mixto;

    @Column(name = "id_titular")
    private Long idTitular;

    @Column(name = "mod_estado", length = 20)
    private String modEstado;

    @Column(name = "mod_id_per_user")
    private Long modIdPerUser;

    @Column(name = "nro_carta", length = 50)
    private String nroCarta;

    @Column(name = "crear_cta", length = 1)
    private String crearCta;

    @Column(name = "id_interno", length = 50)
    private String idInterno;

    @Column(name = "int_mv", length = 50)
    private String intMv;

    @Column(name = "alojamiento_conjunto", length = 1)
    private String alojamientoConjunto;

    // Campos de compatibilidad con interfaz actual
    @Column(name = "tipo_asegurado", length = 50)
    private String tipoAsegurado;

    @Column(name = "parentesco", length = 50)
    private String parentesco;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "codigo_plan", length = 50)
    private String codigoPlan;

    @Column(name = "nombre_plan", length = 150)
    private String nombrePlan;

    @Column(name = "tipo_cobertura", length = 50)
    private String tipoCobertura;

    @Column(name = "porcentaje_cobertura", precision = 5, scale = 2)
    private BigDecimal porcentajeCobertura;

    @Column(name = "copago", precision = 12, scale = 2)
    private BigDecimal copago;

    @Column(name = "deducible", precision = 12, scale = 2)
    private BigDecimal deducible;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio; // Columna existente en BD

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "usuario_registro")
    private String usuarioRegistro;

    @Column(name = "nro_poliza", length = 50)
    private String nroPoliza;

    @Column(name = "cod_asegurado", length = 50)
    private String codAsegurado;

    @Column(name = "plan_seguro", length = 100)
    private String planSeguro;

    @ManyToOne
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "ACTIVO";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

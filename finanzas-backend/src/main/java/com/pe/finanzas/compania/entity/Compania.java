package com.pe.finanzas.compania.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.pe.finanzas.users.entity.DatosPersonales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "datos_companias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIdentityInfo(generator = com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator.class, property = "id")
@org.hibernate.annotations.SQLDelete(sql = "UPDATE datos_companias SET deleted = true WHERE id = ?")
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Compania {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private DatosPersonales personaBase;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "codigo_iafa", length = 20)
    private String codigoIafa;

    @Column(name = "cod_gales", length = 10)
    private String codGales;

    @Column(name = "codeps", length = 2)
    private String codEps;

    @Column(name = "codctr", length = 2)
    private String codCtr;

    @Column(name = "codsec", length = 3)
    private String codSec;

    @Column(name = "id_tipo_cia", length = 2)
    private String idTipoCia;

    @Column(name = "cod_plan", length = 10)
    private String codPlan;

    @Column(name = "representante", length = 80)
    private String representante;

    @Column(name = "auditor", length = 30)
    private String auditor;

    @Column(name = "liquidador", length = 30)
    private String liquidador;

    @Column(name = "financiero", length = 100)
    private String financieroNombre;

    @Column(name = "id_personal_user", length = 50)
    private String idPersonalUser;

    @Column(name = "req_amb", length = 70)
    private String reqAmb;

    @Column(name = "req_hos", length = 70)
    private String reqHos;

    @Column(name = "req_eme", length = 70)
    private String reqEme;

    @Column(name = "req_acc", length = 70)
    private String reqAcc;

    @Column(name = "days_waiting")
    private Long daysWaiting;

    @Column(name = "ctacte", length = 8)
    private String ctaCte;

    @Column(name = "pluctc", length = 8)
    private String pluctc;

    @Column(name = "dias_plazo", length = 3)
    private String diasPlazo;

    @Column(name = "tipo_tarif", length = 1)
    private String tipoTarif;

    @Column(name = "trabaja_cpm")
    private BigDecimal trabajaCpm;

    @Column(name = "importe_cpm")
    private Long importeCpm;

    @Column(name = "afecta_recargo_especial", length = 1)
    private String afectaRecargoEspecial;

    @Column(name = "estado", length = 1)
    @Builder.Default
    private Integer estado = 1;
    
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "nom_corto", length = 50)
    private String nomCorto;

    @Column(name = "tipo_pac", length = 1)
    private String tipoPac;

    @Column(name = "chana", length = 10)
    private String chana;

    @Column(name = "maneja_cob", length = 1)
    private String manejaCob;

    @Column(name = "tiptra", length = 1)
    private String tiptra;

    @Column(name = "borrar", length = 2)
    private String borrar;

    @Column(name = "mostrar", length = 1)
    private String mostrar;

    @Column(name = "mostrar_direc", length = 1)
    private String mostrarDirec;

    @Column(name = "mostrar_dias_plazo", length = 1)
    private String mostrarDiasPlazo;

    @Column(name = "razsol", length = 200)
    private String razSol;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "facturar", length = 2)
    private String facturar;

    @Column(name = "contador")
    private Integer contador;

    @Column(name = "clasif_rep_cobranzas")
    private Integer clasifRepCobranzas;
}

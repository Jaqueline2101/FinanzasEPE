package com.pe.finanzas.paciente.entity;

import com.pe.finanzas.users.entity.DatosPersonales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "datos_paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_datos_personales", referencedColumnName = "id", nullable = false, unique = true)
    private DatosPersonales datosPersonales;

    @Column(name = "nro_historia_clinica", length = 20)
    private String nroHistoriaClinica;

    @Column(name = "grupo_sanguineo", length = 10)
    private String grupoSanguineo;

    @Column(name = "alergias", columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "antecedentes_familiares", columnDefinition = "TEXT")
    private String antecedentesFamiliares;

    @Column(name = "es_asegurado", nullable = false)
    private Boolean esAsegurado;

    @Column(name = "email_familiar", length = 100)
    private String emailFamiliar;

    @Column(name = "whatsapp_familiar", length = 20)
    private String whatsappFamiliar;
}

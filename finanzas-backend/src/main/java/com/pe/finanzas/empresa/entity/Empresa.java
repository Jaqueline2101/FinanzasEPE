package com.pe.finanzas.empresa.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "datos_empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(name = "representante")
    private String representante;

    @Column(name = "auditor")
    private String auditor;

    @Column(name = "liquidador")
    private String liquidador;

    @Column(name = "financiero")
    private String financiero;

    @Column(name = "id_personal_user")
    private String idPersonalUser;

    @Column(name = "fax")
    private String fax;

    @Column(name = "empresas_chana", length = 1)
    private String empresasChana;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "abrev", length = 200)
    private String abrev;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "estado", length = 1)
    private Integer estado;

    @Column(name = "fecha_cre")
    private LocalDate fechaCre;

    @Column(name = "ctacte", length = 50)
    private String ctacte;
}

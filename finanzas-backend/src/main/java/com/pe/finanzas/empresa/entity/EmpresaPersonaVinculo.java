package com.pe.finanzas.empresa.entity;

import java.time.LocalDate;

import com.pe.finanzas.users.entity.DatosPersonales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresa_persona_vinculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaPersonaVinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_empresa", referencedColumnName = "id_empresa", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "id_personal", referencedColumnName = "id", nullable = false)
    private DatosPersonales personal;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "estado")
    private Integer estado;
}

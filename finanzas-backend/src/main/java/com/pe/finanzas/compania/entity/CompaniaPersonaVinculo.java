package com.pe.finanzas.compania.entity;

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
@Table(name = "compania_persona_vinculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompaniaPersonaVinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_compania", referencedColumnName = "id", nullable = false)
    private Compania compania;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", nullable = false)
    private DatosPersonales persona;

    @Column(name = "nro_poliza")
    private String nroPoliza;

    @Column(name = "tipo_afiliacion")
    private String tipoAfiliacion;

    @Column(name = "parentesco")
    private String parentesco;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
}

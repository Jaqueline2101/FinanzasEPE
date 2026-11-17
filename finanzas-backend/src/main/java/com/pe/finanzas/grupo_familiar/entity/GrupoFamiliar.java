package com.pe.finanzas.grupo_familiar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "grupo_familiar", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_titular", "id_familiar" })
}, indexes = {
        @Index(name = "idx_grupo_familiar_titular", columnList = "id_titular"),
        @Index(name = "idx_grupo_familiar_familiar", columnList = "id_familiar"),
        @Index(name = "idx_grupo_familiar_estado", columnList = "estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_titular", nullable = false, length = 20)
    private String idTitular; // DNI del trabajador titular

    @Column(name = "id_familiar", nullable = false, length = 20)
    private String idFamiliar; // DNI del familiar

    @Column(name = "parentesco", nullable = false, length = 20)
    private String parentesco; // CONYUGE, HIJO, HIJA, PADRE, MADRE, HERMANO, HERMANA

    @Column(name = "estado", nullable = false, length = 1)
    @Builder.Default
    private String estado = "1"; // "1" = activo, "0" = inactivo

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(name = "fecha_inactivacion")
    private LocalDate fechaInactivacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}

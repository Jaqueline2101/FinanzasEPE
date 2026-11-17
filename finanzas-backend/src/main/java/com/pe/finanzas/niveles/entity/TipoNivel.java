package com.pe.finanzas.niveles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "niveles_tipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // Ej: GERENCIA

    @Column(nullable = false, length = 100)
    private String nombre; // Ej: Gerencia

    @Column(length = 50)
    private String icono; // Ej: building

    @Column(name = "orden")
    private Integer orden;

    @Builder.Default
    @Column(name = "estado")
    private Integer estado = 1; // 1 = Activo, 0 = Inactivo
}

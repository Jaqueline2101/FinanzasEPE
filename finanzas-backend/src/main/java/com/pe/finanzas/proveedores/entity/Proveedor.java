package com.pe.finanzas.proveedores.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_doc_ident", nullable = false, length = 50)
    private String tipoDocIdent;

    @Column(name = "num_doc_ident", nullable = false, unique = true, length = 20)
    private String numDocIdent;

    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 255)
    private String nombreComercial;

    @Column(length = 1000)
    private String direccion;

    @Column(length = 1000)
    private String sedes;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String telefono;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String distrito;

    @Column(length = 255)
    private String representante;

    @Column(name = "cargo_representante", length = 100)
    private String cargoRepresentante;

    @Column(name = "plazo_dias")
    private Integer plazo; // Plazo de crédito en días

    @Column(length = 100)
    private String banco;

    @Column(name = "cuenta_bancaria", length = 50)
    private String cuentaBancaria;

    @Column(length = 50)
    private String cci;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "tipo_proveedor", length = 50)
    private String tipoProveedor; // BIENES, SERVICIOS, AMBOS

    @Column(length = 1000)
    private String observaciones;

    @Column(length = 20)
    @Builder.Default
    private Integer estado = 1;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }
}

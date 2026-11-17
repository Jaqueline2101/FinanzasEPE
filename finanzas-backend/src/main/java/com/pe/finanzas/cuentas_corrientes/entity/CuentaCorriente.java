package com.pe.finanzas.cuentas_corrientes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finanzas_cuentas_corrientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaCorriente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long idCuenta;

    @Column(name = "nombre_titular", nullable = false, length = 200)
    private String nombreTitular;

    @Column(name = "banco", nullable = false, length = 100)
    private String banco;

    @Column(name = "tipo_cuenta", length = 50)
    private String tipoCuenta; // Ahorros, Corriente, Sueldo

    @Column(name = "numero_cuenta", nullable = false, length = 50)
    private String numeroCuenta;

    @Column(name = "cci", length = 50)
    private String cci;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda; // PEN, USD

    @Column(name = "alias", length = 100)
    private String alias; // "Cuenta Principal", "Caja Chica"

    @Column(name = "cuenta_contable", length = 20)
    private String cuentaContable;

    @Column(name = "codigo_sbs", length = 20)
    private String codigoSBS;

    @Column(name = "saldo", precision = 12, scale = 2)
    private BigDecimal saldo;

    @Column(name = "estado", length = 1)
    private String estado; // A = Activo, I = Inactivo

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

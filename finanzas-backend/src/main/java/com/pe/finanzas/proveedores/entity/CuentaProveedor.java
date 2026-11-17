package com.pe.finanzas.proveedores.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "cuenta_proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_proveedor", nullable = false, unique = true)
    private Proveedor proveedor;

    @Column(name = "saldo_total", nullable = false)
    @Builder.Default
    private BigDecimal saldoTotal = BigDecimal.ZERO;
}

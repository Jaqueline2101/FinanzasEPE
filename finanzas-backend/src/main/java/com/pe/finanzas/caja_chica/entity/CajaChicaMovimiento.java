package com.pe.finanzas.caja_chica.entity;

import com.pe.finanzas.atributos.entity.MetodoPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_chica_movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaChicaMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja_chica", nullable = false)
    private CajaChica cajaChica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 15, scale = 5)
    private BigDecimal monto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "referencia")
    private String referencia;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false)
    private String usuario;

    public enum TipoMovimiento {
        INGRESO, EGRESO
    }
}

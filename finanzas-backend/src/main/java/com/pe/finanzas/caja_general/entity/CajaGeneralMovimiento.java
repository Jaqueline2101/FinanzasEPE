package com.pe.finanzas.caja_general.entity;

import com.pe.finanzas.atributos.entity.MetodoPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_general_movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaGeneralMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja_general", nullable = false)
    private CajaGeneral cajaGeneral;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 15, scale = 5)
    private BigDecimal monto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(name = "metodo_pago_origen", length = 100)
    private String metodoPagoOrigen;

    @Column(name = "metodo_pago_destino", length = 100)
    private String metodoPagoDestino;

    public enum TipoMovimiento {
        INGRESO, EGRESO, TRANSFERENCIA_RECIBIDA, PAGO_PROVEEDOR, TRANSFERENCIA_ENTRE_METODOS
    }
}

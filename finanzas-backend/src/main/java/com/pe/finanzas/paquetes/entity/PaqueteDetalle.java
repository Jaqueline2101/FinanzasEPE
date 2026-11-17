package com.pe.finanzas.paquetes.entity;

import com.pe.finanzas.catalogo.entity.Catalogo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad PaqueteDetalle - Servicios incluidos en un paquete
 */
@Entity
@Table(name = "paquete_detalles", uniqueConstraints = @UniqueConstraint(columnNames = { "id_paquete", "id_servicio" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete_detalle")
    private Long idPaqueteDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paquete", nullable = false)
    private Paquete paquete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Catalogo servicio;

    /**
     * Cantidad del servicio en el paquete
     * Ejemplo: 2 análisis de sangre
     */
    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    /**
     * Precio del servicio en el paquete
     */
    @Column(name = "precio", precision = 12, scale = 4)
    private BigDecimal precio;

    /**
     * Porcentaje de descuento aplicado a este servicio en el paquete
     * Ejemplo: 15.00 = 15%
     */
    @Column(name = "descuento_porcentaje", precision = 5, scale = 2)
    private BigDecimal descuentoPorcentaje;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Calcula el precio real total (cantidad * precio unitario)
     */
    public BigDecimal calcularPrecioReal() {
        if (cantidad != null && precio != null) {
            return cantidad.multiply(precio);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el monto del descuento en soles
     */
    public BigDecimal calcularDescuentoMonto() {
        if (descuentoPorcentaje == null || descuentoPorcentaje.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal precioReal = calcularPrecioReal();
        return precioReal.multiply(descuentoPorcentaje)
                .divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calcula el precio final después del descuento
     */
    public BigDecimal calcularPrecioFinal() {
        return calcularPrecioReal().subtract(calcularDescuentoMonto());
    }

    /**
     * Calcula el subtotal del detalle (precio final con descuento)
     */
    public BigDecimal calcularSubtotal() {
        return calcularPrecioFinal();
    }
}

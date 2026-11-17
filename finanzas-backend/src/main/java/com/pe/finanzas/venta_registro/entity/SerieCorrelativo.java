package com.pe.finanzas.venta_registro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "serie_correlativo", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_sucursal", "tipo_doc", "serie" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieCorrelativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(name = "tipo_doc", length = 20, nullable = false)
    private String tipoDoc; // B, F, NC, ND

    @Column(name = "serie", length = 10, nullable = false)
    private String serie;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero;

    @Column(name = "longitud_numero")
    @Builder.Default
    private Integer longitudNumero = 8;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getNextNumeroPadded() {
        return String.format("%0" + longitudNumero + "d", ultimoNumero + 1);
    }
}

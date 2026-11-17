package com.pe.finanzas.descuentos.entity;

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
@Table(name = "tipo_paciente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_paciente")
    private Long idTipoPaciente;

    @Column(name = "codigo", length = 30, unique = true, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "dsc", precision = 5, scale = 2)
    private BigDecimal dsc;

    @Column(name = "cob", precision = 5, scale = 2)
    private BigDecimal cob;

    @Column(name = "frq", length = 1)
    private String frq;

    @Column(name = "sf", length = 1)
    private String sf;

    @Column(name = "tarif", length = 10)
    private String tarif;

    @Column(name = "ambctc", length = 20)
    private String ambctc;

    @Column(name = "hosctc", length = 20)
    private String hosctc;

    @Column(name = "nombre2", length = 100)
    private String nombre2;

    @Column(name = "codmon", length = 1)
    private String codmon;

    @Column(name = "tipo_tarif", length = 1)
    private String tipoTarif;

    @Column(name = "estado", length = 1)
    private Integer estado;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActivo() {
        return Integer.valueOf(1).equals(estado);
    }

    public boolean tieneDescuento() {
        return dsc != null && dsc.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean tieneCobertura() {
        return cob != null && cob.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean esFrecuente() {
        return "1".equals(frq);
    }
}

package com.pe.finanzas.descuentos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tipo_paciente_servicio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPacienteServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_paciente_servicio")
    private Long idTipoPacienteServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_paciente", nullable = false)
    private TipoPaciente tipoPaciente;

    @Column(name = "tipo_servicio", length = 20, nullable = false)
    private String tipoServicio;

    @Column(name = "descuento", precision = 5, scale = 2)
    private BigDecimal descuento;

    @Column(name = "cobertura", precision = 5, scale = 2)
    private BigDecimal cobertura;

    @Column(name = "estado")
    private String estado;

    public boolean tieneDescuento() {
        return descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean tieneCobertura() {
        return cobertura != null && cobertura.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal calcularBeneficioTotal() {
        BigDecimal desc = descuento != null ? descuento : BigDecimal.ZERO;
        BigDecimal cob = cobertura != null ? cobertura : BigDecimal.ZERO;
        return desc.add(cob);
    }

    public boolean isActivo() {
        return Integer.valueOf(1).equals(estado);
    }
}

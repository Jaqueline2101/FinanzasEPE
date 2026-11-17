package com.pe.finanzas.motor_precios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cred_planes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.SQLDelete(sql = "UPDATE cred_planes SET deleted = true WHERE id_plan = ?")
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class CredPlan {

    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long idPlan;

    @Column(name = "poliza", length = 50)
    private String poliza;

    @Column(name = "nombre", length = 200, nullable = false)
    private String nombre;

    @Column(name = "control_preex", length = 1)
    private String controlPreex;

    @CreationTimestamp
    @Column(name = "fecha_cre", updatable = false)
    private LocalDateTime fechaCre;

    @Column(name = "id_personal_user")
    private Long idPersonalUser;

    @Column(name = "tipo_plan", length = 20)
    private String tipoPlan;

    @Column(name = "id_compania")
    private Long idCompania;

    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(name = "fecha_ini")
    private LocalDate fechaIni;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "abrev", length = 20)
    private String abrev;

    @Column(name = "num_cuotas")
    private Integer numCuotas;

    @Column(name = "estado", length = 1)
    private String estado;

    @Column(name = "contador")
    private Integer contador;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "correlativo", length = 20)
    private String correlativo;

    @Column(name = "np", length = 10)
    private String np;

    @Column(name = "independiente", length = 1)
    private String independiente;

    @Column(name = "paquete", length = 1)
    private String paquete;

    @Column(name = "plan_nuevo", length = 1)
    private String planNuevo;

    @Column(name = "tipo_fact", length = 1)
    private String tipoFact;

    @Column(name = "contador2")
    private Integer contador2;

    @Column(name = "mostrar_emerg", length = 1)
    private String mostrarEmerg;

    @Column(name = "seg_viaje", length = 1)
    private String segViaje;

    @Column(name = "chequeo", length = 1)
    private String chequeo;

    @Column(name = "ggss", length = 1)
    private String ggss;

    @Column(name = "codpro", length = 20)
    private String codpro;

    // Campos adicionales para compatibilidad con la DB actual (si se desea
    // mantener)
    @Column(name = "codigo", length = 50)
    private String codigo;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio; // Alias para fechaIni

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CredPlanDescuento> descuentos = new ArrayList<>();

    // =============================================
    // CAMPOS DE SEGURO: Deducible y Coaseguro
    // =============================================

    /**
     * Indica si este plan tiene configuración de deducible activa.
     * Si es false, los campos de deducible se ignoran en el cálculo.
     */
    @Column(name = "tiene_deducible", length = 1)
    @Builder.Default
    private String tieneDeducible = "0";

    /**
     * Monto fijo que el paciente debe pagar antes de que el seguro aplique.
     * Ejemplo: S/ 200.00
     */
    @Column(name = "deducible", precision = 12, scale = 2)
    private java.math.BigDecimal deducible;

    /**
     * Porcentaje del gasto restante (después del deducible) que paga el paciente.
     * Ejemplo: 20.00 → paciente paga 20%, seguro cubre 80%.
     */
    @Column(name = "porc_coaseguro", precision = 5, scale = 2)
    private java.math.BigDecimal porcentajeCoaseguro;

    /**
     * Monto fijo de copago que el paciente paga por atención.
     */
    @Column(name = "copago", precision = 12, scale = 2)
    private java.math.BigDecimal copago;

    /**
     * Monto máximo anual que la compañía cubre por este plan.
     * Si es null, no hay tope anual.
     */
    @Column(name = "tope_anual", precision = 12, scale = 2)
    private java.math.BigDecimal topeAnual;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public boolean isActivo() {
        return "1".equals(estado);
    }

    public boolean isVigente(LocalDate fecha) {
        if (!isActivo()) {
            return false;
        }
        LocalDate inicio = (fechaIni != null) ? fechaIni : fechaInicio;
        if (inicio != null && fecha.isBefore(inicio)) {
            return false;
        }
        if (fechaFin != null && fecha.isAfter(fechaFin)) {
            return false;
        }
        return true;
    }

    public boolean isVigenteHoy() {
        return isVigente(LocalDate.now());
    }

    public void addDescuento(CredPlanDescuento descuento) {
        descuentos.add(descuento);
        descuento.setPlan(this);
    }
}

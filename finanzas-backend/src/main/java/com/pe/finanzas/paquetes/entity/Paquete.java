package com.pe.finanzas.paquetes.entity;

import com.pe.finanzas.catalogo.entity.TipoAtencion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Paquete - Combos de servicios con precio especial
 * 
 * Ejemplos:
 * - Chequeo Médico Completo: Consulta + Análisis + Radiografía
 * - Paquete Prenatal: Ecografía + Análisis + Consulta
 * - Paquete Preventivo: Varios exámenes con descuento
 */
@Entity
@Table(name = "paquetes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.SQLDelete(sql = "UPDATE paquetes SET deleted = true WHERE id_paquete = ?")
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Paquete {

    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete")
    private Long idPaquete;

    @Column(name = "codigo", length = 20, unique = true, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 200, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Relación con la empresa (opcional)
     */
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_atencion")
    private TipoAtencion tipoAtencionEntity;

    @Column(name = "tipo_atencion", length = 20)
    private String tipoAtencion;

    @Column(name = "tipo_atencion_nombre")
    private String tipoAtencionNombre;

    /**
     * Categoría del paquete: 'PAQUETE_REGULAR' o 'CAMPAÑA'
     */
    @Column(name = "categoria", length = 20)
    @Builder.Default
    private String categoria = "PAQUETE_REGULAR";

    /**
     * Precio del paquete
     */
    @Column(name = "precio_paquete", precision = 12, scale = 4, nullable = false)
    private BigDecimal precioPaquete;

    /**
     * Descuento del paquete (porcentaje o monto)
     */
    @Column(name = "descuento", precision = 10, scale = 4)
    private BigDecimal descuento;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    /**
     * Estado del paquete
     * 1 = Activo, 0 = Inactivo
     */
    @Column(name = "estado", length = 1)
    private String estado;

    // Relación con detalles del paquete
    @OneToMany(mappedBy = "paquete", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaqueteDetalle> detalles = new ArrayList<>();

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Verifica si el paquete está activo
     */
    public boolean isActivo() {
        return "1".equals(estado);
    }

    /**
     * Verifica si el paquete está vigente en una fecha
     */
    public boolean isVigente(LocalDate fecha) {
        if (!isActivo()) {
            return false;
        }
        if (fechaInicio != null && fecha.isBefore(fechaInicio)) {
            return false;
        }
        if (fechaFin != null && fecha.isAfter(fechaFin)) {
            return false;
        }
        return true;
    }

    /**
     * Verifica si el paquete está vigente hoy
     */
    public boolean isVigenteHoy() {
        return isVigente(LocalDate.now());
    }

    /**
     * Verifica si el paquete es de tipo AMBULATORIO
     */
    public boolean isAmbulatorio() {
        return "AMBULATORIO".equalsIgnoreCase(tipoAtencion);
    }

    /**
     * Verifica si el paquete es de tipo HOSPITALARIO
     */
    public boolean isHospitalario() {
        return "HOSPITALARIO".equalsIgnoreCase(tipoAtencion);
    }

    /**
     * Verifica si el paquete es de tipo EMERGENCIA
     */
    public boolean isEmergencia() {
        return "EMERGENCIA".equalsIgnoreCase(tipoAtencion);
    }

    /**
     * Método auxiliar para agregar un detalle
     */
    public void addDetalle(PaqueteDetalle detalle) {
        detalles.add(detalle);
        detalle.setPaquete(this);
    }

    /**
     * Método auxiliar para remover un detalle
     */
    public void removeDetalle(PaqueteDetalle detalle) {
        detalles.remove(detalle);
        detalle.setPaquete(null);
    }
}

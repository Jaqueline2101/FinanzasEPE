package com.pe.finanzas.venta_registro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_orden")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @Column(name = "id_paciente")
    private Long idPaciente;

    @Column(name = "numero", length = 20)
    private String numero;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "estado", length = 20)
    private String estado; // PENDIENTE, PAGADO, ANULADO

    @Column(name = "total_estimado", precision = 12, scale = 4)
    private BigDecimal totalEstimado;

    @Column(name = "medico_nombre", length = 200)
    private String medicoNombre;

    @Column(name = "origen", length = 50)
    private String origen;

    @Column(name = "prioridad", length = 20)
    private String prioridad;

    @Column(name = "paciente_nombre")
    private String pacienteNombre;

    @Column(name = "paciente_doc")
    private String pacienteDoc;

    @OneToMany(mappedBy = "labOrden", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LabOrdenDetalle> detalles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void addDetalle(LabOrdenDetalle detalle) {
        detalles.add(detalle);
        detalle.setLabOrden(this);
    }
}

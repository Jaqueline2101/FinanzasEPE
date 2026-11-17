package com.pe.finanzas.puntos.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puntos_documento", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id_punto", "id_tipo_documento" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntosDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Punto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto", referencedColumnName = "punto")
    private Punto punto;

    // Relación con TipoDocumento
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "serie", nullable = false, length = 10)
    private String serie; // Ej: "B001", "F001"

    @Column(name = "numero_actual", nullable = false)
    @Builder.Default
    private Integer numeroActual = 1;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Método para obtener el siguiente número
    public synchronized String obtenerSiguienteNumero() {
        String numeroFormateado = String.format("%08d", numeroActual);
        return serie + "-" + numeroFormateado;
    }

    // Método para incrementar el correlativo
    public synchronized void incrementarNumero() {
        this.numeroActual++;
    }
}

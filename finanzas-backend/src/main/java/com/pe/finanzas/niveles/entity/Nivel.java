package com.pe.finanzas.niveles.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "niveles")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@org.hibernate.annotations.SQLDelete(sql = "UPDATE niveles SET deleted = true WHERE id_nivel = ?")
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Nivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel")
    private Long idNivel;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "num_nivel", length = 50)
    private String numNivel;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "id_tipo_ate", length = 50)
    private String idTipoAte;

    @Column(name = "centro_costo", length = 100)
    private String centroCosto;

    @Column(name = "cent_cost_limite", precision = 15, scale = 2)
    private BigDecimal centCostLimite;

    @Column(name = "estado")
    private Integer estado = 1;

    // JERARQUÍA
    // idNivelPadre is managed by the @ManyToOne relationship below

    @Column(name = "nivel_jerarquia")
    private Integer nivelJerarquia = 1;

    @Column(name = "orden")
    private Integer orden = 0;

    // AUDITORÍA
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)

    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")

    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    @CreatedBy
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    @LastModifiedBy
    private String updatedBy;

    // RELACIÓN RECURSIVA
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel_padre", referencedColumnName = "id_nivel")
    private Nivel nivelPadre;

    @OneToMany(mappedBy = "nivelPadre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nivel> nivelesHijos = new ArrayList<>();

    // SOFT DELETE
    @Column(name = "deleted")
    private boolean deleted = false;

    // CAMPO TRANSIENT PARA FRONTEND
    @Transient
    private boolean tieneHijos;

    @Transient
    private int totalHijos;

    // MÉTODOS HELPER PARA COMPATIBILIDAD
    public Long getIdNivelPadre() {
        return nivelPadre != null ? nivelPadre.getIdNivel() : null;
    }

    public void setIdNivelPadre(Long idNivelPadre) {
        if (idNivelPadre != null) {
            Nivel padre = new Nivel();
            padre.setIdNivel(idNivelPadre);
            this.nivelPadre = padre;
        } else {
            this.nivelPadre = null;
        }
    }
}

package com.pe.finanzas.kardex.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pe.finanzas.catalogo.entity.Catalogo;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.productos.entity.Producto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kardex {

    @Id
    @Column(name = "id_articulo_kardex", length = 50)
    private String idArticuloKardex; // PK as String per user requirement

    @Column(name = "id_alm_articulo", nullable = false)
    private Long idAlmArticulo; // Reference to specific Producto (Batch/Lot)

    @Column(name = "id_catalogo", nullable = false)
    private Long idCatalogo; 

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", insertable = false, updatable = false)
    @JsonIncludeProperties({ "id", "nombre" })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @NotFound(action = NotFoundAction.IGNORE)
    private Catalogo catalogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alm_articulo", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @NotFound(action = NotFoundAction.IGNORE)
    private Producto producto; // This is the batch/lot record

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", insertable = false, updatable = false)
    @JsonIncludeProperties({ "idSucursal", "nombreSucursal" })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @NotFound(action = NotFoundAction.IGNORE)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false)
    @JsonIncludeProperties({ "id", "nombre" })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @NotFound(action = NotFoundAction.IGNORE)
    private DatosPersonales usuario;

    @Column(name = "id_documento", length = 20)
    private String idDocumento; // Type of document (FAC, BOL, etc)

    @Column(name = "num_doc", length = 100)
    private String numDoc;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_venc")
    private LocalDate fechaVenc;

    @Column(name = "nro_lote", length = 100)
    private String nroLote;

    @Column(nullable = false, length = 100)
    private String operacion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(nullable = false, length = 2)
    private String signo; // + or -

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 4)
    private BigDecimal costoTotal;

    @Column(name = "saldo_cantidad", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoCantidad;

    @Column(name = "saldo_costo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoCostoUnitario; // PMP (Precio Medio Ponderado)

    @Column(name = "saldo_costo_total", nullable = false, precision = 12, scale = 4)
    private BigDecimal saldoCostoTotal;

    @Column(name = "origen_id", length = 100)
    private String origenId;

    @Column(name = "id_mov_detalle")
    private Long idMovimientoDetalle;

    @Column(name = "origen_tipo", length = 100)
    private String origenTipo; 

    @Column(length = 50)
    private String presentacion;

    @Column(name = "id_clasificacion")
    private Long idClasificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificacion", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @NotFound(action = NotFoundAction.IGNORE)
    private ClasificacionMovimiento clasificacion;
    
    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}

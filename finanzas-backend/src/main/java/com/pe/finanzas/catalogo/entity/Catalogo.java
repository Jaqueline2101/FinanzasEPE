package com.pe.finanzas.catalogo.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.pe.finanzas.niveles.entity.Nivel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@SQLDelete(sql = "UPDATE catalogo SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "nombre_servicio", nullable = false, length = 255)
    private String nombreServicio;

    @Column(name = "unidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal unidad;

    @Column(name = "tipo_unidad", length = 50)
    private String tipoUnidad;

    @Column(name = "honorarios_medicos", nullable = false)
    private Boolean honorariosMedicos;

    @Column(name = "tarifa", precision = 10, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "precio_sin_igv", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioSinIgv;

    @Column(name = "precio_con_igv", precision = 12, scale = 4)
    private BigDecimal precioConIgv;

    @Column(name = "precio_compra", precision = 12, scale = 4)
    private BigDecimal precioCompra;

    @Column(name = "margen_ganancia", precision = 12, scale = 4)
    private BigDecimal margenGanancia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel", nullable = false)
    @JsonIgnoreProperties({ "nivelesHijos", "nivelPadre", "hibernateLazyInitializer", "handler" })
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Nivel nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_atencion")
    private TipoAtencion tipoAtencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_precio", nullable = false, length = 50)
    private TipoPrecio tipoPrecio;

    @Column(name = "estado")
    private Integer estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private Tipo tipo;

    @Column(name = "codigo_seus", length = 50)
    private String codigoSeus;

    @Column(name = "moneda", length = 3)
    private String moneda;

    @Column(name = "tiempo_entrega_minutos")
    private Integer tiempoEntregaMinutos;

    @Column(name = "codigo_barra", unique = true, length = 50)
    private String codigoBarra;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "tipo_medicamento", length = 50)
    private String tipoMedicamento;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "id_principio_activo")
    private Long idPrincipioActivo;

    @Column(name = "id_accion_terapeutica")
    private Long idAccionTerapeutica;

    @Column(name = "reg_sanitario", length = 50)
    private String regSanitario;

    @Column(name = "cod_digemid", length = 50)
    private String codDigemid;

    @Column(name = "es_generico")
    @Builder.Default
    private Boolean esGenerico = false;

    @Column(name = "es_controlado")
    @Builder.Default
    private Boolean esControlado = false;

    @Column(name = "venta_con_receta")
    @Builder.Default
    private Boolean ventaConReceta = false;

    @Column(name = "id_categoria")
    private Long idCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_temperatura")
    private TipoTemperatura tipoTemperatura;

    @Column(name = "id_laboratorio")
    private Long idLaboratorio;

    @Column(length = 100)
    private String presentacion;

    @Column(name = "id_ubicacion")
    private Long idUbicacion;

    @Column(length = 255)
    @Builder.Default
    private String foto = "sinfoto.png";

    @Column(name = "usuario_creacion", length = 50)
    private String usuarioCreacion;

    // Units Config
    @Column(name = "maneja_unidad")
    private Boolean manejaUnidad;
    @Column(name = "stock_unidad")
    private BigDecimal stockUnidad;
    @Column(name = "precio_venta_unitario")
    private BigDecimal precioVentaUnitario;
    @Column(name = "tipo_ganancia_unidad", length = 20)
    private String tipoGananciaUnidad;
    @Column(name = "ganancia_unidad")
    private BigDecimal gananciaUnidad;

    @Column(name = "precio_unitario_min")
    private BigDecimal precioUnitarioMin;
    @Column(name = "precio_unitario_max")
    private BigDecimal precioUnitarioMax;

    @Column(name = "maneja_blister")
    private Boolean manejaBlister;
    @Column(name = "stock_blister")
    private BigDecimal stockBlister;
    @Column(name = "precio_blister_min")
    private BigDecimal precioBlisterMin;
    @Column(name = "precio_blister_max")
    private BigDecimal precioBlisterMax;
    @Column(name = "precio_venta_blister")
    private BigDecimal precioVentaBlister;
    @Column(name = "factor_blister")
    private Integer factorBlister;
    @Column(name = "tipo_ganancia_blister", length = 20)
    private String tipoGananciaBlister;
    @Column(name = "ganancia_blister")
    private BigDecimal gananciaBlister;

    @Column(name = "maneja_caja")
    private Boolean manejaCaja;
    @Column(name = "stock_caja")
    private BigDecimal stockCaja;
    @Column(name = "precio_caja_min")
    private BigDecimal precioCajaMin;
    @Column(name = "precio_caja_max")
    private BigDecimal precioCajaMax;
    @Column(name = "precio_venta_caja")
    private BigDecimal precioVentaCaja;
    @Column(name = "factor_caja")
    private Integer factorCaja;
    @Column(name = "tipo_ganancia_caja", length = 20)
    private String tipoGananciaCaja;
    @Column(name = "ganancia_caja")
    private BigDecimal gananciaCaja;

    // Stock Reservado (para Cotizaciones)
    @Column(name = "stock_reservado_unidad")
    @Builder.Default
    private BigDecimal stockReservadoUnidad = BigDecimal.ZERO;

    @Column(name = "stock_reservado_blister")
    @Builder.Default
    private BigDecimal stockReservadoBlister = BigDecimal.ZERO;

    @Column(name = "stock_reservado_caja")
    @Builder.Default
    private BigDecimal stockReservadoCaja = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    public enum TipoPrecio {
        PRECIO_FACTOR("Precio Factor."),
        PRECIO_FIJO("Precio fijo."),
        PRECIO_VARIABLE("Precio Variable.");

        private final String descripcion;

        TipoPrecio(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum Tipo {
        SERVICIO("Servicio"),
        PAQUETE("Paquete"),
        PRODUCTO("Producto");

        private final String descripcion;

        Tipo(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    @JsonProperty("idTipoTemperatura")
    public Long getIdTipoTemperatura() {
        return tipoTemperatura != null ? tipoTemperatura.getId() : null;
    }

    @JsonProperty("tipoTemperaturaNombre")
    public String getTipoTemperaturaNombre() {
        return tipoTemperatura != null ? tipoTemperatura.getNombre() : null;
    }

    @JsonProperty("tempMin")
    public BigDecimal getTempMin() {
        return tipoTemperatura != null ? tipoTemperatura.getTempMin() : null;
    }

    @JsonProperty("tempMax")
    public BigDecimal getTempMax() {
        return tipoTemperatura != null ? tipoTemperatura.getTempMax() : null;
    }
    @Column(name = "tipo_catalogo")
    private String tipoCatalogo;

    @Column(name = "nombre_catalogo")
    private String nombreCatalogo;
}

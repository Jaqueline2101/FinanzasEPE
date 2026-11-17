package com.pe.finanzas.productos.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pe.finanzas.catalogo.entity.Catalogo;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(name = "id_catalogo", nullable = false)
    private Long idCatalogo;

    // Gestión de Inventario Robusta
    @Column(name = "stock_minimo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "stock_maximo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockMaximo = BigDecimal.ZERO;

    @Column(name = "stock_real", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockReal = BigDecimal.ZERO;

    // Unidades de Manejo (Flags)
    @Column(name = "maneja_unidad")
    @Builder.Default
    private Boolean manejaUnidad = true;

    @Column(name = "maneja_blister")
    @Builder.Default
    private Boolean manejaBlister = false;

    @Column(name = "maneja_caja")
    @Builder.Default
    private Boolean manejaCaja = false;

    // Stocks Específicos
    @Column(name = "stock_unidad", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockUnidad = BigDecimal.ZERO;

    @Column(name = "stock_blister", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockBlister = BigDecimal.ZERO;

    @Column(name = "stock_caja", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockCaja = BigDecimal.ZERO;

    // Empaque y Fraccionamiento
    @Column(name = "tipo_blister")
    private Integer tipoBlister;

    @Column(name = "factorBlister")
    @Builder.Default
    private Integer factorBlister = 1;

    @Column(name = "factor_caja")
    @Builder.Default
    private Integer factorCaja = 1;

    // Control de Calidad y Precios Base
    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "fecha_venc")
    private LocalDate fechaVencimiento;

    @Column(name = "registro_invima_lote", length = 50)
    private String registroInvimaLote;

    @Column(name = "dias_alerta_vencimiento")
    @Builder.Default
    private Integer diasAlertaVencimiento = 90;

    // --- DATOS DE COMPRA (Automatización) ---
    @Column(name = "factor_compra")
    @Builder.Default
    private Integer factorCompra = 1;

    @Column(name = "tipo_afectacion_compra")
    @Builder.Default
    private String tipoAfectacionCompra = "GRAVADO_ONEROSO";

    @Column(name = "fecha_reg", updatable = false)
    @Builder.Default
    private LocalDateTime fechaReg = LocalDateTime.now();

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // Relación con Catalogo
    @ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER para que el JSON serialice el catálogo
    @JoinColumn(name = "id_catalogo", insertable = false, updatable = false)
    private Catalogo catalogo;

    @Column(name = "id_almacen")
    private Long idAlmacen;

    @Transient
    @Setter
    @JsonProperty("almacenamiento")
    private String almacenamiento;

    // --- CAMPOS TRANSITORIOS (Para registro desde Frontend) ---
    @Transient
    @Setter
    @JsonProperty("codigo")
    private String codigo;

    @Transient
    @Setter
    @JsonProperty("nombreProducto")
    private String nombreProducto;

    @Transient
    @Setter
    @JsonProperty("marcaLaboratorio")
    private String marcaLaboratorio;

    @Transient
    @Setter
    @JsonProperty("modelo")
    private String modelo;

    @Transient
    @Setter
    @JsonProperty("categoria")
    private String categoria;

    @Transient
    @Setter
    @JsonProperty("codigoBarra")
    private String codigoBarra;

    @Transient
    @Setter
    @JsonProperty("unidadMedida")
    private String unidadMedida;

    @Transient
    @Setter
    @JsonProperty("moneda")
    private String moneda;

    @Transient
    @Getter @Setter
    @JsonProperty("regSanitario")
    private String regSanitario;

    @Transient
    @Getter @Setter
    @JsonProperty("ubicacion")
    private String ubicacion;

    @Transient
    @Getter @Setter
    @JsonProperty("tipo")
    private String tipo;

    @Transient
    @Setter
    @JsonProperty("idTipoTemperatura")
    private Long idTipoTemperatura;

    @Transient
    public String getCodigo() {
        if (this.codigo != null)
            return this.codigo;
        return this.catalogo != null ? this.catalogo.getCodigo() : null;
    }

    public String getCodigoBarra() {
        if (this.codigoBarra != null)
            return this.codigoBarra;
        return this.catalogo != null ? this.catalogo.getCodigoBarra() : null;
    }

    public String getNombreProducto() {
        if (this.nombreProducto != null) {
            return this.nombreProducto;
        }
        return this.catalogo != null ? this.catalogo.getNombre() : null;
    }

    public String getMoneda() {
        if (this.moneda != null) return this.moneda;
        return this.catalogo != null ? this.catalogo.getMoneda() : "PEN";
    }

    public String getCategoria() {
        if (this.categoria != null) return this.categoria;
        return this.catalogo != null ? this.catalogo.getTipoMedicamento() : null;
    }

    public String getUnidadMedida() {
        if (this.unidadMedida != null) return this.unidadMedida;
        return this.catalogo != null ? this.catalogo.getPresentacion() : null;
    }

    public String getModelo() {
        if (this.modelo != null) return this.modelo;
        return this.catalogo != null ? this.catalogo.getModelo() : null;
    }

    public String getMarcaLaboratorio() {
        if (this.marcaLaboratorio != null) return this.marcaLaboratorio;
        if (this.catalogo != null && this.catalogo.getDetalle() != null) {
            String det = this.catalogo.getDetalle();
            if (det.startsWith("Marca: ")) {
                int end = det.indexOf(" | Ubicación:");
                if (end > 7) {
                    return det.substring(7, end);
                } else if (det.length() > 7) {
                    return det.substring(7);
                }
            }
        }
        return null;
    }

    @JsonProperty("marcaModelo")
    public String getMarcaModelo() {
        if (this.catalogo != null && "SERVICIO".equals(this.catalogo.getTipo().name())) {
            return "";
        }
        String m = getMarcaLaboratorio();
        String mod = getModelo();
        if (m == null && mod == null) return "";
        if (m != null && mod != null) return m + " / " + mod;
        return m != null ? m : mod;
    }

    @JsonProperty("idTipoTemperatura")
    public Long getIdTipoTemperatura() {
        if (this.idTipoTemperatura != null) return this.idTipoTemperatura;
        return (this.catalogo != null && this.catalogo.getTipoTemperatura() != null) 
               ? this.catalogo.getTipoTemperatura().getId() : null;
    }

    @JsonProperty("tipoTemperaturaNombre")
    public String getTipoTemperaturaNombre() {
        return (this.catalogo != null && this.catalogo.getTipoTemperatura() != null) 
               ? this.catalogo.getTipoTemperatura().getNombre() : null;
    }

    @JsonProperty("tempMin")
    public BigDecimal getTempMin() {
        return (this.catalogo != null && this.catalogo.getTipoTemperatura() != null) 
               ? this.catalogo.getTipoTemperatura().getTempMin() : null;
    }

    @JsonProperty("tempMax")
    public BigDecimal getTempMax() {
        return (this.catalogo != null && this.catalogo.getTipoTemperatura() != null) 
               ? this.catalogo.getTipoTemperatura().getTempMax() : null;
    }

    public String getAlmacenamiento() {
        if (this.almacenamiento != null) return this.almacenamiento;
        if (this.catalogo != null && this.catalogo.getDetalle() != null) {
            String det = this.catalogo.getDetalle();
            int start = det.indexOf(" | Almacenamiento: ");
            if (start != -1) {
                return det.substring(start + 19);
            }
        }
        return "Normal";
    }
}

package com.pe.finanzas.venta_registro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad VentaDetalle - Detalle/Items de la venta
 * Mapea a la tabla 'venta_detalle' en la base de datos
 * 
 * IMPORTANTE: Todos los campos monetarios usan BigDecimal con 4 decimales
 */
@Entity
@Table(name = "venta_detalle")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movart")
    private Long idMovart;

    // Relación con la cabecera de venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private VentaRegistro ventaRegistro;

    @Column(name = "id_articulo")
    private Long idArticulo;

    @Column(name = "glosa", length = 200)
    private String glosa;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    // ========== CAMPOS FINANCIEROS (4 DECIMALES) ==========

    /**
     * Valor afecto del item
     */
    @Column(name = "valor_af", precision = 12, scale = 4)
    private BigDecimal valorAf;

    /**
     * Valor inafecto del item
     */
    @Column(name = "valor_inaf", precision = 12, scale = 4)
    private BigDecimal valorInaf;

    /**
     * Valor de venta del item
     */
    @Column(name = "valor_venta", precision = 12, scale = 4)
    private BigDecimal valorVenta;

    /**
     * Descuento aplicado al item
     */
    @Column(name = "descuento", precision = 12, scale = 4)
    private BigDecimal descuento;

    /**
     * Descuento especial
     */
    @Column(name = "descuento_esp", precision = 12, scale = 4)
    private BigDecimal descuentoEsp;

    /**
     * Cobertura del item
     */
    @Column(name = "cobertura", precision = 12, scale = 4)
    private BigDecimal cobertura;

    /**
     * Base imponible del item
     */
    @Column(name = "base_imp", precision = 12, scale = 4)
    private BigDecimal baseImp;

    /**
     * IGV del item
     */
    @Column(name = "igv", precision = 12, scale = 4)
    private BigDecimal igv;

    /**
     * Total del item
     */
    @Column(name = "total", precision = 12, scale = 4)
    private BigDecimal total;

    @Column(name = "copago", precision = 12, scale = 4)
    private BigDecimal copago;

    @Column(name = "copago_fact", precision = 12, scale = 4)
    private BigDecimal copagoFact;

    @Column(name = "tipo_copago", length = 1)
    private String tipoCopago;

    @Column(name = "copago_orig", precision = 12, scale = 4)
    private BigDecimal copagoOrig;

    /**
     * Precio unitario del servicio
     */
    @Column(name = "precio_unitario", precision = 12, scale = 4)
    private BigDecimal precioUnitario;

    /**
     * Porcentaje de IGV aplicado (normalmente 18%)
     */
    @Column(name = "porc_igv", precision = 5, scale = 2)
    private BigDecimal porcIgv;

    /**
     * Porcentaje de descuento aplicado
     */
    @Column(name = "porc_dsc", precision = 5, scale = 2)
    private BigDecimal porcDsc;

    /**
     * Porcentaje de cobertura
     */
    @Column(name = "porc_cob", precision = 5, scale = 2)
    private BigDecimal porcCob;

    // ========== FIN CAMPOS FINANCIEROS ==========

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "id_medico_ser")
    private Integer idMedicoSer;

    @Column(name = "id_cita")
    private Long idCita;

    @Column(name = "estado")
    private Integer estado;

    @Column(name = "estado_atencion", length = 1)
    private String estadoAtencion;

    @Column(name = "id_sol_det")
    private Long idSolDet;

    @Column(name = "pago_hono", precision = 12, scale = 4)
    private BigDecimal pagoHono;

    @Column(name = "coberturado", length = 1)
    private String coberturado;

    @Column(name = "id_personal_user")
    private Integer idPersonalUser;

    @Column(name = "user_upd", length = 50)
    private String userUpd;

    @Column(name = "id_cuenta_alm", length = 20)
    private String idCuentaAlm;

    @Column(name = "id_cuenta_cv", length = 20)
    private String idCuentaCv;

    @Column(name = "impo_liq", precision = 12, scale = 4)
    private BigDecimal impoLiq;

    @Column(name = "centro_costo", length = 20)
    private String centroCosto;

    @Column(name = "id_mov_hon")
    private Long idMovHon;

    @Column(name = "agregar_igv", length = 1)
    private String agregarIgv;

    @Column(name = "id_paquete")
    private Long idPaquete;

    @Column(name = "modo_det", length = 1)
    private String modoDet;

    @Column(name = "id_presupuesto")
    private Long idPresupuesto;

    @Column(name = "cambia_precio", length = 1)
    private String cambiaPrecio;

    @Column(name = "id_autoriza_desc")
    private Integer idAutorizaDesc;

    @Column(name = "id_medico_rec")
    private Integer idMedicoRec;

    @Column(name = "consultorio", length = 50)
    private String consultorio;

    @Column(name = "ret_hono", precision = 12, scale = 4)
    private BigDecimal retHono;

    @Column(name = "id_user_anul")
    private Integer idUserAnul;

    @Column(name = "fecanu")
    private LocalDateTime fecanu;

    @Column(name = "prioridad", length = 1)
    private String prioridad;

    @Column(name = "muestra", length = 50)
    private String muestra;

    @Column(name = "equipo", length = 50)
    private String equipo;

    @Column(name = "dscto_pac", precision = 12, scale = 4)
    private BigDecimal dsctoPac;

    @Column(name = "id_examen")
    private Long idExamen;

    @Column(name = "tipo_serv", length = 2)
    private String tipoServ;

    @Column(name = "motivo_modif", length = 200)
    private String motivoModif;

    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "id_personal_temp")
    private Integer idPersonalTemp;

    @Column(name = "id_plan_temp")
    private Long idPlanTemp;

    @Column(name = "id_tipo_ate_temp")
    private Integer idTipoAteTemp;

    @Column(name = "id_serv_fij_temp")
    private Long idServFijTemp;

    @Column(name = "motivo_anul", length = 200)
    private String motivoAnul;

    @Column(name = "id_personal_dig")
    private Integer idPersonalDig;

    @Column(name = "fecha_desc")
    private LocalDateTime fechaDesc;

    @Column(name = "det_transf", length = 1)
    private String detTransf;

    @Column(name = "id_liq")
    private Long idLiq;

    @Column(name = "id_pendiente")
    private Long idPendiente;

    @Column(name = "id_vnt_pendiente")
    private Long idVntPendiente;

    @Column(name = "tipo_precio", length = 1)
    private String tipoPrecio;

    @Column(name = "porc_utilidad", precision = 5, scale = 2)
    private BigDecimal porcUtilidad;

    @Column(name = "rentab", precision = 12, scale = 4)
    private BigDecimal rentab;

    @Column(name = "cu", precision = 12, scale = 4)
    private BigDecimal cu;

    @Column(name = "id_lote")
    private Long idLote;

    @Column(name = "id_personal_cambio")
    private Integer idPersonalCambio;

    @Column(name = "motivo_cambio", length = 200)
    private String motivoCambio;

    @Column(name = "id_medico_ant")
    private Integer idMedicoAnt;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "tipo_desc", length = 20)
    private String tipoDesc;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

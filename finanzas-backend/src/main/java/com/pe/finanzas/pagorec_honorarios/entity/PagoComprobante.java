package com.pe.finanzas.pagorec_honorarios.entity;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pago_comprobante")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    // 📌 Datos Generales del Pago
    @Column(name = "tipo_pago", length = 30)
    private String tipoPago; // Honorario, Liquidación, Adelanto, Otro

    @Column(name = "numero_recibo", length = 20)
    private String numeroRecibo;

    @Column(name = "serie_comprobante", length = 10)
    private String serieComprobante;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "periodo_mes", length = 20)
    private String periodoMes;

    @Column(name = "periodo_anio")
    private Integer periodoAnio;

    @Column(name = "concepto_pago", length = 250)
    private String conceptoPago;

    @Column(name = "fecha_proceso")
    private LocalDate fechaProceso;

    @Column(name = "descripcion_adicional", length = 500)
    private String descripcionAdicional;

    @Column(name = "estado_pago", length = 20)
    private String estadoPago; // Pendiente, Pagado, Anulado

    // 📌 Datos del Beneficiario
    @Column(name = "tipo_beneficiario", length = 20)
    private String tipoBeneficiario; // Proveedor, Médico, Trabajador

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "nombre_razon_social", length = 200)
    private String nombreRazonSocial;

    @Column(name = "area_servicio", length = 100)
    private String areaServicio;

    @Column(name = "centro_costo", length = 50)
    private String centroCosto;

    @Column(name = "cargo_trabajador", length = 100)
    private String cargoTrabajador;

    @Column(name = "area_trabajador", length = 100)
    private String areaTrabajador;

    @Column(name = "tipo_contrato", length = 50)
    private String tipoContrato;

    @Column(name = "estado_laboral", length = 50)
    private String estadoLaboral;

    @Column(name = "sistema_pensionario", length = 30)
    private String sistemaPensionario; // ONP, AFP

    @Column(name = "afp_nombre", length = 50)
    private String afpNombre; // Habitat, Integra, Prima, Profuturo

    @Column(name = "tipo_comision", length = 30)
    private String tipoComision; // Flujo, Mixta

    @Column(name = "porcentaje_comision", precision = 10, scale = 4)
    private BigDecimal porcentajeComision;

    @Column(name = "porcentaje_seguro", precision = 10, scale = 4)
    private BigDecimal porcentajeSeguro;

    // 📌 Montos y Cálculo
    @Column(name = "monto_bruto", precision = 12, scale = 4)
    private BigDecimal montoBruto;

    @Column(name = "descuento", precision = 12, scale = 4)
    private BigDecimal descuento;

    @Column(name = "retencion", precision = 12, scale = 4)
    private BigDecimal retencion;

    @Column(name = "aporte_afp_onp", precision = 12, scale = 4)
    private BigDecimal aporteAfpOnp;

    @Column(name = "otros_descuentos", precision = 12, scale = 4)
    private BigDecimal otrosDescuentos;

    @Column(name = "total_retenciones", precision = 12, scale = 4)
    private BigDecimal totalRetenciones;

    @Column(name = "monto_neto", precision = 12, scale = 4)
    private BigDecimal montoNeto;

    @Column(name = "moneda", length = 3)
    private String moneda; // PEN, USD

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    // 📌 Forma de Pago
    @Column(name = "medio_pago", length = 50)
    private String medioPago;

    @Column(name = "banco_destino", length = 100)
    private String bancoDestino;

    @Column(name = "numero_cuenta", length = 50)
    private String numeroCuenta;

    @Column(name = "numero_operacion", length = 50)
    private String numeroOperacion;

    @Column(name = "codigo_transferencia", length = 50)
    private String codigoTransferencia;

    @Column(name = "fecha_transferencia")
    private LocalDate fechaTransferencia;

    @Column(name = "observacion_pago", length = 500)
    private String observacionPago;

    // 📌 Información Contable
    @Column(name = "cuenta_contable", length = 50)
    private String cuentaContable;

    @Column(name = "subcuenta", length = 50)
    private String subcuenta;

    @Column(name = "tipo_gasto", length = 50)
    private String tipoGasto;

    @Column(name = "numero_asiento", length = 50)
    private String numeroAsiento;

    @Column(name = "glosa_contable", length = 500)
    private String glosaContable;

    @Column(name = "periodo_contable", length = 10)
    private String periodoContable;

    // 📌 Control Interno
    @Column(name = "usuario_registro", length = 50)
    private String usuarioRegistro;

    @Column(name = "usuario_aprobacion", length = 50)
    private String usuarioAprobacion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "estado_validacion", length = 20)
    private String estadoValidacion;

    @Column(name = "motivo_anulacion", length = 250)
    private String motivoAnulacion;

    // Relación con el detalle de liquidación (opcional)
    @OneToMany(mappedBy = "pagoComprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PagoConceptoDetalle> detalles = new ArrayList<>();

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addDetalle(PagoConceptoDetalle detalle) {
        detalles.add(detalle);
        detalle.setPagoComprobante(this);
    }
}

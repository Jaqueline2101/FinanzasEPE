package com.pe.finanzas.plan_cuentas_pmt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanCuentasRegistroDTO {

    // 1. CAMPOS GENERALES
    private Long id;
    private String tipoRegistro; // PROVEEDOR / MEDICO / TRABAJADOR
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String razonSocial;
    private String telefono;
    private String correo;
    private String direccion;
    private String estado; // ACTIVO / INACTIVO
    private LocalDateTime fechaRegistro;
    private String usuarioRegistro;
    private String observaciones;

    // 2. INFORMACIÓN LABORAL / SERVICIO
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String cargo;
    private String funcionPrincipal;
    private String area;
    private String tipoContrato;
    private String modalidadTrabajo;
    private String modalidadPago; // Mensual / Por servicio / Por turno
    private String jefeInmediato;
    private String descripcionServicio;
    private String centroCosto;
    private String motivoCese;
    private String estadoVinculo; // ACTIVO / FINALIZADO

    // 3. DATOS ECONÓMICOS
    private BigDecimal montoAcordado;
    private String moneda;
    private String tipoPago; // Fijo / Variable
    private String frecuenciaPago;
    private Boolean retencionAplica;
    private BigDecimal porcentajeRetencion;
    private String cuentaContableAsociada;
    private String observacionesPago;

    // 4. DATOS BANCARIOS
    private String banco;
    private String tipoCuenta;
    private String numeroCuenta;
    private String cci;
    private String titularCuenta;
    private String monedaCuenta;

    // 5. CAMPOS ADICIONALES MÉDICO
    private String especialidad;
    private String numeroColegiatura;
    private String rne;
    private String turno;
    private String tipoAtencion;

    // 6. CAMPOS ADICIONALES PROVEEDOR
    private String tipoProveedor;
    private String contactoPrincipal;
    private String rubro;
    private String condicionPago;

    // 7. CAMPOS ADICIONALES TRABAJADOR
    private String tipoPlanilla;
    private String afpOnp;
    private String numeroAfp;
    private String regimenLaboral;
    private String jornadaLaboral;
    private BigDecimal sueldoBase;
    private BigDecimal bonificaciones;
}

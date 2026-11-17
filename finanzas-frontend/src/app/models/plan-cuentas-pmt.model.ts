export interface PlanCuentasRegistro {
    id?: number;
    tipoRegistro: string; // PROVEEDOR / MEDICO / TRABAJADOR
    tipoDocumento: string;
    numeroDocumento: string;
    nombres: string;
    apellidoPaterno: string;
    apellidoMaterno: string;
    razonSocial?: string;
    telefono: string;
    correo: string;
    direccion: string;
    estado: string; // ACTIVO / INACTIVO
    fechaRegistro?: string;
    usuarioRegistro?: string;
    observaciones?: string;

    // INFORMACIÓN LABORAL
    fechaInicio?: string;
    fechaFin?: string;
    cargo?: string;
    funcionPrincipal?: string;
    area?: string;
    tipoContrato?: string;
    modalidadTrabajo?: string; // Agregado
    modalidadPago?: string;
    jefeInmediato?: string;
    descripcionServicio?: string;
    centroCosto?: string;
    motivoCese?: string;
    estadoVinculo?: string;

    // DATOS ECONÓMICOS
    montoAcordado?: number;
    moneda?: string;
    tipoPago?: string;
    frecuenciaPago?: string;
    retencionAplica?: boolean;
    porcentajeRetencion?: number;
    cuentaContableAsociada?: string;
    observacionesPago?: string;

    // DATOS BANCARIOS
    banco?: string;
    tipoCuenta?: string;
    numeroCuenta?: string;
    cci?: string;
    titularCuenta?: string;
    monedaCuenta?: string;

    // MÉDICO
    especialidad?: string;
    numeroColegiatura?: string;
    rne?: string;
    turno?: string;
    tipoAtencion?: string;

    // PROVEEDOR
    tipoProveedor?: string;
    contactoPrincipal?: string;
    rubro?: string;
    condicionPago?: string;

    // TRABAJADOR
    tipoPlanilla?: string;
    afpOnp?: string;
    numeroAfp?: string;
    regimenLaboral?: string;
    jornadaLaboral?: string;
    sueldoBase?: number;
    bonificaciones?: number;
}

export interface HistorialMovimiento {
    fecha: string;
    usuario: string;
    accion: string;
    detalle: string;
    valorAnterior?: string;
    valorNuevo?: string;
}

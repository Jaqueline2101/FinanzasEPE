export interface OrdenCompra {
    id?: number;
    numeroOrden: string;
    fechaEmision: string;
    fechaEstimadaEntrega?: string;
    tipoCompra?: string;
    centroCosto?: string;
    areaSolicitante?: string;
    responsableCompra?: string;
    moneda: string;
    tipoCambio: number;
    estado: string;
    observaciones?: string;

    // Adelanto
    permiteAdelanto: boolean;
    tipoAdelanto?: string;
    porcentajeAdelanto?: number;
    montoAdelanto?: number;
    fechaAdelanto?: string;
    estadoAdelanto?: string;
    saldoPendienteAdelanto?: number;

    // Proveedor
    idProveedor: number;
    rucProveedor?: string;
    razonSocialProveedor?: string;
    direccionFiscal?: string;
    condicionPago?: string;
    plazoDias?: number;

    // Totales
    subtotal: number;
    totalDescuento: number;
    totalIgv: number;
    totalGeneral: number;

    // Control financiero
    totalAdelantado?: number;
    saldoPorPagar?: number;
    estadoPago?: string;

    // Logística
    lugarEntrega?: string;
    direccionEntrega?: string;
    fechaEntregaAcordada?: string;
    responsableRecepcion?: string;
    observacionesLogisticas?: string;

    // Control Interno
    usuarioRegistro?: string;
    fechaRegistro?: string;
    usuarioAprobacion?: string;
    fechaAprobacion?: string;
    motivoAnulacion?: string;
    numCotizacionRelacionada?: string;
    numRequerimientoInterno?: string;
    fechaAnulacion?: string;
    usuarioAnulacion?: string;
    fechaActualizacion?: string;

    detalles: OrdenCompraDetalle[];
}

export interface OrdenCompraDetalle {
    id?: number;
    codigoItem?: string;
    descripcion: string;
    unidadMedida?: string;
    cantidad: number;
    precioUnitario: number;
    descuento: number;
    subtotal: number;
    afectoIgv: boolean;
    igv: number;
    total: number;
}

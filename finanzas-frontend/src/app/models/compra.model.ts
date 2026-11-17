export interface DetalleCompra {
    id?: number;
    idProducto: number;
    productoNombre?: string;
    cantidad: number;
    unidadMedida: string;
    factorConversion: number;
    lote?: string;
    fechaVencimiento?: string; // ISO Date string
    precioUnitario: number;
    porcentajeDescuento: number;
    esBonificacion: boolean;
    valorVenta: number;
}

export interface Compra {
    id?: number;
    idProveedor: number;
    proveedorRazonSocial?: string;
    idSucursal: number;
    tipoComprobante: string;
    serie: string;
    correlativo: string;
    fechaEmision: string; // ISO Date string
    fechaVencimiento?: string;
    condicionPago: string;
    moneda: string;
    valorVentaGravado: number;
    valorVentaInafecto: number;
    subtotal: number;
    igv: number;
    total: number;
    percepcion: number;
    totalPagar: number;
    estado?: string;
    fechaRegistro?: string;
    detalles: DetalleCompra[];
}

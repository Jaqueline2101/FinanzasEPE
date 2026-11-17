export interface MovimientoFinanciero {
    id: number;
    tipo: 'INGRESO' | 'EGRESO';
    monto: number;
    concepto: string;
    idUsuario: number;
    nombreUsuario?: string; // New
    idSucursal: number;
    idCuenta?: number; // New linked account
    fechaMovimiento: string;
    origen?: string;
    idOrigen?: number;
    tipoComprobante?: string; // New
    numeroComprobante?: string; // New
    metodoPago?: string;
    codigoContable?: string; // New
    beneficiario?: string; // New
    anulado: boolean;
}

export interface BalanceDTO {
    totalIngresos: number;
    totalEgresos: number;
    saldoActual: number;
    rango: string;
}

export interface MovimientoDTO {
    tipo: 'INGRESO' | 'EGRESO';
    monto: number;
    concepto: string;
    idUsuario: number;
    idSucursal: number;
    idCuenta?: number; // New
    fechaMovimiento?: string; // New
    origen?: string;
    idOrigen?: number;
    metodoPago?: string;
    tipoComprobante?: string; // New
    numeroComprobante?: string; // New
    codigoContable?: string; // New
    beneficiario?: string; // New
}

export interface TransferenciaDTO {
    idCuentaOrigen: number;
    idCuentaDestino: number;
    monto: number;
    referencia: string;
    idUsuario: number;
    idSucursal: number;
    descripcion: string;
}

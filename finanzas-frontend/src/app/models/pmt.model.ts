export interface PmtResumen {
    titular: string;
    tipoEntidad: string;
    totalCargos: number;
    totalAbonos: number;
    saldoActual: number;
    ultimoMovimiento: string;
}

export interface PmtMovimiento {
    fecha: string;
    concepto: string;
    tipoComprobante: string;
    numeroComprobante: string;
    cargo: number;
    abono: number;
    saldo: number;
}

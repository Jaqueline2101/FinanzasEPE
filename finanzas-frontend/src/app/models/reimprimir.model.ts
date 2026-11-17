export interface DocumentoReimpresion {
    id: number;
    fecha: string;
    tipoDocumento: string;
    serie: string;
    numero: string;
    clienteBeneficiario: string;
    total: number;
    moneda: string;
    estado: string;
    usuario: string;
    moduloOrigen: 'VENTAS' | 'HONORARIOS' | 'PAGO_PERSONAL';
}

export interface ReimpresionFilter {
    fechaDesde: string | null;
    fechaHasta: string | null;
    tipoDocumento: string | null;
    serie: string | null;
    numero: string | null;
    numDocIdentidad: string | null;
    moduloOrigen: string | null;
    query?: string | null;
}

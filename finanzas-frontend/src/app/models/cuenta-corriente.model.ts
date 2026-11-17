export interface CuentaCorriente {
    idCuenta: number;
    nombreTitular: string;
    banco: string;
    numeroCuenta: string;
    cci?: string;
    moneda: string; // PEN, USD
    alias?: string;
    cuentaContable?: string;
    codigoSBS?: string;
    saldo: number;
    estado: string; // A, I
    createdAt?: string;
    updatedAt?: string;
}

import { CuentaCorriente } from "./cuenta-corriente.model";

export interface Movimiento {
    idMovimiento: number;
    tipoMovimiento: 'INGRESO' | 'EGRESO';
    fechaEmision: string | Date;
    descripcion: string;
    monto: number;
    moneda: 'PEN' | 'USD';
    beneficiario: string;
    tipoComprobante?: string;
    numeroComprobante?: string;
    codigoContable?: string;
    categoria?: string;
    estado: 'A' | 'I';
    evidenciaUrl?: string;
    cuenta: CuentaCorriente;
    idUsuario?: number;
    createdAt?: string;
    updatedAt?: string;
}

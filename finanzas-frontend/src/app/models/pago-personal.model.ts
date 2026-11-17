import { PagoComprobante } from './pago-honorarios.model';


export interface PagoPersonalRequest {
    tipoPago: string;
    periodoMes?: string | number;
    periodoAnio?: number;
    fechaPago: string;
    fechaProceso?: string;
    conceptoPago: string;
    estadoPago?: string;
    observacionPago?: string;
    // Datos del Trabajador
    numeroDocumento: string;
    nombreRazonSocial: string;
    cargoTrabajador?: string;
    areaTrabajador?: string;
    centroCosto?: string;
    tipoContrato?: string;
    estadoLaboral?: string;
    // Totales resumidos
    montoBruto?: number;
    totalDescuentos?: number;
    montoNeto?: number;
    moneda: string;
    tipoCambio?: number;
    // Forma de Pago
    medioPago?: string;
    bancoDestino?: string;
    numeroCuenta?: string;
    numeroOperacion?: string;
    codigoTransferencia?: string;
    fechaDeposito?: string;
    // Información Contable
    cuentaContable?: string;
    subcuenta?: string;
    numeroAsiento?: string;
    glosaContable?: string;
    periodoContable?: string;
    // Detalle
    detalles: PagoPersonalConceptoRequest[];
}

export interface PagoPersonalConceptoRequest {
    nombreConcepto: string;
    tipoConcepto: string; // INGRESO, DESCUENTO, APORTE
    monto: number;
}

import { CronogramaPago } from './cronograma-pago.model';

export interface CompraDeuda {
    idCompra: number;
    proveedorRazonSocial: string;
    serie: string;
    fechaEmision?: string;

    // Calculated fields
    cuotasTotal: number;
    cuotasPagadas: number;
    montoTotal: number;
    montoPagado: number;
    montoPendiente: number;
    vencimientoFinal: string; // Fecha de la cuota pendiente más próxima o lejana

    estadoGeneral: 'AL_DIA' | 'POR_VENCER' | 'VENCIDO' | 'PAGADO';
    colorEstado?: string; // semáforo

    cuotas: CronogramaPago[];
}

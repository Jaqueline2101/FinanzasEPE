export interface CronogramaPago {
    idCronograma: number;
    compra?: any; // Para mostrar datos de la compra (serie, proveedor)
    proveedorRazonSocial?: string; // Flattened specifically for table display
    idCompra?: number;
    numeroCuota: number;
    fechaVencimiento: string; // ISO Date
    montoCuota: number;
    estado: 'PENDIENTE' | 'PAGADO' | 'VENCIDO';
    fechaPagoReal?: string;
    comprobantePago?: string;

    // Auxiliar para frontend
    diasRestantes?: number;
    colorEstado?: string;
}

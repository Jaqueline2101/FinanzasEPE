import { Page } from './page.model';

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
}

export interface ValorizacionDTO {
    id: number;
    codigo: string;
    nombre: string;
    categoria: string;
    lote: string;
    stock: number;
    stockMinimo: number;
    costoUnitario: number;
    precioVenta: number;
    valorTotal: number;
    fechaVencimiento: string;
    estado: 'VERDE' | 'AMARILLO' | 'ROJO';
}

export interface ValorizacionResumenDTO {
    totalProductos: number;
    valorTotalInventario: number;
    productosSinStock: number;
    productosStockBajo: number;
    productosProximosVencer: number;
    valorizacionPorCategoria: { [key: string]: number };
    topProductosValorizados: { [key: string]: number };
}

export interface ValorizacionResponse {
    resumen: ValorizacionResumenDTO;
    detalle: Page<ValorizacionDTO>;
}

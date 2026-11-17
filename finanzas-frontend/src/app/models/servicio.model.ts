export interface Servicio {
    id?: number;
    codigo: string;
    nombreServicio: string;
    descripcion?: string;
    unidad?: number;
    tipoUnidad?: string;
    honorariosMedicos?: boolean;
    tarifa?: number;
    precioSinIgv?: number;
    precioConIgv?: number;
    idNivel?: number;
    idCategoria?: number;
    nivel?: any;
    idTipoAtencion?: number | null;
    tipoAtencionNombre?: string;
    tipoPrecio?: string;
    estado?: number;
    tipo?: 'SERVICIO' | 'PRODUCTO';
    codigoSeus?: string;
    regSanitario?: string;
    moneda?: string;
    precioCompra?: number;
    margenGanancia?: number;
    nivelDescripcion?: string;
    estadoDescripcion?: string;
    tipoDescripcion?: string;
    categoriaNombre?: string;
    tipoMedicamento?: string;
    esGenerico?: boolean;
    esControlado?: boolean;
    ventaConReceta?: boolean;
}

export interface ServicioRequest {
    nombreServicio: string;
    codigo?: string;
    descripcion?: string;
    unidad?: number;
    tipoUnidad?: string;
    precioSinIgv?: number;
    precioConIgv: number;
    precioCompra?: number;
    margenGanancia?: number;
    idNivel: number | null;
    idCategoria?: number | null;
    idTipoAtencion: number | null;
    codigoSeus?: string;
    regSanitario?: string;
    moneda?: string;
}

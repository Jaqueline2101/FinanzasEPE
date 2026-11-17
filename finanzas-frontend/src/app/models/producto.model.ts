export interface Catalogo {
    idCatalogo?: number;
    nombre?: string;
    codigo?: string;
    descripcion?: string;

    // Expanded fields
    nombreServicio?: string;
    modelo?: string;
    tipoMedicamento?: string;
    codigoBarra?: string;
    presentacion?: string;
    regSanitario?: string;
    moneda?: string;
    idTipoTemperatura?: number;
    tipoTemperaturaNombre?: string;
    tempMin?: number;
    tempMax?: number;
    detalle?: string;
    tipo?: 'SERVICIO' | 'PRODUCTO';
}

export interface Producto {
    idProducto: number;
    idSucursal: number;
    idCatalogo: number;
    catalogo?: Catalogo; // Expanded by backend

    // Transient Fields (Frontend <-> Backend communication)
    nombreProducto?: string;
    marcaLaboratorio?: string;
    modelo?: string;
    categoria?: string;
    codigo?: string;
    codigoBarra?: string;
    unidadMedida?: string;
    moneda?: string;
    regSanitario?: string;
    idTipoTemperatura?: number;
    ubicacion?: string;
    almacenamiento?: string;

    // Inventario
    stockMinimo: number;
    stockMaximo: number;
    stockReal: number;

    // Unidades (Flags)
    manejaUnidad: boolean;

    // Datos Compra / Lote
    precioCompra: number;
    fechaVencimiento: string; // YYYY-MM-DD
    registroInvimaLote?: string;
    diasAlertaVencimiento?: number;

    // Datos de Compra (Automatización)
    factorCompra?: number;
    tipoAfectacionCompra?: string;

    // Metadata
    fechaReg?: string;
    ultimaActualizacion?: string;
    idAlmacen?: number;
    tipo?: 'SERVICIO' | 'PRODUCTO';
    marcaModelo?: string;
}

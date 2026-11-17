export interface Convenio {
    idConvenio?: number;
    idPlan?: number; // Match backend
    codigo?: string;
    nombre: string;
    empresa?: string;
    nombreEmpresa?: string; // Match backend DTO
    idCompania?: number;
    idEmpresa?: number; // Added to fix link deletion logic
    descripcion?: string;
    fechaInicio: string;
    fechaFin: string;
    estado: string; // '1' = Activo, '0' = Inactivo
    tipo?: string;
    activo?: boolean;
    vigente?: boolean; // Match backend

    // Nuevos campos sincronizados con CredPlan
    poliza?: string;
    controlPreex?: string;
    fechaCre?: string;
    idPersonalUser?: number;
    fechaIni?: string;
    abrev?: string;
    numCuotas?: number;
    contador?: number;
    detalle?: string;
    correlativo?: string;
    np?: string;
    independiente?: string;
    paquete?: string;
    planNuevo?: string;
    tipoFact?: string;
    contador2?: number;
    mostrarEmerg?: string;
    segViaje?: string;
    chequeo?: string;
    ggss?: string;
    codpro?: string;

    // Campos de Seguro Médico
    copago?: number;
    deducible?: number;
    porcentajeCoaseguro?: number;

    descuentos?: any[];
}

export interface ConvenioRequest {
    nombre: string;
    empresa?: string;
    idCompania?: number; // Added
    idEmpresa?: number; // Added for linking
    descripcion?: string;
    fechaInicio: string;
    fechaFin: string;
    estado: string;
    tipo?: string;

    // Linked Company Fields
    codigo?: string;
    representante?: string;
    codeps?: string; // Cod. Plan
    ctacte?: string; // Cuenta Corriente

    // Nuevos campos sincronizados con CredPlan
    poliza?: string;
    controlPreex?: string;
    idPersonalUser?: number;
    fechaIni?: string;
    abrev?: string;
    numCuotas?: number;
    contador?: number;
    detalle?: string;
    correlativo?: string;
    np?: string;
    independiente?: string;
    paquete?: string;
    planNuevo?: string;
    tipoFact?: string;
    contador2?: number;
    mostrarEmerg?: string;
    segViaje?: string;
    chequeo?: string;
    ggss?: string;
    codpro?: string;

    // Campos de Seguro Médico
    copago?: number;
    deducible?: number;
    porcentajeCoaseguro?: number;

    // Descuentos
    descuentos?: DescuentoRequest[];
}

export interface DescuentoRequest {
    idServicio?: number | null;
    nombreServicio?: string; // UI helper
    porcentajeDescuento: number;
    tipoDescuento: string; // PORCENTAJE
    tipoAtencion?: string;
    nivelServicio?: number;
    estado?: string;
}

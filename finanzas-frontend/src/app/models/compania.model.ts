export interface Compania {
    id: number;            // Mapped from backend 'id'
    idCompania?: number;   // Deprecated/Optional
    codigo: string;
    nombre: string;         // Nombre Comercial
    razSol: string;         // Razón Social
    codigoIafa: string;    // RUC o Código IAFA
    idTipoCia: number;    // Tipo de Compañía (1: EPS, 2: Seguros, etc.)
    estado: any;            // 1 (Activo) | 0 (Inactivo) - Puede venir como number o string

    // Configuración Administrativa
    diasPlazo?: number;
    facturar?: string;      // 'S' | 'N'
    ctacte?: string;        // Cuenta Corriente
    tipoTarifa?: string;    // Tipo de Tarifario

    // Contactos
    representante?: string;
    auditor?: string;
    liquidador?: string;
    financieroNombre?: string;
    contador?: string;
    email?: string;
    fonLocal?: string;
    direcc?: string;

    // Otros
    abrev?: string;
    nom_corto?: string;
    codEps?: string;
    codPlan?: string;
    fechaCreacion?: string;
    tipoNombre?: string;
    facturarTexto?: string;
    personaBase?: any;
}

export interface CompaniaRequest {
    codigo: string;
    nombre: string;
    razSol: string;
    codigoIafa: string;
    idTipoCia: number;
    estado: string;

    // New fields
    representante?: string;
    codEps?: string;
    codPlan?: string;
    ctacte?: string;
    diasPlazo?: number;
    facturar?: string;
    tipoTarifa?: string;
    direcc?: string;
    financieroNombre?: string;
    fechaCreacion?: string;
    email?: string;
    fonLocal?: string;
    mostrarDirec?: string;
    mostrarDiasPlazo?: string;
}

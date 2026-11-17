export interface Nivel {
    idNivel?: number;
    nombre: string;
    numNivel?: string;
    tipo?: string;
    idTipoAte?: string;
    centroCosto?: string;
    centCostLimite?: number;
    estado?: number;

    // Jerarquía
    idNivelPadre?: number;
    nivelJerarquia?: number;
    rutaCompleta?: string;
    orden?: number;

    // Datos adicionales
    nombrePadre?: string;
    tieneHijos?: boolean;
    totalHijos?: number;

    createdAt?: Date;
    updatedAt?: Date;
}

export interface NivelTree {
    idNivel: number;
    nombre: string;
    numNivel?: string;
    tipo?: string;
    estado?: number;
    nivelJerarquia: number;
    rutaCompleta?: string;
    orden?: number;

    idNivelPadre?: number;
    tieneHijos: boolean;
    expandido?: boolean;

    hijos?: NivelTree[];

    // UI
    icono?: string;
    color?: string;
    checked?: boolean;
}
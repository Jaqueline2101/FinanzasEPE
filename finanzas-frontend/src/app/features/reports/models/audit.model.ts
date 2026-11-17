export interface SessionDto {
    idSession: number;
    idPersonal: number;
    nombreUsuario: string;
    emailUsuario: string | null;
    dtIngreso: string;
    dtAcceso: string | null;
    dtFin?: string;
    ip: string;
    idJsp: string | null;
    urlAcceso: string | null;
    activa: boolean;
    duracionMinutos: number | null;
    estadoSesion: string; // ACTIVA, CERRADA, EXPIRADA
}

export interface SessionFilterDto {
    idPersonal?: number;
    ip?: string;
    activa?: boolean;
    fechaInicio?: string;
    fechaFin?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
}

export interface LabAuditoriaDto {
    idAuditoria: number;
    tabla: string;
    idRegistro: string;
    accion: string; // INSERT, UPDATE, DELETE
    idPersonalUser: number;
    nombreUsuario: string;
    emailUsuario: string | null;
    fecha: string;
    datoAnterior: string | null; // JSON
    datoNuevo: string | null; // JSON
    descripcionCambio: string;
    cantidadCambios: number | null;
    ip?: string;
    estado?: string;
    userAgent?: string;
    url?: string;
    method?: string;
}

export interface AuditFilterDto {
    tabla?: string;
    idRegistro?: string;
    accion?: string;
    idPersonalUser?: number;
    fechaInicio?: string;
    fechaFin?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
}

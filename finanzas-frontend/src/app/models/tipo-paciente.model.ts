export interface TipoPaciente {
    id?: number;
    nombre: string;
    descripcion?: string;
    porcentajeDescuento?: number;
    activo?: boolean;
}

export interface TipoPacienteRequest {
    nombre: string;
    descripcion?: string;
    porcentajeDescuento?: number;
    activo?: boolean;
}

export interface TipoPacienteResponse {
    success: boolean;
    message: string;
    data?: TipoPaciente | TipoPaciente[];
}

export interface GrupoFamiliar {
    id: number;
    idTitular: string;
    idFamiliar: string;
    parentesco: string;
    estado: string;
    fechaRegistro: string;
    fechaInactivacion?: string;
    observaciones?: string;
    nombreTitular?: string;
    nombreFamiliar?: string;
    edad?: number;
}

export interface GrupoFamiliarRequest {
    idTitular: string;
    idFamiliar: string;
    parentesco: string;
    observaciones?: string;
    // Campos opcionales para creación de persona
    nombre?: string;
    apepat?: string;
    apemat?: string;
    nacfec?: string; // string 'YYYY-MM-DD'
    sexo?: string;
}

export interface ValidacionTrabajadorResponse {
    esValido: boolean;
    nombre?: string;
    idEmpresa?: number;
    mensaje?: string;
}

export const PARENTESCOS = [
    { label: 'Cónyuge', value: 'CONYUGE' },
    { label: 'Hijo/a', value: 'HIJO' },
    { label: 'Padre', value: 'PADRE' },
    { label: 'Madre', value: 'MADRE' },
    { label: 'Hermano/a', value: 'HERMANO' }
];

export interface Sucursal {
    idSucursal: number;
    nombreSucursal: string;
    direccion: string;
    telefono?: string;
    celular?: string;
    email?: string;
    estado?: number; // 1: Activo, 0: Inactivo
}

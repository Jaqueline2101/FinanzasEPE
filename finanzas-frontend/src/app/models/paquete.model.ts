import { Servicio } from './servicio.model';

// DTO for Services inside a Package
export interface ServicioPaquete {
    idDetalle: number;
    idServicio: number;
    nombreServicio: string;
    cantidad: number;
    precioUnitario?: number;  // Precio unitario del servicio
    precio?: number;          // Alias para precioUnitario
    descuentoPorcentaje?: number;  // Porcentaje de descuento aplicado (0-100)
    descuentoMonto?: number;  // Monto del descuento en soles (valor original ingresado)
}

export interface Paquete {
    idPaquete?: number;
    codigo: string;
    nombre: string;
    descripcion?: string;
    idEmpresa?: number;
    idTipoAtencion?: number | null;
    tipoAtencionNombre?: string;
    tipoAtencion?: string; // AMBULATORIO, HOSPITALARIO, EMERGENCIA
    categoria?: string; // PAQUETE_REGULAR, CAMPAÑA
    precioPaquete: number;
    descuento?: number;
    fechaInicio?: string;
    fechaFin?: string;
    estado: string;
    servicios?: ServicioPaquete[]; // Field name must match backend 'servicios'

    // UI Calculated Fields
    precioOferta?: number;
    precioReal?: number;
    ahorroTexto?: string;
}

export interface PaqueteRequest {
    codigo: string;
    nombre: string;
    descripcion?: string;
    idEmpresa?: number;
    idTipoAtencion?: number | null;
    tipoAtencion?: string;
    categoria?: string;
    precioPaquete: number;
    descuento?: number;
    fechaInicio?: string;
    fechaFin?: string;
    estado: string;
    servicios?: {
        idServicio: number;
        cantidad: number;
        descuentoPorcentaje?: number;
        descuentoMonto?: number;
    }[];
}

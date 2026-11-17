export interface RepDescuentoResponse {
    idAutorizacion: number;
    fechaSolicitud: string;
    fechaResolucion: string;
    pacienteNombre: string;
    pacienteDni: string;
    doctorNombre: string;
    cajeroNombre: string;
    autorizadorNombre: string;
    montoOriginal: number;
    montoDescuento: number;
    montoFinal: number;
    estado: string;
    motivoSolicitud: string;
    motivoRechazo: string;
}

export interface RepDescuentoParams {
    fechaInicio?: string;
    fechaFin?: string;
    estado?: string;
    query?: string;
}

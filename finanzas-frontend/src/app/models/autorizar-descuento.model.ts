export interface AutorizacionRequest {
    idVenta?: number;
    montoOriginal: number;
    montoDescuento: number;
    montoFinal: number;
    motivoSolicitud: string;
    idCajero: number;
    pacienteNombre?: string;
    pacienteDni?: string;
    doctorNombre?: string;
}



export interface AutorizacionResolucion {
    idAutorizador: number;
    motivoRechazo?: string;
}

export interface AutorizacionResponse {
    idAutorizacion: number;
    idVenta?: number;
    montoOriginal: number;
    montoDescuento: number;
    montoFinal: number;
    motivoSolicitud: string;
    estado: string;
    pacienteNombre?: string;
    pacienteDni?: string;
    doctorNombre?: string;



    cajeroNombre: string;
    cajeroDni: string;

    autorizadorNombre?: string;
    motivoRechazo?: string;

    fechaSolicitud: string;
    fechaResolucion?: string;
}

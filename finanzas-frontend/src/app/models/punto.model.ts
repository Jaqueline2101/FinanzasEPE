export interface Punto {
    idPunto: number;
    punto: string;
    nombre: string;
    idSucursal: number;
    nombreSucursal?: string;
    tipo: string;
    valido: string;
    imprimeComp: string;
    agrupaServ: string;
    buscaXIniciales: string;
    cargaMedico: string;
    cargaCuenta: number;
    cobra: string;
    ipAccesoModulo: string;
    modulo: number;
    arqueo: string;
    fe: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface PuntosDocumento {
    idPuntoDocumento: number;
    idTipoDocumento: number;
    codigoTipoDocumento: string;
    nombreTipoDocumento: string;
    serie: string;
    numeroActual: number;
    siguienteNumero: number;
    activo: string;
}

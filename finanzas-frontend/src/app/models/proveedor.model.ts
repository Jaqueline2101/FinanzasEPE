export interface Proveedor {
    id?: number;
    tipoDocIdent: string;
    numDocIdent: string;
    razonSocial: string;
    nombreComercial?: string;
    direccion?: string;
    sedes?: string;
    telefono?: string;
    email?: string;
    departamento?: string; // Ubigeo simple
    provincia?: string;
    distrito?: string;
    representante?: string;
    cargoRepresentante?: string;
    plazo?: number;
    banco?: string;
    cuentaBancaria?: string;
    cci?: string;
    sitioWeb?: string;
    tipoProveedor?: string;
    observaciones?: string;
    estado: any;
    fechaRegistro?: string;
}

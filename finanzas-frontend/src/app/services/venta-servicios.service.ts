import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// =========================================================================
// INTERFACES (DTOs)
// =========================================================================

export interface ServicioDTO {
    idServicio: number;
    nombreServicio: string;
    codigo: string;
    precioBase: number;
    precioConvenio?: number;
    precioEspecial?: number;
    precio?: number; // Compatibilidad con algunos endpoints
}

export interface OrdenPendienteDTO {
    idOrden: number;
    idPaciente?: number;
    pacienteNombre?: string;
    pacienteDoc?: string;
    numero: string;
    fecha: string;
    estado: string;
    numExamenes: number;
    totalEstimado?: number;
    detalles?: OrdenDetalleDTO[];
    // Patient Type Flags
    pacienteEsFamiliar?: boolean;
    pacienteIdEmpresa?: number;
    pacienteIdCompania?: number;
    // New fields requested
    medicoNombre?: string;
    origen?: string;
    prioridad?: string;
}

export interface OrdenDetalleDTO {
    idOrdenDet?: number;
    idExamen: number;
    codigo: string;
    nombre: string;
    estado?: string;
    precio?: number;
}

export interface CrearVentaDTO {
    idOrden?: number; // Si es cobro de orden existente
    idPaciente: number;
    tipoCliente: 'PARTICULAR' | 'CONVENIO' | 'DESCUENTOS';
    idConvenio?: number;
    items: ItemVentaDTO[];
    metodoPago: string;
    refMetodoPago?: string;
    // Campos para Pagos Divididos
    montoEfectivo?: number;
    montoTarjeta?: number;
    montoTransferencia?: number;
    refTarjeta?: string;
    refTransferencia?: string;
    tipoComprobante: 'BOLETA' | 'FACTURA';
    descuentoManual?: number;
    idAutorizacionDescuento?: number;
    // Campos adicionales requeridos por el backend actual (VentaRequest)
    idSucursal?: number;
    idPersonal?: number;
    idAlmacen?: number;
    fecha?: string;
    tipoDoc?: string;
    serie?: string;
    numdoc?: string;
    nombrePac?: string;
    nroDni?: string;
    tipoDni?: string;
    ruc?: string;
    razon?: string;
    direcRuc?: string;
    aplicarDescuentoAutomatico?: boolean;
    tipoPac?: string; // Mapeo de tipoCliente
    vuelto?: number;
}

export interface ItemVentaDTO {
    idArticulo: number; // idServicio
    cantidad: number;
    precioUnitario?: number; // Opcional, backend lo puede calcular
    glosa?: string;
    tipoDescuento?: string;
    porcentajeDescuento?: number;
    porcentajeCobertura?: number;
    idMedicoSer?: number;
    consultorio?: string;
}

@Injectable({
    providedIn: 'root'
})
export class VentaService {
    private baseUrl = `${environment.apiUrl}`;
    private apiUrlVentas = `${environment.apiUrl}/ventas`;

    constructor(private http: HttpClient) { }

    // =========================================================================
    // VENTAS
    // =========================================================================

    /**
     * Registra una venta completa (con o sin orden previa)
     */
    crearVenta(venta: CrearVentaDTO): Observable<any> {
        return this.http.post(this.apiUrlVentas, venta);
    }

    /**
     * Obtiene historial de ventas (para reportes)
     */
    listarVentas(params?: any): Observable<any> {
        return this.http.get(this.apiUrlVentas, { params });
    }

    /**
     * Obtiene una venta por ID
     */
    obtenerVenta(idVenta: number): Observable<any> {
        return this.http.get(`${this.apiUrlVentas}/${idVenta}`);
    }

    // =========================================================================
    // ÓRDENES
    // =========================================================================

    /**
     * Busca órdenes pendientes de pago
     * @param idPaciente - Opcional: filtra por paciente específico
     */
    buscarOrdenesPendientes(idPaciente?: number): Observable<OrdenPendienteDTO[]> {
        const url = idPaciente
            ? `${this.apiUrlVentas}/ordenes/pendientes/${idPaciente}`
            : `${this.apiUrlVentas}/ordenes/pendientes`;
        return this.http.get<OrdenPendienteDTO[]>(url);
    }

    /**
     * Obtiene detalle completo de una orden
     */
    getOrdenById(idOrden: number): Observable<any> {
        return this.http.get(`${this.apiUrlVentas}/ordenes/${idOrden}`);
    }

    /**
     * Crea una orden SIN pago (para consultorio médico)
     * TODO: Verificar endpoint backend para esto
     */
    crearOrdenSinPago(orden: any): Observable<any> {
        return this.http.post(`${this.apiUrlVentas}/ordenes`, orden);
    }

    /**
     * Registra pago de una orden existente
     */
    pagarOrden(idOrden: number, payload: any): Observable<any> {
        return this.http.post(`${this.apiUrlVentas}/pagar-orden/${idOrden}`, payload);
    }

    buscarPorRuc(ruc: string): Observable<any> {
        return this.http.get(`${this.baseUrl}/companias/buscar-por-ruc/${ruc}`);
    }

    buscarSugerencias(term: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/companias/buscar-sugerencias`, {
            params: { term }
        });
    }

    // =========================================================================
    // SERVICIOS / CATÁLOGO
    // =========================================================================

    /**
     * Busca servicios con precios según contexto
     * @param term - Término de búsqueda
     * @param idConvenio - Opcional: aplica precios de convenio
     * @param tipoCliente - Opcional: aplica descuentos especiales
     */
    searchServicios(
        term: string,
        idConvenio?: number,
        tipoCliente?: string
    ): Observable<ServicioDTO[]> {
        let params = new HttpParams()
            .set('term', term)
            .set('size', '10');

        if (idConvenio) params = params.set('idConvenio', idConvenio.toString());
        if (tipoCliente) params = params.set('tipoCliente', tipoCliente);

        return this.http.get<ServicioDTO[]>(`${this.baseUrl}/servicios/search`, { params });
    }

    /**
     * Obtiene catálogo completo de servicios
     */
    getServicios(idConvenio?: number, tipoCliente?: string): Observable<ServicioDTO[]> {
        // Reutilizamos la lógica de búsqueda si hay filtros
        if (idConvenio || tipoCliente) {
            return this.searchServicios('', idConvenio, tipoCliente);
        }

        const params = new HttpParams()
            .set('size', '100')
            .set('sort', 'nombreServicio,ASC');

        return this.http.get<ServicioDTO[]>(`${this.baseUrl}/servicios`, { params });
    }

    // =========================================================================
    // VALIDACIONES
    // =========================================================================

    /**
     * Valida si una orden puede cobrarse (no duplicada, etc.)
     */
    validarOrdenParaCobro(idOrden: number): Observable<{ valida: boolean; mensaje?: string }> {
        return this.http.get<any>(`${this.apiUrlVentas}/ordenes/${idOrden}/validar`);
    }
}

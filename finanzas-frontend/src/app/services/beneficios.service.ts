import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface BeneficioItem {
    idPlan: number;
    codigo: string;
    nombre: string;
    tipoPlan: string;
    fechaInicio: string | null;
    fechaFin: string | null;
    estado: string;
    activo: boolean;
    vigente: boolean;

    // Nuevos campos sincronizados con CredPlan
    poliza?: string;
    controlPreex?: string;
    fechaCre?: string;
    idPersonalUser?: number;
    fechaIni?: string;
    abrev?: string;
    numCuotas?: number;
    contador?: number;
    detalle?: string;
    correlativo?: string;
    np?: string;
    independiente?: string;
    paquete?: string;
    planNuevo?: string;
    tipoFact?: string;
    contador2?: number;
    mostrarEmerg?: string;
    segViaje?: string;
    chequeo?: string;
    ggss?: string;
    codpro?: string;
}

export interface BeneficioRequest {
    codigo: string;
    nombre: string;
    tipoPlan: string;
    fechaInicio: string | null;
    fechaFin: string | null;
    estado: string;

    // Nuevos campos sincronizados con CredPlan
    poliza?: string;
    controlPreex?: string;
    idPersonalUser?: number;
    fechaIni?: string;
    abrev?: string;
    numCuotas?: number;
    contador?: number;
    detalle?: string;
    correlativo?: string;
    np?: string;
    independiente?: string;
    paquete?: string;
    planNuevo?: string;
    tipoFact?: string;
    contador2?: number;
    mostrarEmerg?: string;
    segViaje?: string;
    chequeo?: string;
    ggss?: string;
    codpro?: string;
}

@Injectable({ providedIn: 'root' })
export class BeneficiosService {

    private apiUrl = `${environment.apiUrl}/beneficios`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    /** Lista beneficios por tipo: 'GENERAL' | 'FAMILIAR' */
    listarPorTipo(tipoPlan: string): Observable<BeneficioItem[]> {
        return this.http.get<any>(
            `${this.apiUrl}?tipoPlan=${tipoPlan}`,
            { headers: this.getHeaders() }
        ).pipe(map(res => res.data || []));
    }

    /** Crea un nuevo beneficio especial */
    crear(request: BeneficioRequest): Observable<BeneficioItem> {
        return this.http.post<any>(
            this.apiUrl,
            request,
            { headers: this.getHeaders() }
        ).pipe(map(res => res.data));
    }

    /** Aplica descuento masivo a múltiples niveles de un plan */
    aplicarDescuentoMasivo(idPlan: number, payload: {
        idNiveles: number[];
        descuentoPorcentaje?: number;
        descuentoFijo?: number;
        precioFijo?: number;
        tipoDescuento: 'PORCENTAJE' | 'MONTO_FIJO' | 'TARIFA_PLANA' | 'PRECIO_FIJO';
    }): Observable<number> {
        return this.http.post<any>(
            `${this.apiUrl}/${idPlan}/descuentos/masivo`,
            payload,
            { headers: this.getHeaders() }
        ).pipe(map(res => res.data));
    }

    /** Actualiza un beneficio existente */
    actualizar(id: number, request: BeneficioRequest): Observable<BeneficioItem> {
        return this.http.put<any>(
            `${this.apiUrl}/${id}`,
            request,
            { headers: this.getHeaders() }
        ).pipe(map(res => res.data));
    }

    /** Elimina (borrado lógico) un beneficio */
    eliminar(id: number): Observable<void> {
        return this.http.delete<any>(
            `${this.apiUrl}/${id}`,
            { headers: this.getHeaders() }
        ).pipe(map(res => res.data));
    }
}

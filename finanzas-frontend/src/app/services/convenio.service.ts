import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Convenio, ConvenioRequest } from '../models/convenio.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ConvenioService {
    private apiUrl = `${environment.apiUrl}/convenios`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    /**
     * Listar todos los convenios
     */
    listarConvenios(): Observable<Convenio[]> {
        return this.http.get<any>(this.apiUrl, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data.content || response.data)
            );
    }

    /**
     * Listar convenios activos
     */
    listarConveniosActivos(): Observable<Convenio[]> {
        return this.http.get<any>(`${this.apiUrl}/activos`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Listar convenios vigentes
     */
    listarConveniosVigentes(): Observable<Convenio[]> {
        return this.http.get<any>(`${this.apiUrl}/vigentes`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Obtener convenio por ID
     */
    obtenerConvenio(id: number): Observable<Convenio> {
        return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Obtener convenio por Código
     */
    obtenerPorCodigo(codigo: string): Observable<Convenio> {
        return this.http.get<any>(`${this.apiUrl}/codigo/${codigo}`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Listar planes por compañía
     */
    listarPlanesPorCompania(idCompania: number): Observable<Convenio[]> {
        return this.http.get<any>(`${this.apiUrl}/por-compania/${idCompania}`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Crear nuevo convenio
     */
    crearConvenio(request: ConvenioRequest): Observable<Convenio> {
        return this.http.post<any>(this.apiUrl, request, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Actualizar convenio
     */
    actualizarConvenio(id: number, request: ConvenioRequest): Observable<Convenio> {
        return this.http.put<any>(`${this.apiUrl}/${id}`, request, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Cambiar estado de convenio
     */
    cambiarEstado(id: number, estado: string): Observable<void> {
        return this.http.patch<any>(`${this.apiUrl}/${id}/estado?estado=${estado}`, {}, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Eliminar convenio
     */
    /**
     * Eliminar convenio
     */
    eliminarConvenio(id: number): Observable<void> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Agregar regla de descuento a un convenio
     */
    agregarRegla(idConvenio: number, regla: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idConvenio}/reglas`, regla, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    /**
     * Eliminar regla de descuento
     */
    eliminarRegla(idConvenio: number, idRegla: number): Observable<void> {
        return this.http.delete<any>(`${this.apiUrl}/${idConvenio}/reglas/${idRegla}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    /**
     * Actualizar regla de descuento
     */
    actualizarRegla(idConvenio: number, idRegla: number, regla: any): Observable<any> {
        return this.http.put<any>(`${this.apiUrl}/${idConvenio}/reglas/${idRegla}`, regla, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    /**
     * Vincular empresa a compañía (Crear Plan Automático)
     */
    vincularEmpresa(idCompania: number, idEmpresa: number): Observable<Convenio> {
        return this.http.post<any>(`${this.apiUrl}/vincular-empresa?idCompania=${idCompania}&idEmpresa=${idEmpresa}`, {}, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    /**
     * Vincular múltiples empresas a una compañía (Crear Planes Automáticos)
     */
    vincularEmpresasMasivo(request: any): Observable<Convenio[]> {
        return this.http.post<any>(`${this.apiUrl}/vincular-empresas-masivo`, request, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    /**
     * Generar siguiente código de plan
     */
    generarCodigo(idCompania: number): Observable<string> {
        return this.http.get<any>(`${this.apiUrl}/generar-codigo/${idCompania}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    /**
     * Clonar descuentos de un plan a otro
     */
    clonarDescuentos(idOrigen: number, idDestino: number): Observable<void> {
        return this.http.post<any>(`${this.apiUrl}/${idDestino}/clonar-desde/${idOrigen}`, {}, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }
}

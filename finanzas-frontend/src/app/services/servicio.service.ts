import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Servicio } from '../models/servicio.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ServicioService {
    private apiUrl = `${environment.apiUrl}/servicios`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    getAll(page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'ASC'): Observable<any> {
        const params = `?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
        return this.http.get<any>(`${this.apiUrl}${params}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    getByEstado(estado: number, page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'ASC'): Observable<any> {
        const params = `?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
        return this.http.get<any>(`${this.apiUrl}/estado/${estado}${params}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    search(term: string, page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'ASC', idConvenio?: number): Observable<any> {
        let params = `?term=${term}&page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
        if (idConvenio) params += `&idConvenio=${idConvenio}`;

        return this.http.get<any>(`${this.apiUrl}/search${params}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    searchByEstado(term: string, estado: number, page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'ASC'): Observable<any> {
        const params = `?term=${term}&page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
        return this.http.get<any>(`${this.apiUrl}/search/estado/${estado}${params}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    smartSearch(term: string, idConvenio?: number, tipoCliente?: string, nivel?: string, idNivel?: number): Observable<any> {
        let params = `?term=${term}`;
        if (idConvenio) params += `&idConvenio=${idConvenio}`;
        if (tipoCliente) params += `&tipoCliente=${tipoCliente}`;
        if (nivel && nivel !== 'TODO') params += `&nivel=${nivel}`;
        if (idNivel) params += `&idNivel=${idNivel}`;

        return this.http.get<any>(`${this.apiUrl}/smart-search${params}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    getById(id: number): Observable<Servicio> {
        return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    create(servicio: any): Observable<Servicio> {
        return this.http.post<any>(`${this.apiUrl}`, servicio, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    update(id: number, servicio: any): Observable<Servicio> {
        return this.http.put<any>(`${this.apiUrl}/${id}`, servicio, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    listarServiciosActivos(): Observable<Servicio[]> {
        return this.http.get<any>(`${this.apiUrl}/list`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data),
                map((list: any[]) => (list || []).filter(item => 
                    item.estado === true || 
                    item.estado === 1 || 
                    item.estado === '1' || 
                    item.estado === 'A' ||
                    item.estado === 'ACTIVO'
                ))
            );
    }

    listarTodosSimple(): Observable<Servicio[]> {
        return this.http.get<any>(`${this.apiUrl}/list`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    getTiposAtencion(): Observable<any[]> {
        return this.http.get<any>(`${environment.apiUrl}/tipos-atencion/activos`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    aplicarDescuento(request: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/aplicar-descuento`, request, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    obtenerDescuentos(idPlan: number): Observable<any[]> {
        return this.http.get<any>(`${this.apiUrl}/descuentos/plan/${idPlan}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }
}


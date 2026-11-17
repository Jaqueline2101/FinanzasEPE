import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Paquete, PaqueteRequest } from '../models/paquete.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class PaqueteService {
    private apiUrl = `${environment.apiUrl}/paquetes`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    /**
     * Listar todos los paquetes
     */
    listarPaquetes(page: number = 0, size: number = 20, categoria: string = ''): Observable<any> {
        let params: any = { page, size };
        if (categoria) params.categoria = categoria;

        return this.http.get<any>(this.apiUrl, { headers: this.getHeaders(), params })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Listar paquetes activos
     */
    listarPaquetesActivos(): Observable<Paquete[]> {
        return this.http.get<any>(`${this.apiUrl}/activos`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Listar paquetes vigentes
     */
    listarPaquetesVigentes(): Observable<Paquete[]> {
        return this.http.get<any>(`${this.apiUrl}/vigentes`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Obtener paquete por ID
     */
    obtenerPaquete(id: number): Observable<Paquete> {
        return this.http.get<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Crear nuevo paquete
     */
    crearPaquete(request: PaqueteRequest): Observable<Paquete> {
        return this.http.post<any>(this.apiUrl, request, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Actualizar paquete
     */
    actualizarPaquete(id: number, request: PaqueteRequest): Observable<Paquete> {
        return this.http.put<any>(`${this.apiUrl}/${id}`, request, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Cambiar estado de paquete
     */
    cambiarEstado(id: number, estado: string): Observable<void> {
        return this.http.patch<any>(`${this.apiUrl}/${id}/estado?estado=${estado}`, {}, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }

    /**
     * Eliminar paquete
     */
    eliminarPaquete(id: number): Observable<void> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(
                map(response => response.data)
            );
    }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TipoPaciente, TipoPacienteRequest, TipoPacienteResponse } from '../models/tipo-paciente.model';

@Injectable({
    providedIn: 'root'
})
export class TipoPacienteService {
    private apiUrl = `${environment.apiUrl}/tipos-paciente`;

    constructor(private http: HttpClient) { }

    /**
     * Listar todos los tipos de paciente
     */
    listarTodos(): Observable<TipoPacienteResponse> {
        return this.http.get<TipoPacienteResponse>(this.apiUrl);
    }

    /**
     * Listar tipos de paciente activos
     */
    listarActivos(): Observable<TipoPacienteResponse> {
        return this.http.get<TipoPacienteResponse>(`${this.apiUrl}/activos`);
    }

    /**
     * Listar tipos con descuento
     */
    listarConDescuento(): Observable<TipoPacienteResponse> {
        return this.http.get<TipoPacienteResponse>(`${this.apiUrl}/con-descuento`);
    }

    /**
     * Obtener tipo de paciente por ID
     */
    obtenerPorId(id: number): Observable<TipoPacienteResponse> {
        return this.http.get<TipoPacienteResponse>(`${this.apiUrl}/${id}`);
    }

    /**
     * Crear nuevo tipo de paciente
     */
    crear(request: TipoPacienteRequest): Observable<TipoPacienteResponse> {
        return this.http.post<TipoPacienteResponse>(this.apiUrl, request);
    }

    /**
     * Actualizar tipo de paciente
     */
    actualizar(id: number, request: TipoPacienteRequest): Observable<TipoPacienteResponse> {
        return this.http.put<TipoPacienteResponse>(`${this.apiUrl}/${id}`, request);
    }

    /**
     * Cambiar estado (activar/desactivar)
     */
    cambiarEstado(id: number, estado: string): Observable<TipoPacienteResponse> {
        const params = new HttpParams().set('estado', estado);
        return this.http.patch<TipoPacienteResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
    }

    /**
     * Eliminar tipo de paciente
     */
    eliminar(id: number): Observable<TipoPacienteResponse> {
        return this.http.delete<TipoPacienteResponse>(`${this.apiUrl}/${id}`);
    }
}

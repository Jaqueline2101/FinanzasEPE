import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
    GrupoFamiliar,
    GrupoFamiliarRequest,
    ValidacionTrabajadorResponse
} from '../models/grupo-familiar.model';

@Injectable({
    providedIn: 'root'
})
export class GrupoFamiliarService {

    private apiUrl = `${environment.apiUrl}/grupo-familiar`;

    constructor(private http: HttpClient) { }

    registrarFamiliar(request: GrupoFamiliarRequest): Observable<GrupoFamiliar> {
        return this.http.post<GrupoFamiliar>(this.apiUrl, request);
    }

    obtenerFamiliaresPorTitular(dni: string): Observable<GrupoFamiliar[]> {
        return this.http.get<GrupoFamiliar[]>(`${this.apiUrl}/titular/${dni}`);
    }

    validarTrabajador(dni: string): Observable<ValidacionTrabajadorResponse> {
        return this.http.get<ValidacionTrabajadorResponse>(`${this.apiUrl}/validar-trabajador/${dni}`);
    }

    desactivarFamiliar(id: number): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${id}/desactivar`, {});
    }

    obtenerTitularPorFamiliar(dni: string): Observable<GrupoFamiliar> {
        return this.http.get<GrupoFamiliar>(`${this.apiUrl}/familiar/${dni}`);
    }

    esFamiliarDeTrabajador(dni: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/es-familiar/${dni}`);
    }
}

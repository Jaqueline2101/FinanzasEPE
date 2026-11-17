import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CredCtaCte } from '../models/cred-ctacte.model';

@Injectable({
    providedIn: 'root'
})
export class CredCtaCteService {
    private apiUrl = `${environment.apiUrl}/cred-ctacte`;

    constructor(private http: HttpClient) { }

    crear(cuenta: CredCtaCte): Observable<CredCtaCte> {
        return this.http.post<CredCtaCte>(this.apiUrl, cuenta);
    }

    actualizar(id: number, cuenta: CredCtaCte): Observable<CredCtaCte> {
        return this.http.put<CredCtaCte>(`${this.apiUrl}/${id}`, cuenta);
    }

    obtenerPorId(id: number): Observable<CredCtaCte> {
        return this.http.get<CredCtaCte>(`${this.apiUrl}/${id}`);
    }

    listarTodas(): Observable<CredCtaCte[]> {
        return this.http.get<CredCtaCte[]>(this.apiUrl);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    listarPorPaciente(idPaciente: number): Observable<CredCtaCte[]> {
        return this.http.get<CredCtaCte[]>(`${this.apiUrl}/paciente/${idPaciente}`);
    }

    listarPorDni(dni: string): Observable<CredCtaCte[]> {
        return this.http.get<CredCtaCte[]>(`${this.apiUrl}/dni/${dni}`);
    }

    previsualizarNroCtacte(idCompania: number): Observable<{ nroCtacte: string }> {
        return this.http.get<{ nroCtacte: string }>(`${this.apiUrl}/sequence/next/${idCompania}`);
    }
}

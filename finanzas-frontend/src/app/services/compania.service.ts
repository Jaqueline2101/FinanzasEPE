import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Compania, CompaniaRequest } from '../models/compania.model';

@Injectable({
    providedIn: 'root'
})
export class CompaniaService {
    private apiUrl = `${environment.apiUrl}/companias`;

    constructor(private http: HttpClient) { }

    getCompanias(): Observable<Compania[]> {
        return this.http.get<Compania[]>(this.apiUrl);
    }

    getCompania(id: number): Observable<Compania> {
        return this.http.get<Compania>(`${this.apiUrl}/${id}`);
    }

    createCompania(compania: CompaniaRequest): Observable<Compania> {
        return this.http.post<Compania>(this.apiUrl, compania);
    }

    updateCompania(id: number, compania: CompaniaRequest): Observable<Compania> {
        return this.http.put<Compania>(`${this.apiUrl}/${id}`, compania);
    }

    deleteCompania(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getByRuc(ruc: string): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/buscar-por-ruc/${ruc}`);
    }

    buscarSugerencias(term: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/buscar-sugerencias`, { params: { term } });
    }
}

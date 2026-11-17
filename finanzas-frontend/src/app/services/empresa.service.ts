
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { map } from 'rxjs/operators';

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export interface Empresa {
    idEmpresa: number;
    codigo: string;
    ruc?: string;
    nombre: string;
    representante?: string;
    telefono?: string;
    direccion?: string;
    abrev?: string;
    idPersonalUser?: string;
    fechaCre?: string;
    estado: string;
}

@Injectable({
    providedIn: 'root'
})
export class EmpresaService {
    private apiUrl = `${environment.apiUrl}/empresas`;

    constructor(private http: HttpClient) { }

    getEmpresas(): Observable<Empresa[]> {
        return this.http.get<ApiResponse<Empresa[]>>(this.apiUrl)
            .pipe(map(response => response.data.map(e => ({
                ...e,
                ruc: e.idPersonalUser || e.ruc || e.codigo || 'N/A'
            }))));
    }

    getEmpresa(id: number): Observable<Empresa> {
        return this.http.get<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`)
            .pipe(map(response => {
                const e = response.data;
                return {
                    ...e,
                    ruc: e.idPersonalUser || e.ruc || e.codigo || 'N/A'
                };
            }));
    }

    createEmpresa(empresa: Empresa): Observable<Empresa> {
        return this.http.post<ApiResponse<Empresa>>(this.apiUrl, empresa)
            .pipe(map(response => response.data));
    }

    updateEmpresa(id: number, empresa: Empresa): Observable<Empresa> {
        return this.http.put<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`, empresa)
            .pipe(map(response => response.data));
    }

    deleteEmpresa(id: number): Observable<void> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`)
            .pipe(map(() => void 0));
    }

    getEmpresasPorCompania(idCompania: number): Observable<Empresa[]> {
        return this.http.get<ApiResponse<Empresa[]>>(`${this.apiUrl}/por-compania/${idCompania}`)
            .pipe(map(response => response.data.map(e => ({
                ...e,
                ruc: e.idPersonalUser || e.ruc || e.codigo || 'N/A' // Corrected mapping
            }))));
    }

    checkRuc(ruc: string): Observable<boolean> {
        return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/check-ruc/${ruc}`)
            .pipe(map(response => response.data));
    }
}

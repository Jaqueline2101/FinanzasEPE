import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/modals/response.model';
import { Punto } from '../../models/punto.model';

@Injectable({
    providedIn: 'root'
})
export class PuntoService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/puntos`;

    listarPorSucursal(idSucursal: number): Observable<ApiResponse<Punto[]>> {
        return this.http.get<ApiResponse<Punto[]>>(`${this.apiUrl}/sucursal/${idSucursal}`);
    }

    listarActivosPorSucursal(idSucursal: number): Observable<ApiResponse<Punto[]>> {
        return this.http.get<ApiResponse<Punto[]>>(`${this.apiUrl}/sucursal/${idSucursal}/activos`);
    }

    crear(punto: Partial<Punto>): Observable<ApiResponse<Punto>> {
        return this.http.post<ApiResponse<Punto>>(this.apiUrl, punto);
    }

    actualizar(id: number, punto: Partial<Punto>): Observable<ApiResponse<Punto>> {
        return this.http.put<ApiResponse<Punto>>(`${this.apiUrl}/${id}`, punto);
    }

    cambiarEstado(id: number, activo: boolean): Observable<ApiResponse<void>> {
        return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/estado?activo=${activo}`, {});
    }

    eliminar(id: number): Observable<ApiResponse<void>> {
        return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
}

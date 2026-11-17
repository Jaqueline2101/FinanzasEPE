import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../shared/modals/response.model';
import { AutorizacionRequest, AutorizacionResolucion, AutorizacionResponse } from '../models/autorizar-descuento.model';

@Injectable({
    providedIn: 'root'
})
export class AutorizarDescuentoService {

    private apiUrl = `${environment.apiUrl}/autorizaciones-descuento`;

    constructor(private http: HttpClient) { }

    solicitarDescuento(request: AutorizacionRequest): Observable<ApiResponse<AutorizacionResponse>> {
        return this.http.post<ApiResponse<AutorizacionResponse>>(`${this.apiUrl}/solicitar`, request);
    }

    aprobarDescuento(id: number, request: AutorizacionResolucion): Observable<ApiResponse<AutorizacionResponse>> {
        return this.http.put<ApiResponse<AutorizacionResponse>>(`${this.apiUrl}/${id}/aprobar`, request);
    }

    rechazarDescuento(id: number, request: AutorizacionResolucion): Observable<ApiResponse<AutorizacionResponse>> {
        return this.http.put<ApiResponse<AutorizacionResponse>>(`${this.apiUrl}/${id}/rechazar`, request);
    }

    listarPendientes(): Observable<ApiResponse<AutorizacionResponse[]>> {
        return this.http.get<ApiResponse<AutorizacionResponse[]>>(`${this.apiUrl}/estado/pendientes`);
    }

    listarResueltos(): Observable<ApiResponse<AutorizacionResponse[]>> {
        return this.http.get<ApiResponse<AutorizacionResponse[]>>(`${this.apiUrl}/estado/resueltos`);
    }

    listarPorCajero(idCajero: number): Observable<ApiResponse<AutorizacionResponse[]>> {
        return this.http.get<ApiResponse<AutorizacionResponse[]>>(`${this.apiUrl}/cajero/${idCajero}`);
    }
}

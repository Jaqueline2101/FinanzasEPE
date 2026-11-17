import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../shared/modals/response.model';
import { PagoComprobante } from '../models/pago-honorarios.model';

@Injectable({
    providedIn: 'root'
})
export class PagoHonorariosService {
    private apiUrl = `${environment.apiUrl}/pagos-honorarios`;

    constructor(private http: HttpClient) { }

    listar(): Observable<PagoComprobante[]> {
        return this.http.get<ApiResponse<PagoComprobante[]>>(this.apiUrl).pipe(
            map(response => response.success ? response.data : [])
        );
    }

    obtenerPorId(id: number): Observable<PagoComprobante | null> {
        return this.http.get<ApiResponse<PagoComprobante>>(`${this.apiUrl}/${id}`).pipe(
            map(response => response.success ? response.data : null)
        );
    }

    registrar(pago: PagoComprobante): Observable<ApiResponse<PagoComprobante>> {
        return this.http.post<ApiResponse<PagoComprobante>>(this.apiUrl, pago);
    }
}

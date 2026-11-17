import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../shared/modals/response.model';
import { PagoComprobante } from '../models/pago-honorarios.model';

import { PagoPersonalRequest } from '../models/pago-personal.model';

@Injectable({
    providedIn: 'root'
})
export class PagoPersonalService {

    private apiUrl = `${environment.apiUrl}/pagos-personal`;

    constructor(private http: HttpClient) { }

    listarPagos(): Observable<ApiResponse<PagoComprobante[]>> {
        return this.http.get<ApiResponse<PagoComprobante[]>>(this.apiUrl);
    }

    obtenerPorId(id: number): Observable<ApiResponse<PagoComprobante>> {
        return this.http.get<ApiResponse<PagoComprobante>>(`${this.apiUrl}/${id}`);
    }

    registrarPago(pago: PagoPersonalRequest): Observable<ApiResponse<PagoComprobante>> {
        return this.http.post<ApiResponse<PagoComprobante>>(this.apiUrl, pago);
    }

    registrarPagosMasivos(pagos: PagoPersonalRequest[]): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/bulk`, pagos);
    }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

import { RepDescuentoResponse, RepDescuentoParams } from '../models/rep-descuento.model';

export interface PageResponse<T> {
    content: T[];
    number: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class RepDescuentoService {
    private apiUrl = `${environment.apiUrl}/reportes/descuentos`;

    constructor(private http: HttpClient) { }

    obtenerReporte(params: RepDescuentoParams, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<RepDescuentoResponse>>> {
        let httpParams = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (params.fechaInicio) httpParams = httpParams.set('fechaInicio', params.fechaInicio);
        if (params.fechaFin) httpParams = httpParams.set('fechaFin', params.fechaFin);
        if (params.estado) httpParams = httpParams.set('estado', params.estado);
        if (params.query) httpParams = httpParams.set('query', params.query);

        return this.http.get<ApiResponse<PageResponse<RepDescuentoResponse>>>(this.apiUrl, { params: httpParams });
    }
}

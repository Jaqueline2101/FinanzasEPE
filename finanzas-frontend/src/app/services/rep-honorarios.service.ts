import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
}

export interface RepHonorariosRequestDTO {
    fechaInicio: string;
    fechaFin: string;
    tipoPago?: string;
    tipoBeneficiario?: string;
    estadoPago?: string;
    query?: string;
}

export interface RepHonorariosResponseDTO {
    idPago: number;
    fechaEmision: any;
    comprobante: string;
    beneficiario: string;
    documento: string;
    tipoPago: string;
    concepto: string;
    montoBruto: number;
    retencion: number;
    montoNeto: number;
    moneda: string;
    medioPago: string;
    estado: string;
}

export interface PageResponse<T> {
    content: T[];
    number: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export interface RepHonorariosResumenDTO {
    totalBruto: number;
    totalRetenciones: number;
    totalNeto: number;
    totalRegistros: number;
    detallePagos: PageResponse<RepHonorariosResponseDTO>;
}

@Injectable({
    providedIn: 'root'
})
export class RepHonorariosService {
    private apiUrl = `${environment.apiUrl}/reportes/honorarios`;

    constructor(private http: HttpClient) { }

    generarReporte(
        request: RepHonorariosRequestDTO,
        page: number = 0,
        size: number = 10
    ): Observable<ApiResponse<RepHonorariosResumenDTO>> {
        let params = new HttpParams()
            .set('fechaInicio', request.fechaInicio)
            .set('fechaFin', request.fechaFin)
            .set('page', page.toString())
            .set('size', size.toString());

        if (request.tipoPago) params = params.set('tipoPago', request.tipoPago);
        if (request.tipoBeneficiario) params = params.set('tipoBeneficiario', request.tipoBeneficiario);
        if (request.estadoPago) params = params.set('estadoPago', request.estadoPago);
        if (request.query) params = params.set('query', request.query);

        return this.http.get<ApiResponse<RepHonorariosResumenDTO>>(this.apiUrl, { params });
    }
}

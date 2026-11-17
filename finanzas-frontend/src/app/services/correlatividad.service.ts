import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CorrelatividadRequestDTO {
    idSucursal: number | null;
    anio: number;
    mes: number;
}

export interface CorrelatividadItemDTO {
    tipoDoc: string;
    serie: string;
    cantidad: number;
    desde: string;
    hasta: string;
    baseImp: number;
    igv: number;
    total: number;
    mes?: number;
}

export interface CorrelatividadResponseDTO {
    items: CorrelatividadItemDTO[];
    totalBaseImp: number;
    totalIgv: number;
    totalGeneral: number;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
}

export interface CorrelatividadDetalleRequestDTO {
    idSucursal: number | null;
    anio: number;
    mes: number;
    tipoDoc: string;
    serie: string;
}

@Injectable({
    providedIn: 'root'
})
export class CorrelatividadService {
    private apiUrl = `${environment.apiUrl}/reportes/correlatividad`;

    constructor(private http: HttpClient) { }

    obtenerResumen(request: CorrelatividadRequestDTO): Observable<ApiResponse<CorrelatividadResponseDTO>> {
        return this.http.post<ApiResponse<CorrelatividadResponseDTO>>(`${this.apiUrl}/resumen`, request);
    }

    obtenerDetalle(request: CorrelatividadDetalleRequestDTO): Observable<ApiResponse<any[]>> {
        return this.http.post<ApiResponse<any[]>>(`${this.apiUrl}/detalle`, request);
    }
}

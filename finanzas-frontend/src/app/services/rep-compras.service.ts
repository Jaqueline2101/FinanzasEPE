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

export interface RepComprasRequestDTO {
    fechaInicio: string;
    fechaFin: string;
    idSucursal?: number;
    idProveedor?: number;
    estado?: string;
}

export interface RepComprasResponseDTO {
    idCompra: number;
    fechaEmision: string;
    fechaRegistro: string;
    tipoComprobante: string;
    serie: string;
    correlativo: string;
    proveedor: string;
    rucProveedor: string;
    moneda: string;
    valorVentaGravado: number;
    valorVentaInafecto: number;
    igv: number;
    total: number;
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

export interface RepComprasResumenDTO {
    totalGravado: number;
    totalInafecto: number;
    totalExonerado: number;
    totalIGV: number;
    totalGeneral: number;
    detalleCompras: PageResponse<RepComprasResponseDTO>;
}

@Injectable({
    providedIn: 'root'
})
export class RepComprasService {
    private apiUrl = `${environment.apiUrl}/reportes/compras`;

    constructor(private http: HttpClient) { }

    generarReporte(
        request: RepComprasRequestDTO,
        page: number = 0,
        size: number = 10,
        sortBy: string = 'fechaEmision',
        direction: string = 'desc'
    ): Observable<RepComprasResumenDTO> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sortBy', sortBy)
            .set('direction', direction);

        return this.http.post<RepComprasResumenDTO>(
            this.apiUrl,
            request,
            { params }
        );
    }
}

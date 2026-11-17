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

export interface RepVentasRequestDTO {
    fechaInicio: string;
    fechaFin: string;
    idSucursal?: number;
    idPersonal?: number;
    estado?: string;
    query?: string;
    tipoAtencion?: string;
}

export interface RepVentasResponseDTO {
    idVenta: number;
    fecha: string;
    comprobante: string;
    tipoDoc: string;
    paciente: string;
    tipoAtencion: string;
    metodoPago: string;
    total: number;
    estado: string;
    cajeroId: number;
    cajeroNombre?: string;
}

export interface PageResponse<T> {
    content: T[];
    number: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export interface RepVentasResumenDTO {
    totalGeneral: number;
    totalEfectivo: number;
    totalTarjeta: number;
    totalAnulado: number;
    detalleVentas: PageResponse<RepVentasResponseDTO>;
}

@Injectable({
    providedIn: 'root' // Mantenemos el estándar de inyección raíz para servicios
})
export class RepVentasService {
    private apiUrl = `${environment.apiUrl}/reportes/ventas`;

    constructor(private http: HttpClient) { }

    generarReporte(
        request: RepVentasRequestDTO,
        page: number = 0,
        size: number = 10,
        sortStr: string = 'fechaDesc'
    ): Observable<ApiResponse<RepVentasResumenDTO>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sortStr', sortStr);

        return this.http.post<ApiResponse<RepVentasResumenDTO>>(
            `${this.apiUrl}/generar`,
            request,
            { params }
        );
    }
}

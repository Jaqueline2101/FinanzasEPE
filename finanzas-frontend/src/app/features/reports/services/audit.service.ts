import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { LabAuditoriaDto, AuditFilterDto } from '../models/audit.model';

@Injectable({
    providedIn: 'root'
})
export class AuditService {
    private apiUrl = `${environment.apiUrl}/reports/audit`;

    constructor(private http: HttpClient) { }

    /**
     * Buscar auditorías con filtros
     */
    searchAudits(filters: AuditFilterDto): Observable<ApiResponse<PageResponse<LabAuditoriaDto>>> {
        return this.http.post<ApiResponse<PageResponse<LabAuditoriaDto>>>(`${this.apiUrl}/search`, filters);
    }

    /**
     * Listar todas las auditorías (paginado)
     */
    getAllAudits(page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<LabAuditoriaDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', 'fecha,desc');

        return this.http.get<ApiResponse<PageResponse<LabAuditoriaDto>>>(this.apiUrl, { params });
    }

    /**
     * Listar auditorías de una tabla
     */
    getAuditsByTable(tableName: string, page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<LabAuditoriaDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<LabAuditoriaDto>>>(`${this.apiUrl}/table/${tableName}`, { params });
    }

    /**
     * Obtener historial de un registro específico
     */
    getRecordHistory(table: string, id: string): Observable<ApiResponse<LabAuditoriaDto[]>> {
        return this.http.get<ApiResponse<LabAuditoriaDto[]>>(`${this.apiUrl}/record/${table}/${id}`);
    }

    /**
     * Listar auditorías de un usuario
     */
    getUserAudits(userId: number, page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<LabAuditoriaDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<LabAuditoriaDto>>>(`${this.apiUrl}/user/${userId}`, { params });
    }

    /**
     * Listar auditorías por acción
     */
    getAuditsByAction(action: string, page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<LabAuditoriaDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<LabAuditoriaDto>>>(`${this.apiUrl}/action/${action}`, { params });
    }

    /**
     * Obtener últimas auditorías
     */
    getLatestAudits(cantidad: number = 50): Observable<ApiResponse<PageResponse<LabAuditoriaDto>>> {
        const params = new HttpParams().set('cantidad', cantidad.toString());
        return this.http.get<ApiResponse<PageResponse<LabAuditoriaDto>>>(`${this.apiUrl}/latest`, { params });
    }

    /**
     * Contar auditorías por tabla
     */
    countByTable(tableName: string): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/table/${tableName}/count`);
    }

    /**
     * Contar auditorías por usuario
     */
    countByUser(userId: number): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/user/${userId}/count`);
    }
}

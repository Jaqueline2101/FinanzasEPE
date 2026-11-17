import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../shared/modals/response.model';
import { SessionDto, SessionFilterDto } from '../models/audit.model';

@Injectable({
    providedIn: 'root'
})
export class SessionService {
    private apiUrl = `${environment.apiUrl}/reports/sessions`;

    constructor(private http: HttpClient) { }

    /**
     * Buscar sesiones con filtros
     */
    searchSessions(filters: SessionFilterDto): Observable<ApiResponse<PageResponse<SessionDto>>> {
        return this.http.post<ApiResponse<PageResponse<SessionDto>>>(`${this.apiUrl}/search`, filters);
    }

    /**
     * Listar todas las sesiones (paginado)
     */
    getAllSessions(page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<SessionDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', 'dtIngreso,desc');

        return this.http.get<ApiResponse<PageResponse<SessionDto>>>(this.apiUrl, { params });
    }

    /**
     * Listar sesiones activas
     */
    getActiveSessions(page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<SessionDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<SessionDto>>>(`${this.apiUrl}/active/page`, { params });
    }

    /**
     * Obtener sesión por ID
     */
    getSessionById(id: number): Observable<ApiResponse<SessionDto>> {
        return this.http.get<ApiResponse<SessionDto>>(`${this.apiUrl}/${id}`);
    }

    /**
     * Listar sesiones de un usuario
     */
    getUserSessions(userId: number, page: number = 0, size: number = 20): Observable<ApiResponse<PageResponse<SessionDto>>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<ApiResponse<PageResponse<SessionDto>>>(`${this.apiUrl}/user/${userId}`, { params });
    }

    /**
     * Obtener última sesión de un usuario
     */
    getLastUserSession(userId: number): Observable<ApiResponse<SessionDto>> {
        return this.http.get<ApiResponse<SessionDto>>(`${this.apiUrl}/user/${userId}/last`);
    }

    /**
     * Contar sesiones activas
     */
    countActiveSessions(): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/active/count`);
    }

    /**
     * Contar sesiones activas por usuario
     */
    countActiveUserSessions(userId: number): Observable<ApiResponse<number>> {
        return this.http.get<ApiResponse<number>>(`${this.apiUrl}/user/${userId}/active/count`);
    }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../shared/modals/response.model';
import { Sucursal } from '../../models/sucursal.model';

@Injectable({
    providedIn: 'root'
})
export class SucursalService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/sucursales`;

    getAll(page = 0, size = 100): Observable<ApiResponse<PageResponse<Sucursal>>> {
        return this.http.get<ApiResponse<PageResponse<Sucursal>>>(`${this.apiUrl}?page=${page}&size=${size}`);
    }

    // Add other methods if needed by other components, inferred names:
    create(data: any): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(this.apiUrl, data);
    }

    update(id: number, data: any): Observable<ApiResponse<any>> {
        return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${id}`, data);
    }

    delete(id: number): Observable<ApiResponse<any>> {
        return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${id}`);
    }
}

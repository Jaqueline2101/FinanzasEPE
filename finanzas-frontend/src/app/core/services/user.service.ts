import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse } from '../../features/auth/services/auth.service';
import { environment } from '../../../environments/environment';

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
}

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) { }

    /**
     * Cambia la contraseña de un usuario
     */
    changePassword(userId: number, request: ChangePasswordRequest): Observable<boolean> {
        const token = localStorage.getItem('token');
        if (!token) {
            return throwError(() => new Error('No existe token de autenticación'));
        }

        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${userId}/change-password`, request, { headers })
            .pipe(
                map(response => response.success),
                catchError(error => {
                    console.error('Error cambiando contraseña:', error);
                    return throwError(() => error);
                })
            );
    }

    /**
     * Resetea la contraseña de un usuario (Admin)
     */
    resetPassword(userId: number, newPassword: string, mustChangePassword: boolean = true): Observable<boolean> {
        return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${userId}/reset-password`, {
            newPassword,
            mustChangePassword
        }).pipe(
            map(response => response.success),
            catchError(error => {
                console.error('Error reseteando contraseña:', error);
                return throwError(() => error);
            })
        );
    }

    /**
     * Obtiene todos los usuarios con paginación
     */
    getAllUsers(page: number = 0, size: number = 10): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}?page=${page}&size=${size}`);
    }

    /**
     * Busca usuarios por nombre o DNI
     */
    searchUsers(query: string, page: number = 0, size: number = 10): Observable<ApiResponse<any>> {
        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/search?q=${query}&page=${page}&size=${size}`);
    }

    /**
     * Obtiene usuarios por rol
     */
    getUsersByRole(roleName: string): Observable<any[]> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`
        });

        return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/role/${roleName}`, { headers })
            .pipe(
                map(response => response.data),
                catchError(error => {
                    console.error(`Error obteniendo usuarios por rol ${roleName}:`, error);
                    return throwError(() => error);
                })
            );
    }
}

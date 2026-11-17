import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../shared/modals/response.model';
import {
    AssignDirectPermissionsRequest,
    TogglePermissionModeRequest,
    UserWithPermissionsResponse
} from '../models/user-permission.model';

@Injectable({
    providedIn: 'root'
})
export class UserPermissionService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/users`;

    getUserWithPermissions(userId: number): Observable<ApiResponse<UserWithPermissionsResponse>> {
        return this.http.get<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/${userId}/with-permissions`);
    }

    assignDirectPermissions(request: AssignDirectPermissionsRequest): Observable<ApiResponse<any>> {
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/permissions/assign`, request);
    }

    togglePermissionMode(request: TogglePermissionModeRequest): Observable<ApiResponse<UserWithPermissionsResponse>> {
        return this.http.put<ApiResponse<UserWithPermissionsResponse>>(`${this.apiUrl}/permissions/toggle-mode`, request);
    }

    clearAllDirectPermissions(userId: number): Observable<ApiResponse<any>> {
        return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${userId}/permissions/all`);
    }
}

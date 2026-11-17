import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  UserResponse,
  CreateUserRequest,
  UpdateUserRequest,
  ChangePasswordRequest,
  UserStats
} from '../models/user.model';
import { Sucursal } from '../models/sucursal.model';
import { ApiResponse, PageResponse } from '../shared/modals/response.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  // ========================================
  // CRUD BÁSICO
  // ========================================

  createUser(userData: CreateUserRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(this.apiUrl, userData);
  }

  promoteUser(employeeId: number, userData: CreateUserRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/${employeeId}/promote`, userData);
  }

  updateUser(id: number, userData: UpdateUserRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}`, userData);
  }

  getUserById(id: number): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}`);
  }

  deleteUser(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  uploadPhoto(id: number, file: File): Observable<ApiResponse<UserResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}/photo`, formData);
  }

  // ========================================
  // LISTADOS PAGINADOS Y BÚSQUEDA
  // ========================================

  private getPaginationParams(page: number, size: number, sort: string, direction: string = 'asc'): HttpParams {
    return new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort},${direction}`);
  }

  getAllUsers(
    page = 0,
    size = 10,
    sort = 'nombre',
    direction = 'asc'
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, sort, direction);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(this.apiUrl, { params });
  }

  getActiveUsers(page = 0, size = 10, sort = 'nombre', direction = 'asc'): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, sort, direction);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/active`, {
      params,
    });
  }

  getUsersByRole(
    roleName: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre');
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(
      `${this.apiUrl}/role/${roleName}`,
      { params }
    );
  }

  searchUsers(q: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre').set('q', q);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/search`, {
      params,
    });
  }

  buscarPersonas(q: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre').set('q', q);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/buscar-personas`, {
      params,
    });
  }

  searchByFilter(
    q: string,
    type: string,
    page = 0,
    size = 10
  ): Observable<ApiResponse<PageResponse<UserResponse>>> {
    const params = this.getPaginationParams(page, size, 'nombre').set('query', q).set('type', type);
    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(`${this.apiUrl}/filter`, {
      params,
    });
  }

  // ========================================
  // GESTIÓN DE SEGURIDAD Y ESTADO
  // ========================================

  /**
   * Activar o desactivar un usuario mediante PATCH
   */
  toggleUserStatus(id: number, active: boolean): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('active', active.toString());
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/status`, {}, { params });
  }

  getUserStats(): Observable<ApiResponse<UserStats>> {
    return this.http.get<ApiResponse<UserStats>>(`${this.apiUrl}/stats`);
  }

  /**
   * Cambiar contraseña (puede ser llamado por el admin o por el propio usuario)
   */
  changePassword(id: number, request: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/change-password`, request);
  }

  // ========================================
  // GESTIÓN DE ROLES
  // ========================================

  assignRoles(id: number, roleIds: number[]): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}/roles`, roleIds);
  }

  /**
   * Remueve roles específicos (Envía los IDs en el cuerpo de la petición DELETE)
   */
  removeRoles(id: number, roleIds: number[]): Observable<ApiResponse<UserResponse>> {
    return this.http.delete<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}/roles`, {
      body: roleIds,
    });
  }
  /** Obtener sucursales asignadas a un usuario */
  getSucursalesAsignadas(userId: number): Observable<ApiResponse<Sucursal[]>> {
    return this.http.get<ApiResponse<Sucursal[]>>(`${this.apiUrl}/${userId}/sucursales`);
  }

  /** Asignar sucursales a un usuario */
  assignSucursales(userId: number, sucursalIds: number[]): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${userId}/sucursales`, sucursalIds);
  }

  /** Remover sucursales de un usuario */
  removeSucursales(userId: number, sucursalIds: number[]): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${userId}/sucursales`, {
      body: sucursalIds,
    });
  }

  /** Verificar si un DNI ya existe */
  checkNumdocExists(numdoc: string): Observable<ApiResponse<boolean>> {
    const params = new HttpParams().set('numdoc', numdoc);
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/check-numdoc`, { params });
  }

  /** Verificar si un Email ya existe */
  checkEmailExists(email: string): Observable<ApiResponse<boolean>> {
    const params = new HttpParams().set('email', email);
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/check-email`, { params });
  }

  private buildPageParams(page: number, size: number, sort: string, direction: string): HttpParams {
    return new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sort},${direction}`); // Spring prefiere este formato para el Pageable
  }
}

import { Injectable, signal, computed, effect, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of, tap, map, catchError, finalize, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { MenuDinamicoService } from '../../../shared/sidebar/menu-dinamico.service';
import { PermissionService2 } from './permission2.service';
import { environment } from '../../../../environments/environment';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
}

export interface TokenInfo {
    timeRemainingMs: number;
    timeRemainingSeconds: number;
    expirationTimeMs: number;
    isExpired: boolean;
    token?: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    content: T;
    timestamp: string;
}

export interface LoginData {
    token: string;
    type: string;
    userId: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    sexo: string;
    roles: string[];
    permissions: string[];
    sessionTimeoutMs: number;
    fotoPerfil?: string;
    sucursalId?: number;
    sucursalNombre?: string;
    puntoId?: number;
    puntoNombre?: string;
    mustChangePassword?: boolean;
}

export interface Usuario {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    rol: string;
    token: string;
    sexo: string;
    permissions: string[];
    fotoPerfil?: string;
    sucursalActualId?: number;
    sucursalActualNombre?: string;
    puntoActualId?: number;
    puntoActualNombre?: string;
    mustChangePassword?: boolean;
}

// ... existing code ...

// ... existing code ...

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    public static readonly ROLE_CAJERO = 'CAJERO';

    private apiUrl = `${environment.apiUrl}/auth`;
    private http = inject(HttpClient);
    private router = inject(Router);
    private menuService = inject(MenuDinamicoService);
    private permissionService = inject(PermissionService2);

    // ✅ SIGNALS STATE
    public currentUser = signal<Usuario | null>(null);
    public currentSucursal = signal<string | null>(null);
    public currentPunto = signal<string | null>(null);
    public loading = signal<boolean>(false);

    // ✅ COMPUTED VALUES
    public isAuthenticated = computed(() => !!this.currentUser() && !!this.currentUser()?.token);
    public userFullName = computed(() => {
        const user = this.currentUser();
        return user ? `${user.firstName} ${user.lastName}` : '';
    });

    constructor() {
        // Inicializar estado desde localStorage
        const userStorage = localStorage.getItem('currentUser');
        if (userStorage) {
            this.currentUser.set(JSON.parse(userStorage));
        }

        const userId = localStorage.getItem('userId');
        if (userId) {
            const savedSucursal = localStorage.getItem(`sucursalActual_${userId}`);
            if (savedSucursal) {
                this.currentSucursal.set(savedSucursal);
            }
            const savedSucursalId = localStorage.getItem(`sucursalActualId_${userId}`);
            if (savedSucursalId) {
                const user = this.currentUser();
                if (user) {
                    this.currentUser.set({ ...user, sucursalActualId: parseInt(savedSucursalId) });
                }
            }
            const savedPunto = localStorage.getItem(`puntoActual_${userId}`);
            if (savedPunto) {
                this.currentPunto.set(savedPunto);
            }
        }
    }

    // ✅ LOGIN
    login(username: string, password: string): Observable<LoginData> {
        this.loading.set(true);
        return this.http.post<ApiResponse<LoginData>>(`${this.apiUrl}/login`, {
            login: username,
            password
        }).pipe(
            map(apiResponse => apiResponse.data),
            tap(loginData => {
                console.log('✅ [AUTH] Login exitoso:', loginData);
                this.handleLoginSuccess(loginData);
            }),
            catchError(error => {
                console.error('❌ [AUTH] Error en login:', error);
                return throwError(() => error);
            }),
            finalize(() => this.loading.set(false))
        );
    }

    // ✅ REGISTER
    register(data: RegisterRequest): Observable<ApiResponse<any>> {
        this.loading.set(true);
        return this.http.post<ApiResponse<any>>(`${this.apiUrl}/register`, data).pipe(
            tap(response => console.log('✅ [AUTH] Registro exitoso:', response)),
            catchError(error => {
                console.error('❌ [AUTH] Error en registro:', error);
                return throwError(() => error);
            }),
            finalize(() => this.loading.set(false))
        );
    }

    private handleLoginSuccess(loginData: LoginData) {
        const rolPrincipal = loginData.roles && loginData.roles.length > 0 ? loginData.roles[0] : 'USUARIO';

        const user: Usuario = {
            id: loginData.userId,
            username: loginData.username,
            rol: rolPrincipal,
            token: loginData.token,
            sexo: loginData.sexo,
            firstName: loginData.firstName,
            lastName: loginData.lastName,
            permissions: loginData.permissions,
            fotoPerfil: loginData.fotoPerfil,
            sucursalActualId: loginData.sucursalId,
            sucursalActualNombre: loginData.sucursalNombre,
            puntoActualId: loginData.puntoId,
            puntoActualNombre: loginData.puntoNombre,
            mustChangePassword: loginData.mustChangePassword
        };

        // Guardar en LocalStorage
        localStorage.setItem('currentUser', JSON.stringify(user));
        localStorage.setItem('token', loginData.token);
        if (loginData.userId) {
            localStorage.setItem('userId', loginData.userId.toString());

            // 🔄 PERSISTENCIA: Restaurar sucursal y punto si vienen del backend
            if (loginData.sucursalNombre) {
                localStorage.setItem(`sucursalActual_${loginData.userId}`, loginData.sucursalNombre);
                this.currentSucursal.set(loginData.sucursalNombre);
            }
            if (loginData.sucursalId) {
                localStorage.setItem(`sucursalActualId_${loginData.userId}`, loginData.sucursalId.toString());
            }
            if (loginData.puntoNombre) {
                localStorage.setItem(`puntoActual_${loginData.userId}`, loginData.puntoNombre);
                this.currentPunto.set(loginData.puntoNombre);
            }
        }

        if (loginData.firstName && loginData.lastName) {
            localStorage.setItem('userFullName', `${loginData.firstName} ${loginData.lastName}`);
        }

        // Actualizar Signals y Servicios
        this.currentUser.set(user);
        this.permissionService.setUser(loginData);

        this.menuService.cargarMenu(loginData.userId).subscribe({
            next: () => console.log('✅ [AUTH] Menú cargado'),
            error: (err) => console.error('❌ [AUTH] Error cargando menú:', err)
        });
    }

    // ✅ UPDATE PHOTO
    updateCurrentUserPhoto(photoUrl: string): void {
        const currentUser = this.currentUser();
        if (currentUser) {
            const updatedUser = { ...currentUser, fotoPerfil: photoUrl };
            this.currentUser.set(updatedUser);
            localStorage.setItem('currentUser', JSON.stringify(updatedUser));
        }
    }

    // ✅ LOGOUT
    logout(): void {
        const token = localStorage.getItem('token');
        if (token) {
            const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
            this.http.post(`${this.apiUrl}/logout`, {}, { headers }).pipe(
                finalize(() => this.doLogoutCleanup())
            ).subscribe();
        } else {
            this.doLogoutCleanup();
        }
    }

    private doLogoutCleanup(): void {
        const userId = localStorage.getItem('userId');

        localStorage.removeItem('currentUser');
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('userFullName');

        if (userId) {
            localStorage.removeItem(`sucursalActual_${userId}`);
            localStorage.removeItem(`sucursalActualId_${userId}`);
            localStorage.removeItem(`puntoActual_${userId}`);
        }

        // Limpiar estado
        this.currentUser.set(null);
        this.currentSucursal.set(null);
        this.permissionService.clearPermisos();
        this.menuService.clearMenu();

        this.router.navigate(['/login']);
    }

    // ✅ SUCURSALES (Mantenemos Observables para peticiones HTTP)
    getSucursalesAutorizadas(userId: number): Observable<ApiResponse<any[]>> {
        return this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/users/${userId}/sucursales`);
    }

    seleccionarSucursal(userId: number, sucursalId: number, password: string, puntoId?: number): Observable<ApiResponse<{ token: string; sucursalNombre: string, puntoNombre?: string }>> {
        return this.http.post<ApiResponse<{ token: string; sucursalNombre: string, puntoNombre?: string }>>(
            `${this.apiUrl}/current-sucursal`,
            { userId, sucursalId, password, puntoId }
        ).pipe(
            tap(response => {
                if (response.success && response.data) {
                    console.log('✅ [AUTH] Sucursal seleccionada:', response.data.sucursalNombre);

                    localStorage.setItem('token', response.data.token);
                    if (userId) {
                        localStorage.setItem(`sucursalActual_${userId}`, response.data.sucursalNombre);
                        localStorage.setItem(`sucursalActualId_${userId}`, sucursalId.toString());
                        if (response.data.puntoNombre) {
                            localStorage.setItem(`puntoActual_${userId}`, response.data.puntoNombre);
                        } else {
                            localStorage.removeItem(`puntoActual_${userId}`);
                        }
                    }

                    // Actualizar Signal
                    this.currentSucursal.set(response.data.sucursalNombre);
                    this.currentPunto.set(response.data.puntoNombre || null);

                    // Actualizar usuario con nuevo token y nueva sucursal
                    const currentUser = this.currentUser();
                    if (currentUser) {
                        this.currentUser.set({
                            ...currentUser,
                            token: response.data.token,
                            sucursalActualId: sucursalId,
                            sucursalActualNombre: response.data.sucursalNombre,
                            puntoActualId: puntoId || currentUser.puntoActualId,
                            puntoActualNombre: response.data.puntoNombre || currentUser.puntoActualNombre
                        });
                    }
                }
            })
        );
    }

    // ✅ UTILS
    getSucursalFromToken(): string | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.sucursalNombre || null;
        } catch (error) {
            console.error('Error decodificando token:', error);
            return null;
        }
    }
    private decodeToken(token: string): any {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            return JSON.parse(jsonPayload);
        } catch (error) {
            console.error('Error al decodificar token UTF-8:', error);
            return null;
        }
    }
    getSucursalIdFromToken(): number | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload && payload.sucursalId ? Number(payload.sucursalId) : null;
    }

    getUsuarioIdFromToken(): string | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        const payload = this.decodeToken(token);
        return payload ? payload.sub || payload.username || null : null;
    }
    getPuntoIdFromToken(): number | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.puntoId || null;
        } catch (error) {
            return null;
        }
    }

    getPuntoNombreFromToken(): string | null {
        const token = localStorage.getItem('token');
        if (!token) return null;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.puntoNombre || null;
        } catch (error) {
            return null;
        }
    }

    hasAnyPermission(permissions: string[]): boolean {
        const user = this.currentUser();
        if (!user || !user.permissions) return false;
        return permissions.some(permission => user.permissions.includes(permission));
    }

    hasAllPermissions(permissions: string[]): boolean {
        const user = this.currentUser();
        if (!user || !user.permissions) return false;
        return permissions.every(permission => user.permissions.includes(permission));
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    // ✅ RESTORED METHODS FOR COMPATIBILITY

    private sucursalUpdatedSubject = new BehaviorSubject<boolean>(false);
    public onSucursalUpdated$ = this.sucursalUpdatedSubject.asObservable();

    confirmSucursalUpdated(): void {
        this.sucursalUpdatedSubject.next(true);
    }

    getTokenInfo(): Observable<ApiResponse<TokenInfo>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
        return this.http.get<ApiResponse<TokenInfo>>(`${this.apiUrl}/token-info`, { headers });
    }

    getSessionTimeout(): Observable<number> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
        return this.http.get<ApiResponse<{ valor: number }>>(`${this.apiUrl}/timeout`, { headers })
            .pipe(map(response => response.data.valor));
    }

    updateSessionTimeout(minutes: number): Observable<void> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
        return this.http.put<void>(`${this.apiUrl}/update-timeout`, { minutes }, { headers });
    }

    refreshToken(): Observable<ApiResponse<TokenInfo>> {
        const token = localStorage.getItem('token');
        const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
        return this.http.post<ApiResponse<TokenInfo>>(`${this.apiUrl}/refresh-token`, {}, { headers })
            .pipe(
                tap(response => {
                    if (response.success && response.data && response.data.token) {
                        localStorage.setItem('token', response.data.token);
                    }
                })
            );
    }

    verificarToken(): Observable<boolean> {
        const token = localStorage.getItem('token');
        if (!token) return of(false);
        return this.http.get<{ valid: boolean }>(`${this.apiUrl}/verificar-token`).pipe(
            map(response => response.valid),
            catchError(() => {
                this.logout();
                return of(false);
            })
        );
    }

    cambiarContraseña(contraseñaActual: string, contraseñaNueva: string): Observable<any> {
        return this.http.put(`${this.apiUrl}/cambiar-contraseña`, {
            contraseñaActual,
            contraseñaNueva
        });
    }
}
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

    // ✅ SESIÓN DIRECTA ADMINISTRADOR
    setSesionAdministradorDirecto(): void {
        const dummyToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin_access_token_demo';
        const adminData: LoginData = {
            token: dummyToken,
            type: 'Bearer',
            userId: 1,
            username: 'admin',
            email: 'admin@finanzas.pe',
            firstName: 'Administrador',
            lastName: 'Sistema',
            sexo: 'M',
            roles: ['SUPER_ADMIN', 'ADMIN'],
            permissions: [
                'ACCESO_TOTAL', 'DASHBOARD_VER', 'USUARIOS_VER', 'REPORTES_VER',
                'VENTAS_VER', 'COMPRAS_VER', 'CAJA_VER', 'CONFIGURACION_VER',
                'ADMIN', 'SUPER_ADMIN', 'GESTION_USUARIOS', 'ASIGNAR_ROLES'
            ],
            sessionTimeoutMs: 1800000,
            fotoPerfil: 'default-avatar.png',
            sucursalId: 1,
            sucursalNombre: 'Sede Principal',
            puntoId: 1,
            puntoNombre: 'Caja 1',
            mustChangePassword: false
        };

        const defaultMenu = [
            {
                id: 1,
                titulo: 'Dashboard',
                ruta: '/admin/dashboard',
                icono: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-layout-dashboard"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>',
                orden: 1,
                tipo: 'ITEM' as const
            },
            {
                id: 2,
                titulo: 'Reportes',
                icono: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-clipboard-plus"><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M9 14h6"/><path d="M12 17v-6"/></svg>',
                orden: 2,
                tipo: 'GRUPO' as const,
                children: [
                    { id: 21, titulo: 'Ventas', ruta: '/admin/reportes/ventas', orden: 1, tipo: 'ITEM' as const },
                    { id: 22, titulo: 'Compras', ruta: '/admin/reportes/compras', orden: 2, tipo: 'ITEM' as const },
                    { id: 23, titulo: 'Descuentos', ruta: '/admin/reportes/descuentos', orden: 3, tipo: 'ITEM' as const },
                    { id: 24, titulo: 'Honorarios', ruta: '/admin/reportes/honorarios', orden: 4, tipo: 'ITEM' as const },
                    { id: 25, titulo: 'Pagos Personal', ruta: '/admin/reportes/pagos', orden: 5, tipo: 'ITEM' as const },
                    { id: 26, titulo: 'Otros Pagos', ruta: '/admin/reportes/otros-pagos', orden: 6, tipo: 'ITEM' as const },
                    { id: 27, titulo: 'Auditoría', ruta: '/admin/reportes/auditoria', orden: 7, tipo: 'ITEM' as const },
                    { id: 28, titulo: 'Correlatividad', ruta: '/admin/reportes/correlatividad', orden: 8, tipo: 'ITEM' as const },
                    { id: 29, titulo: 'Kardex', ruta: '/admin/reportes/kardex', orden: 9, tipo: 'ITEM' as const },
                    { id: 30, titulo: 'Valorización', ruta: '/admin/reportes/valorizacion', orden: 10, tipo: 'ITEM' as const },
                    { id: 31, titulo: 'Comprobantes Anulados', ruta: '/admin/reportes/comprobantes-anulados', orden: 11, tipo: 'ITEM' as const },
                    { id: 32, titulo: 'Empresas Atendidas', ruta: '/admin/reportes/empresas-atendidas', orden: 12, tipo: 'ITEM' as const }
                ]
            },
            {
                id: 3,
                titulo: 'Procesos',
                icono: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-cpu"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><path d="M15 2v2"/><path d="M15 20v2"/><path d="M2 15h2"/><path d="M2 9h2"/><path d="M20 15h2"/><path d="M20 9h2"/><path d="M9 2v2"/><path d="M9 20v2"/></svg>',
                orden: 3,
                tipo: 'GRUPO' as const,
                children: [
                    { id: 41, titulo: 'Punto de Venta', ruta: '/admin/procesos/punto-venta', orden: 1, tipo: 'ITEM' as const },
                    { id: 42, titulo: 'Productos', ruta: '/admin/procesos/productos', orden: 2, tipo: 'ITEM' as const },
                    { id: 43, titulo: 'Reimpresión', ruta: '/admin/procesos/reimpresion', orden: 3, tipo: 'ITEM' as const }
                ]
            },
            {
                id: 4,
                titulo: 'Caja Chica',
                icono: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-wallet"><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/><path d="M18 12a2 2 0 0 0 0 4h4v-4Z"/></svg>',
                orden: 4,
                tipo: 'GRUPO' as const,
                children: [
                    { id: 51, titulo: 'Movimientos', ruta: '/admin/caja-chica/movimientos', orden: 1, tipo: 'ITEM' as const },
                    { id: 52, titulo: 'Ingresos y Gastos', ruta: '/admin/caja-chica/ingresos-gastos', orden: 2, tipo: 'ITEM' as const },
                    { id: 53, titulo: 'Arqueo de Caja', ruta: '/admin/caja-chica/arqueo-caja', orden: 3, tipo: 'ITEM' as const }
                ]
            },
            {
                id: 5,
                titulo: 'Configuraciones',
                icono: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-settings"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.1a2 2 0 0 1-1-1.72v-.51a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>',
                orden: 5,
                tipo: 'GRUPO' as const,
                children: [
                    {
                        id: 61,
                        titulo: 'Seguridad',
                        tipo: 'SUBMENU' as const,
                        orden: 1,
                        children: [
                            { id: 611, titulo: 'Usuarios', ruta: '/admin/configuraciones/seguridad/users', orden: 1, tipo: 'ITEM' as const },
                            { id: 612, titulo: 'Roles', ruta: '/admin/configuraciones/seguridad/roles', orden: 2, tipo: 'ITEM' as const },
                            { id: 613, titulo: 'Permisos', ruta: '/admin/configuraciones/seguridad/permisos', orden: 3, tipo: 'ITEM' as const }
                        ]
                    },
                    { id: 62, titulo: 'Sucursales', ruta: '/admin/configuraciones/sucursales', orden: 2, tipo: 'ITEM' as const },
                    { id: 63, titulo: 'Niveles', ruta: '/admin/configuraciones/niveles', orden: 3, tipo: 'ITEM' as const },
                    { id: 64, titulo: 'Catálogo', ruta: '/admin/configuraciones/catalogo', orden: 4, tipo: 'ITEM' as const },
                    { id: 65, titulo: 'Paquetes', ruta: '/admin/configuraciones/paquetes', orden: 5, tipo: 'ITEM' as const },
                    { id: 66, titulo: 'Empresas', ruta: '/admin/configuraciones/empresas', orden: 6, tipo: 'ITEM' as const },
                    { id: 67, titulo: 'Beneficios Especiales', ruta: '/admin/configuraciones/beneficios-especiales', orden: 7, tipo: 'ITEM' as const },
                    { id: 68, titulo: 'Plan de Cuentas', ruta: '/admin/configuraciones/plan-cuentas', orden: 8, tipo: 'ITEM' as const },
                    { id: 69, titulo: 'Cuentas Convenio', ruta: '/admin/configuraciones/cuentas-convenio', orden: 9, tipo: 'ITEM' as const },
                    { id: 70, titulo: 'Autorizar Descuentos', ruta: '/admin/configuraciones/autorizar-descuentos', orden: 10, tipo: 'ITEM' as const },
                    { id: 71, titulo: 'Cambio Sucursal', ruta: '/admin/cambio-sucursal', orden: 11, tipo: 'ITEM' as const }
                ]
            }
        ];

        localStorage.setItem('userMenu', JSON.stringify(defaultMenu));
        this.handleLoginSuccess(adminData);
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
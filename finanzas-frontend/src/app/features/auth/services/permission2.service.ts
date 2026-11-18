import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';

export interface PermisosUsuario {
    permisos: Set<string>;
    modulos: Set<string>;
    isAdmin: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class PermissionService2 {
    // ✅ CAMBIADO: Ahora usa el endpoint correcto
    private readonly API_URL = `${environment.apiUrl}/users`;

    private permisosSubject = new BehaviorSubject<Set<string>>(new Set());
    private modulosSubject = new BehaviorSubject<Set<string>>(new Set());
    private isAdminSubject = new BehaviorSubject<boolean>(false);
    private usuarioId: number | null = null;

    public permisos$ = this.permisosSubject.asObservable();
    public modulos$ = this.modulosSubject.asObservable();
    public isAdmin$ = this.isAdminSubject.asObservable();

    constructor(private http: HttpClient) {
        this.cargarPermisosDesdeStorage();
    }

    /**
     * Configura el usuario y carga sus permisos desde el backend
     */
    setUser(userData: any): void {
        this.usuarioId = userData.userId || userData.id;

        // Si los permisos vienen en effectivePermissionNames (desde el login)
        if (userData.effectivePermissionNames) {
            const permisosSet = new Set<string>(userData.effectivePermissionNames);
            this.permisosSubject.next(permisosSet);
            localStorage.setItem('userPermisos', JSON.stringify([...permisosSet]));
        }
        // Si vienen en permissions (formato alternativo)
        else if (userData.permissions) {
            const permisosSet = new Set<string>(userData.permissions);
            this.permisosSubject.next(permisosSet);
            localStorage.setItem('userPermisos', JSON.stringify([...permisosSet]));
        }

        // Verificar si es admin
        const roles = userData.roles || userData.roleNames || [];
        const isAdmin = roles.includes('ADMIN') || roles.includes('ADMINISTRADOR') || roles.includes('SUPER_ADMIN');
        this.isAdminSubject.next(isAdmin);
        localStorage.setItem('isAdmin', isAdmin.toString());

        // Cargar permisos frescos del backend
        if (this.usuarioId) {
            this.cargarPermisosDelBackend(this.usuarioId);
        }
    }

    /**
     * Carga los permisos desde localStorage al iniciar
     */
    private cargarPermisosDesdeStorage(): void {
        try {
            const permisosGuardados = localStorage.getItem('userPermisos');
            if (permisosGuardados) {
                const permisosArray = JSON.parse(permisosGuardados) as string[];
                this.permisosSubject.next(new Set<string>(permisosArray));
            }

            const isAdminGuardado = localStorage.getItem('isAdmin');
            if (isAdminGuardado) {
                this.isAdminSubject.next(isAdminGuardado === 'true');
            }

            const userData = localStorage.getItem('currentUser');
            if (userData) {
                const user = JSON.parse(userData);
                this.usuarioId = user.userId || user.id;
            }
        } catch (e) {
            console.error('Error cargando permisos desde storage:', e);
        }
    }

    /**
     * Carga los permisos actualizados desde el backend
     * ✅ CAMBIADO: Usa el endpoint correcto /api/users/{id}/permissions
     */
    private cargarPermisosDelBackend(usuarioId: number): void {
        // Obtener permisos efectivos (nombres)
        this.http.get<any>(`${this.API_URL}/${usuarioId}/permissions`).subscribe({
            next: (response) => {
                const permisos = response.data || [];
                const permisosSet = new Set<string>(permisos);
                this.permisosSubject.next(permisosSet);
                localStorage.setItem('userPermisos', JSON.stringify([...permisosSet]));

                // Extraer módulos de los permisos
                this.extraerModulos(permisos);
            },
            error: (err) => {
                console.error('Error cargando permisos:', err);
                // Intentar extraer módulos de los permisos que ya tenemos
                this.extraerModulos(Array.from(this.permisosSubject.value));
            }
        });
    }

    /**
     * Extrae los módulos únicos de los permisos
     */
    private extraerModulos(permisos: string[]): void {
        const modulos = new Set<string>();

        permisos.forEach(permiso => {
            // Asume formato: MODULO_ACCION (ej: USUARIO_CREAR)
            const partes = permiso.split('_');
            if (partes.length >= 2) {
                modulos.add(partes[0]); // Primer parte es el módulo
            }
        });

        this.modulosSubject.next(modulos);
    }

    /**
     * Refresca los permisos desde el backend
     */
    refreshPermisos(): Observable<void> {
        if (!this.usuarioId) {
            throw new Error('Usuario no configurado');
        }

        return this.http.get<any>(`${this.API_URL}/${this.usuarioId}/permissions`).pipe(
            tap(response => {
                const permisos: string[] = response.data || [];
                const permisosSet = new Set<string>(permisos);
                this.permisosSubject.next(permisosSet);
                localStorage.setItem('userPermisos', JSON.stringify([...permisosSet]));
                this.extraerModulos(permisos);
            }),
            map(() => void 0)
        );
    }

    // ========================================
    // MÉTODOS DE VERIFICACIÓN DE PERMISOS
    // ========================================

    isAdmin(): boolean {
        if (this.isAdminSubject.value || this.permisosSubject.value.has('ACCESO_TOTAL')) {
            return true;
        }
        try {
            if (localStorage.getItem('isAdmin') === 'true') return true;
            const userStr = localStorage.getItem('currentUser');
            if (userStr) {
                const u = JSON.parse(userStr);
                const username = (u.username || u.login || u.nombreUsuario || '').toLowerCase();
                if (username === 'admin' || username === 'superadmin') return true;
                const roles = u.roles || u.roleNames || [];
                if (Array.isArray(roles) && roles.some(r => typeof r === 'string' && r.toUpperCase().includes('ADMIN'))) {
                    return true;
                }
            }
        } catch (e) {}
        return false;
    }

    hasPermission(permiso: string): boolean {
        if (this.isAdmin()) return true;
        return this.permisosSubject.value.has(permiso);
    }

    hasAllPermissions(permisos: string[]): boolean {
        if (this.isAdmin()) return true;
        const userPermisos = this.permisosSubject.value;
        return permisos.every(p => userPermisos.has(p));
    }

    hasAnyPermission(permisos: string[]): boolean {
        if (this.isAdmin()) return true;
        const userPermisos = this.permisosSubject.value;
        return permisos.some(p => userPermisos.has(p));
    }

    hasModuleAccess(modulo: string): boolean {
        if (this.isAdmin()) return true;
        const userPermisos = this.permisosSubject.value;
        return Array.from(userPermisos).some(p => p.startsWith(modulo + '_'));
    }

    canCreate(modulo: string): boolean {
        return this.hasPermission(`${modulo}_CREAR`);
    }

    canRead(modulo: string): boolean {
        return this.hasPermission(`${modulo}_LEER`);
    }

    canUpdate(modulo: string): boolean {
        return this.hasPermission(`${modulo}_ACTUALIZAR`);
    }

    canDelete(modulo: string): boolean {
        return this.hasPermission(`${modulo}_ELIMINAR`);
    }

    getModuleCRUDPermissions(modulo: string): Observable<any> {
        if (!this.usuarioId) {
            throw new Error('Usuario no configurado');
        }
        return this.http.get(`${this.API_URL}/${this.usuarioId}/crud?modulo=${modulo}`);
    }

    clearPermisos(): void {
        this.permisosSubject.next(new Set());
        this.modulosSubject.next(new Set());
        this.isAdminSubject.next(false);
        this.usuarioId = null;
        localStorage.removeItem('userPermisos');
        localStorage.removeItem('isAdmin');
    }

    getPermisos(): Set<string> {
        return this.permisosSubject.value;
    }

    getModulos(): Set<string> {
        return this.modulosSubject.value;
    }
}
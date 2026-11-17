import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../features/auth/services/auth.service';

@Injectable({
    providedIn: 'root'
})
export class PuntoVentaCheckGuard {
    constructor(
        private authService: AuthService,
        private router: Router
    ) { }

    canActivate(
        route: import('@angular/router').ActivatedRouteSnapshot,
        state: import('@angular/router').RouterStateSnapshot
    ):
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {

        // 0. Si ya estamos en la página de cambio de sucursal, permitir siempre
        // Esto evita bucles infinitos
        if (state.url.includes('cambio-sucursal')) {
            return true;
        }

        // 1. Verificar si el usuario es CAJERO
        // Si NO es cajero, permitir acceso directo (no requiere punto de venta)
        const currentUser = this.authService.currentUser();
        // Verificar 'rol' (singular) y también 'roles' (plural) si existiera en el objeto runtime aunque no en la interfaz
        const userAny = currentUser as any;
        const esCajero = currentUser?.rol === AuthService.ROLE_CAJERO || (userAny?.roles && userAny.roles.includes(AuthService.ROLE_CAJERO));

        if (!esCajero) {
            return true;
        }

        // 2. Verificar si hay un punto seleccionado en el servicio (Signal)
        const currentPunto = this.authService.currentPunto();
        if (currentPunto) {
            return true;
        }

        // 3. Verificar en localStorage como respaldo
        const userId = localStorage.getItem('userId');
        if (userId) {
            const savedPunto = localStorage.getItem(`puntoActual_${userId}`);
            if (savedPunto) {
                // Si existe en LS pero no en Signal, actualizar Signal y permitir
                this.authService.currentPunto.set(savedPunto);
                return true;
            }
        }

        // 4. Si no hay punto seleccionado, redirigir a selección con mensaje
        console.warn('🚧 [GUARD] Acceso denegado: Se requiere seleccionar un Punto de Venta.');
        // Importante: La ruta correcta es /admin/cambio-sucursal
        this.router.navigate(['/admin/cambio-sucursal'], {
            queryParams: {
                error: 'punto_requerido',
                t: new Date().getTime()
            }
        });
        return false;
    }

    canActivateChild(
        route: import('@angular/router').ActivatedRouteSnapshot,
        state: import('@angular/router').RouterStateSnapshot
    ):
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {
        return this.canActivate(route, state);
    }
}

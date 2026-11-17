import { Injectable } from '@angular/core';
import { Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../features/auth/services/auth.service';
import { PermissionService2 } from '../../features/auth/services/permission2.service';
import { map, take } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard {
    constructor(
        private authService: AuthService,
        private permissionService: PermissionService2,
        private router: Router
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {

        const currentUser = this.authService.currentUser();

        if (currentUser && currentUser.token) {
            // 1. Verificar si la ruta requiere permisos específicos
            const requiredPermission = route.data['permission'] as string;
            
            if (requiredPermission) {
                return this.permissionService.permisos$.pipe(
                    take(1),
                    map(perms => {
                        if (perms.has(requiredPermission)) {
                            return true;
                        } else {
                            console.warn(`⛔ Acceso denegado a la ruta ${state.url}. Falta el permiso: ${requiredPermission}`);
                            this.router.navigate(['/admin/error/403']); // Redirigir a una página de acceso denegado
                            return false;
                        }
                    })
                );
            }
            
            // 2. Si no requiere permisos o los tiene, permitir
            return true;
        }

        // No autenticado, redirigir al login
        this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
        return false;
    }

    canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot):
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {
        return this.canActivate(childRoute, state);
    }
}

import { Routes } from '@angular/router';

export const RRHH_ROUTES: Routes = [
    {
        path: 'empleados',
        loadComponent: () => import('../gestion-empleados/gestion-empleados.component').then(m => m.GestionEmpleadosComponent),
        data: { title: 'Gestión de Empleados' }
    },
    {
        path: 'planillas',
        loadComponent: () => import('../pago-personal/pago-personal.component').then(m => m.PagoPersonalComponent),
        data: { title: 'Planilla de Pagos' }
    }
];

import { Routes } from '@angular/router';

export const REPORTS_ROUTES: Routes = [

    {
        path: 'auditoria',
        loadComponent: () => import('./pages/auditoria/auditoria.component').then(m => m.AuditoriaComponent),
        data: { title: 'Auditoría de Datos' }
    }
];

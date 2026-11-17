import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/pages/login/login.component';
import { CambioSucursalComponent } from './features/auth/pages/cambio-sucursal/cambio-sucursal.component';
import { AuthGuard } from './core/guards/auth.guard';
import { MainLayoutComponent } from './shared/layout/main-layout.component';
import { PuntoVentaCheckGuard } from './core/guards/punto-venta-check.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },

  {
    path: 'admin',
    component: MainLayoutComponent,
    canActivate: [AuthGuard, PuntoVentaCheckGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        data: { title: 'Dashboard' }
      },
      {
        path: 'cambio-sucursal',
        component: CambioSucursalComponent
      },
      {
        path: 'reportes',
        children: [

          {
            path: 'ventas',
            loadComponent: () => import('./features/rep-ventas/rep-ventas.component').then(m => m.RepVentasComponent),
            data: { title: 'Reporte de Ventas' }
          },
          {
            path: 'compras',
            loadComponent: () => import('./features/rep-compras/rep-compras.component').then(m => m.RepComprasComponent),
            data: { title: 'Reporte de Compras' }
          },
          {
            path: 'descuentos',
            loadComponent: () => import('./features/rep-descuentos/rep-descuentos.component').then(m => m.RepDescuentosComponent),
            data: { title: 'Reporte de Descuentos' }
          },
          {
            path: 'honorarios',
            loadComponent: () => import('./features/rep-honorarios/rep-honorarios.component').then(m => m.RepHonorariosComponent),
            data: { title: 'Reporte de Honorarios' }
          },
          {
            path: 'pagos',
            loadComponent: () => import('./features/rep-pago-personal/rep-pago-personal.component').then(m => m.RepPagoPersonalComponent),
            data: { title: 'Reporte de Pagos de Personal' }
          },
          {
            path: 'otros-pagos',
            loadComponent: () => import('./features/rep-otros-pagos/rep-otros-pagos.component').then(m => m.RepOtrosPagosComponent),
            data: { title: 'Reporte de Otros Pagos' }
          },

          {
            path: 'auditoria',
            loadComponent: () => import('./features/reports/pages/auditoria/auditoria.component').then(m => m.AuditoriaComponent),
            data: { title: 'Auditoría de Datos' }
          },
          {
            path: 'correlatividad',
            loadComponent: () => import('./features/correlatividad/correlatividad.component').then(m => m.CorrelatividadComponent),
            data: { title: 'Correlatividad de Documentos' }
          },
          {
            path: 'kardex',
            loadComponent: () => import('./features/kardex/kardex.component').then(m => m.KardexComponent),
            data: { title: 'Kardex de Inventario' }
          },
          {
            path: 'valorizacion',
            loadComponent: () => import('./features/valorizacion/valorizacion.component').then(m => m.ValorizacionComponent),
            data: { title: 'Reporte de Valorización' }
          },
          {
            path: 'comprobantes-anulados',
            loadComponent: () => import('./features/rep-comprobantes-anulados/rep-comprobantes-anulados.component').then(m => m.RepComprobantesAnuladosComponent),
            data: { title: 'Reporte de Comprobantes Anulados' }
          },
          {
            path: 'empresas-atendidas',
            loadComponent: () => import('./features/rep-empresas-atendidas/rep-empresas-atendidas.component').then(m => m.RepEmpresasAtendidasComponent),
            data: { title: 'Empresas Más Atendidas' }
          }
        ]
      },
      {
        path: 'configuraciones',
        loadChildren: () => import('./features/usuarios/configuraciones.routes').then(m => m.CONFIGURACIONES_ROUTES)
      },
      {
        path: 'procesos',
        loadChildren: () => import('./features/usuarios/procesos.routes').then(m => m.PROCESOS_ROUTES)
      },
      {
        path: 'rrhh',
        loadChildren: () => import('./features/usuarios/gestion-empleados.route').then(m => m.RRHH_ROUTES)
      },
      {
        path: 'caja-chica',
        children: [
          {
            path: 'movimientos',
            loadComponent: () => import('./features/registro-movimientos/registro-movimientos.component').then(m => m.RegistroMovimientosComponent),
            data: { title: 'Registro de Movimientos' }
          },
          {
            path: 'ingresos-gastos',
            loadComponent: () => import('./features/ingresos-gastos/ingresos-gastos.component').then(m => m.IngresosGastosComponent),
            data: { title: 'Ingresos y Gastos' }
          },
          {
            path: 'arqueo-caja',
            loadComponent: () => import('./features/arqueo-caja/arqueo-caja.component').then(m => m.ArqueoCajaComponent),
            data: { title: 'Arqueo de Caja' }
          }
        ]
      },
      {
        path: 'caja',
        children: [
          {
            path: 'cuentas-corriente',
            loadComponent: () => import('./features/cuentas-corrientes/cuentas-corrientes.component').then(m => m.CuentasCorrientesComponent),
            data: { title: 'Cuentas Corrientes' }
          },
          {
            path: 'operaciones',
            loadComponent: () => import('./features/gestion-operaciones/gestion-operaciones.component').then(m => m.GestionOperacionesComponent),
            data: { title: 'Gestión de Operaciones' }
          }
        ]
      },
      {
        path: 'punto-venta',
        children: [
          {
            path: 'anular-comprobante',
            loadComponent: () =>
              import('./features/anular-comprobante/anular-comprobante.component')
                .then(m => m.AnularComprobanteComponent),
            data: { title: 'Anular Comprobante' }
          },
          {
            path: 'anular-falta-autorizar',
            loadComponent: () =>
              import('./features/anular-falta-autorizar/solicitud-anulacion.component')
                .then(m => m.SolicitudAnulacionComponent),
            data: { title: 'Anular falta autorizar' }
          },
          {
            path: 'notas-credito',
            loadComponent: () =>
              import('./features/nota-credito/nota-credito.component')
                .then(m => m.NotaCreditoComponent),
            data: { title: 'Notas de Crédito' }
          }
        ]
      },
      { path: 'proveedores/:id/cuenta', loadComponent: () => import('./features/proveedores/cuenta-proveedor-list/cuenta-proveedor-list.component').then(m => m.CuentaProveedorListComponent) },

    ]
  },
  { path: '**', redirectTo: 'login' }
];

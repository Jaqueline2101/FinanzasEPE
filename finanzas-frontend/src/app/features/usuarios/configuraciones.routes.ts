import { Routes } from '@angular/router';
import { NivelesComponent } from '../niveles/niveles.component';
import { SEGURIDAD_ROUTES } from './seguridad.routes';

export const CONFIGURACIONES_ROUTES: Routes = [
  { path: 'areas', component: NivelesComponent },
  { path: 'niveles', component: NivelesComponent },
  { path: 'cambio-punto', loadComponent: () => import('../auth/pages/cambio-sucursal/cambio-sucursal.component').then(m => m.CambioSucursalComponent) },
  { path: 'cambio-clave', loadComponent: () => import('./cambio-clave/cambio-clave.component').then(m => m.CambioClaveComponent) },
  { path: 'mi-perfil', loadComponent: () => import('./mi-perfil/mi-perfil.component').then(m => m.MiPerfilComponent), data: { title: 'Configuración / Mi Perfil' } },
  { path: 'servicios', loadComponent: () => import('../gestion-catalogo/servicios.component').then(m => m.ServiciosComponent) },
  { path: 'catalogo', loadComponent: () => import('../gestion-catalogo/servicios.component').then(m => m.ServiciosComponent) },

  { path: 'paquetes', loadComponent: () => import('../gestion-paquetes/paquetes.component').then(m => m.PaquetesComponent) },

  // { path: 'plantilla-resultados', loadComponent: () => import('../plantilla-resultados/components/plantilla-list.component').then(m => m.PlantillaListComponent) },
  { path: 'companias', loadComponent: () => import('../gestion-companias/gestion-companias.component').then(m => m.GestionCompaniasComponent) },
  {
    path: 'empresas',
    loadComponent: () => import('../gestion-empresas/gestion-empresas.component').then(m => m.GestionEmpresasComponent),
    data: { title: 'Gestión de Empresas' }
  },
  {
    path: 'beneficios-especiales',
    loadComponent: () => import('../beneficios-especiales/beneficios-list.component').then(m => m.BeneficiosListComponent),
    data: { title: 'Beneficios Especiales' }
  },
  {
    path: 'plan-cuentas',
    loadComponent: () => import('../plan-cuentasPMT/plan-cuentas-pmt.component').then(m => m.PlanCuentasPmtComponent),
    data: { title: 'Plan de cuentas (P. M. T.)' }
  },
  // { path: 'companias/:id/vinculos', loadComponent: () => import('../compania/vinculo/vinculo.component').then(m => m.VinculoCompaniaComponent) },
  {
    path: 'cuentas-convenio',
    loadComponent: () => import('../cuentas-convenio/cuentas-convenio.component').then(m => m.CuentasConvenioComponent),
    data: { title: 'Cuentas de Convenio' }
  },
  {
    path: 'autorizar-descuentos',
    loadComponent: () => import('../autorizar-descuento/autorizar-descuento.component').then(m => m.AutorizarDescuentoComponent),
    data: { title: 'Autorizar Descuentos' }
  },
  {
    path: 'sucursales',
    loadComponent: () => import('../sucursales/sucursales.component').then(m => m.SucursalesComponent),
    data: { title: 'Gestión de Sucursales' }
  },
  {
    path: 'seguridad',
    loadChildren: () => SEGURIDAD_ROUTES
  },
];
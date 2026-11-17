import { Routes } from '@angular/router';
import { PagosComponent } from '../cuentas-pagar/pagos.component';

// import { LabOrdenesComponent } from '../lab-ordenes/lab-ordenes.component';
// import { LabOrdenesDetallesComponent } from '../lab-ordenes/lab-ordenes-detalles/lab-ordenes-detalles.component';
// import { AtencionesProcesoComponent } from '../procesosatenciones/atenciones-proceso.component';
// import { LibroAtencionesComponent } from '../reports/pages/libro-atenciones/libro-atenciones.component';
// import { IngresoResultadosComponent } from '../procesos/ingreso-resultados/ingreso-resultados.component';
// import { GestionAtencionPageComponent } from '../procesosatenciones/gestion-atencion-page/gestion-atencion-page.component';
// import { HistorialOrdenesComponent } from '../procesos/historial-ordenes/historial-ordenes.component';
// import { OrdenesPendientesComponent } from '../procesos/ordenes-pendientes/ordenes-pendientes.component';

export const PROCESOS_ROUTES: Routes = [
  {
    path: 'punto-venta',
    loadComponent: () => import('../venta-servicios/venta-servicios.component').then(m => m.VentaServiciosPageComponent)
  },
  {
    path: 'proveedores',
    loadComponent: () => import('../proveedores/proveedores.component').then(m => m.ProveedoresComponent)
  },
  {
    path: 'productos',
    loadComponent: () => import('../productos/productos.component').then(m => m.ProductosComponent)
  },
  {
    path: 'compras/registro',
    loadComponent: () => import('../registrar-compras/registro-compra/registro-compra.component').then(m => m.RegistroCompraComponent)
  },
  {
    path: 'compras',
    loadComponent: () => import('../registrar-compras/compras.component').then(m => m.ComprasComponent)
  },
  {
    path: 'compras/anular',
    loadComponent: () => import('../anular-compra/anular-compra.component').then(m => m.AnularCompraComponent)
  },
  {
    path: 'compras/detalle/:id',
    loadComponent: () => import('../registrar-compras/detalle-compra/detalle-compra.component').then(m => m.DetalleCompraComponent)
  },
  {
    path: 'compras/ajustar',
    loadComponent: () => import('../ajustar-compra/ajustar-compra.component').then(m => m.AjustarCompraComponent)
  },
  {
    path: 'pagos',
    component: PagosComponent
  },
  {
    path: 'otros-pagos',
    loadComponent: () => import('../otros-pagos/otros-pagos.component').then(m => m.OtrosPagosComponent)
  },
  {
    path: 'pago-honorarios',
    loadComponent: () => import('../pagorec-honorarios/pagorec-honorarios.component').then(m => m.PagorecHonorariosComponent)
  },
  {
    path: 'pagos-personal',
    loadComponent: () => import('../pago-personal/pago-personal.component').then(m => m.PagoPersonalComponent)
  },
  {
    path: 'reimpresion',
    loadComponent: () => import('../reimprimir/reimpresion.component').then(m => m.ReimpresionComponent)
  },
  {
    path: 'orden-compra',
    loadComponent: () => import('../orden-compra/orden-compra.component').then(m => m.OrdenCompraComponent)
  },
  // RUTAS COMENTADAS PORQUE LOS COMPONENTES NO EXISTEN EN ESTA MIGRACION
  /*
  {
    path: 'lista-ordenes', // Era 'ordenes' pero chocaba, verificar nombre
    loadComponent: () => import('../lab-ordenes/lab-ordenes.component').then(m => m.LabOrdenesComponent)
  },
  {
    path: 'atenciones',
    loadComponent: () => import('../procesosatenciones/atenciones-proceso.component').then(m => m.AtencionesProcesoComponent)
  },
  {
      path: 'atenciones/:idOrden',
      loadComponent: () => import('../lab-ordenes/lab-ordenes-detalles/lab-ordenes-detalles.component').then(m => m.LabOrdenesDetallesComponent)
  },
  {
    path: 'libro-atenciones',
    loadComponent: () => import('../reports/pages/libro-atenciones/libro-atenciones.component').then(m => m.LibroAtencionesComponent)
  },
  {
    path: 'ingreso-resultados',
    loadComponent: () => import('../procesos/ingreso-resultados/ingreso-resultados.component').then(m => m.IngresoResultadosComponent)
  },
  {
    path: 'gestion-atencion',
    loadComponent: () => import('../procesosatenciones/gestion-atencion-page/gestion-atencion-page.component').then(m => m.GestionAtencionPageComponent)
  },
  {
      path: 'historial-ordenes',
      loadComponent: () => import('../procesos/historial-ordenes/historial-ordenes.component').then(m => m.HistorialOrdenesComponent)
  },
  {
      path: 'ordenes-pendientes',
      loadComponent: () => import('../procesos/ordenes-pendientes/ordenes-pendientes.component').then(m => m.OrdenesPendientesComponent)
  },
  {
      path: 'tomas/exam-details/:ordenId/:examId',
      loadComponent: () => import('../reports/pages/exam-details/exam-details.component').then(m => m.ExamDetailsComponent)
  }
  */
  /*
  { path: 'atenciones/gestion/:id', component: GestionAtencionPageComponent },
  { path: 'historial-ordenes', component: HistorialOrdenesComponent },
  */
];

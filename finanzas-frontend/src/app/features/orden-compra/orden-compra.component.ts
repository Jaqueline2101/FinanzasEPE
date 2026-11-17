import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { OrdenCompraService } from '../../services/orden-compra.service';
import { OrdenCompra } from '../../models/orden-compra.model';
import { ModalOrdenCompraComponent } from './modal-orden/modal-orden-compra.component';
import { ModalDetalleOrdenComponent } from './modal-detalle/modal-detalle-orden.component';
import { AlertasService } from '../../services/alertas.service';
import { animate, style, transition, trigger } from '@angular/animations';

@Component({
    selector: 'app-orden-compra',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent,
        PrimaryButtonComponent,
        ModalOrdenCompraComponent,
        ModalDetalleOrdenComponent
    ],
    templateUrl: './orden-compra.component.html',
    animations: [
        trigger('fadeIn', [
            transition(':enter', [
                style({ opacity: 0, transform: 'translateY(10px)' }),
                animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
            ])
        ])
    ]
})
export class OrdenCompraComponent implements OnInit {
    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/dashboard' },
        { label: 'Compras', route: '/compras' },
        { label: 'Orden de Compra', route: '/admin/procesos/orden-compra' }
    ];

    columnas: Columna[] = [
        { field: 'id', header: '#', tipo: 'index', subField: [] },
        { field: 'numeroOrden', header: 'N° ORDEN', subField: [] },
        { field: 'fechaEmision', header: 'F. EMISIÓN', tipo: 'date', subField: [] },
        { field: 'razonSocialProveedor', header: 'PROVEEDOR', subField: [] },
        { field: 'moneda', header: 'MON', subField: [] },
        { field: 'totalGeneral', header: 'TOTAL', tipo: 'currency', subField: ['moneda'] },
        { field: 'estado', header: 'ESTADO', tipo: 'order-status', subField: [] },
        { field: 'condicionPago', header: 'PAGO', subField: [] }
    ];

    registros: OrdenCompra[] = [];
    filteredRegistros: OrdenCompra[] = [];
    displayedRegistros: OrdenCompra[] = [];
    loading = false;
    isFullScreenView = false;
    showDetailModal = false;
    selectedOrden?: OrdenCompra;

    // Configuración de Filtros
    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'N° Orden', value: 'NUMERO' },
        { label: 'Proveedor', value: 'PROVEEDOR' }
    ];

    estadoOptions = [
        { label: 'Todas', value: 'TODOS' },
        { label: 'Pendiente', value: 'PENDIENTE' },
        { label: 'Aprobado', value: 'APROBADO' },
        { label: 'Entregado', value: 'ENTREGADO' },
        { label: 'Anulado', value: 'ANULADO' }
    ];

    // Paginación
    currentPage = 1;
    pageSize = 10;
    totalPages = 1;

    constructor(
        private ordenService: OrdenCompraService,
        private alertasService: AlertasService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        // Safe Start: Diferimos la carga inicial para evitar errores de ciclo de vida
        setTimeout(() => {
            this.cargarRegistros();
        });
    }

    cargarRegistros() {
        // Deferimos el inicio de carga para evitar NG0100
        setTimeout(() => {
            this.loading = true;
            this.cdr.detectChanges();

            this.ordenService.listar().subscribe({
                next: (data) => {
                    this.registros = data;
                    // Aplicar filtros iniciales (TODOS)
                    this.onFilter({ term: '', searchType: 'ALL', estado: 'TODOS', fechaDesde: '', fechaHasta: '' });
                    
                    setTimeout(() => {
                        this.loading = false;
                        this.cdr.detectChanges();
                    });
                },
                error: (err) => {
                    console.error('Error cargando órdenes de compra', err);
                    setTimeout(() => {
                        this.loading = false;
                        this.cdr.detectChanges();
                    });
                }
            });
        });
    }

    get paginatedRegistros() {
        const start = (this.currentPage - 1) * this.pageSize;
        return this.registros.slice(start, start + this.pageSize);
    }

    onFilter(criteria: FilterCriteria) {
        // Safe Filter: Deferimos para evitar errores de ciclo de vida
        setTimeout(() => {
            const term = (criteria.term || '').toLowerCase();
            const type = criteria.searchType || 'ALL';
            const estado = criteria.estado || 'TODOS';
            const from = criteria.fechaDesde ? new Date(criteria.fechaDesde + 'T00:00:00') : null;
            const to = criteria.fechaHasta ? new Date(criteria.fechaHasta + 'T23:59:59') : null;

            this.filteredRegistros = this.registros.filter(o => {
                // 1. Filtro por término
                let matchesTerm = true;
                if (term) {
                    const num = (o.numeroOrden || '').toLowerCase();
                    const prov = (o.razonSocialProveedor || '').toLowerCase();
                    if (type === 'NUMERO') matchesTerm = num.includes(term);
                    else if (type === 'PROVEEDOR') matchesTerm = prov.includes(term);
                    else matchesTerm = num.includes(term) || prov.includes(term);
                }

                // 2. Filtro por estado
                let matchesStatus = true;
                if (estado !== 'TODOS') matchesStatus = o.estado === estado;

                // 3. Filtro por fechas
                let matchesDates = true;
                if (from && to && o.fechaEmision) {
                    const emisionDate = new Date(o.fechaEmision);
                    if (emisionDate < from || emisionDate > to) matchesDates = false;
                }

                return matchesTerm && matchesStatus && matchesDates;
            });

            this.currentPage = 1;
            this.updatePagination();
            this.cdr.detectChanges();
        });
    }

    updatePagination() {
        this.totalPages = Math.ceil(this.filteredRegistros.length / this.pageSize);
        if (this.totalPages === 0) this.totalPages = 1;

        const start = (this.currentPage - 1) * this.pageSize;
        this.displayedRegistros = this.filteredRegistros.slice(start, start + this.pageSize);
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.updatePagination();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 1;
        this.updatePagination();
    }

    abrirNuevo() {
        setTimeout(() => {
            this.selectedOrden = undefined;
            this.isFullScreenView = true;
            this.cdr.detectChanges();
        });
    }

    editar(orden: OrdenCompra) {
        setTimeout(() => {
            this.selectedOrden = orden;
            this.isFullScreenView = true;
            this.cdr.detectChanges();
        });
    }

    onDelete(orden: OrdenCompra) {
        this.alertasService.deleteConfirm(
            '¿Eliminar orden de compra?',
            `La orden ${orden.numeroOrden} será eliminada permanentemente.`
        ).then((confirmed) => {
            if (confirmed && orden.id) {
                setTimeout(() => {
                    this.loading = true;
                    this.ordenService.eliminar(orden.id!).subscribe({
                        next: () => {
                            this.alertasService.success('Eliminado', 'La orden de compra ha sido eliminada.');
                            this.cargarRegistros();
                        },
                        error: (err) => {
                            console.error('Error al eliminar orden', err);
                            this.loading = false;
                            this.alertasService.error('Error', 'No se pudo eliminar la orden de compra.');
                            this.cdr.detectChanges();
                        }
                    });
                });
            }
        });
    }

    verDetalle(orden: OrdenCompra) {
        this.selectedOrden = orden;
        this.showDetailModal = true;
    }

    onCancelForm() {
        this.isFullScreenView = false;
        this.selectedOrden = undefined;
    }

    onSaveSuccess() {
        // Deferimos el cierre de la vista para asegurar estabilidad
        setTimeout(() => {
            this.isFullScreenView = false;
            this.selectedOrden = undefined;
            this.cdr.detectChanges();

            // Pequeño delay adicional para la carga de datos
            setTimeout(() => {
                this.cargarRegistros();
                this.alertasService.success('¡Éxito!', 'Operación completada correctamente.');
            }, 50);
        });
    }
}

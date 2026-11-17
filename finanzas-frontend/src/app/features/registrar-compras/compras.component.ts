import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import Swal from 'sweetalert2';
import { CommonModule } from '@angular/common';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { CompraService } from '../../services/compra.service';
import { Router } from '@angular/router';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { AuthService } from '../auth/services/auth.service';

@Component({
    selector: 'app-compras',
    standalone: true,
    imports: [
        CommonModule,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        PrimaryButtonComponent,
        BreadcrumbComponent,
        BarraFiltrosComponent
    ],
    templateUrl: './compras.component.html'
})
export class ComprasComponent implements OnInit {
    compras: any[] = [];
    loading = false;

    // Pagination
    currentPage = 0;
    pageSize = 10;
    totalElements = 0;
    totalPages = 0;

    // Filter state
    currentFilters: FilterCriteria = {
        term: '',
        searchType: 'ALL',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Serie', value: 'serie' },
        { label: 'Correlativo', value: 'correlativo' },
        { label: 'Proveedor', value: 'proveedor' }
    ];

    estadoOptions = [
        { label: 'Todas', value: 'TODOS' },
        { label: 'Registradas', value: 'ACTIVO' },
        { label: 'Anuladas', value: 'INACTIVO' }
    ];

    columnas: Columna[] = [
        { field: 'index', header: '#', subField: [], tipo: 'index' },
        { field: 'fechaEmision', header: 'Fecha', subField: [], tipo: 'date' },
        { field: 'serie', header: 'Serie', subField: [] },
        { field: 'correlativo', header: 'Número', subField: [] },
        { field: 'proveedor', header: 'Proveedor', subField: ['razonSocial'], tipo: 'object' },
        { field: 'moneda', header: 'Mon', subField: [] },
        { field: 'valorVentaGravado', header: 'Gravado', subField: [], tipo: 'currency' },
        { field: 'valorVentaInafecto', header: 'Inafecto', subField: [], tipo: 'currency' },
        { field: 'igv', header: 'IGV', subField: [], tipo: 'currency' },
        { field: 'total', header: 'Total', subField: [], tipo: 'currency' },
        {
            field: 'estado',
            header: 'Estado',
            subField: [],
            tipo: 'badge',
            badgeConfig: {
                container: 'rounded-full px-2 py-0.5 text-xs font-bold',
                from: 'REGISTRADO', to: 'ANULADO'
            }
        }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Procesos' },
        { label: 'Compras' }
    ];

    constructor(
        private compraService: CompraService,
        private authService: AuthService,
        private router: Router,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading = true;
        const sucursalId = this.authService.currentUser()?.sucursalActualId || 1;
        
        this.compraService.listarPorSucursal(sucursalId, this.currentPage, this.pageSize, this.currentFilters).subscribe({
            next: (resp) => {
                this.compras = resp.content;
                this.totalElements = resp.totalElements;
                this.totalPages = resp.totalPages;
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.loadData();
    }

    handleFilters(criteria: FilterCriteria) {
        this.currentFilters = criteria;
        this.currentPage = 0;
        this.loadData();
    }

    onNew() {
        this.router.navigate(['/admin/procesos/compras/registro']);
    }

    onViewDetails(item: any) {
        if (item && item.id) {
            this.router.navigate(['/admin/procesos/compras/detalle', item.id]);
        }
    }

    onDelete(item: any) {
        Swal.fire({
            title: '¿Estás seguro?',
            text: "No podrás revertir esto. La compra será eliminada permanentemente.",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                this.loading = true;
                this.compraService.eliminar(item.id).subscribe({
                    next: () => {
                        this.loading = false;
                        this.cd.detectChanges();
                        Swal.fire(
                            'Eliminado!',
                            'La compra ha sido eliminada.',
                            'success'
                        );
                        this.loadData();
                    },
                    error: (err) => {
                        this.loading = false;
                        this.cd.detectChanges();
                        console.error(err);
                        Swal.fire(
                            'Error',
                            'No se pudo eliminar la compra.',
                            'error'
                        );
                    }
                });
            }
        });
    }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { ProveedorService } from '../../services/proveedor.service';
import { AlertasService } from '../../services/alertas.service';
import { Proveedor } from '../../models/proveedor.model';
import { ModalProveedorComponent } from './modal-proveedor/modal-proveedor.component';
import { ModalDetalleProveedorComponent } from './modal-detalle/modal-detalle.component';
import { Router } from '@angular/router';

@Component({
    selector: 'app-proveedores',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        PageHeaderComponent,
        TablaGeneralComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        ModalProveedorComponent,
        ModalDetalleProveedorComponent,
        BarraFiltrosComponent,
        PaginationComponent
    ],
    templateUrl: './proveedores.component.html'
})
export class ProveedoresComponent implements OnInit {
    columnas: Columna[] = [
        { field: '', header: '#', tipo: 'index', subField: [] },
        { field: 'numDocIdent', header: 'RUC / DNI', subField: [] },
        { field: 'razonSocial', header: 'Razón Social', subField: [] },
        { field: 'telefono', header: 'Teléfono', subField: [] },
        { field: 'email', header: 'Email', subField: [] },
        { field: 'direccion', header: 'Dirección', subField: [] },
        { field: 'sedes', header: 'Sede', subField: [] },
        {
            field: 'estado',
            header: 'Estado',
            subField: [],
            tipo: 'status',
            align: 'center'
        }
    ];
    proveedores: Proveedor[] = [];
    filteredProveedores: Proveedor[] = [];
    displayedProveedores: Proveedor[] = [];
    loading = false;

    currentPage = 0;
    pageSize = 10;
    totalPages = 0;
    pageSizeOptions = [5, 10, 25, 50];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Procesos' },


        { label: 'Proveedores' }
    ];

    showModal = false;
    selectedProveedor: Proveedor | null = null;

    showDetailModal = false;
    selectedProveedorDetalle: Proveedor | null = null;

    constructor(
        private proveedorService: ProveedorService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef,
        private router: Router
    ) { }

    ngOnInit() {
        // Safe Start: Diferimos la carga inicial al siguiente tick para que el componente 
        // termine de renderizarse sin errores de ciclo de vida.
        setTimeout(() => {
            this.loadData();
        });
    }

    loadData() {
        this.loading = true;
        this.proveedorService.getAll().subscribe({
            next: (data) => {
                this.proveedores = (Array.isArray(data) ? data : []).map(p => ({
                    ...p,
                    estado: (p.estado === 1 || p.estado === '1' || p.estado === 'Habilitado') ? 'Habilitado' : 'Inhabilitado'
                }));
                this.onFilter({ term: '', searchType: 'ALL', estado: 'TODOS', fechaDesde: '', fechaHasta: '' });
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.alertasService.error('Error', 'No se pudieron cargar los proveedores');
            }
        });
    }

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Nombre / Razón Social', value: 'NOMBRE' },
        { label: 'RUC / DNI', value: 'RUC' }
    ];

    estadoOptions = [
        { label: 'Todos', value: 'TODOS' },
        { label: 'Habilitado', value: 'Habilitado' },
        { label: 'Inhabilitado', value: 'Inhabilitado' }
    ];

    onFilter(criteria: FilterCriteria) {
        // Safe Filter: Diferimos la ejecución para evitar NG0100 cuando el filtro
        // se dispara durante el renderizado inicial o cambios en componentes hijos.
        setTimeout(() => {
            const term = criteria.term ? criteria.term.trim().toLowerCase() : '';
            const type = criteria.searchType || 'ALL';
            const estado = criteria.estado || 'TODOS';
            const from = criteria.fechaDesde ? new Date(criteria.fechaDesde + 'T00:00:00') : null;
            const to = criteria.fechaHasta ? new Date(criteria.fechaHasta + 'T23:59:59') : null;

            this.filteredProveedores = this.proveedores.filter(p => {
                let matchesTerm = true;
                if (term) {
                    const rs = (p.razonSocial || '').toLowerCase();
                    const nd = (p.numDocIdent || '');
                    const nc = (p.nombreComercial || '').toLowerCase();

                    if (type === 'NOMBRE') {
                        matchesTerm = rs.includes(term) || nc.includes(term);
                    } else if (type === 'RUC') {
                        matchesTerm = nd.includes(term);
                    } else {
                        matchesTerm = rs.includes(term) || nd.includes(term) || nc.includes(term);
                    }
                }

                let matchesEstado = true;
                if (estado !== 'TODOS') {
                    matchesEstado = p.estado === estado;
                }

                let matchesDates = true;
                if (from && to) {
                    if (p.fechaRegistro) {
                        const regDate = new Date(p.fechaRegistro);
                        if (regDate < from || regDate > to) {
                            matchesDates = false;
                        }
                    } else {
                        matchesDates = true;
                    }
                }

                return matchesTerm && matchesEstado && matchesDates;
            });

            this.currentPage = 0;
            this.updatePagination();
            this.cd.detectChanges();
        });
    }

    filterProveedores() {
        this.filteredProveedores = this.proveedores.filter(p => p.estado !== 'Inhabilitado');
        this.currentPage = 0;
        this.updatePagination();
    }

    updatePagination(): void {
        this.totalPages = Math.ceil(this.filteredProveedores.length / this.pageSize);
        if (this.totalPages === 0) this.totalPages = 1;
        const start = this.currentPage * this.pageSize;
        const end = start + this.pageSize;

        this.displayedProveedores = this.filteredProveedores.slice(start, end);
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.updatePagination();
    }

    onPageSizeChange(size: number): void {
        this.pageSize = size;
        this.currentPage = 0;
        this.updatePagination();
    }

    onNew() {
        setTimeout(() => {
            this.selectedProveedor = null;
            this.showModal = true;
            this.cd.detectChanges();
        });
    }

    onEdit(item: Proveedor) {
        setTimeout(() => {
            const fullItem = this.proveedores.find(p => p.id === item.id) || item;
            this.selectedProveedor = fullItem;
            this.showModal = true;
            this.cd.detectChanges();
        });
    }

    onViewDetails(item: Proveedor) {
        const id = item.id;
        if (!id) return;

        setTimeout(() => {
            this.loading = true;
            this.proveedorService.getById(id).subscribe({
                next: (fullItem) => {
                    // Normalizar estado senior: Asegura que el Pipe UpperCase no falle al recibir un número
                    this.selectedProveedorDetalle = {
                        ...fullItem,
                        estado: (fullItem.estado === 1 || fullItem.estado === '1' || fullItem.estado === 'Habilitado') ? 'Habilitado' : 'Inhabilitado'
                    };
                    this.showDetailModal = true;
                    this.loading = false;
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error fetching provider details:', err);
                    this.alertasService.error('Error', 'No se pudo cargar los detalles del proveedor');
                    this.loading = false;
                    this.cd.detectChanges();
                }
            });
        });
    }

    closeDetailModal() {
        this.showDetailModal = false;
        this.selectedProveedorDetalle = null;
    }

    onDelete(item: Proveedor) {
        this.alertasService.deleteConfirm(
            '¿Eliminar proveedor?',
            `${item.razonSocial} será eliminado permanentemente.`
        ).then((confirmed) => {
            if (confirmed && item.id) {
                // Deferir para evitar NG0100 al cambiar loading desde una promesa de alerta
                setTimeout(() => {
                    this.loading = true;
                    this.proveedorService.delete(item.id!).subscribe(() => {
                        this.alertasService.success('Eliminado', 'Proveedor eliminado correctamente');
                        this.loadData();
                    });
                });
            }
        });
    }

    onSave(data: any) {
        const id = this.selectedProveedor?.id || data.id;

        const request$ = this.selectedProveedor && id
            ? this.proveedorService.update(id, data)
            : this.proveedorService.create(data);

        request$.subscribe({
            next: () => {
                setTimeout(() => {
                    this.showModal = false;
                    this.loadData();
                    this.alertasService.success('¡Éxito!', 'Proveedor guardado correctamente');
                    this.cd.detectChanges();
                });
            },
            error: (err: any) => {
                console.error(err);
                this.alertasService.error('Error', 'No se pudo guardar el proveedor');
            }
        });
    }

    closeModal() {
        this.showModal = false;
        this.selectedProveedor = null;
    }


    irACuenta(proveedor: Proveedor): void {
        if (proveedor.id) {
            this.router.navigate(['/admin/proveedores', proveedor.id, 'cuenta']);
        }
    }
}

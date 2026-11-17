import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { Servicio } from '../../models/servicio.model';
import { ServicioService } from '../../services/servicio.service';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { ModalDetalleServicioComponent } from './modal-detalle/modal-detalle.component';
import { ModalServicioComponent } from './modal-servicio/modal-servicio.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-servicios',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        PrimaryButtonComponent,
        ModalServicioComponent,
        ModalDetalleServicioComponent,
        BarraFiltrosComponent
    ],
    templateUrl: './servicios.component.html',
    styleUrls: ['./servicios.component.css']
})
export class ServiciosComponent implements OnInit {
    breadcrumbItems = [
        { label: 'Configuración', route: '/admin/configuraciones' },
        { label: 'Gestión de Servicios' }
    ];

    activeTab: 'servicios' = 'servicios';

    // Data
    servicios: Servicio[] = [];

    // Pagination
    currentPage: number = 0;
    pageSize: number = 10;
    totalElements: number = 0;
    totalPages: number = 0;

    // Current State of Filters
    currentFilters: FilterCriteria = {
        term: '',
        searchType: 'ALL',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    sortBy: string = 'createdAt';
    sortDir: string = 'DESC';

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'Nombre/Código', value: 'NAME_CODE' },
        { label: 'Nivel', value: 'NIVEL' },
        { label: 'Categoría', value: 'CATEGORIA' }
    ];

    estadoOptions = [
        { label: 'Todas', value: 'TODOS' },
        { label: 'Activas', value: 'ACTIVO' },
        { label: 'Inactivas', value: 'INACTIVO' }
    ];

    categorias = [
        { id: 1, nombre: 'Antibióticos' },
        { id: 2, nombre: 'Material de Curación' },
        { id: 3, nombre: 'Equipos Médicos' },
        { id: 4, nombre: 'Insumos Médicos' },
        { id: 5, nombre: 'Material Odontológico' },
        { id: 6, nombre: 'Reactivos de Laboratorio' }
    ];

    // Modal State
    showModal: boolean = false;
    showDetailModal: boolean = false;
    selectedServicio: Servicio | null = null;

    // Table Configuration
    columnas: Columna[] = [
        { field: '#', header: '#', tipo: 'index', subField: [''] },
        { field: 'codigo', header: 'Código', tipo: 'text', subField: [''] },
        { field: 'nombreServicio', header: 'Servicio', tipo: 'text', subField: [''] },
        { field: 'tipoDescripcion', header: 'Tipo', tipo: 'text', subField: [''] },
        { field: 'nivelDescripcion', header: 'Nivel', tipo: 'text', subField: [''] },
        { field: 'precioConIgv', header: 'Precio Final', tipo: 'currency', subField: ['moneda'] },
        { field: 'tipoAtencionNombre', header: 'Tipo-Atención', tipo: 'badge', subField: [''] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [''] },
    ];

    constructor(
        private servicioService: ServicioService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.cargarDatos();
    }

    handleFilters(filters: FilterCriteria): void {
        this.currentFilters = filters;
        this.currentPage = 0;
        this.cargarDatos();
    }

    cargarDatos(): void {
        const { term, estado } = this.currentFilters;
        let requestObservable;
        
        const page = this.currentPage;
        const size = this.pageSize;
        const sortBy = this.sortBy;
        const sortDir = this.sortDir;

        if (term && term.trim().length > 0) {
            if (estado !== 'TODOS') {
                const estadoNum = estado === 'ACTIVO' ? 1 : 0;
                requestObservable = this.servicioService.searchByEstado(term.trim(), estadoNum, page, size, sortBy, sortDir);
            } else {
                requestObservable = this.servicioService.search(term.trim(), page, size, sortBy, sortDir);
            }
        } else {
            if (estado !== 'TODOS') {
                const estadoNum = estado === 'ACTIVO' ? 1 : 0;
                requestObservable = this.servicioService.getByEstado(estadoNum, page, size, sortBy, sortDir);
            } else {
                requestObservable = this.servicioService.getAll(page, size, sortBy, sortDir);
            }
        }

        requestObservable.subscribe({
            next: (data: any) => {
                // El backend devuelve un PageResponse que contiene el campo 'content' con el array de datos.
                const listData = data?.content || (Array.isArray(data) ? data : []);
                
                this.servicios = listData.map((s: any) => ({
                    ...s,
                    categoriaNombre: this.categorias.find(c => c.id === s.idCategoria)?.nombre || '-'
                }));

                this.totalElements = data?.totalElements || this.servicios.length;
                this.totalPages = data?.totalPages || Math.ceil(this.totalElements / this.pageSize);
                this.cd.detectChanges();
            },
            error: (err: any) => {
                console.error('Error loading services', err);
                this.servicios = [];
                this.totalElements = 0;
                this.totalPages = 0;
                this.cd.detectChanges();
            }
        });
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.cargarDatos();
    }

    openCreateModal(): void {
        this.selectedServicio = null;
        this.showModal = true;
        this.cd.detectChanges();
    }

    editarServicio(servicio: Servicio): void {
        if (!servicio.id) return;
        this.servicioService.getById(servicio.id).subscribe({
            next: (fullServicio) => {
                this.selectedServicio = fullServicio;
                this.showModal = true;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error fetching service details', err);
                this.selectedServicio = servicio;
                this.showModal = true;
                this.cd.detectChanges();
            }
        });
    }

    verResultados(servicio: Servicio): void {
        if (!servicio.id) return;
        this.servicioService.getById(servicio.id).subscribe({
            next: (fullServicio) => {
                this.selectedServicio = fullServicio;
                this.showDetailModal = true;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error fetching service details', err);
                this.selectedServicio = servicio;
                this.showDetailModal = true;
                this.cd.detectChanges();
            }
        });
    }

    closeDetailModal(): void {
        this.showDetailModal = false;
        this.selectedServicio = null;
    }

    closeModal(): void {
        this.showModal = false;
        this.selectedServicio = null;
    }

    onSaveSuccess(): void {
        this.closeModal();
        this.cargarDatos();
    }

    deleteServicio(servicio: Servicio): void {
        this.alertasService.deleteConfirm(
            '¿Estás seguro?', 
            'No podrás revertir la eliminación de este servicio'
        ).then((confirmed) => {
            if (confirmed && servicio.id) {
                this.servicioService.delete(servicio.id).subscribe({
                    next: () => {
                        this.cargarDatos();
                        this.alertasService.success('Eliminado', 'El servicio ha sido eliminado.');
                    },
                    error: (err) => this.alertasService.error('Error', 'No se pudo eliminar el servicio')
                });
            }
        });
    }
}
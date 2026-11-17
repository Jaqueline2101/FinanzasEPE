
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { EmpresaService, Empresa } from '../../services/empresa.service';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';

interface EmpresaExtended extends Empresa {
    abrev?: string;
    idPersonalUser?: string;
}
import { ModalEmpresaComponent } from './modal-empresa/modal-empresa.component';
import { ModalDetalleEmpresaComponent } from './modal-detalle/modal-detalle-empresa.component';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-gestion-empresas',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TablaGeneralComponent,
        PageHeaderComponent,
        PaginationComponent,
        BreadcrumbComponent,
        BarraFiltrosComponent,
        ModalEmpresaComponent,
        ModalDetalleEmpresaComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './gestion-empresas.component.html',
    styleUrls: ['./gestion-empresas.component.css']
})
export class GestionEmpresasComponent implements OnInit {

    breadcrumbItems = [
        { label: 'Configuración', route: '/admin/configuraciones' },
        { label: 'Gestión de Empresas' }
    ];

    empresas: Empresa[] = [];
    empresasFiltradas: Empresa[] = [];
    loading = true;

    // Modal State
    showModal: boolean = false;
    showModalDetalle: boolean = false;
    selectedEmpresa: Empresa | null = null;
    selectedEmpresaDetalle: Empresa | null = null;

    // Pagination
    currentPage: number = 0;
    pageSize: number = 10;
    totalElements: number = 0;
    totalPages: number = 0;

    // Filters
    searchTerm = '';
    filtroEstado = 'TODOS';
    fechaDesde: string = '';
    fechaHasta: string = '';

    searchOptions = [
        { label: 'Todas', value: 'TODOS' },
        { label: 'Activas', value: 'ACTIVO' },
        { label: 'Inactivas', value: 'INACTIVO' }
    ];

    searchTypeOptions = [
        { label: 'Todas', value: 'ALL' },
        { label: 'Por Nombre', value: 'NOMBRE' },
        { label: 'Por RUC', value: 'RUC' }
    ];

    columnas: Columna[] = [
        { field: 'index', header: '#', tipo: 'text', subField: [] }, // Custom index column
        { field: 'codigo', header: 'CÓDIGO', tipo: 'text', subField: [] },
        { field: 'ruc', header: 'RUC', tipo: 'text', subField: [] },
        { field: 'nombre', header: 'RAZÓN SOCIAL', tipo: 'text', subField: [] },
        { field: 'telefono', header: 'TELÉFONO', tipo: 'text', subField: [] },
        { field: 'fechaCre', header: 'FECHA DE CREACIÓN', tipo: 'date', subField: [] },
        { field: 'estado', header: 'ESTADO', tipo: 'status-badge', subField: ['estado'] }
    ];

    constructor(
        private empresaService: EmpresaService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit() {
        this.resetDates();
        this.cargarEmpresas();
    }

    resetDates() {
        const today = new Date();
        const lastMonth = new Date();
        lastMonth.setDate(today.getDate() - 30);

        this.fechaDesde = lastMonth.toISOString().split('T')[0];
        this.fechaHasta = today.toISOString().split('T')[0];
    }

    cargarEmpresas() {
        this.loading = true;
        this.empresaService.getEmpresas().subscribe({
            next: (data) => {

                this.empresas = data.reverse().map((e, index) => ({
                    ...e,
                    ruc: e.idPersonalUser || e.ruc, // Map 'idPersonalUser' (storage for RUC) to 'ruc' (table field)
                    estado: String(e.estado),
                    index: index + 1
                }));

                this.aplicarFiltrosLocal();
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando empresas', err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    aplicarFiltrosLocal() {
        let temp = [...this.empresas];

        if (this.searchTerm) {
            const lower = this.searchTerm.toLowerCase();
            temp = temp.filter(e =>
                e.nombre?.toLowerCase().includes(lower) ||
                e.codigo?.toLowerCase().includes(lower) ||
                e.ruc?.toLowerCase().includes(lower)
            );
        }

        if (this.filtroEstado === 'TODOS') {
            // Exclude inactive '0' by default
            temp = temp.filter(e => e.estado !== '0');
        } else if (this.filtroEstado !== 'TODOS') {
            const estadoVal = this.filtroEstado === 'ACTIVO' ? '1' : '0';
            temp = temp.filter(e => e.estado == estadoVal);
        }

        if (this.fechaDesde && this.fechaHasta) {
            const desde = new Date(this.fechaDesde);
            const hasta = new Date(this.fechaHasta);
            hasta.setHours(23, 59, 59, 999);

            temp = temp.filter(e => {
                const fechaCre = e.fechaCre ? new Date(e.fechaCre) : null;
                return fechaCre ? (fechaCre >= desde && fechaCre <= hasta) : true;
            });
        }

        temp = temp.map((e, i) => ({ ...e, index: i + 1 }));

        this.totalElements = temp.length;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);

        const start = this.currentPage * this.pageSize;
        this.empresasFiltradas = temp.slice(start, start + this.pageSize);
    }

    handleFilters(filters: FilterCriteria) {
        this.searchTerm = filters.term;
        this.filtroEstado = filters.estado;
        this.fechaDesde = filters.fechaDesde;
        this.fechaHasta = filters.fechaHasta;
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.aplicarFiltrosLocal();
    }

    openDetalle(empresa: Empresa) {
        this.selectedEmpresaDetalle = empresa;
        this.showModalDetalle = true;
    }

    closeDetalle() {
        this.showModalDetalle = false;
        this.selectedEmpresaDetalle = null;
    }

    nuevaEmpresa() {
        this.selectedEmpresa = null;
        this.showModal = true;
    }

    editarEmpresa(item: any) {
        this.selectedEmpresa = item;
        this.showModal = true;
    }

    closeModal() {
        this.showModal = false;
        this.selectedEmpresa = null;
    }

    onSaveSuccess() {
        this.cargarEmpresas();
        this.closeModal();
    }

    eliminarEmpresa(item: any) {
        this.alertasService.deleteConfirm(
            '¿Estás seguro?',
            'No podrás revertir esto'
        ).then((result) => {
            if (result) {
                this.empresaService.deleteEmpresa(item.idEmpresa).subscribe({
                    next: () => {
                        this.alertasService.success('¡Eliminado!', 'La empresa ha sido eliminada.');
                        this.cargarEmpresas();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', 'No se pudo eliminar la empresa');
                        console.error(err);
                    }
                });
            }
        });
    }
}

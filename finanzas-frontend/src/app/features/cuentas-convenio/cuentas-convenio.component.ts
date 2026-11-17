import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { CredCtaCteService } from '../../services/cred-ctacte.service';
import { CompaniaService } from '../../services/compania.service';
import { CredCtaCte } from '../../models/cred-ctacte.model';
import { ModalCuentaConvenioComponent } from './modal-cuenta-convenio/modal-cuenta-convenio.component';
import { ModalDetalleCuentaComponent } from './modal-detalle/modal-detalle-cuenta.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-cuentas-convenio',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        ModalCuentaConvenioComponent,
        ModalDetalleCuentaComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent
    ],
    templateUrl: './cuentas-convenio.component.html',
    styleUrls: ['./cuentas-convenio.component.css']
})
export class CuentasConvenioComponent implements OnInit {
    breadcrumbItems = [
        { label: 'Configuración', route: '/admin/configuraciones' },
        { label: 'Cuentas de Convenio' }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Código', value: 'CODIGO' },
        { label: 'Asegurado', value: 'PACIENTE' },
        { label: 'Empresa', value: 'EMPRESA' },
        { label: 'Compañía', value: 'COMPANIA' }
    ];

    cuentas: CredCtaCte[] = [];
    
    // Filtros unificados
    filterCriteria: FilterCriteria = {
        term: '',
        searchType: 'ALL',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    companias: any[] = [];
    loading = true;

    currentPage = 0;
    pageSize = 10;

    get totalPages(): number {
        return Math.ceil(this.filteredCuentas.length / this.pageSize);
    }

    get paginatedCuentas(): CredCtaCte[] {
        const start = this.currentPage * this.pageSize;
        return this.filteredCuentas.slice(start, start + this.pageSize);
    }

    get filteredCuentas(): CredCtaCte[] {
        return this.cuentas.filter(c => {
            const criteria = this.filterCriteria;
            
            // 1. Filtro por Tipo y Término
            const lower = (criteria.term || '').toLowerCase();
            let matchesSearch = true;
            if (lower) {
                if (criteria.searchType === 'ALL') {
                    matchesSearch = (c.nroCtacte || '').toLowerCase().includes(lower) ||
                        (c.paciente?.nombre || '').toLowerCase().includes(lower) ||
                        (c.paciente?.apepat || '').toLowerCase().includes(lower) ||
                        (c.compania?.nombre || '').toLowerCase().includes(lower) ||
                        (c.empresa?.nombre || '').toLowerCase().includes(lower);
                } else if (criteria.searchType === 'CODIGO') {
                    matchesSearch = (c.nroCtacte || '').toLowerCase().includes(lower);
                } else if (criteria.searchType === 'PACIENTE') {
                    matchesSearch = (c.paciente?.nombre || '').toLowerCase().includes(lower) ||
                        (c.paciente?.apepat || '').toLowerCase().includes(lower);
                } else if (criteria.searchType === 'EMPRESA') {
                    matchesSearch = (c.empresa?.nombre || '').toLowerCase().includes(lower);
                } else if (criteria.searchType === 'COMPANIA') {
                    matchesSearch = (c.compania?.nombre || '').toLowerCase().includes(lower);
                }
            }

            // 2. Filtro por Estado
            const matchesEstado = criteria.estado === 'TODOS' || c.estado === criteria.estado;

            // 3. Filtro por Fechas (Vigencia)
            let matchesDates = true;
            if (criteria.fechaDesde && c.fechaInicio) {
                matchesDates = matchesDates && c.fechaInicio >= criteria.fechaDesde;
            }
            if (criteria.fechaHasta && c.fechaInicio) {
                matchesDates = matchesDates && c.fechaInicio <= criteria.fechaHasta;
            }

            return matchesSearch && matchesEstado && matchesDates;
        });
    }

    columnas: Columna[] = [
        { field: '', header: '#', tipo: 'index', subField: [] },
        { field: 'nroCtacte', header: 'Código', tipo: 'text', subField: [] },
        { field: 'nombreCompleto', header: 'Asegurado', tipo: 'text', subField: [] },
        { field: 'empresaNombre', header: 'Empresa', tipo: 'text', subField: [] },
        { field: 'nombrePlan', header: 'Plan / Compañía', tipo: 'title-subtitle', subField: ['companiaNombre'] },
        { field: 'vigencia', header: 'Vigencia', tipo: 'multi-date', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    mostrarModal = false;
    showDetailModal = false;
    selectedCuenta: CredCtaCte | null = null;

    constructor(
        private service: CredCtaCteService,
        private companiaService: CompaniaService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit() {
        setTimeout(() => {
            this.companiaService.getCompanias().subscribe(res => {
                this.companias = res;
                this.cargarCuentas();
            });
        }, 0);
    }

    cargarCuentas() {
        this.loading = true;
        this.service.listarTodas().subscribe({
            next: (data) => {
                this.cuentas = data.map(c => this.mapParaTabla(c));
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando cuentas de convenio', err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    handleFilters(criteria: FilterCriteria) {
        this.filterCriteria = criteria;
        this.currentPage = 0;
        this.cd.detectChanges();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.cd.detectChanges();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.cd.detectChanges();
    }

    nuevaCuenta() {
        this.selectedCuenta = null;
        this.mostrarModal = true;
    }

    editarCuenta(cuenta: CredCtaCte) {
        this.selectedCuenta = cuenta;
        this.mostrarModal = true;
    }

    onDetailView(cuenta: any) {
        this.selectedCuenta = cuenta;
        this.showDetailModal = true;
    }

    eliminarCuenta(id: number) {
        this.alertasService.deleteConfirm(
            '¿Eliminar cuenta de convenio?',
            'Esta acción no se puede deshacer.'
        ).then((result) => {
            if (result) {
                this.service.eliminar(id).subscribe({
                    next: () => {
                        this.alertasService.success('Eliminado', 'La cuenta ha sido eliminada correctamente');
                        this.cargarCuentas();
                    },
                    error: () => this.alertasService.error('Error', 'No se pudo eliminar la cuenta')
                });
            }
        });
    }

    onSaveSuccess() {
        this.mostrarModal = false;
        this.cargarCuentas();
    }

    closeModal() {
        this.mostrarModal = false;
        this.showDetailModal = false;
        this.selectedCuenta = null;
    }

    mapParaTabla(c: any) {
        let companiaObj = c.compania;
        if (typeof c.compania === 'number' || typeof c.compania === 'string') {
            companiaObj = this.companias.find(comp => comp.id == c.compania) || c.compania;
        }

        return {
            ...c,
            nroCtacte: c.nroCtacte || 'No asignado',
            nombreCompleto: c.nombre ? `${c.nombre} ${c.apellidoPaterno || ''} ${c.apellidoMaterno || ''}` : `${c.paciente?.nombre || ''} ${c.paciente?.apepat || ''}`,
            empresaNombre: c.empresa?.nombre || c.empresaNombre || 'Particular',
            companiaNombre: companiaObj?.razSol || companiaObj?.nombre || c.companiaNombre || 'Sin Compañía',
            vigencia: `${c.fechaInicio} | ${c.fechaFin}`
        };
    }

    onEdit(item: any) {
        this.editarCuenta(item);
    }

    onDelete(item: any) {
        this.eliminarCuenta(item.idCtacte);
    }
}

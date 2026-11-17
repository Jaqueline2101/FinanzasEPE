import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { CuentaCorrienteService } from '../../services/cuenta-corriente.service';
import { CuentaCorriente } from '../../models/cuenta-corriente.model';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { CuentaCorrienteModalComponent } from './modal-corrientes/modal-corriente-modal.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { DetalleCorrientesComponent } from './detalle-corrientes/detalle-corrientes.component';
import { AlertasService } from '../../services/alertas.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
    selector: 'app-cuentas-corrientes',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        PrimaryButtonComponent,
        BreadcrumbComponent,
        TablaGeneralComponent,
        CuentaCorrienteModalComponent,
        BarraFiltrosComponent,
        PaginationComponent,
        DetalleCorrientesComponent
    ],
    templateUrl: './cuentas-corrientes.component.html'
})
export class CuentasCorrientesComponent implements OnInit {

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja General' },
        { label: 'Gestión Cuentas Corriente' }
    ];

    allCuentas: any[] = [];
    filteredCuentas: any[] = [];
    displayedCuentas = signal<any[]>([]);
    loading = signal<boolean>(false);

    // Pagination
    currentPage = 0;
    pageSize = 10;
    totalPages = 0;

    isModalOpen = false;
    isDetailOpen = false;
    isEditMode = false;
    currentCuenta: Partial<CuentaCorriente> = {};
    detailCuenta: Partial<CuentaCorriente> = {};

    columnas: Columna[] = [
        { field: 'index', header: '#', tipo: 'index', subField: [] },
        { field: 'banco', header: 'Banco', tipo: 'text', subField: [] },
        { field: 'codigoSBS', header: 'Código SBS', tipo: 'text', subField: [] },
        { field: 'numeroCuenta', header: 'N° Cuenta', tipo: 'text', subField: [] },
        { field: 'cci', header: 'CCI (Interbancario)', tipo: 'text', subField: [] },
        { field: 'cuentaContable', header: 'Cuenta Contable', tipo: 'text', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Banco', value: 'banco' },
        { label: 'N° Cuenta', value: 'numeroCuenta' }
    ];

    constructor(
        private service: CuentaCorrienteService,
        private alertasService: AlertasService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        setTimeout(() => {
            this.loadCuentas();
        });
    }

    loadCuentas() {
        this.loading.set(true);
        this.service.getAll().subscribe({
            next: (data) => {
                const processed = data.map(c => ({
                    ...c,
                    monedaDisplay: c.moneda === 'PEN' ? 'Soles (S/)' : 'Dólares ($)'
                }));
                this.allCuentas = processed;
                this.filteredCuentas = processed;
                this.updateTable();
                this.loading.set(false);
            },
            error: (err) => {
                console.error(err);
                this.loading.set(false);
            }
        });
    }

    onFilter(criteria: FilterCriteria) {
        setTimeout(() => {
            const term = (criteria.term || '').toLowerCase();
            const type = criteria.searchType || 'ALL';

            this.filteredCuentas = this.allCuentas.filter(item => {
                let matchesTerm = true;
                if (term) {
                    if (type === 'ALL') {
                        matchesTerm = item.banco?.toLowerCase().includes(term) ||
                            item.numeroCuenta?.toLowerCase().includes(term) ||
                            item.alias?.toLowerCase().includes(term);
                    } else {
                        matchesTerm = item[type]?.toLowerCase().includes(term);
                    }
                }
                return matchesTerm;
            });

            this.currentPage = 0;
            this.updateTable();
            this.cdr.detectChanges();
        });
    }

    updateTable() {
        this.totalPages = Math.ceil(this.filteredCuentas.length / this.pageSize);
        if (this.totalPages === 0) this.totalPages = 1;

        const startIndex = this.currentPage * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        const pageSlice = this.filteredCuentas.slice(startIndex, endIndex);
        this.displayedCuentas.set(pageSlice);
        this.cdr.detectChanges();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.updateTable();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.updateTable();
    }

    openModal(cuenta?: CuentaCorriente) {
        if (cuenta) {
            this.isEditMode = true;
            this.currentCuenta = { ...cuenta };
        } else {
            this.isEditMode = false;
            this.currentCuenta = {
                moneda: 'PEN',
                saldo: 0,
                estado: 'A',
                nombreTitular: ''
            };
        }
        this.isModalOpen = true;
    }

    closeModal() {
        this.isModalOpen = false;
    }

    openDetail(cuenta: CuentaCorriente) {
        this.detailCuenta = { ...cuenta };
        this.isDetailOpen = true;
    }

    closeDetail() {
        this.isDetailOpen = false;
    }

    saveCuenta(data: Partial<CuentaCorriente>) {
        this.loading.set(true);

        if (this.isEditMode && data.idCuenta) {
            this.service.update(data.idCuenta, data as CuentaCorriente).subscribe({
                next: () => {
                    this.handleSuccess('Cuenta actualizada correctamente');
                },
                error: () => this.loading.set(false)
            });
        } else {
            this.service.create(data as CuentaCorriente).subscribe({
                next: () => {
                    this.handleSuccess('Cuenta creada correctamente');
                },
                error: () => this.loading.set(false)
            });
        }
    }

    deleteCuenta(id: number) {
        this.alertasService.confirm(
            '¿Desactivar Cuenta?',
            "La cuenta pasará a estado inactivo."
        ).then((confirmed) => {
            if (confirmed) {
                this.service.delete(id).subscribe(() => {
                    this.loadCuentas();
                    this.alertasService.success('Desactivada', 'La cuenta ha sido desactivada.');
                });
            }
        });
    }

    private handleSuccess(msg: string) {
        this.loading.set(false);
        this.isModalOpen = false;
        this.loadCuentas();
        this.alertasService.success('¡Éxito!', msg);
    }
}

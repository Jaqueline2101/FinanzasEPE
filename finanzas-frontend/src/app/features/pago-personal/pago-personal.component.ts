import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { PagoPersonalService } from '../../services/pago-personal.service';
import { PagoComprobante } from '../../models/pago-honorarios.model';

import { ModalPersonalComponent } from './modal-personal/modal-personal.component';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';
import { ModalComponent } from '../../shared/components/modal/modal';
import { EmpleadoService, EmpleadoDTO } from '../../services/empleado.service';
import { PagoPersonalRequest } from '../../models/pago-personal.model';
import { PagoMasivoComponent } from './pago-masivo/pago-masivo.component';
import { AlertasService } from '../../services/alertas.service';
import { animate, style, transition, trigger } from '@angular/animations';

@Component({
    selector: 'app-pago-personal',
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
        ModalPersonalComponent,
        ModalDetalleComponent,
        ModalComponent,
        PagoMasivoComponent
    ],
    templateUrl: './pago-personal.component.html',
    styleUrls: ['./pago-personal.component.css'],
    animations: [
        trigger('slideInOut', [
            transition(':enter', [
                style({ transform: 'translateX(100%)' }),
                animate('300ms ease-out', style({ transform: 'translateX(0)' }))
            ]),
            transition(':leave', [
                animate('300ms ease-in', style({ transform: 'translateX(100%)' }))
            ])
        ])
    ]
})
export class PagoPersonalComponent implements OnInit {
    registros: PagoComprobante[] = [];
    filteredRegistros: PagoComprobante[] = [];
    displayedRegistros: PagoComprobante[] = [];
    loading = false;
    isFullScreenView = false;
    showDetalleModal = false;

    // Bulk Mode State
    isBulkMode = false;
    currentPage = 1;
    selectedPago?: PagoComprobante;
    pageSize = 10;
    totalPagesCount = 1;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Procesos', route: '/admin/procesos' },
        { label: 'Pago de Personal' }
    ];

    columnas: Columna[] = [
        { field: 'idPago', header: '#', tipo: 'index', subField: [], align: 'center' },
        { field: 'nombreRazonSocial', header: 'EMPLEADO', tipo: 'title-subtitle', subField: ['cargoTrabajador'] },
        { field: 'tipoPago', header: 'TIPO', tipo: 'badge', subField: [], badgeConfig: { container: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200' } },
        { field: 'montoNeto', header: 'MONTO NETO', tipo: 'currency', subField: ['moneda'] },
        { field: 'medioPago', header: 'MEDIO', tipo: 'text', subField: [] },
        { field: 'estadoPago', header: 'ESTADO', tipo: 'status', subField: [] }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Empleado', value: 'EMPLEADO' },
        { label: 'Concepto', value: 'CONCEPTO' }
    ];

    estadoOptions = [
        { label: 'TODOS', value: 'TODOS' },
        { label: 'SUELDO', value: 'SUELDO' },
        { label: 'ADELANTO', value: 'ADELANTO' },
        { label: 'BONO', value: 'BONO' },
        { label: 'COMISIÓN', value: 'COMISIÓN' },
        { label: 'REEMBOLSO', value: 'REEMBOLSO' }
    ];

    constructor(
        private pagoService: PagoPersonalService,
        private alertasService: AlertasService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        setTimeout(() => {
            this.cargarRegistros();
        });
    }

    cargarRegistros() {
        setTimeout(() => {
            this.loading = true;
            this.cdr.detectChanges();

            this.pagoService.listarPagos().subscribe({
                next: (response) => {
                    this.registros = (response.data || []).map(r => ({
                        ...r,
                        estadoPago: (r.estadoPago === 'PENDIENTE' || r.estadoPago === 'PAGADO') ? 'ACTIVO' : 'INACTIVO'
                    }));
                    
                    this.onFilter({ term: '', searchType: 'ALL', estado: 'TODOS', fechaDesde: '', fechaHasta: '' });
                    
                    setTimeout(() => {
                        this.loading = false;
                        this.cdr.detectChanges();
                    });
                },
                error: (err) => {
                    console.error('Error cargando registros de pago personal', err);
                    setTimeout(() => {
                        this.loading = false;
                        this.cdr.detectChanges();
                    });
                }
            });
        });
    }

    onFilter(criteria: FilterCriteria) {
        setTimeout(() => {
            const term = (criteria.term || '').toLowerCase();
            const type = criteria.searchType || 'ALL';
            const estado = criteria.estado || 'TODOS';
            const from = criteria.fechaDesde ? new Date(criteria.fechaDesde + 'T00:00:00') : null;
            const to = criteria.fechaHasta ? new Date(criteria.fechaHasta + 'T23:59:59') : null;

            this.filteredRegistros = this.registros.filter(r => {
                let matchesTerm = true;
                if (term) {
                    const emp = (r.nombreRazonSocial || '').toLowerCase();
                    const con = (r.conceptoPago || '').toLowerCase();
                    const doc = (r.numeroDocumento || '').toLowerCase();
                    
                    if (type === 'EMPLEADO') matchesTerm = emp.includes(term) || doc.includes(term);
                    else if (type === 'CONCEPTO') matchesTerm = con.includes(term);
                    else matchesTerm = emp.includes(term) || con.includes(term) || doc.includes(term);
                }

                let matchesType = true;
                if (estado !== 'TODOS') matchesType = r.tipoPago === estado;

                let matchesDates = true;
                if (from && to && r.fechaPago) {
                    const pagoDate = new Date(r.fechaPago);
                    if (pagoDate < from || pagoDate > to) matchesDates = false;
                }

                return matchesTerm && matchesType && matchesDates;
            });

            this.currentPage = 1;
            this.updatePagination();
            this.cdr.detectChanges();
        });
    }

    updatePagination() {
        this.totalPagesCount = Math.ceil(this.filteredRegistros.length / this.pageSize);
        if (this.totalPagesCount === 0) this.totalPagesCount = 1;

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

    get totalPages(): number {
        return this.totalPagesCount;
    }

    abrirMasivo() {
        setTimeout(() => {
            this.isBulkMode = true;
            this.isFullScreenView = false;
            this.cdr.detectChanges();
        });
    }

    cancelarMasivo() {
        setTimeout(() => {
            this.isBulkMode = false;
            this.cdr.detectChanges();
        });
    }

    onMasivoSuccess() {
        this.cargarRegistros();
        this.cancelarMasivo();
        this.alertasService.success('¡Éxito!', 'Pago masivo procesado correctamente.');
    }

    abrirNuevo() {
        setTimeout(() => {
            this.selectedPago = undefined;
            this.isFullScreenView = true;
            this.cdr.detectChanges();
        });
    }

    editar(pago: PagoComprobante) {
        setTimeout(() => {
            this.selectedPago = pago;
            this.isFullScreenView = true;
            this.cdr.detectChanges();
        });
    }

    verDetalle(pago: PagoComprobante) {
        this.selectedPago = pago;
        this.showDetalleModal = true;
    }

    cerrarDetalle() {
        this.showDetalleModal = false;
        this.selectedPago = undefined;
    }

    onCancelForm() {
        this.isFullScreenView = false;
        this.selectedPago = undefined;
    }

    guardarPago(request: any) {
        setTimeout(() => {
            this.loading = true;
            this.pagoService.registrarPago(request).subscribe({
                next: (response) => {
                    this.cargarRegistros();
                    this.onCancelForm();
                    this.alertasService.success('¡Éxito!', 'Pago registrado correctamente.');
                },
                error: (err) => {
                    console.error('Error en la petición de registro:', err);
                    setTimeout(() => {
                        this.loading = false;
                        this.cdr.detectChanges();
                        this.alertasService.error('Error', 'No se pudo registrar el pago.');
                    });
                }
            });
        });
    }
}

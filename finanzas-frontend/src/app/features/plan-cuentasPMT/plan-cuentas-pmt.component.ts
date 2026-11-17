import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PlanCuentasPmtService } from '../../services/plan-cuentas-pmt.service';
import { AlertasService } from '../../services/alertas.service';
import { PlanCuentasRegistro, HistorialMovimiento } from '../../models/plan-cuentas-pmt.model';
import { animate, style, transition, trigger } from '@angular/animations';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { ModalCuentasComponent } from './modal-cuentas/modal-cuentas.component';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';

@Component({
    selector: 'app-plan-cuentas-pmt',
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
        ModalCuentasComponent,
        ModalDetalleComponent
    ],
    templateUrl: './plan-cuentas-pmt.component.html',
    styleUrls: ['./plan-cuentas-pmt.component.css'],
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
export class PlanCuentasPmtComponent implements OnInit {
    registros: PlanCuentasRegistro[] = [];
    historial: HistorialMovimiento[] = [];
    loading = false;
    showForm = false;
    showHistory = false;
    showDetailModal = false; // Nuevo
    isModalOpen = false;
    selectedRegistro: PlanCuentasRegistro | undefined;
    
    // Filtros unificados
    filterCriteria: FilterCriteria = {
        term: '',
        searchType: 'TODOS',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    filterOptions = [
        { label: 'TODOS', value: 'TODOS' },
        { label: 'PROVEEDOR', value: 'PROVEEDOR' },
        { label: 'MEDICO', value: 'MEDICO' },
        { label: 'TRABAJADOR', value: 'TRABAJADOR' }
    ];

    // Paginación
    currentPage = 0;
    pageSize = 10;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuraciones', route: '/admin/configuraciones' },
        { label: 'Plan de cuentas (P. M. T.)' }
    ];

    columnas: Columna[] = [
        { field: '', header: '#', tipo: 'index', subField: [] },
        { field: 'nombres', header: 'Nombre / Razón Social', tipo: 'title-subtitle', subField: ['apellidoPaterno'] },
        { field: 'tipoRegistro', header: 'Tipo', tipo: 'badge', subField: [], badgeConfig: { container: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200' } },
        { field: 'numeroDocumento', header: 'Documento', tipo: 'text', subField: [] },
        { field: 'fechaInicio', header: 'Fecha de Inicio', tipo: 'date', subField: [] },
        { field: 'fechaFin', header: 'Fecha de Término', tipo: 'date', subField: [] },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    constructor(
        private fb: FormBuilder,
        private pmtService: PlanCuentasPmtService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.cargarRegistros();
    }

    // Métodos de Tabla y Paginación
    onPageChange(page: number) {
        this.currentPage = page;
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
    }

    get totalPages(): number {
        return Math.ceil(this.filteredRegistros.length / this.pageSize);
    }

    get paginatedRegistros() {
        const start = this.currentPage * this.pageSize;
        const end = start + this.pageSize;
        return this.filteredRegistros.slice(start, end);
    }

    onEdit(item: any) {
        this.editar(item);
    }

    onHistory(item: any) {
        this.verHistorial(item.id);
    }

    onViewDetails(item: any) {
        this.loading = true;
        this.cd.detectChanges();
        this.pmtService.obtenerPorId(item.id).subscribe({
            next: (fullRecord) => {
                setTimeout(() => {
                    this.selectedRegistro = fullRecord;
                    this.showDetailModal = true;
                    this.loading = false;
                    this.cd.detectChanges();
                });
            },
            error: (err) => {
                console.error('Error al obtener detalle', err);
                setTimeout(() => {
                    this.loading = false;
                    this.cd.detectChanges();
                });
            }
        });
    }


    cargarRegistros() {
        this.loading = true;
        this.cd.detectChanges();
        this.pmtService.listar().subscribe({
            next: (data) => {
                setTimeout(() => {
                    this.registros = data;
                    this.loading = false;
                    this.cd.detectChanges(); // Forzar actualización de la tabla
                });
            },
            error: (err) => {
                console.error('Error cargando registros', err);
                setTimeout(() => {
                    this.loading = false;
                    this.cd.detectChanges();
                });
            }
        });
    }

    abrirNuevo() {
        setTimeout(() => {
            this.selectedRegistro = undefined;
            this.isModalOpen = true;
            this.cd.detectChanges();
        });
    }

    editar(registro: PlanCuentasRegistro) {
        if (!registro.id) return;
        this.loading = true;
        this.cd.detectChanges();
        this.pmtService.obtenerPorId(registro.id).subscribe({
            next: (fullRecord) => {
                setTimeout(() => {
                    // Fusionar con los datos de la lista por si el detalle omite campos clave (como tipoRegistro)
                    this.selectedRegistro = { ...registro, ...fullRecord };
                    this.isModalOpen = true;
                    this.loading = false;
                    this.cd.detectChanges();
                });
            },
            error: (err) => {
                this.alertasService.error('Error', 'No se pudo cargar el detalle completo del registro.');
                console.error('Error al obtener registro para editar', err);
                setTimeout(() => {
                    this.loading = false;
                    this.cd.detectChanges();
                });
            }
        });
    }

    onDelete(registro: PlanCuentasRegistro) {
        if (!registro.id) return;

        this.alertasService.deleteConfirm(
            '¿Eliminar Registro?',
            `¿Estás seguro de eliminar a "${registro.nombres}"? Esta acción no se puede deshacer.`
        ).then(confirmed => {
            if (confirmed) {
                this.loading = true;
                this.cd.detectChanges();
                this.pmtService.eliminar(registro.id!).subscribe({
                    next: () => {
                        this.alertasService.success('¡Eliminado!', 'El registro ha sido eliminado correctamente.');
                        this.cargarRegistros();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', 'Hubo un problema al intentar eliminar el registro.');
                        console.error('Error al eliminar', err);
                        this.loading = false;
                    }
                });
            }
        });
    }

    verHistorial(id: number) {
        this.pmtService.obtenerHistorial(id).subscribe(data => {
            this.historial = data;
            this.showHistory = true;
        });
    }

    guardar(data: any) {
        // Fusionar datos nuevos con el registro original para no perder campos (Bancarios/Económicos)
        const payload = this.selectedRegistro ? { ...this.selectedRegistro, ...data } : { ...data };

        // SANEAMIENTO DE PAYLOAD (Eliminar metadatos que el servidor suele proteger y causan Error 500)
        delete (payload as any).fechaRegistro;
        delete (payload as any).usuarioRegistro;
        delete (payload as any).fechaCre;
        delete (payload as any).idUsuarioCre;
        delete (payload as any).idUsuarioMod;
        delete (payload as any).fechaMod;

        // Programador Senior: Envolvemos TODO el flujo de suscripción en un setTimeout 
        // para asegurar que el cambio de estado 'loading' no ocurra durante un ciclo de check
        setTimeout(() => {
            this.loading = true;
            this.cd.detectChanges();

            const request = payload.id
                ? this.pmtService.actualizar(payload.id, payload)
                : this.pmtService.registrar(payload);

            request.subscribe({
                next: () => {
                    const mensaje = payload.id ? 'Registro actualizado correctamente.' : 'El registro se ha guardado correctamente.';
                    this.alertasService.success('¡Éxito!', mensaje);
                    
                    // Diferimos el cierre del modal y la recarga al siguiente ciclo
                    setTimeout(() => {
                        this.cargarRegistros();
                        this.isModalOpen = false;
                        this.showForm = false;
                        this.loading = false;
                        this.cd.detectChanges();
                    });
                },
                error: (err) => {
                    this.alertasService.error('Error', 'Hubo un problema al intentar procesar el registro.');
                    console.error('Error al guardar', err);
                    setTimeout(() => {
                        this.loading = false;
                        this.cd.detectChanges();
                    });
                }
            });
        });
    }

    onCancelForm() {
        setTimeout(() => {
            this.isModalOpen = false;
            this.selectedRegistro = undefined;
            this.cd.detectChanges();
        });
    }

    handleFilters(criteria: FilterCriteria) {
        this.filterCriteria = criteria;
        this.currentPage = 0;
    }

    get filteredRegistros() {
        return this.registros.filter(r => {
            const criteria = this.filterCriteria;
            
            // 1. Filtro por Tipo de Registro (Search Selector)
            const matchesType = !criteria.searchType || criteria.searchType === 'TODOS' || criteria.searchType === 'ALL' || r.tipoRegistro === criteria.searchType;
            
            // 2. Filtro por Estado (Dropdown)
            const matchesEstado = !criteria.estado || criteria.estado === 'TODOS' || r.estado === criteria.estado;

            // 3. Filtro por Texto (Term)
            const searchLower = (criteria.term || '').toLowerCase();
            const fullName = `${r.nombres} ${r.apellidoPaterno || ''} ${r.apellidoMaterno || ''} ${r.razonSocial || ''}`.toLowerCase();
            const matchesSearch = !searchLower ||
                fullName.includes(searchLower) ||
                (r.numeroDocumento && r.numeroDocumento.includes(searchLower));

            // 4. Filtro por Rango de Fechas (Si están presentes)
            // Usamos fechaInicio como referencia para el filtrado temporal
            let matchesDates = true;
            if (criteria.fechaDesde && r.fechaInicio) {
                matchesDates = matchesDates && r.fechaInicio >= criteria.fechaDesde;
            }
            if (criteria.fechaHasta && r.fechaInicio) {
                matchesDates = matchesDates && r.fechaInicio <= criteria.fechaHasta;
            }

            return matchesType && matchesEstado && matchesSearch && matchesDates;
        });
    }
}

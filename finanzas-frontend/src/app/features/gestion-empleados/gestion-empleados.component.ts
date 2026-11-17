import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { EmpleadoService, EmpleadoDTO } from '../../services/empleado.service';
import { ModalEmpleadoComponent } from './modal-empleado/modal-empleado.component';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-gestion-empleados',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        ModalEmpleadoComponent,
        ModalDetalleComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent,
        PrimaryButtonComponent
    ],
    templateUrl: './gestion-empleados.component.html'
})
export class GestionEmpleadosComponent implements OnInit {
    empleados: EmpleadoDTO[] = [];
    loading = false;
    mostrarModal = false;
    mostrarDetalle = false;
    selectedEmpleadoId: number | null = null;

    // Pagination
    currentPage = 0; // PaginationComponent uses 0-based index
    pageSize = 10;
    totalPages = 0;
    paginatedData: any[] = []; // Changed type to any to allow extra props

    columnas: Columna[] = [
        { field: 'itemIndex', header: '#', tipo: 'text', subField: [] },
        { field: 'nombreCompleto', header: 'Empleado', tipo: 'title-subtitle', subField: ['email'], align: 'left' },
        { field: 'identificacion', header: 'DNI / RUC', tipo: 'text', subField: [], align: 'center' },
        { field: 'nombreOcupacion', header: 'Cargo', tipo: 'text', subField: [], align: 'center' },
        { field: 'fechaIngreso', header: 'Fecha Ingreso', tipo: 'date', subField: [] },
        {
            field: 'cantidadFamiliares',
            header: 'Familiares',
            tipo: 'badge',
            subField: [],
            badgeConfig: { from: 'from-blue-50', to: 'to-blue-100', text: 'text-blue-700', border: 'border-blue-200' }
        },
        { field: 'estado', header: 'Estado', tipo: 'status', subField: [] }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'RR.HH.', route: '/admin/rrhh' },
        { label: 'Gestión de Empleados' }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'DNI/RUC', value: 'identificacion' },
        { label: 'Nombre', value: 'nombreCompleto' }
    ];

    currentSearchQuery = '';
    currentSearchType = 'ALL';
    filtroEstado = 'TODOS';
    fechaDesde = '';
    fechaHasta = '';

    allEmpleados: EmpleadoDTO[] = []; // Store full list for filtering

    constructor(private empleadoService: EmpleadoService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService) { }

    ngOnInit(): void {
        this.initDefaultDates();
        this.loadEmpleados();
    }

    initDefaultDates() {
        // We set them to empty so the initial view shows ALL employees
        // instead of filtering to the last 30 days by default.
        this.fechaDesde = '';
        this.fechaHasta = '';
    }

    private formatDate(date: Date): string {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();
        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;
        return [year, month, day].join('-');
    }

    loadEmpleados() {
        this.loading = true;
        this.empleadoService.getAll().subscribe({
            next: (data) => {
                // Filter for ACTIVE employees ONLY (Operational view)
                // Also exclude 'PACIENTE' roles
                const employeesOnly = data.filter(e => {
                    const isEmployee = !e.roles?.includes('PACIENTE');
                    const isActive = ((e.estado as any) === true || (e.estado as any) === 1 || (e.estado as any) === '1');
                    return isEmployee && isActive;
                });

                // Sort by ID descending (Newest first)
                const sortedData = employeesOnly.sort((a, b) => (b.id || 0) - (a.id || 0));

                this.allEmpleados = sortedData.map((e, index) => {
                    const nombre = (e.nombre || '').trim();
                    const apepat = (e.apepat || '').trim();
                    const apemat = (e.apemat || '').trim();
                    const nombreCompleto = `${nombre} ${apepat} ${apemat}`.trim().replace(/\s+/g, ' ');

                    return {
                        ...e,
                        itemIndex: index + 1, // Numeration 1, 2, 3...
                        nombreCompleto: nombreCompleto || 'Sin nombre',
                        identificacion: e.ruc || e.numdoc,
                        nombreOcupacion: e.nombreOcupacion || 'No asignado'
                    };
                });
                this.empleados = [...this.allEmpleados];
                this.applyFilter(); // Apply any existing filter
                this.updatePagination();
                this.loading = false;
                this.cd.detectChanges(); // Force update
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    handleFilters(filters: FilterCriteria) {
        this.currentSearchQuery = filters.term.toLowerCase();
        this.currentSearchType = filters.searchType;
        this.filtroEstado = filters.estado;
        this.fechaDesde = filters.fechaDesde;
        this.fechaHasta = filters.fechaHasta;
        this.currentPage = 0;
        this.applyFilter();
        this.updatePagination();
    }

    applyFilter() {
        let temp = [...this.allEmpleados];

        // 1. Search Filter
        if (this.currentSearchQuery) {
            temp = temp.filter(emp => {
                if (this.currentSearchType === 'ALL') {
                    return (emp as any).nombreCompleto.toLowerCase().includes(this.currentSearchQuery) ||
                        ((emp as any).identificacion && (emp as any).identificacion.toLowerCase().includes(this.currentSearchQuery));
                } else {
                    const val = (emp as any)[this.currentSearchType];
                    return val && String(val).toLowerCase().includes(this.currentSearchQuery);
                }
            });
        }

        // 2. Status Filter
        if (this.filtroEstado !== 'TODOS') {
            const estadoBool = this.filtroEstado === 'ACTIVO';
            temp = temp.filter(emp => {
                // Handle both boolean and numeric (1/0) or string ('1'/'0') status
                const empEstado = ((emp.estado as any) === true || (emp.estado as any) === 1 || (emp.estado as any) === '1');
                return empEstado === estadoBool;
            });
        }

        // 3. Date Filter (on fechaIngreso)
        if (this.fechaDesde && this.fechaHasta) {
            // Use date string comparison for YYYY-MM-DD to avoid timezone issues
            const start = this.fechaDesde; 
            const end = this.fechaHasta;

            temp = temp.filter(emp => {
                if (!emp.fechaIngreso) return false;
                // If fechaIngreso is a full timestamp, take only the date part
                const empDate = emp.fechaIngreso.split('T')[0];
                return empDate >= start && empDate <= end;
            });
        }

        this.empleados = temp;
    }

    nuevoEmpleado() {
        this.selectedEmpleadoId = null;
        this.mostrarModal = true;
    }

    editarEmpleado(empleado: EmpleadoDTO) {
        this.selectedEmpleadoId = empleado.id || null;
        this.mostrarModal = true;
    }

    eliminarEmpleado(empleado: EmpleadoDTO) {
        this.empleadoService.updateState(empleado.id!, false).subscribe({
            next: () => {
                // Defer removal to avoid NG0100 and allow successful confirmation feedback
                setTimeout(() => {
                    // Operational removal: Delete from local arrays
                    this.allEmpleados = this.allEmpleados.filter(e => e.id !== empleado.id);
                    this.applyFilter();
                    this.updatePagination();
                    
                    this.alertasService.success('Empleado Eliminado', 'El registro ha sido retirado de la gestión activa.');
                    this.cd.detectChanges();
                }, 0);
            },
            error: () => {
                this.alertasService.error('Error', 'No se pudo eliminar el registro del empleado');
            }
        });
    }

    onModalClose(guardado: boolean) {
        this.mostrarModal = false;
        if (guardado) {
            this.currentPage = 0; // Reset to page 1 to see new/edited entry
            this.loadEmpleados();
        }
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.updatePagination();
    }

    updatePagination() {
        this.totalPages = Math.ceil(this.empleados.length / this.pageSize);
        const start = this.currentPage * this.pageSize;
        const end = start + this.pageSize;
        this.paginatedData = this.empleados.slice(start, end);
    }

    onEdit(item: any) {
        this.editarEmpleado(item);
    }

    onDelete(item: any) {
        this.alertasService.deleteConfirm(
            '¿Eliminar Empleado?',
            `¿Estás seguro de que deseas eliminar a ${item.nombreCompleto}? Esta acción lo retirará de la lista de pagos y gestión.`
        ).then(confirmado => {
            if (confirmado) {
                this.eliminarEmpleado(item);
            }
        });
    }

    onView(item: any) {
        this.selectedEmpleadoId = item.id || null;
        this.mostrarDetalle = true;
    }
}

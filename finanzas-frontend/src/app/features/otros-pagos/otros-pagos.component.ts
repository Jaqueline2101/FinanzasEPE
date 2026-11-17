import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { OtrosPagosService } from '../../services/otros-pagos.service';
import { ModalPagosComponent } from './modal-pagos/modal-pagos.component';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';
import { AuthService } from '../auth/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AlertasService } from '../../services/alertas.service';
import { AlertasComponent } from '../../shared/components/alertas/alertas';


@Component({
    selector: 'app-otros-pagos',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TablaGeneralComponent,
        PaginationComponent,
        ModalPagosComponent,
        ModalDetalleComponent,
        BreadcrumbComponent,
        PageHeaderComponent,
        BarraFiltrosComponent,
        PrimaryButtonComponent,
        AlertasComponent
    ],
    templateUrl: './otros-pagos.component.html'
})
export class OtrosPagosComponent implements OnInit {
    // Option types for Strict Typing in template

    // Breadcrumb
    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Procesos', route: '/procesos' },


        { label: 'Otros Pagos' }
    ];

    // Table Columns
    columnas: Columna[] = [
        { field: 'id', header: '#', tipo: 'index', subField: [] },
        { field: 'beneficiario', header: 'Nombre / Persona', tipo: 'text', subField: [] },
        { field: 'tipoPersona', header: 'Tipo', tipo: 'badge', subField: [] },
        { field: 'dni', header: 'DNI', tipo: 'text', subField: [] },
        { field: 'monto', header: 'Monto', tipo: 'currency', subField: [] },
        { field: 'metodoPago', header: 'Método', tipo: 'text', subField: [] }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Beneficiario', value: 'BENEFICIARIO' },
        { label: 'Concepto', value: 'CONCEPTO' }
    ];

    estadoOptions = [
        { label: 'TODAS', value: 'TODAS' },
        { label: 'PENDIENTE', value: 'PENDIENTE' },
        { label: 'PAGADO', value: 'PAGADO' },
        { label: 'ANULADO', value: 'ANULADO' }
    ];

    // Signals and Data
    loading = true;
    showModal = false;
    totalRecords = 0;
    currentPage = 0;
    pageSize = 10;
    totalPages = 0;
    searchTerm = '';

    datos: any[] = [];
    datosPaginados: any[] = [];

    // Form
    currentSucursalId: number = 1;
    currentUserId: number = 1;


    constructor(
        private otrosPagosService: OtrosPagosService,
        private authService: AuthService,
        private http: HttpClient,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService,
        private router: Router
    ) { }

    ngOnInit() {
        this.initContext();
    }

    private initContext() {
        const user = this.authService.currentUser();
        if (user) {
            this.currentUserId = user.id;
            this.currentSucursalId = user.sucursalActualId || 1;
            this.cargarPagos();
        } else {
            // Fallback to localStorage if signal is not yet set (rare)
            const userStr = localStorage.getItem('currentUser');
            if (userStr) {
                const userObj = JSON.parse(userStr);
                this.currentUserId = userObj.id;
                this.currentSucursalId = userObj.sucursalActualId || 1;
                this.cargarPagos();
            }
        }
    }



    cargarPagos() {
        this.loading = true;

        const params = {
            idSucursal: this.currentSucursalId.toString(),
            page: this.currentPage.toString(),
            size: this.pageSize.toString()
        };

        this.otrosPagosService.obtenerOtrosPagos(params).subscribe({
            next: (res) => {
                this.datos = res.content || [];
                this.totalRecords = res.totalElements || 0;
                this.totalPages = res.totalPages || 0;
                this.actualizarVista();
                this.loading = false;
                this.cd.detectChanges(); 
            },
            error: () => {
                this.loading = false;
                this.cd.detectChanges(); 
            }
        });
    }

    actualizarVista() {
        // Aquí podríamos aplicar el buscador local si no viene del server
        if (this.searchTerm) {
            this.datosPaginados = this.datos.filter(d =>
                d.beneficiario?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                d.concepto?.toLowerCase().includes(this.searchTerm.toLowerCase())
            );
        } else {
            this.datosPaginados = [...this.datos];
        }
    }

    onFilter(criteria: FilterCriteria) {
        this.searchTerm = criteria.term;
        // Podríamos extender FilterCriteria para manejar el searchType si el backend lo soporta
        this.currentPage = 0;
        this.actualizarVista();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.cargarPagos();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.cargarPagos();
    }



    onPagoGuardado() {
        this.cargarPagos();
        this.closeModal(); // Ensure closeModal cleans up state
    }

    // Edit and View Actions
    // Edit and View Actions
    selectedPayment: any = null;
    showDetalleModal = false; // Separate modal for View

    editarPago(item: any) {
        this.selectedPayment = item;
        this.showModal = true;
    }

    verDetallePago(item: any) {
        this.selectedPayment = item;
        this.showDetalleModal = true;
    }

    openModal() {
        this.selectedPayment = null;
        this.showModal = true;
    }

    closeModal() {
        this.showModal = false;
        setTimeout(() => {
            if (!this.showModal) this.selectedPayment = null; // Clear if not opening other modal
        }, 300);
    }

    closeDetalleModal() {
        this.showDetalleModal = false;
        setTimeout(() => {
            if (!this.showDetalleModal) this.selectedPayment = null;
        }, 300);
    }

    anularPago(item: any) {
        this.alertasService.confirm(
            '¿Anular este pago?',
            'Esta acción no se puede deshacer.',
            'Sí, anular',
            'Cancelar',
            'bg-rose-600 hover:bg-rose-700 shadow-rose-600/30'
        ).then((confirmed) => {
            if (confirmed) {
                this.http.delete(`${environment.apiUrl}/finanzas/movimiento/${item.id}`, {
                    params: { idUsuario: '1', motivo: 'Anulación manual' }
                }).subscribe({
                    next: () => {
                        this.alertasService.success('Anulado', 'El pago ha sido anulado.');
                        this.cargarPagos();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', err.error?.message || 'No se pudo anular el pago');
                    }
                });
            }
        });
    }

    irAReportes() {
        this.router.navigate(['/admin/reportes/otros-pagos']);
    }
}

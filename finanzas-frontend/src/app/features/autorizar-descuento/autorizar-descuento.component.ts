import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../notificaciones-service/notification.service';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { SearchGenericComponent } from '../../shared/components/reusable-search-selector/reusable-search-selector';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import Swal from 'sweetalert2';
import { AutorizarDescuentoService } from '../../services/autorizar-descuento.service';
import { AutorizacionResponse } from '../../models/autorizar-descuento.model';
import { ModalResolucionComponent } from './modal-resolucion/modal-resolucion.component';
import { ModalComponent } from '../../shared/components/modal/modal';

@Component({
    selector: 'app-autorizar-descuento',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        BreadcrumbComponent,
        SearchGenericComponent,
        TablaGeneralComponent,
        PaginationComponent,
        ModalResolucionComponent,
        ModalComponent
    ],
    templateUrl: './autorizar-descuento.component.html',
    styleUrls: ['./autorizar-descuento.component.css']
})
export class AutorizarDescuentoComponent implements OnInit {
    breadcrumbItems = [
        { label: 'Configuración', route: '/admin/configuraciones' },
        { label: 'Autorizar Descuentos' }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Cajero', value: 'CAJERO' },
        { label: 'Estado', value: 'ESTADO' }
    ];

    pendientes: AutorizacionResponse[] = [];
    historial: AutorizacionResponse[] = [];
    filteredHistorial: AutorizacionResponse[] = [];

    loadingPendientes = false;
    loadingHistorial = false;
    searchTerm = '';

    currentPage = 0;
    pageSize = 10;

    get totalPages(): number {
        return Math.ceil(this.filteredHistorial.length / this.pageSize);
    }

    get paginatedHistorial(): AutorizacionResponse[] {
        const start = this.currentPage * this.pageSize;
        return this.filteredHistorial.slice(start, start + this.pageSize);
    }

    columnas: Columna[] = [
        { field: '', header: '#', tipo: 'index', subField: [] },
        { field: 'fechaResolucion', header: 'RESOLUCIÓN', tipo: 'date', subField: [] },
        { field: 'pacienteNombre', header: 'PACIENTE', tipo: 'text', subField: [] },
        { field: 'doctorNombre', header: 'MÉDICO', tipo: 'text', subField: [] },
        { field: 'cajeroNombre', header: 'CAJERO', tipo: 'text', subField: [] },
        { field: 'montoDescuento', header: 'DIFERENCIA (S/)', tipo: 'currency', subField: [] },
        { field: 'estado', header: 'ESTADO', tipo: 'order-status', subField: [] }
    ];


    mostrarModal = false;
    selectedAutorizacion: AutorizacionResponse | null = null;
    accionModal: 'APROBAR' | 'RECHAZAR' | 'RESOLVER' = 'APROBAR';

    // Para el modal detalles de historial (si se quisiera ver detalle de rechazo)
    mostrarDetalle = false;

    constructor(
        private service: AutorizarDescuentoService,
        private cd: ChangeDetectorRef,
        private route: ActivatedRoute,
        private router: Router,
        private notificationService: NotificationService
    ) { }

    ngOnInit() {
        this.cargarPendientes();
        this.cargarHistorial();
    }

    cargarPendientes() {
        this.loadingPendientes = true;
        this.service.listarPendientes().subscribe({
            next: (res: any) => {
                if (res.success) {
                    this.pendientes = res.data;
                    this.verificarQueryParamApertura();
                }
                this.loadingPendientes = false;
                this.cd.detectChanges();
            },
            error: (err: any) => {
                console.error('Error cargando pendientes', err);
                this.loadingPendientes = false;
                this.cd.detectChanges();
            }
        });
    }

    verificarQueryParamApertura() {
        const openId = this.route.snapshot.queryParamMap.get('openId');
        if (openId) {
            const id = Number(openId);
            const found = this.pendientes.find(p => p.idAutorizacion === id);
            if (found) {
                // Remove the query param so it doesn't reopen on refresh
                this.router.navigate([], { queryParams: { openId: null }, queryParamsHandling: 'merge', replaceUrl: true });
                this.abrirResolver(found);
            }
        }
    }

    abrirResolver(item: AutorizacionResponse) {
        this.accionModal = 'RESOLVER';
        this.selectedAutorizacion = item;
        this.mostrarModal = true;
        this.cd.detectChanges();
    }

    cargarHistorial() {
        this.loadingHistorial = true;
        this.service.listarResueltos().subscribe({
            next: (res: any) => {
                if (res.success) {
                    this.historial = res.data;
                    this.onSearch({ q: this.searchTerm, type: 'ALL' });
                }
                this.loadingHistorial = false;
                this.cd.detectChanges();
            },
            error: (err: any) => {
                console.error('Error cargando historial', err);
                this.loadingHistorial = false;
                this.cd.detectChanges();
            }
        });
    }

    onSearch(event: { q: string, type: string }) {
        this.searchTerm = event.q;
        const type = event.type;

        if (!this.searchTerm) {
            this.filteredHistorial = [...this.historial];
        } else {
            const lower = this.searchTerm.toLowerCase();
            this.filteredHistorial = this.historial.filter(a => {
                if (type === 'ALL') {
                    return a.cajeroNombre?.toLowerCase().includes(lower) ||
                        a.pacienteNombre?.toLowerCase().includes(lower) ||
                        a.pacienteDni?.toLowerCase().includes(lower) ||
                        a.doctorNombre?.toLowerCase().includes(lower) ||
                        a.estado?.toLowerCase().includes(lower) ||
                        a.motivoSolicitud?.toLowerCase().includes(lower);

                } else if (type === 'CAJERO') {
                    return a.cajeroNombre?.toLowerCase().includes(lower);
                } else if (type === 'ESTADO') {
                    return a.estado?.toLowerCase().includes(lower);
                }
                return false;
            });
        }
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

    abrirAprobar(item: AutorizacionResponse) {
        this.accionModal = 'APROBAR';
        this.selectedAutorizacion = item;
        this.mostrarModal = true;
        this.cd.detectChanges();
    }

    abrirRechazar(item: AutorizacionResponse) {
        this.accionModal = 'RECHAZAR';
        this.selectedAutorizacion = item;
        this.mostrarModal = true;
        this.cd.detectChanges();
    }

    verDetalles(item: AutorizacionResponse) {
        this.selectedAutorizacion = item;
        this.mostrarDetalle = true;
        this.cd.detectChanges();
    }

    onSaveSuccess() {
        this.mostrarModal = false;
        
        if (this.selectedAutorizacion) {
            this.notificationService.removeNotificationByContentId('AUTORIZACION_DESCUENTO', this.selectedAutorizacion.idAutorizacion);
        }
        
        this.cargarPendientes();
        this.cargarHistorial();
    }

    closeModal() {
        this.mostrarModal = false;
        this.selectedAutorizacion = null;
    }
}

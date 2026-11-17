import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { ModalAnularComponent } from './modal-anular/modal-anular.component';
import { environment } from '../../../environments/environment';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import Swal from 'sweetalert2';

// ─── DTO: lo que devuelve el backend por venta ───────────────────────────────
export interface VentaResumen {
    idVenta: number;
    fecha: string;           // 'yyyy-MM-dd'
    comprobante: string;     // 'B 001-00000002'
    tipoDoc: string;
    paciente: string;
    tipoAtencion: string | null;
    metodoPago: string;
    total: number;
    estado: string;          // 'PAGADO' | 'ANULADO'
    cajeroId: number;
    cajeroNombre?: string;
    // campo auxiliar para la tabla
    fechaFormat?: string;
    hoySoloFecha?: boolean;  // si la venta es del día de hoy
}

@Component({
    selector: 'app-anular-comprobante',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        BreadcrumbComponent,
        PaginationComponent,
        TablaGeneralComponent,
        ModalAnularComponent,
        BarraFiltrosComponent
    ],
    providers: [DatePipe],
    templateUrl: './anular-comprobante.component.html',
})
export class AnularComprobanteComponent implements OnInit {

    // ─── Breadcrumb ─────────────────────────────────────────────────────────
    breadcrumbItems = [
        { label: 'Punto de Venta', route: '/admin/punto-venta' },
        { label: 'Anular Comprobante', route: '/admin/punto-venta/anular-comprobante' },
    ];

    // ─── Filtros ─────────────────────────────────────────────────────────────
    fechaFiltroInicio: string = this.hoy();
    fechaFiltroFin: string = this.hoy();
    busquedaTexto: string = '';
    searchType: string = 'ALL';
    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Nro Comprobante', value: 'COMPROBANTE' },
        { label: 'Paciente', value: 'PACIENTE' }
    ];

    // ─── Tabla ────────────────────────────────────────────────────────────────
    columnas: Columna[] = [
        { header: 'N° Comprobante', field: 'comprobante', align: 'center' },
        { header: 'Fecha', field: 'fechaFormat', align: 'center' },
        { header: 'Paciente', field: 'paciente', align: 'left' },
        { 
            header: 'Tipo Atención', 
            field: 'tipoAtencion', 
            align: 'center', 
            tipo: 'badge',
            badgeConfig: { from: 'from-blue-50', to: 'to-blue-100', text: 'text-blue-700', border: 'border-blue-200' }
        },
        { header: 'Método Pago', field: 'metodoPago', align: 'center' },
        { header: 'Total', field: 'total', align: 'right', tipo: 'currency' },
        { header: 'Estado', field: 'estado', align: 'center', tipo: 'order-status' },
        { 
            header: 'Acción', 
            field: 'dummy', 
            tipo: 'action-button',
            actionLabel: 'Anular',
            actionIcon: 'fa-ban',
            actionClass: 'bg-rose-50 text-rose-700 border border-rose-200 hover:bg-rose-100 hover:border-rose-400',
            align: 'center' 
        }
    ];

    ventas: VentaResumen[] = [];
    ventasFiltradas: VentaResumen[] = [];
    isLoading = false;

    // ─── Paginación ───────────────────────────────────────────────────────────
    paginaActual = 0;
    tamanoPagina = 10;
    totalElements = 0;
    totalPages = 0;

    // ─── Modal anulación ──────────────────────────────────────────────────────
    mostrarModal = false;
    ventaSeleccionada: VentaResumen | null = null;
    anulando = false;

    private api = `${environment.apiUrl}/ventas`;

    constructor(
        private http: HttpClient,
        private datePipe: DatePipe,
        private cd: ChangeDetectorRef,
    ) { }

    ngOnInit(): void {
        this.cargarVentas();
    }

    // ─── Carga ventas del día ─────────────────────────────────────────────────
    cargarVentas(): void {
        this.isLoading = true;
        const params = new HttpParams()
            .set('fechaInicio', this.fechaFiltroInicio)
            .set('fechaFin', this.fechaFiltroFin)
            .set('page', '0')
            .set('size', '200');     // traemos bastantes para filtrar localmente

        this.http.get<any>(this.api, { params }).subscribe({
            next: (res) => {
                // El endpoint GET /api/ventas devuelve Page<VentaRegistroDto>
                // Adaptamos la respuesta al DTO local
                const content: any[] = res?.data?.content ?? res?.content ?? [];
                this.ventas = content.map((v: any) => ({
                    idVenta: v.idVenta,
                    fecha: v.fecha,          // 'yyyy-MM-dd'
                    comprobante: `${v.tipoDoc ?? ''} ${v.serie ?? ''}-${v.numdoc ?? ''}`.trim(),
                    tipoDoc: v.tipoDoc,
                    paciente: v.nombrePac ?? v.paciente ?? '',
                    tipoAtencion: v.tipoAtencion ?? '—',
                    metodoPago: this.formatMetodoPago(v.metodoPago),
                    total: Number(v.total ?? 0),
                    estado: v.estado === 10 ? 'ANULADO' : (v.estado === 16 || v.estado === 1 ? 'PAGADO' : (typeof v.estado === 'string' ? v.estado : 'PAGADO')),
                    cajeroId: v.idPersonalUser ?? 0,
                    fechaFormat: this.datePipe.transform(v.fecha, 'dd/MM/yyyy') ?? v.fecha,
                }));
                this.aplicarFiltroLocal();
                this.isLoading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando ventas', err);
                this.isLoading = false;
                Swal.fire('Error', 'No se pudieron cargar las ventas del día', 'error');
                this.cd.detectChanges();
            },
        });
    }

    // ─── Filtro local por texto ───────────────────────────────────────────────
    aplicarFiltroLocal(): void {
        const q = this.busquedaTexto.toLowerCase().trim();
        const base = this.ventas.filter(v => v.estado === 'PAGADO');   // solo anulables
        this.ventasFiltradas = q
            ? base.filter(v => {
                if (this.searchType === 'PACIENTE') return v.paciente.toLowerCase().includes(q);
                if (this.searchType === 'COMPROBANTE') return v.comprobante.toLowerCase().includes(q);
                return v.paciente.toLowerCase().includes(q) || v.comprobante.toLowerCase().includes(q);
            })
            : base;
        this.totalElements = this.ventasFiltradas.length;
        this.totalPages = Math.ceil(this.totalElements / this.tamanoPagina);
        this.paginaActual = 0;
    }

    get ventasPaginadas(): VentaResumen[] {
        const start = this.paginaActual * this.tamanoPagina;
        return this.ventasFiltradas.slice(start, start + this.tamanoPagina);
    }

    onPageChange(page: number): void {
        this.paginaActual = page;
    }

    onPageSizeChange(size: number): void {
        this.tamanoPagina = size;
        this.paginaActual = 0;
        this.totalPages = Math.ceil(this.totalElements / this.tamanoPagina);
    }

    onFilter(criteria: FilterCriteria): void {
        this.busquedaTexto = criteria.term || '';
        this.searchType = criteria.searchType || 'ALL';
        
        const fechaCambio = criteria.fechaDesde !== this.fechaFiltroInicio || criteria.fechaHasta !== this.fechaFiltroFin;
        
        this.fechaFiltroInicio = criteria.fechaDesde || this.hoy();
        this.fechaFiltroFin = criteria.fechaHasta || this.hoy();

        if (fechaCambio) {
            this.cargarVentas();
        } else {
            this.aplicarFiltroLocal();
        }
    }

    // ─── Abrir modal de anulación ─────────────────────────────────────────────
    abrirAnulacion(venta: VentaResumen): void {
        // Regla: solo se puede anular el mismo día
        if (venta.fecha !== this.hoy()) {
            Swal.fire({
                icon: 'warning',
                title: 'Fuera de plazo',
                html: `El comprobante <strong>${venta.comprobante}</strong> es de una fecha anterior.<br>
                       Debe usar <strong>"Anular falta autorizar"</strong> para este caso.`,
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#f59e0b',
            });
            return;
        }
        this.ventaSeleccionada = venta;
        this.anulando = false;
        this.mostrarModal = true;
    }

    cerrarModal(): void {
        if (this.anulando) return;
        this.mostrarModal = false;
        this.ventaSeleccionada = null;
    }

    onAction(event: {item: any, col: Columna}): void {
        this.abrirAnulacion(event.item);
    }

    // ─── Confirmar anulación (recibe el motivo validado desde el hijo) ─────────
    confirmarAnulacion(motivo: string): void {
        if (!this.ventaSeleccionada || !motivo) return;

        // Doble confirmación visual
        Swal.fire({
            icon: 'warning',
            title: '¿Confirmar anulación?',
            html: `Se anulará definitivamente el comprobante:<br>
                   <strong>${this.ventaSeleccionada.comprobante}</strong><br>
                   <span class="text-sm text-gray-500">Paciente: ${this.ventaSeleccionada.paciente}</span><br>
                   <span class="text-sm text-gray-500">Total: S/ ${Number(this.ventaSeleccionada.total).toFixed(2)}</span><br><br>
                   <em>Esta acción <b>no se puede deshacer</b>.</em>`,
            showCancelButton: true,
            confirmButtonText: 'Sí, anular',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#e11d48',
            cancelButtonColor: '#64748b',
            reverseButtons: true,
        }).then((result) => {
            if (!result.isConfirmed || !this.ventaSeleccionada) return;

            this.anulando = true;
            const params = new HttpParams()
                .set('motivo', motivo);

            this.http.delete<any>(
                `${this.api}/${this.ventaSeleccionada.idVenta}`,
                { params }
            ).subscribe({
                next: () => {
                    this.anulando = false;
                    this.mostrarModal = false;
                    Swal.fire({
                        icon: 'success',
                        title: '¡Anulado!',
                        text: `El comprobante ${this.ventaSeleccionada!.comprobante} fue anulado correctamente.`,
                        timer: 3000,
                        showConfirmButton: false,
                    });
                    this.ventaSeleccionada = null;
                    this.cargarVentas();
                },
                error: (err) => {
                    this.anulando = false;
                    const msg = err?.error?.message ?? 'Ocurrió un error al anular el comprobante.';
                    Swal.fire('Error al anular', msg, 'error');
                    this.cd.detectChanges();
                },
            });
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    // getters motivoValido / motivoChars ahora viven en ModalAnularComponent

    /** Retorna true si la fecha dada (yyyy-MM-dd) es el día de hoy */
    isToday(fecha: string): boolean {
        return fecha === this.hoy();
    }

    private hoy(): string {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    private formatMetodoPago(metodo: string | null | undefined): string {
        if (!metodo) return '—';
        if (metodo === 'EFECTIVO' || metodo === '1') return 'Efectivo';
        if (metodo === 'YAPE_PLIN' || metodo === '2' || metodo === '3') return 'Yape/Plin';
        if (metodo === 'TRANSFERENCIA' || metodo === '4') return 'Transferencia';
        if (metodo === 'MIXTO') return 'Mixto';
        return metodo;
    }
}

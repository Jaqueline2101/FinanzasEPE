import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { trigger, transition, style, animate } from '@angular/animations';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { ModalNotaComponent } from './modal-nota/modal-nota.component';
import { environment } from '../../../environments/environment';
import { SearchGenericComponent } from '../../shared/components/reusable-search-selector/reusable-search-selector';
import Swal from 'sweetalert2';

// ─── DTOs ────────────────────────────────────────────────────────────────────
export interface VentaParaNC {
    idVenta: number;
    fecha: string;
    comprobante: string;
    tipoDoc: string;        // Solo B o F llegan aquí
    paciente: string;
    metodoPago: string;
    total: number;
    estado: string;
    fechaFormat?: string;
}

export interface NotaCreditoItem {
    idNc: number;
    numeroNc: string;       // "NC-B 00000001"
    comprobanteRef: string;
    tipoComprobante: string;
    paciente: string;
    motivoCodigo: string;
    motivoDescripcion: string;
    detalle: string;
    montoNc: number;
    montoOriginal: number;
    estado: string;         // EMITIDA | ANULADA
    nombreUsuario: string;
    fechaEmision: string;
    fechaCreacion: string;
}

export interface MotivoSunat {
    codigo: string;
    descripcion: string;
}

@Component({
    selector: 'app-nota-credito',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, BreadcrumbComponent, PaginationComponent, ModalNotaComponent, SearchGenericComponent, TablaGeneralComponent],
    providers: [DatePipe],
    templateUrl: './nota-credito.component.html',
    animations: [
        trigger('fadeIn', [
            transition(':enter', [
                style({ opacity: 0, transform: 'translateY(10px)' }),
                animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
            ])
        ])
    ]
})
export class NotaCreditoComponent implements OnInit {

    // ─── Breadcrumb ──────────────────────────────────────────────────────────
    breadcrumbItems = [
        { label: 'Punto de Venta', route: '/admin/punto-venta' },
        { label: 'Notas de Crédito', route: '/admin/punto-venta/notas-credito' },
    ];

    // ─── Configuración Tablas ────────────────────────────────────────────────
    columnasVentas: Columna[] = [
        { field: 'index', header: '#', align: 'center', tipo: 'index' },
        { 
            field: 'tipoDoc', 
            header: 'TIPO', 
            align: 'center', 
            tipo: 'badge',
            badgeConfig: { from: 'bg-violet-50', text: 'text-violet-700', border: 'border-violet-200' }
        },
        { field: 'comprobante', header: 'N° COMPROBANTE', align: 'center' },
        { field: 'fechaFormat', header: 'FECHA', align: 'center' },
        { field: 'paciente', header: 'PACIENTE', align: 'left' },
        { field: 'total', header: 'TOTAL', align: 'right', tipo: 'currency' },
        { 
            field: 'acciones', 
            header: 'ACCIONES', 
            align: 'center', 
            tipo: 'custom-button-action',
            actionLabel: 'Nota Crédito',
            actionIcon: 'fa-file-circle-plus',
            actionClass: '!bg-blue-600 hover:!bg-blue-700 !text-white !border-blue-600 px-3 py-1 scale-95'
        }
    ];

    columnasHistorial: Columna[] = [
        { field: 'index', header: '#', align: 'center', tipo: 'index' },
        { field: 'numeroNc', header: 'N° NC', align: 'center' },
        { field: 'estado', header: 'ESTADO', align: 'center', tipo: 'order-status' },
        { field: 'tipoComprobante', header: 'TIPO', align: 'center', tipo: 'badge' },
        { field: 'comprobanteRef', header: 'REF', align: 'left' },
        { field: 'montoNc', header: 'MONTO NC', align: 'right', tipo: 'currency' },
        { 
            field: 'acciones', 
            header: 'ACCIONES', 
            align: 'center', 
            tipo: 'custom-button-action',
            actionLabel: 'Anular Nota Crédito',
            actionIcon: 'fa-times',
            actionClass: '!bg-rose-600 hover:!bg-rose-700 !text-white !border-rose-600 px-3 py-1 scale-95'
        }
    ];

    handleActionVenta(item: any): void {
        this.abrirModal(item);
    }

    handleActionHistorial(item: any): void {
        if (item.estado === 'EMITIDA') {
            this.anularNC(item);
        }
    }

    // ─── Pestañas ────────────────────────────────────────────────────────────
    pestanaActiva: 'buscar' | 'historial' = 'buscar';

    @ViewChild(SearchGenericComponent) searchComponent!: SearchGenericComponent;

    // ─── Tab 1: Buscar comprobante (solo Boletas y Facturas) ─────────────────
    fechaDesde: string = this.hace30dias();
    fechaHasta: string = this.hoy();
    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'N° Comprobante', value: 'DOC' },
        { label: 'Paciente', value: 'PAC' }
    ];
    busquedaTexto: string = '';
    ventas: VentaParaNC[] = [];
    ventasFiltradas: VentaParaNC[] = [];
    isLoadingVentas = false;

    paginaVentas = 0;
    tamanoVentas = 10;
    totalVentas = 0;
    totalPagesVentas = 0;

    // ─── Modal emitir NC ─────────────────────────────────────────────────────
    mostrarModal = false;
    ventaSeleccionada: VentaParaNC | null = null;
    emitiendo = false;

    // ─── Tab 2: Historial de NCs ──────────────────────────────────────────────
    fechaDesdeNC: string = this.hace30dias();
    fechaHastaNC: string = this.hoy();
    notasCredito: NotaCreditoItem[] = [];
    isLoadingNC = false;
    filtroEstadoNC: string = '';

    paginaNC = 0;
    tamanoNC = 10;
    totalNC = 0;
    totalPagesNC = 0;

    // ─── Contexto ────────────────────────────────────────────────────────────
    idSucursal = 1;
    idUsuario = 0;
    totalElements = 0;

    private apiVentas = `${environment.apiUrl}/ventas`;
    private apiNC = `${environment.apiUrl}/notas-credito`;

    constructor(
        private http: HttpClient,
        private datePipe: DatePipe,
        private cd: ChangeDetectorRef,
    ) { }

    ngOnInit(): void {
        this.cargarContexto();
    }

    private cargarContexto(): void {
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            this.idSucursal = user.idSucursal || 1;
        }
        this.cd.detectChanges();
        this.cargarVentas();
    }

    // ─── TAB 1: Buscar Boletas y Facturas ────────────────────────────────────

    cargarVentas(): void {
        this.isLoadingVentas = true;
        this.cd.detectChanges();
        const params = new HttpParams()
            .set('fechaInicio', this.fechaDesde)
            .set('fechaFin', this.fechaHasta)
            .set('page', '0')
            .set('size', '200');

        this.http.get<any>(this.apiVentas, { params }).subscribe({
            next: (res) => {
                const content: any[] = res?.data?.content ?? res?.content ?? [];
                const todas = content.map((v: any) => ({
                    idVenta: v.idVenta,
                    fecha: v.fecha,
                    comprobante: `${v.tipoDoc ?? ''} ${v.serie ?? ''}-${v.numdoc ?? ''}`.trim(),
                    tipoDoc: (v.tipoDoc ?? '').trim().toUpperCase(),
                    paciente: v.nombrePac ?? v.paciente ?? '',
                    metodoPago: v.metodoPago ?? '—',
                    total: Number(v.total ?? 0),
                    estado: v.estado ?? 'PAGADO',
                    fechaFormat: this.datePipe.transform(v.fecha, 'dd/MM/yyyy') ?? v.fecha,
                }));

                // SOLO Boletas (B) y Facturas (F) — tickets internos NO generan NC
                setTimeout(() => {
                    this.ventas = todas.filter(v =>
                        (v.tipoDoc === 'B' || v.tipoDoc === 'F') && v.estado !== 'ANULADO'
                    );
                    this.aplicarFiltroRefined('', 'ALL');
                    this.isLoadingVentas = false;
                    this.cd.detectChanges();
                }, 0);
            },
            error: () => {
                this.isLoadingVentas = false;
                Swal.fire('Error', 'No se pudieron cargar los comprobantes.', 'error');
            }
        });
    }


    handleSearch(event: { q: string, type: string }): void {
        this.busquedaTexto = event.q;
        this.aplicarFiltroRefined(event.q, event.type);
        // detectChanges ya se llama en aplicarFiltroRefined
    }

    aplicarFiltroRefined(q: string, type: string): void {
        const query = q.toLowerCase().trim();
        this.ventasFiltradas = query
            ? this.ventas.filter(v => {
                if (type === 'DOC') return v.comprobante.toLowerCase().includes(query);
                if (type === 'PAC') return v.paciente.toLowerCase().includes(query);
                return v.comprobante.toLowerCase().includes(query) || v.paciente.toLowerCase().includes(query);
            })
            : [...this.ventas];

        this.totalVentas = this.ventasFiltradas.length;
        this.totalPagesVentas = Math.ceil(this.totalVentas / this.tamanoVentas);
        this.paginaVentas = 0;
        this.totalElements = this.totalVentas;
        this.cd.detectChanges();
    }

    get ventasPaginadas(): VentaParaNC[] {
        const start = this.paginaVentas * this.tamanoVentas;
        return this.ventasFiltradas.slice(start, start + this.tamanoVentas);
    }

    onFiltrarVentas(): void { this.cargarVentas(); }

    onPageVentas(p: number): void { 
        this.paginaVentas = p; 
        this.cd.detectChanges();
    }
    onPageSizeVentas(s: number): void {
        this.tamanoVentas = s;
        this.paginaVentas = 0;
        this.totalPagesVentas = Math.ceil(this.totalVentas / s);
        this.cd.detectChanges();
    }

    // ─── Modal emitir NC ─────────────────────────────────────────────────────

    onRestablecer(): void {
        this.fechaDesde = this.hace30dias();
        this.fechaHasta = this.hoy();
        this.busquedaTexto = '';
        if (this.searchComponent) {
            this.searchComponent.reset();
        }
        this.cd.detectChanges();
        this.cargarVentas();
    }

    abrirModal(venta: VentaParaNC): void {
        this.ventaSeleccionada = venta;
        this.mostrarModal = true;
    }

    cerrarModal(): void {
        if (this.emitiendo) return;
        this.mostrarModal = false;
        this.ventaSeleccionada = null;
    }

    emitirNC(datos: { motivoCodigo: string; montoNc: number; detalleNc: string }): void {
        if (this.emitiendo) return;

        this.emitiendo = true;
        const body = {
            idVentaRef: this.ventaSeleccionada!.idVenta,
            idSucursal: this.idSucursal,
            idUsuario: this.idUsuario,
            motivoCodigo: datos.motivoCodigo,
            montoNc: datos.montoNc,
            detalle: datos.detalleNc || null,
        };

        this.http.post<any>(this.apiNC, body).subscribe({
            next: (res) => {
                this.emitiendo = false;
                const nc = res?.data;
                Swal.fire({
                    icon: 'success',
                    title: '¡Nota de Crédito emitida!',
                    html: `Número: <strong>${nc?.numeroNc ?? ''}</strong><br>
                           Comprobante ref: ${this.ventaSeleccionada!.comprobante}<br>
                           Monto: <strong>S/ ${Number(nc?.montoNc ?? 0).toFixed(2)}</strong>`,
                    confirmButtonColor: '#7c3aed',
                });
                this.cerrarModal();
                this.cargarVentas();
                this.cd.detectChanges();
            },
            error: (err) => {
                this.emitiendo = false;
                const msg = err?.error?.message ?? 'No se pudo emitir la Nota de Crédito.';
                Swal.fire('Error', msg, 'error');
                this.cd.detectChanges();
            }
        });
    }

    // ─── TAB 2: Historial NC ─────────────────────────────────────────────────

    cambiarPestana(tab: 'buscar' | 'historial'): void {
        this.pestanaActiva = tab;
        this.cd.detectChanges();
        if (tab === 'historial') this.cargarHistorial();
    }

    cargarHistorial(): void {
        this.isLoadingNC = true;
        this.cd.detectChanges();
        let params = new HttpParams()
            .set('idSucursal', this.idSucursal.toString())
            .set('page', this.paginaNC.toString())
            .set('size', this.tamanoNC.toString());

        if (this.fechaDesdeNC) params = params.set('fechaDesde', this.fechaDesdeNC);
        if (this.fechaHastaNC) params = params.set('fechaHasta', this.fechaHastaNC);
        if (this.filtroEstadoNC) params = params.set('estado', this.filtroEstadoNC);

        this.http.get<any>(this.apiNC, { params }).subscribe({
            next: (res) => {
                const page = res?.data ?? res;
                this.notasCredito = (page?.content ?? []).map((n: any) => ({
                    ...n,
                    montoNc: Number(n.montoNc ?? 0),
                    montoOriginal: Number(n.montoOriginal ?? 0),
                    fechaEmision: this.datePipe.transform(n.fechaEmision, 'dd/MM/yyyy') ?? n.fechaEmision,
                    fechaCreacion: this.datePipe.transform(n.fechaCreacion, 'dd/MM/yyyy HH:mm') ?? n.fechaCreacion,
                }));
                this.totalNC = page?.totalElements ?? 0;
                this.totalPagesNC = page?.totalPages ?? 0;
                this.isLoadingNC = false;
                this.cd.detectChanges();
            },
            error: () => {
                this.isLoadingNC = false;
                Swal.fire('Error', 'No se pudo cargar el historial.', 'error');
            }
        });
    }

    onFiltroEstado(): void { this.paginaNC = 0; this.cargarHistorial(); }
    onPageNC(p: number): void { 
        this.paginaNC = p; 
        this.cargarHistorial(); 
        this.cd.detectChanges();
    }

    anularNC(nc: NotaCreditoItem): void {
        Swal.fire({
            icon: 'warning',
            title: '¿Anular Nota de Crédito?',
            html: `Se anulará la NC: <strong>${nc.numeroNc}</strong><br>
                   Referencia: <em>${nc.comprobanteRef}</em><br>
                   Monto: <strong>S/ ${Number(nc.montoNc).toFixed(2)}</strong><br><br>
                   <b>Esta acción no restaura la venta original.</b>`,
            showCancelButton: true,
            confirmButtonText: 'Sí, anular NC',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#dc2626',
            cancelButtonColor: '#64748b',
            reverseButtons: true,
        }).then(result => {
            if (!result.isConfirmed) return;
            this.http.put<any>(`${this.apiNC}/${nc.idNc}/anular?idUsuario=${this.idUsuario}`, {})
                .subscribe({
                    next: () => {
                        Swal.fire('Anulada', 'La Nota de Crédito fue anulada.', 'info');
                        this.cargarHistorial();
                    },
                    error: (err) => Swal.fire('Error', err?.error?.message ?? 'No se pudo anular la NC.', 'error')
                });
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    badgeEstado(estado: string): string {
        return estado === 'EMITIDA'
            ? 'bg-violet-50 text-violet-700 border-violet-300'
            : 'bg-rose-50 text-rose-700 border-rose-300';
    }

    badgeIcon(estado: string): string {
        return estado === 'EMITIDA' ? 'fa-file-invoice' : 'fa-times-circle';
    }

    badgeTipo(tipo: string): string {
        return tipo === 'BOLETA'
            ? 'bg-sky-50 text-sky-700 border-sky-200'
            : 'bg-indigo-50 text-indigo-700 border-indigo-200';
    }

    private hoy(): string {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    private hace30dias(): string {
        const d = new Date();
        d.setDate(d.getDate() - 30);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }
}

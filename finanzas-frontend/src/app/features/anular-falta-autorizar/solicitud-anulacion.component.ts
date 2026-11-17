import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { AutorizarDescuentoService } from '../../services/autorizar-descuento.service';
import { AutorizacionResponse } from '../../models/autorizar-descuento.model';
import { environment } from '../../../environments/environment';
import Swal from 'sweetalert2';
import { forkJoin } from 'rxjs';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';

// ─── DTOs ────────────────────────────────────────────────────────────────────
export interface VentaParaSolicitud {
    idVenta: number;
    fecha: string;          // 'yyyy-MM-dd'
    comprobante: string;
    tipoDoc: string;
    paciente: string;
    metodoPago: string;
    total: number;
    estado: string;
    fechaFormat?: string;
}

export interface SolicitudAnulacion {
    idSolicitud: number;
    idVenta: number;
    comprobante: string;
    fechaVenta: string;
    montoVenta: number;
    motivoSolicitud: string;
    estado: string;         // 'PENDIENTE' | 'APROBADO' | 'RECHAZADO'
    nombreSolicitante: string;
    nombreAutorizador?: string;
    motivoRechazo?: string;
    fechaCreacion: string;
    fechaResolucion?: string;
}

@Component({
    selector: 'app-solicitud-anulacion',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, BreadcrumbComponent, TablaGeneralComponent, BarraFiltrosComponent, PaginationComponent],
    providers: [DatePipe],
    templateUrl: './solicitud-anulacion.component.html',
})
export class SolicitudAnulacionComponent implements OnInit {

    // ─── Breadcrumb ──────────────────────────────────────────────────────────
    breadcrumbItems = [
        { label: 'Punto de Venta', route: '/admin/punto-venta' },
        { label: 'Anular falta autorizar', route: '/admin/punto-venta/anular-falta-autorizar' },
    ];

    // ─── Bandeja Unificada ───────────────────────────────────────────────────
    solicitudesUnificadas: any[] = [];
    solicitudesFiltradas: any[] = [];
    isLoading = false;

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Detalle / Ref.', value: 'REF' },
        { label: 'Solicitante', value: 'SOLICITANTE' }
    ];

    busquedaTexto: string = '';
    searchType: string = 'ALL';
    fechaFiltroInicio: string = '';
    fechaFiltroFin: string = '';

    // ─── Paginación ──────────────────────────────────────────────────────────
    paginaActual = 0;
    tamanoPagina = 10;
    totalElements = 0;
    totalPages = 0;

    columnasUnificadas: Columna[] = [
        { header: 'Tipo', field: '_tipoBadge', align: 'center', tipo: 'badge',
          badgeConfig: { from: 'from-blue-50', to: 'to-blue-100', text: 'text-blue-700', border: 'border-blue-200' }
        },
        { header: 'Fecha', field: '_fechaFormat', align: 'center' },
        { header: 'Detalle / Ref.', field: '_referencia', align: 'left' },
        { header: 'Monto', field: '_monto', align: 'right', tipo: 'currency' },
        { header: 'Solicitante', field: '_solicitante', align: 'left' },
        { header: 'Motivo', field: '_motivo', align: 'left' },
        { header: 'Estado', field: '_estadoFinal', align: 'center', tipo: 'badge' }
    ];

    // ─── Contexto ────────────────────────────────────────────────────────────
    idSucursal = 1;
    idUsuario = 0;

    private apiSolicitudes = `${environment.apiUrl}/solicitudes-anulacion`;

    constructor(
        private http: HttpClient,
        private datePipe: DatePipe,
        private cd: ChangeDetectorRef,
        private autorizarDescuentoService: AutorizarDescuentoService
    ) { }

    ngOnInit(): void {
        this.cargarContexto();
    }

    private cargarContexto(): void {
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            this.idUsuario = user.id || user.userId || 0;
            this.idSucursal = user.idSucursal || 1;
        }
        this.cargarTodo();
    }

    cargarTodo(): void {
        this.isLoading = true;

        let paramsAnulaciones = new HttpParams()
            .set('idSucursal', this.idSucursal.toString())
            .set('page', '0')
            .set('size', '100'); // Cargamos las últimas 100 anulaciones

        forkJoin({
            anulaciones: this.http.get<any>(this.apiSolicitudes, { params: paramsAnulaciones }),
            descuentos: this.autorizarDescuentoService.listarPorCajero(this.idUsuario)
        }).subscribe({
            next: (res) => {
                const anulacionesRaw = res.anulaciones?.data?.content || res.anulaciones?.content || [];
                const descuentosRaw = res.descuentos?.data || [];

                const anulacionesMapped = anulacionesRaw.map((s: any) => {
                    const estadoLabel = s.estado === 'APROBADO' ? 'ANULADO' : s.estado;
                    return {
                        ...s,
                        _tipoBadge: 'ANULACIÓN',
                        _badgeConfigTipo: { from: 'from-rose-50', to: 'to-rose-100', text: 'text-rose-700', border: 'border-rose-200' },
                        _fechaFormat: this.datePipe.transform(s.fechaCreacion, 'dd/MM/yyyy HH:mm') ?? s.fechaCreacion,
                        _fechaObj: new Date(s.fechaCreacion),
                        _referencia: s.comprobante,
                        _monto: s.montoVenta,
                        _solicitante: s.nombreSolicitante,
                        _motivo: s.motivoSolicitud,
                        _estadoFinal: estadoLabel,
                        _badgeConfig: this.getBadgeConfig(estadoLabel)
                    };
                });

                const descuentosMapped = descuentosRaw.map((s: any) => {
                    const estadoLabel = s.estado === 'APROBADO' ? 'ACEPTADO' : s.estado;
                    return {
                        ...s,
                        _tipoBadge: 'DESCUENTO',
                        _badgeConfigTipo: { from: 'from-indigo-50', to: 'to-indigo-100', text: 'text-indigo-700', border: 'border-indigo-200' },
                        _fechaFormat: this.datePipe.transform(s.fechaSolicitud, 'dd/MM/yyyy HH:mm') ?? s.fechaSolicitud,
                        _fechaObj: new Date(s.fechaSolicitud),
                        _referencia: `Dscto: S/ ${s.montoDescuento}`,
                        _monto: s.montoFinal,
                        _solicitante: s.cajeroNombre,
                        _motivo: s.motivoSolicitud,
                        _estadoFinal: estadoLabel,
                        _badgeConfig: this.getBadgeConfig(estadoLabel)
                    };
                });

                this.solicitudesUnificadas = [...anulacionesMapped, ...descuentosMapped]
                    .sort((a, b) => b._fechaObj.getTime() - a._fechaObj.getTime());

                this.aplicarFiltroLocal();
                this.isLoading = false;
                this.cd.detectChanges();
            },
            error: () => {
                this.isLoading = false;
                this.cd.detectChanges();
            }
        });
    }

    onFilter(criteria: FilterCriteria): void {
        this.busquedaTexto = criteria.term || '';
        this.searchType = criteria.searchType || 'ALL';
        this.aplicarFiltroLocal();
    }

    aplicarFiltroLocal(): void {
        const q = this.busquedaTexto.toLowerCase().trim();
        this.solicitudesFiltradas = q
            ? this.solicitudesUnificadas.filter(s => {
                const refMatch = s._referencia?.toLowerCase().includes(q);
                const solMatch = s._solicitante?.toLowerCase().includes(q);
                if (this.searchType === 'REF') return refMatch;
                if (this.searchType === 'SOLICITANTE') return solMatch;
                return refMatch || solMatch;
            })
            : this.solicitudesUnificadas;
        this.totalElements = this.solicitudesFiltradas.length;
        this.totalPages = Math.ceil(this.totalElements / this.tamanoPagina);
        this.paginaActual = 0;
    }

    get solicitudesPaginadas(): any[] {
        const start = this.paginaActual * this.tamanoPagina;
        return this.solicitudesFiltradas.slice(start, start + this.tamanoPagina);
    }

    onPageChange(page: number): void {
        this.paginaActual = page;
    }

    onPageSizeChange(size: number): void {
        this.tamanoPagina = size;
        this.paginaActual = 0;
        this.totalPages = Math.ceil(this.totalElements / this.tamanoPagina);
    }


    private getBadgeConfig(estado: string) {
        switch (estado) {
            case 'PENDIENTE': return { from: 'from-amber-50', to: 'to-amber-100', text: 'text-amber-700', border: 'border-amber-200' };
            case 'APROBADO': 
            case 'ACEPTADO': return { from: 'from-emerald-50', to: 'to-emerald-100', text: 'text-emerald-700', border: 'border-emerald-200' };
            case 'ANULADO': return { from: 'from-purple-50', to: 'to-purple-100', text: 'text-purple-700', border: 'border-purple-200' };
            case 'RECHAZADO': return { from: 'from-rose-50', to: 'to-rose-100', text: 'text-rose-700', border: 'border-rose-200' };
            default: return { from: 'from-slate-50', to: 'to-slate-100', text: 'text-slate-700', border: 'border-slate-200' };
        }
    }


    // ─── Helpers ─────────────────────────────────────────────────────────────

    badgeEstado(estado: string): string {
        switch (estado) {
            case 'PENDIENTE': return 'bg-amber-50 text-amber-700 border-amber-300';
            case 'APROBADO': return 'bg-emerald-50 text-emerald-700 border-emerald-300';
            case 'RECHAZADO': return 'bg-rose-50 text-rose-700 border-rose-300';
            default: return 'bg-slate-50 text-slate-600 border-slate-200';
        }
    }

    badgeIcon(estado: string): string {
        switch (estado) {
            case 'PENDIENTE': return 'fa-clock';
            case 'APROBADO': return 'fa-check-circle';
            case 'RECHAZADO': return 'fa-times-circle';
            default: return 'fa-circle';
        }
    }

    private hoy(): string {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    private ayer(): string {
        const d = new Date();
        d.setDate(d.getDate() - 1);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    private hace30dias(): string {
        const d = new Date();
        d.setDate(d.getDate() - 30);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }
}

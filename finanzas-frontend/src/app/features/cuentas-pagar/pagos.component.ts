import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PagosService } from '../../services/pagos.service';
import { CronogramaPago } from '../../models/cronograma-pago.model';
import { CompraDeuda } from '../../models/compra-deuda.model';
import { PagosModalComponent } from './pagos-modal/pagos-modal.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-pagos',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule,
        PageHeaderComponent,
        BreadcrumbComponent,
        PaginationComponent,
        TablaGeneralComponent,
        PagosModalComponent,
        BarraFiltrosComponent
    ],
    templateUrl: './pagos.component.html'
})
export class PagosComponent implements OnInit {
    comprasDeuda: CompraDeuda[] = [];
    filteredCompras: CompraDeuda[] = [];
    loading = false;
    // Configuración Tabla General (Nivel: COMPRAS)
    columnas: Columna[] = [
        { field: 'index', header: '#', subField: [], tipo: 'index' },
        { field: 'vencimientoFinal', header: 'Vencimiento', subField: [] }, // Último vencimiento o próximo
        { field: 'proveedorRazonSocial', header: 'Proveedor', subField: [] },
        { field: 'serie', header: 'Documento', subField: [] },
        // Custom format for "1/3"
        { field: 'avance', header: 'Cuotas', subField: [], tipo: 'text' },
        { field: 'montoPendiente', header: 'Deuda Pendiente', subField: [], tipo: 'currency' },
        {
            field: 'estadoGeneral',
            header: 'Estado',
            subField: [],
            tipo: 'badge',
            badgeConfig: {
                container: 'rounded-full px-2 py-0.5 text-xs font-bold',
                from: 'bg-emerald-100 text-emerald-700 border-emerald-200',
                to: 'bg-red-100 text-red-700 border-red-200' // Dynamic override in semaphore logic
            }
        },
        {
            field: 'productos', // Changed from 'Gestionar' to 'Productos'
            header: 'Productos', // Changed header
            subField: [],
            tipo: 'text', // Simple text list
        }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Procesos' },


        { label: 'Cuentas por Pagar' }
    ];

    currentPage = 0;
    pageSize = 10;
    totalPages = 0;

    countVerde = 0;
    countAmbar = 0;
    countRojo = 0;
    totalDeudaGlobal = 0;

    showModalDetalle = false;
    selectedCompra: CompraDeuda | null = null;

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Proveedor', value: 'PROVEEDOR' },
        { label: 'Documento', value: 'DOCUMENTO' }
    ];

    estadoOptions = [
        { label: 'Todas', value: 'TODOS' },
        { label: 'Vencidas', value: 'VENCIDO' },
        { label: 'Por Vencer', value: 'POR_VENCER' },
        { label: 'Al Día', value: 'AL_DIA' }
    ];

    constructor(private pagosService: PagosService, private cd: ChangeDetectorRef) { }

    ngOnInit(): void {
        this.cargarDatos();
    }

    cargarDatos() {
        this.loading = true;
        this.pagosService.listarDeudas({ size: 200, sort: 'fechaVencimiento,asc' }).subscribe({
            next: (page) => {
                const rawCuotas = page.content;
                this.procesarYAgrupar(rawCuotas);
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    procesarYAgrupar(cuotas: CronogramaPago[]) {
        const grupos = new Map<number, CompraDeuda>();

        cuotas.forEach(c => {
            const idCompra = c.compra?.id || c.idCompra || 0;

            if (!grupos.has(idCompra)) {
                // Extract product names
                const detalles = c.compra?.detalles || [];
                const nombresProductos = detalles.map((d: any) => d.producto?.nombreProducto).filter((n: any) => n).join(', ');

                grupos.set(idCompra, {
                    idCompra: idCompra,
                    proveedorRazonSocial: c.compra?.proveedor?.razonSocial || 'Desconocido',
                    serie: c.compra?.serie || 'S/N',
                    cuotas: [],
                    cuotasTotal: 0,
                    cuotasPagadas: 0,
                    montoTotal: 0,
                    montoPagado: 0,
                    montoPendiente: 0,
                    vencimientoFinal: '', // Se calculará después
                    estadoGeneral: 'AL_DIA'
                });
                // Add dynamic property for products
                (grupos.get(idCompra) as any)['productos'] = nombresProductos;
            }

            const grupo = grupos.get(idCompra)!;
            grupo.cuotas.push(c);
            grupo.montoTotal += c.montoCuota;

            if (c.estado === 'PAGADO') {
                grupo.cuotasPagadas++;
                grupo.montoPagado += c.montoCuota;
            } else {
                grupo.montoPendiente += c.montoCuota;
            }
        });

        const lista: CompraDeuda[] = [];
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        this.countVerde = 0;
        this.countAmbar = 0;
        this.countRojo = 0;
        this.totalDeudaGlobal = 0;

        grupos.forEach(g => {
            g.cuotas.sort((a, b) => new Date(a.fechaVencimiento).getTime() - new Date(b.fechaVencimiento).getTime());
            g.cuotasTotal = g.cuotas.length;

            const primeraPendiente = g.cuotas.find(c => c.estado !== 'PAGADO');
            const relevante = primeraPendiente || g.cuotas[g.cuotas.length - 1];
            g.vencimientoFinal = relevante.fechaVencimiento;

            const tieneVencida = g.cuotas.some(c => c.estado !== 'PAGADO' && new Date(c.fechaVencimiento + 'T00:00:00') < today);

            const tresDias = new Date(today);
            tresDias.setDate(today.getDate() + 3);
            const tienePorVencer = g.cuotas.some(c => c.estado !== 'PAGADO' && new Date(c.fechaVencimiento + 'T00:00:00') <= tresDias && new Date(c.fechaVencimiento + 'T00:00:00') >= today);

            if (g.montoPendiente <= 0.01) {
                g.estadoGeneral = 'PAGADO';
                g.colorEstado = 'green';
            } else if (tieneVencida) {
                g.estadoGeneral = 'VENCIDO';
                g.colorEstado = 'red';
                this.countRojo++;
            } else if (tienePorVencer) {
                g.estadoGeneral = 'POR_VENCER';
                g.colorEstado = 'amber';
                this.countAmbar++;
            } else {
                g.estadoGeneral = 'AL_DIA';
                g.colorEstado = 'green';
                this.countVerde++;
            }

            this.totalDeudaGlobal += g.montoPendiente;

            (g as any)['avance'] = `${g.cuotasPagadas}/${g.cuotasTotal}`;

            if (g.montoPendiente > 0.01) {
                lista.push(g);
            }
        });

        this.comprasDeuda = lista.sort((a, b) => {
            const score = (estado: string) => {
                if (estado === 'VENCIDO') return 0;
                if (estado === 'POR_VENCER') return 1;
                return 2;
            };
            return score(a.estadoGeneral) - score(b.estadoGeneral);
        });

        this.filteredCompras = [...this.comprasDeuda];
        this.calculatePagination();

        // Si hay un modal abierto, actualizar la referencia de selectedCompra para sincronizar datos
        if (this.selectedCompra) {
            const updated = this.comprasDeuda.find(c => c.idCompra === this.selectedCompra?.idCompra);
            if (updated) {
                this.selectedCompra = updated;
            }
        }
    }

    onFilter(criteria: FilterCriteria) {
        this.filteredCompras = this.comprasDeuda.filter(c => {
            // 1. Filtrar por término y tipo
            let matchesTerm = true;
            if (criteria.term) {
                const term = criteria.term.toLowerCase();
                if (criteria.searchType === 'PROVEEDOR') {
                    matchesTerm = (c.proveedorRazonSocial || '').toLowerCase().includes(term);
                } else if (criteria.searchType === 'DOCUMENTO') {
                    matchesTerm = (c.serie || '').toLowerCase().includes(term);
                } else {
                    matchesTerm = (c.proveedorRazonSocial || '').toLowerCase().includes(term) ||
                        (c.serie || '').toLowerCase().includes(term);
                }
            }

            // 2. Filtrar por estado
            let matchesEstado = true;
            if (criteria.estado !== 'TODOS') {
                matchesEstado = c.estadoGeneral === criteria.estado;
            }

            // 3. Filtrar por fechas (vencimientoFinal)
            let matchesDates = true;
            if (criteria.fechaDesde && criteria.fechaHasta) {
                const d = new Date(c.vencimientoFinal + 'T00:00:00');
                const from = new Date(criteria.fechaDesde + 'T00:00:00');
                const to = new Date(criteria.fechaHasta + 'T00:00:00');
                matchesDates = d >= from && d <= to;
            }

            return matchesTerm && matchesEstado && matchesDates;
        });

        this.currentPage = 0;
        this.calculatePagination();
    }

    onPageChange(page: number) {
        this.currentPage = page;
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.calculatePagination();
    }

    calculatePagination() {
        this.totalPages = Math.ceil(this.filteredCompras.length / this.pageSize);
        if (this.totalPages === 0) this.totalPages = 1;
    }

    onActionClick(item: any) {
        this.selectedCompra = item;
        this.showModalDetalle = true;
    }

    closeModal() {
        this.showModalDetalle = false;
        this.selectedCompra = null;
    }

    onPagoExitoso() {
        this.cargarDatos();
    }
}

import { Component, OnInit, ChangeDetectorRef, signal, computed, WritableSignal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { SearchGenericComponent } from '../../shared/components/reusable-search-selector/reusable-search-selector';

import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { ProductoService } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';
import { forkJoin } from 'rxjs';
import { KardexService, KardexResponse } from '../../services/kardex.service';
import { AlertasService } from '../../services/alertas.service';
import { trigger, transition, style, animate } from '@angular/animations';
import { ModalProductoComponent } from './modal-producto/modal-producto.component';
import { ModalDetalleProductoComponent } from './detalle-producto/modal-detalle.component';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';

interface ProductoConsolidado {
    idCatalogo: number;
    idCatalogoStr: string;
    nombre: string;
    codigo: string;
    categoria: string;
    unidadMedida: string;
    stockTotal: number;
    lotes: Producto[];
    isExpanded?: boolean;
}

@Component({
    selector: 'app-productos',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        BreadcrumbComponent,
        PrimaryButtonComponent,
        TablaGeneralComponent,
        BarraFiltrosComponent,
        PaginationComponent,
        ModalProductoComponent,
        ModalDetalleProductoComponent
    ],
    templateUrl: './productos.component.html',
    styleUrls: ['./productos.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductosComponent implements OnInit {
    // Signals para el estado base
    productos = signal<Producto[]>([]);
    kardexMovements = signal<KardexResponse[]>([]);
    filterCriteria = signal<FilterCriteria>({
        term: '',
        searchType: 'ALL',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    });

    currentPage = signal(0);
    pageSize = signal(10);
    pageSizeOptions = [5, 10, 25, 50];

    // Signals Computados (Mágica Senior)
    consolidados = computed(() => {
        const prods = this.productos();
        const movements = this.kardexMovements();
        return this._consolidarData(prods, movements);
    });

    filteredConsolidados = computed(() => {
        const all = this.consolidados();
        const criteria = this.filterCriteria();
        return this._filtrarData(all, criteria);
    });

    totalPages = computed(() => {
        const count = this.filteredConsolidados().length;
        const size = this.pageSize();
        return Math.max(1, Math.ceil(count / size));
    });

    displayedConsolidados = computed(() => {
        const allFiltered = this.filteredConsolidados();
        const start = this.currentPage() * this.pageSize();
        const end = start + this.pageSize();
        return allFiltered.slice(start, end);
    });

    today = new Date();
    isLoading = false;
    searchTerm = '';
    searchType = 'ALL';

    // Breadcrumbs
    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Procesos' },
        { label: 'Productos' }
    ];

    // UI State Signals (Senior Standard)
    showDetailModal = signal(false);
    showModal = signal(false);
    selectedProducto = signal<Producto | null>(null);
    selectedProductoDetalle = signal<Producto | null>(null);

    columnas: Columna[] = [
        { field: 'index', header: '#', tipo: 'index', align: 'center' },
        { field: 'codigo', header: 'Código', tipo: 'badge', badgeConfig: { from: 'from-slate-100', to: 'to-slate-200', text: 'text-slate-600', border: 'border-slate-300' } },
        { field: 'nombre', header: 'Producto / Bien', tipo: 'title-subtitle', subField: ['idCatalogoStr'] },
        { field: 'categoria', header: 'Categoría', tipo: 'badge' },
        { field: 'stockTotal', header: 'Stock Total', tipo: 'badge', badgeConfig: { from: 'from-indigo-100', to: 'to-indigo-200', text: 'text-indigo-700', border: 'border-indigo-300' } },
        { field: 'unidadMedida', header: 'Unidad' }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Nombre de Producto', value: 'NOMBRE' },
        { label: 'Categoría', value: 'CATEGORIA' }
    ];

    constructor(
        private productoService: ProductoService,
        private kardexService: KardexService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadProductos();
    }

    loadProductos(): void {
        this.isLoading = false;

        // 1. Carga inmediata de Productos (Prioridad: Visualización)
        this.productoService.getAll().subscribe({
            next: (productos: Producto[]) => {
                setTimeout(() => {
                    this.productos.set(productos);
                    console.log('Productos cargados para visualización inicial');
                }, 0);
            },
            error: (err) => {
                console.error('Error cargando productos', err);
                this.alertasService.error('Error', 'No se pudieron cargar los productos');
            }
        });

        // 2. Carga diferida de Kardex (Prioridad: Sincronización de Stock)
        this.kardexService.getTodos(0, 2000).subscribe({
            next: (resp: any) => {
                setTimeout(() => {
                    this.kardexMovements.set(resp.content || resp || []);
                    console.log('Historial de Kardex sincronizado (2000 registros)');
                }, 0);
            },
            error: (err) => {
                console.error('Error cargando historial de kardex', err);
            }
        });
    }

    private _consolidarData(productos: Producto[], kardexMovements: KardexResponse[]): ProductoConsolidado[] {
        if (!productos || productos.length === 0) return [];

        const groups = new Map<number, Producto[]>();

        // 1. Agrupar productos base por su ID de catálogo
        productos.forEach(p => {
            const id = p.idCatalogo;
            if (!groups.has(id)) {
                groups.set(id, []);
            }
            groups.get(id)!.push(p);
        });

        // 2. Indexación de movimientos de Kardex para alto rendimiento (O(N+M))
        const stockByBatch = new Map<string, number>();
        kardexMovements.forEach(m => {
            const mDate = m.fechaVenc ? new Date(m.fechaVenc).toISOString().split('T')[0] : 'SINFECHA';
            const key = `${m.idCatalogo}_${m.nroLote || 'SINLOTE'}_${mDate}`;
            const current = stockByBatch.get(key) || 0;
            const cant = Number(m.cantidad);
            stockByBatch.set(key, m.signo === '+' ? current + cant : current - cant);
        });

        // 3. Procesar cada catálogo para consolidar lotes
        return Array.from(groups.values()).map(lotesDelCatalogo => {
            const first = lotesDelCatalogo[0];
            const visualLotesMap = new Map<string, Producto>();

            lotesDelCatalogo.forEach(p => {
                const pDate = p.fechaVencimiento ? new Date(p.fechaVencimiento).toISOString().split('T')[0] : 'SINFECHA';
                const visualKey = `${p.registroInvimaLote || 'SINLOTE'}_${pDate}`;
                const batchKey = `${p.idCatalogo}_${p.registroInvimaLote || 'SINLOTE'}_${pDate}`;
                
                // RESILIENCIA: El stockReal final es el del Kardex SI existe, de lo contrario respetamos el de la DB
                const kardexStock = stockByBatch.get(batchKey);
                const stockRealFinal = (kardexStock !== undefined) ? kardexStock : (p.stockReal || 0);

                if (!visualLotesMap.has(visualKey)) {
                    visualLotesMap.set(visualKey, {
                        ...p,
                        stockReal: stockRealFinal
                    });
                }
            });

            const visualLotes = Array.from(visualLotesMap.values());

            return {
                idCatalogo: first.idCatalogo,
                idCatalogoStr: `ID: ${first.idCatalogo}`,
                nombre: first.nombreProducto || 'N/A',
                codigo: first.codigo || 'N/A',
                categoria: first.categoria || 'N/A',
                unidadMedida: first.unidadMedida || 'N/A',
                stockTotal: visualLotes.reduce((sum, p) => sum + p.stockReal, 0),
                lotes: visualLotes.sort((a, b) => {
                    if (!a.fechaVencimiento) return 1;
                    if (!b.fechaVencimiento) return -1;
                    return new Date(a.fechaVencimiento).getTime() - new Date(b.fechaVencimiento).getTime();
                }),
                isExpanded: false
            };
        }).sort((a, b) => {
            const maxIdA = Math.max(...a.lotes.map(l => l.idProducto || 0));
            const maxIdB = Math.max(...b.lotes.map(l => l.idProducto || 0));
            return maxIdB - maxIdA;
        });
    }

    private _filtrarData(consolidados: ProductoConsolidado[], criteria: FilterCriteria): ProductoConsolidado[] {
        const term = (criteria.term || '').toLowerCase();
        const type = criteria.searchType;
        const estado = criteria.estado;
        const from = criteria.fechaDesde ? new Date(criteria.fechaDesde) : null;
        const to = criteria.fechaHasta ? new Date(criteria.fechaHasta) : null;

        return consolidados.filter(c => {
            let matchesTerm = true;
            if (term) {
                if (type === 'NOMBRE') matchesTerm = c.nombre.toLowerCase().includes(term);
                else if (type === 'CATEGORIA') matchesTerm = c.categoria.toLowerCase().includes(term);
                else matchesTerm = c.nombre.toLowerCase().includes(term) || c.categoria.toLowerCase().includes(term);
            }

            let matchesEstado = true;
            if (estado !== 'TODOS') {
                const isStockActive = c.stockTotal > 0 ? 'ACTIVO' : 'INACTIVO';
                matchesEstado = isStockActive === estado;
            }

            let matchesDates = true;
            if (from || to) {
                matchesDates = c.lotes.some(l => {
                    if (!l.fechaVencimiento) return false;
                    const venc = new Date(l.fechaVencimiento);
                    if (from && venc < from) return false;
                    if (to && venc > to) return false;
                    return true;
                });
            }

            return matchesTerm && matchesEstado && matchesDates;
        });
    }

    handleFilters(criteria: FilterCriteria): void {
        this.filterCriteria.set(criteria);
        this.currentPage.set(0);
    }

    onPageChange(page: number): void {
        this.currentPage.set(page);
    }

    onPageSizeChange(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0);
    }

    openModal(): void {
        this.selectedProducto.set(null);
        this.showModal.set(true);
    }

    closeModal(): void {
        this.showModal.set(false);
        this.showDetailModal.set(false);
        this.selectedProducto.set(null);
        this.selectedProductoDetalle.set(null);
    }

    onViewDetails(item: any): void {
        const firstBatch = item.lotes ? item.lotes[0] : item;
        const stockToShow = item.stockTotal > 0 ? item.stockTotal : (firstBatch.stockReal || 0);
        
        const prod = { 
            ...firstBatch, 
            stockReal: stockToShow 
        };
        
        this.selectedProductoDetalle.set(prod);
        this.showDetailModal.set(true);
    }

    onEdit(item: any): void {
        const prod = item.lotes ? item.lotes[0] : item;
        this.selectedProducto.set(prod);
        this.showModal.set(true);
    }

    onAddLote(item: any): void {
        const prod = item.lotes ? item.lotes[0] : item;

        this.selectedProducto.set({
            ...prod,
            idProducto: undefined, 
            lote: '',
            stockReal: 0,
            fechaVencimiento: undefined,
            registroInvimaLote: ''
        });
        this.showModal.set(true);
    }

    onDelete(item: any): void {
        const prod = item.lotes ? item.lotes[0] : item;
        this.alertasService.deleteConfirm(
            '¿Estás seguro?',
            'No podrás revertir esto. Solo se eliminará este lote/registro específico.'
        ).then((confirmed) => {
            if (confirmed) {
                this.productoService.delete(prod.idProducto).subscribe({
                    next: () => {
                        this.alertasService.success('Eliminado', 'El registro ha sido eliminado.');
                        this.loadProductos();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', 'No se pudo eliminar el registro.');
                    }
                });
            }
        });
    }

    onSave(formValue: any): void {
        const id = this.selectedProducto()?.idProducto;

        const payload: any = {
            idProducto: formValue.idProducto ?? null,
            idSucursal: Number(formValue.idSucursal),
            idCatalogo: formValue.idCatalogo || null,
            codigo: formValue.codigo?.trim() || '',
            nombreProducto: formValue.nombreProducto?.trim() || '',
            categoria: formValue.categoria || '',
            codigoBarra: formValue.codigoBarra?.trim() || '',
            marcaLaboratorio: formValue.marcaLaboratorio?.trim() || '',
            modelo: formValue.modelo?.trim() || '',
            unidadMedida: formValue.unidadMedida || 'Unidad',
            moneda: formValue.moneda || 'PEN',
            regSanitario: formValue.regSanitario?.trim() || '',
            ubicacion: formValue.ubicacion?.trim() || '',
            almacenamiento: formValue.almacenamiento || 'Normal',
            idTipoTemperatura: formValue.idTipoTemperatura ? Number(formValue.idTipoTemperatura) : null,
            fechaVencimiento: formValue.fechaVencimiento && formValue.fechaVencimiento.trim() !== '' ? formValue.fechaVencimiento : null,
            registroInvimaLote: formValue.registroInvimaLote?.trim() || '',
            precioCompra: Number(formValue.precioCompra) || 0,
            stockReal: Number(formValue.stockReal) || 0,
            stockMinimo: Number(formValue.stockMinimo) || 0,
            stockMaximo: Number(formValue.stockMaximo) || 0,
            factorCompra: Number(formValue.factorCompra) || 1,
            manejaUnidad: true,
            tipoAfectacionCompra: formValue.tipoAfectacionCompra || 'GRAVADO_ONEROSO',
            diasAlertaVencimiento: Number(formValue.diasAlertaVencimiento) || 30,
            tipo: 'PRODUCTO'
        };

        console.log('📦 PAYLOAD ENVIADO:', JSON.stringify(payload, null, 2));

        const request$ = id
            ? this.productoService.update(id, payload)
            : this.productoService.create(payload);

        request$.subscribe({
            next: () => {
                this.closeModal();
                this.loadProductos();
                this.alertasService.success('¡Éxito!', `Registro ${id ? 'actualizado' : 'creado'} correctamente`);
            },
            error: (err) => {
                console.error('ERROR COMPLETO:', err);
                const serverMsg = err.error?.message || err.error?.error || err.message || 'Error desconocido';
                console.log('📌 MENSAJE DEL SERVIDOR:', serverMsg);
                this.alertasService.error('Error del servidor', serverMsg);
            }
        });
    }
}
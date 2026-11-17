import {
    Component,
    Input,
    OnInit,
    OnChanges,
    SimpleChanges,
    ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { NivelService } from '../../../services/nivel.service';
import { ServicioService } from '../../../services/servicio.service';
import { BeneficiosService } from '../../../services/beneficios.service';
import { NivelTree } from '../../../models/nivel.model';
import {
    TablaGeneralComponent,
    Columna
} from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { BeneficioItem } from '../../../services/beneficios.service';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-descuento-general',
    standalone: true,
    imports: [CommonModule, FormsModule, TablaGeneralComponent, PaginationComponent, SearchGenericComponent],
    templateUrl: './descuento-general.component.html'
})
export class DescuentoGeneralComponent implements OnInit, OnChanges {

    @Input() plan!: BeneficioItem;

    codigoBusqueda = '';
    nombreEntidadSeleccionada = '';
    entidadSeleccionada: { type: 'SERVICIO' | 'NIVEL'; id: any; data: any } | null = null;
    nodoSeleccionado: any = null;
    isSearching = false;
    private searchSubject = new Subject<string>();

    descuentoFijo: number | null = null;
    descuentoPorcentaje: number | null = null;
    precioFijo: number | null = null;

    mostrarArbol = true;
    mostrarTabla = false;
    arbolNiveles: NivelTree[] = [];
    arbolLoading = false;

    descuentosTableData: any[] = [];
    filteredDescuentosData: any[] = [];
    paginatedDescuentosData: any[] = [];
    rawServiciosHijos: any[] = [];
    allDiscounts: any[] = [];
    descuentosLoading = false;
    isSaving = false;

    currentPage: number = 0;
    pageSize: number = 10;
    totalElements: number = 0;
    totalPages: number = 0;

    // Paginación para el Árbol (Niveles Raíz)
    currentPageArbol: number = 0;
    pageSizeArbol: number = 12; // 4 columnas x 3 filas
    totalPagesArbol: number = 0;
    paginatedArbolNiveles: NivelTree[] = [];

    searchOptions = [
        { label: 'Nombre Servicio', value: 'nombreServicio' },
        { label: 'Código', value: 'codigo' }
    ];
    currentSearchQuery: string = '';
    currentSearchType: string = 'nombreServicio';

    columnasDescuentos: Columna[] = [
        { field: 'checked', header: '', tipo: 'checkbox', subField: [], align: 'center' },
        { field: 'index', header: '#', tipo: 'index', subField: [], align: 'center' },
        { field: 'codigo', header: 'Código', tipo: 'text', subField: [], align: 'center' },
        { field: 'nivelNombre', header: 'Nombre de Servicio', tipo: 'text', subField: [], align: 'left' },
        { field: 'tipoAtencionNombre', header: 'Tipo de Atención', tipo: 'text', subField: [], align: 'center' },
        { field: 'servicioNombre', header: 'Precio Real', tipo: 'text', subField: [], align: 'left' },
        { field: 'descuentoFijo', header: 'Desc. Fijo', tipo: 'input-number', subField: [], align: 'center', disabledField: 'checked' },
        { field: 'precioFijo', header: 'Tarifa Plana', tipo: 'input-number', subField: [], align: 'center', disabledField: 'checked' },
        { field: 'descuentoPorcentaje', header: 'Desc. %', tipo: 'input-number', subField: [], align: 'center', disabledField: 'checked' },
        { field: 'precioFinal', header: 'Precio Final', tipo: 'text', subField: [], align: 'center' }
    ];

    get hasSelectedItems(): boolean {
        return this.filteredDescuentosData ? this.filteredDescuentosData.some(item => item.checked) : false;
    }

    get labelBotonAccion(): string {
        return this.hasSelectedItems ? 'Actualizar Seleccionados' : 'Aplicar';
    }

    get claseBotonAccion(): string {
        return this.hasSelectedItems
            ? 'inline-flex justify-center items-center py-2 px-4 border border-transparent shadow-sm text-xs font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 whitespace-nowrap h-[34px] transition-colors'
            : 'inline-flex justify-center items-center py-2 px-4 border border-transparent shadow-sm text-xs font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 whitespace-nowrap h-[34px] transition-colors';
    }

    constructor(
        private nivelService: NivelService,
        private servicioService: ServicioService,
        private beneficiosService: BeneficiosService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.inicializar();

        this.searchSubject
            .pipe(debounceTime(500), distinctUntilChanged())
            .subscribe(term => this.buscarEntidad(term));
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['plan'] && !changes['plan'].firstChange) {
            this.reiniciar();
        }
    }

    private inicializar() {
        this.cargarArbol();
        if (this.plan?.idPlan) {
            this.cargarDescuentosSilencioso();
        }
    }

    private reiniciar() {
        this.limpiarSeleccion();
        this.mostrarArbol = true;
        this.mostrarTabla = false;
        this.currentPageArbol = 0;
        this.cargarDescuentosSilencioso();
        this.cd.detectChanges();
    }

    // ── Árbol ──────────────────────────────────────

    cargarArbol() {
        this.arbolLoading = true;
        this.nivelService.obtenerArbolCompleto().subscribe({
            next: res => {
                this.arbolNiveles = res.data || [];
                this.updatePaginationArbol();
                this.arbolLoading = false;
                this.cd.detectChanges();
            },
            error: () => {
                this.arbolLoading = false;
                this.cd.detectChanges();
            }
        });
    }

    updatePaginationArbol(): void {
        this.totalPagesArbol = Math.ceil(this.arbolNiveles.length / this.pageSizeArbol);
        if (this.currentPageArbol >= this.totalPagesArbol) {
            this.currentPageArbol = Math.max(0, this.totalPagesArbol - 1);
        }
        const startIndex = this.currentPageArbol * this.pageSizeArbol;
        const endIndex = startIndex + this.pageSizeArbol;
        this.paginatedArbolNiveles = this.arbolNiveles.slice(startIndex, endIndex);
        this.cd.detectChanges();
    }

    onPageChangeArbol(page: number) {
        this.currentPageArbol = page;
        this.updatePaginationArbol();
    }

    toggleNodoArbol(nodo: NivelTree, ev: Event) {
        ev.stopPropagation();
        nodo.expandido = !nodo.expandido;
        this.cd.detectChanges();
    }

    toggleCheckRaiz(raiz: any, ev: Event) {
        ev.stopPropagation();
        raiz.checked = !raiz.checked;
    }

    seleccionarTodo() {
        if (this.mostrarArbol) {
            const todosMarcados = this.arbolNiveles.every((r: any) => r.checked);
            this.arbolNiveles.forEach((r: any) => (r.checked = !todosMarcados));
        } else if (this.mostrarTabla) {
            const todosMarcados = this.paginatedDescuentosData.every((d: any) => d.checked);
            this.paginatedDescuentosData.forEach((d: any) => (d.checked = !todosMarcados));
        }
    }

    toggleTodosTabla(checked: boolean) {
        this.paginatedDescuentosData.forEach((d) => (d.checked = checked));
        this.cd.detectChanges();
    }

    seleccionarDesdeArbol(entidad: NivelTree, tipo: 'NIVEL' | 'SERVICIO') {
        const term = entidad.numNivel || entidad.nombre;
        this.codigoBusqueda = term;
        this.nombreEntidadSeleccionada = entidad.nombre;
        this.buscarEntidad(term);
    }

    // ── Búsqueda ──────────────────────────────────

    onCodigoChange(term: string) {
        this.searchSubject.next(term);
    }

    buscarEntidad(term: string) {
        if (!term) { this.limpiarSeleccion(); return; }
        this.isSearching = true;
        this.nombreEntidadSeleccionada = 'Buscando...';
        this.currentPage = 0;

        this.servicioService.search(term, this.currentPage, this.pageSize, 'id', 'ASC').subscribe({
            next: response => {
                const servicios = response.content || [];
                this.totalElements = response.totalElements || 0;
                this.totalPages = response.totalPages || 0;

                const exactMatch = servicios.find((s: any) => s.codigo === term);
                if (exactMatch) {
                    this.seleccionarEntidad('SERVICIO', exactMatch);
                    this.rawServiciosHijos = [exactMatch];
                    this.isSearching = false;
                    this.mostrarTabla = true;
                    this.mostrarArbol = false;
                    this.procesarTablaDescuentos();
                } else {
                    this.nivelService.obtenerPorCodigo(term).subscribe({
                        next: res => {
                            if (res.data) this.seleccionarEntidad('NIVEL', res.data);
                            else this.handleNotFound();
                            this.isSearching = false;
                            this.cd.detectChanges();
                        },
                        error: () => { this.handleNotFound(); this.isSearching = false; this.cd.detectChanges(); }
                    });
                    return;
                }
            },
            error: () => {
                this.nivelService.obtenerPorCodigo(term).subscribe({
                    next: res => {
                        if (res.data) this.seleccionarEntidad('NIVEL', res.data);
                        else this.handleNotFound();
                        this.isSearching = false;
                        this.cd.detectChanges();
                    },
                    error: () => { this.handleNotFound(); this.isSearching = false; this.cd.detectChanges(); }
                });
            }
        });
    }

    buscarEntidadSilencioso(term: string): void {
        this.servicioService.search(term, 0, this.pageSize, 'id', 'ASC').subscribe({
            next: (response) => {
                const servicios = response.content || [];
                const exactMatch = servicios.find((s: any) => s.codigo === term);
                if (exactMatch) {
                    this.rawServiciosHijos = [exactMatch];
                    this.procesarTablaDescuentos();
                }
            }
        });
    }

    listarHijos() {
        if (!this.entidadSeleccionada) {
            this.descuentosLoading = true;
            this.servicioService.search('', this.currentPage, this.pageSize, 'id', 'ASC').subscribe({
                next: (res) => {
                    const servicios = res.content || [];
                    this.totalElements = res.totalElements || 0;
                    this.totalPages = res.totalPages || 0;

                    this.rawServiciosHijos = servicios;
                    this.mostrarArbol = false;
                    this.mostrarTabla = true;
                    this.procesarTablaDescuentos();

                    if (servicios.length === 0) {
                        this.alertasService.info('Información', 'No hay servicios asignados en el catálogo.');
                    }
                    this.descuentosLoading = false;
                    this.cd.detectChanges();
                },
                error: (err) => {
                    this.rawServiciosHijos = [];
                    console.error(err);
                    this.alertasService.error('Error', 'No se pudieron cargar los servicios del catálogo.');
                    this.descuentosLoading = false;
                    this.cd.detectChanges();
                }
            });
            return;
        }

        this.descuentosLoading = true;

        if (this.entidadSeleccionada.type === 'NIVEL') {
            const nombre = this.entidadSeleccionada.data?.nombre || '';
            this.servicioService.search(nombre, this.currentPage, this.pageSize, 'id', 'ASC').subscribe({
                next: res => {
                    const servicios = res.content || [];
                    this.totalElements = res.totalElements || 0;
                    this.totalPages = res.totalPages || 0;

                    this.rawServiciosHijos = servicios;
                    this.mostrarArbol = false;
                    this.mostrarTabla = true;
                    this.procesarTablaDescuentos();
                    if (this.rawServiciosHijos.length === 0) {
                        this.alertasService.info('Información', 'Este nivel no tiene servicios asignados.');
                    }
                    this.descuentosLoading = false;
                    this.cd.detectChanges();
                },
                error: (err) => {
                    this.rawServiciosHijos = [];
                    console.error(err);
                    this.alertasService.error('Error', 'No se pudieron cargar los servicios del catálogo.');
                    this.descuentosLoading = false;
                    this.cd.detectChanges();
                }
            });
        } else {
            if (this.rawServiciosHijos.length > 0) {
                this.mostrarArbol = false;
                this.mostrarTabla = true;
                this.procesarTablaDescuentos();
                this.descuentosLoading = false;
            }
        }
    }

    cargarDescuentosSilencioso() {
        if (!this.plan?.idPlan) return;
        this.servicioService.obtenerDescuentos(this.plan.idPlan).subscribe({
            next: data => {
                this.allDiscounts = data;
                if (this.entidadSeleccionada && this.codigoBusqueda) {
                    this.buscarEntidadSilencioso(this.codigoBusqueda);
                } else if (this.entidadSeleccionada && this.rawServiciosHijos.length > 0) {
                    this.procesarTablaDescuentos();
                }
                this.cd.detectChanges();
            }
        });
    }

    onDescuentoChange(tipo: 'FIJO' | 'PORCENTAJE' | 'TARIFA') {
        if (tipo === 'FIJO') { this.descuentoPorcentaje = null; this.precioFijo = null; }
        else if (tipo === 'PORCENTAJE') { this.descuentoFijo = null; this.precioFijo = null; }
        else if (tipo === 'TARIFA') { this.descuentoFijo = null; this.descuentoPorcentaje = null; }
        if (this.rawServiciosHijos.length > 0) this.procesarTablaDescuentos();
    }

    procesarTablaDescuentos() {
        this.descuentosTableData = this.rawServiciosHijos.map(servicio => {

            const existing = this.allDiscounts.find(d => d.idServicio === servicio.id)
                || this.encontrarDescuentoParaNivel(this.entidadSeleccionada?.id);

            const precioVal = servicio.precioConIgv != null ? Number(servicio.precioConIgv) : 0;
            const precioStr = (servicio.moneda === 'USD' ? '$ ' : 'S/ ') + precioVal.toFixed(2);

            let finalFijo = 0, finalPorc = 0, finalTarifa: number | null = null;
            let existingPrecioFijo = existing && existing.precioFijo != null ? Number(existing.precioFijo) : null;
            const existingItem = this.descuentosTableData ? this.descuentosTableData.find(d => d.id === servicio.id) : null;
            let isChecked = existingItem ? existingItem.checked : false;
            let currentIntent = existingItem ? existingItem.tipoDescuentoIntent : null;

            if (isChecked) {
                if (this.precioFijo != null && this.precioFijo >= 0) {
                    finalTarifa = this.precioFijo;
                    currentIntent = 'TARIFA_PLANA';
                } else if (this.descuentoPorcentaje != null && this.descuentoPorcentaje > 0) {
                    finalPorc = this.descuentoPorcentaje;
                    finalFijo = Number((precioVal * (finalPorc / 100)).toFixed(2));
                    currentIntent = 'PORCENTAJE';
                } else if (this.descuentoFijo != null && this.descuentoFijo > 0) {
                    finalFijo = this.descuentoFijo;
                    finalPorc = precioVal > 0 ? Number(((finalFijo / precioVal) * 100).toFixed(2)) : 0;
                    currentIntent = 'FIJO';
                } else if (existingItem) {
                    finalFijo = existingItem.descuentoFijo != null ? existingItem.descuentoFijo : (Number(existing.descuentoFijo) || 0);
                    finalPorc = existingItem.descuentoPorcentaje != null ? existingItem.descuentoPorcentaje : (Number(existing.descuentoPorcentaje) || 0);
                    finalTarifa = existingItem.precioFijo !== undefined ? existingItem.precioFijo : existingPrecioFijo;
                    currentIntent = existingItem.tipoDescuentoIntent || existing?.tipoDescuento;
                }
            } else {
                if (existing) {
                    if (existingPrecioFijo != null) {
                        finalTarifa = existingPrecioFijo;
                    } else {
                        finalFijo = Number(existing.descuentoFijo) || 0;
                        finalPorc = Number(existing.descuentoPorcentaje) || 0;
                    }
                }
            }

            if (finalPorc > 0 && finalFijo === 0 && finalTarifa == null) {
                finalFijo = Number((precioVal * (finalPorc / 100)).toFixed(2));
            }

            let calcFinal = finalTarifa != null ? finalTarifa : Math.max(0, precioVal - finalFijo);

            return {
                ...servicio,
                checked: isChecked,
                codigo: servicio.codigo || '-',
                nivelNombre: servicio.nombreServicio,
                tipoAtencionNombre: servicio.tipoAtencionNombre || 'AMBULATORIO',
                servicioNombre: precioStr,
                descuentoFijo: finalTarifa != null ? null : finalFijo,
                precioFijo: finalTarifa,
                descuentoPorcentaje: finalTarifa != null ? null : finalPorc,
                precioFinal: 'S/ ' + calcFinal.toFixed(2),
                tipoDescuentoIntent: currentIntent || (existing ? existing.tipoDescuento : null)
            };
        });

        this.applyTableFilter();
    }

    handleTableSearch(event: { q: string; type: string }): void {
        this.currentSearchQuery = event.q.trim().toLowerCase();
        this.currentSearchType = event.type;
        this.currentPage = 0;
        this.applyTableFilter();
    }

    applyTableFilter(): void {
        if (!this.currentSearchQuery) {
            this.filteredDescuentosData = [...this.descuentosTableData];
        } else {
            this.filteredDescuentosData = this.descuentosTableData.filter(item => {
                if (this.currentSearchType === 'nombreServicio') {
                    return item.nivelNombre?.toLowerCase().includes(this.currentSearchQuery);
                } else if (this.currentSearchType === 'codigo') {
                    return item.codigo?.toLowerCase().includes(this.currentSearchQuery);
                }
                return false;
            });
        }
        this.updatePagination();
    }

    updatePagination(): void {
        this.totalPages = Math.ceil(this.filteredDescuentosData.length / this.pageSize);
        if (this.currentPage >= this.totalPages) {
            this.currentPage = Math.max(0, this.totalPages - 1);
        }
        const startIndex = this.currentPage * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        this.paginatedDescuentosData = this.filteredDescuentosData.slice(startIndex, endIndex);
        this.cd.detectChanges();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.updatePagination();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.updatePagination();
    }

    handleTableInputChange(event: { item: any; col: any; value: any }) {
        const { item, col, value } = event;
        const precioReal = this.rawServiciosHijos.find(s => s.id === item.id)?.precioConIgv || 0;
        const precioVal = Number(precioReal);

        if (col.field === 'descuentoFijo') {
            const nuevoFijo = Number(value) || 0;
            item.descuentoFijo = nuevoFijo;
            const nuevoPorc = precioVal > 0 ? (nuevoFijo / precioVal) * 100 : 0;
            item.descuentoPorcentaje = Number(nuevoPorc.toFixed(2));
            item.precioFijo = null;
            item.precioFinal = 'S/ ' + Math.max(0, precioVal - nuevoFijo).toFixed(2);
            item.tipoDescuentoIntent = 'FIJO';
        } else if (col.field === 'descuentoPorcentaje') {
            const nuevoPorc = Number(value) || 0;
            item.descuentoPorcentaje = nuevoPorc;
            const nuevoFijo = precioVal * (nuevoPorc / 100);
            item.descuentoFijo = Number(nuevoFijo.toFixed(2));
            item.precioFijo = null;
            item.precioFinal = 'S/ ' + Math.max(0, precioVal - nuevoFijo).toFixed(2);
            item.tipoDescuentoIntent = 'PORCENTAJE';
        } else if (col.field === 'precioFijo') {
            const nuevaTarifa = Number(value) || 0;
            item.precioFijo = nuevaTarifa;
            item.descuentoPorcentaje = null;
            item.descuentoFijo = null;
            item.precioFinal = 'S/ ' + nuevaTarifa.toFixed(2);
            item.tipoDescuentoIntent = 'TARIFA_PLANA';
        }
        this.cd.detectChanges();
    }

    handleCheckboxChange(event: { item: any; checked: boolean }) {
        this.procesarTablaDescuentos();
    }

    private encontrarDescuentoParaNivel(nivelId: number | undefined | null): any {
        if (nivelId == null) return null;

        const exacto = this.allDiscounts.find(d => d.idNivel === nivelId && !d.idServicio);
        if (exacto) return exacto;

        const padreId = this.encontrarPadreEnArbol(nivelId, this.arbolNiveles);
        if (padreId != null) return this.encontrarDescuentoParaNivel(padreId);

        return null;
    }

    private encontrarPadreEnArbol(nivelId: number, nodos: NivelTree[], padreId: number | null = null): number | null {
        for (const nodo of nodos) {
            if (nodo.idNivel === nivelId) return padreId;
            if (nodo.hijos?.length) {
                const resultado = this.encontrarPadreEnArbol(nivelId, nodo.hijos, nodo.idNivel);
                if (resultado !== undefined) return resultado;
            }
        }
        return undefined as any;
    }

    aplicarDescuento() {
        if (this.hasMasivoSeleccion) {
            this.aplicarDescuentoMasivo();
            return;
        }

        if (!this.validarRequisitosBasicos()) return;

        let payload: any = {
            idPlan: this.plan.idPlan,
            descuentoFijo: this.descuentoFijo || 0,
            descuentoPorcentaje: this.descuentoPorcentaje || 0,
            precioFijo: this.precioFijo,
        };

        if (this.entidadSeleccionada) {
            const checkedServices = this.filteredDescuentosData.filter(d => d.checked);

            if (checkedServices.length > 0) {
                this.aplicarDescuentosSeleccionados(checkedServices);
                return;
            }

            if (this.entidadSeleccionada.type === 'SERVICIO') {
                payload.tipoAplicacion = 'SERVICIO';
                payload.idServicio = this.entidadSeleccionada.id;
            } else {
                payload.tipoAplicacion = 'NIVEL';
                payload.idNivel = this.entidadSeleccionada.id;
                payload.idTipoAtencion = null;
            }
        } else {
            this.alertasService.warning(
                'Atención',
                'Seleccione una Entidad (Servicio/Nivel) por código o lista.'
            );
            return;
        }

        this.enviarSolicitudDescuento(payload, '¿Confirmar aplicación de descuento?');
    }

    private validarRequisitosBasicos(): boolean {
        if (!this.plan || !this.plan.idPlan) {
            this.alertasService.error('Error', 'No se ha identificado el plan seleccionado.');
            return false;
        }

        const haySeleccion = this.hasSelectedItems;
        const hayDescuentoGlobal =
            (this.descuentoFijo !== null && this.descuentoFijo >= 0) ||
            (this.descuentoPorcentaje !== null && this.descuentoPorcentaje >= 0) ||
            (this.precioFijo !== null && this.precioFijo >= 0);

        if (!hayDescuentoGlobal && !haySeleccion) {
            this.alertasService.warning(
                'Atención',
                'Ingrese al menos un valor de descuento o tarifa plana.'
            );
            return false;
        }
        return true;
    }

    private enviarSolicitudDescuento(payload: any, pregunta: string) {
        this.alertasService.confirm(
            'Confirmar', 
            pregunta, 
            'Sí, aplicar', 
            'Cancelar',
            'bg-green-600 hover:bg-green-700 shadow-green-600/30',
            'success'
        ).then((confirmed) => {
            if (confirmed) {
                this.servicioService.aplicarDescuento(payload).subscribe({
                    next: (res) => {
                        this.alertasService.success('Éxito', 'Descuentos aplicados correctamente.');
                        this.limpiarDespuesDeAplicar();
                        this.cargarDescuentosSilencioso();
                    },
                    error: (err) => {
                        console.error('Error aplicando descuento:', err);
                        this.alertasService.error('Error', 'No se pudo aplicar el descuento. Intente nuevamente.');
                    },
                });
            }
        });
    }

    private aplicarDescuentosSeleccionados(items: any[]) {
        this.alertasService.confirm(
            'Confirmar',
            `¿Aplicar descuento a los ${items.length} servicios seleccionados?`,
            'Sí, aplicar',
            'Cancelar',
            'bg-green-600 hover:bg-green-700 shadow-green-600/30',
            'success'
        ).then((confirmed) => {
            if (confirmed) {
                this.isSaving = true;
                this.procesarSecuencialmente(items);
            }
        });
    }

    private procesarSecuencialmente(items: any[]) {
        if (items.length === 0) {
            this.isSaving = false;
            this.alertasService.success('Éxito', 'Descuentos aplicados correctamente.');
            this.limpiarDespuesDeAplicar();
            this.cargarDescuentosSilencioso();
            return;
        }

        const item = items[0];
        const payload = {
            idPlan: Number(this.plan.idPlan),
            descuentoFijo: Number(item.descuentoFijo || 0),
            descuentoPorcentaje: Number(item.descuentoPorcentaje || 0),
            precioFijo: item.precioFijo !== undefined && item.precioFijo !== null ? Number(item.precioFijo) : null,
            tipoAplicacion: 'SERVICIO',
            idServicio: Number(item.id),
            idNivel: (this.entidadSeleccionada && this.entidadSeleccionada.type === 'NIVEL') ? Number(this.entidadSeleccionada.id) : null,
            idTipoAtencion: null,
            tipoDescuento: item.tipoDescuentoIntent || null
        };

        this.servicioService.aplicarDescuento(payload).subscribe({
            next: () => this.procesarSecuencialmente(items.slice(1)),
            error: () => this.procesarSecuencialmente(items.slice(1))
        });
    }

    limpiarDespuesDeAplicar() {
        this.descuentoFijo = null;
        this.descuentoPorcentaje = null;
        this.precioFijo = null;
        if (this.descuentosTableData) {
            this.descuentosTableData.forEach(item => item.checked = false);
        }
        this.applyTableFilter();
    }

    private aplicarDescuentoMasivo() {
        const hayDescuento =
            (this.descuentoPorcentaje != null && this.descuentoPorcentaje > 0) ||
            (this.descuentoFijo != null && this.descuentoFijo > 0) ||
            (this.precioFijo != null && this.precioFijo >= 0);

        if (!hayDescuento) {
            this.alertasService.warning('Atención', 'Ingrese un valor de descuento o tarifa plana antes de aplicar.');
            return;
        }

        const idNiveles = this.nivelesChecked
            .map((r: any) => r.idNivel as number)
            .filter(id => !!id);

        if (idNiveles.length === 0) {
            this.alertasService.warning('Atención', 'Los niveles seleccionados no tienen ID numérico.');
            return;
        }

        const tipoDescuento = this.precioFijo != null && this.precioFijo >= 0
            ? 'TARIFA_PLANA' :
            (this.descuentoPorcentaje != null && this.descuentoPorcentaje > 0
                ? 'PORCENTAJE' : 'MONTO_FIJO');

        let val = tipoDescuento === 'PORCENTAJE' ? this.descuentoPorcentaje + '%' :
            (tipoDescuento === 'TARIFA_PLANA' ? 'Tarifa S/ ' + this.precioFijo : 'S/ ' + this.descuentoFijo);
        const resumen = `${this.nivelesChecked.length} nivel(es) con ${val}`;

        this.alertasService.confirm(
            'Confirmar descuento masivo',
            `¿Aplicar descuento a "${resumen}"? Puedes ajustar niveles individuales después.`,
            'Sí, aplicar masivo',
            'Cancelar',
            'bg-green-600 hover:bg-green-700 shadow-green-600/30',
            'success'
        ).then(confirmed => {
            if (confirmed) {
                this.beneficiosService.aplicarDescuentoMasivo(this.plan.idPlan, {
                    idNiveles,
                    descuentoPorcentaje: tipoDescuento === 'PORCENTAJE' ? this.descuentoPorcentaje! : undefined,
                    descuentoFijo: tipoDescuento === 'MONTO_FIJO' ? this.descuentoFijo! : undefined,
                    precioFijo: tipoDescuento === 'TARIFA_PLANA' ? this.precioFijo! : undefined,
                    tipoDescuento: tipoDescuento as any
                }).subscribe({
                    next: (aplicadas) => {
                        this.alertasService.success('Éxito', `Descuento aplicado a ${aplicadas} nivel(es).`);

                        this.arbolNiveles.forEach((r: any) => r.checked = false);
                        this.descuentoFijo = null;
                        this.descuentoPorcentaje = null;
                        this.precioFijo = null;
                        this.cargarDescuentosSilencioso();
                    },
                    error: err => {
                        console.error(err);
                        this.alertasService.error('Error', 'No se pudo aplicar el descuento masivo.');
                    }
                });
            }
        });
    }

    get nivelesChecked(): any[] {
        return this.arbolNiveles.filter((r: any) => r.checked);
    }

    get hasMasivoSeleccion(): boolean {
        return this.nivelesChecked.length > 0;
    }



    seleccionarEntidad(type: 'SERVICIO' | 'NIVEL', data: any) {
        this.entidadSeleccionada = { type, id: type === 'SERVICIO' ? data.id : data.idNivel, data };
        this.nombreEntidadSeleccionada = type === 'SERVICIO' ? data.nombreServicio : data.nombre;
        this.cd.detectChanges();
    }

    handleNotFound() {
        this.entidadSeleccionada = null;
        this.nombreEntidadSeleccionada = 'No encontrado';
        this.cd.detectChanges();
    }

    limpiarSeleccion() {
        this.codigoBusqueda = '';
        this.entidadSeleccionada = null;
        this.nombreEntidadSeleccionada = '';
        this.rawServiciosHijos = [];
        this.descuentosTableData = [];
        this.mostrarTabla = false;
        this.mostrarArbol = true;
        this.cargarDescuentosSilencioso();
        this.cd.detectChanges();
    }
}

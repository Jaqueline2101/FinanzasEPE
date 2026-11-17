import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Paquete, PaqueteRequest } from '../../../models/paquete.model';
import { PaqueteService } from '../../../services/paquete.service';
import { ServicioService } from '../../../services/servicio.service';
import { Servicio } from '../../../models/servicio.model';
import { NivelService } from '../../../services/nivel.service';
import { Nivel, NivelTree } from '../../../models/nivel.model';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-paquete',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent, PaginationComponent],
    templateUrl: './modal-paquete.component.html',
    styleUrls: [],
    styles: [`
        .custom-scrollbar::-webkit-scrollbar {
            width: 4px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
            background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
            background: #e2e8f0;
            border-radius: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
            background: #cbd5e1;
        }
    `]
})
export class ModalPaqueteComponent implements OnInit, OnChanges {
    @Input() isVisible: boolean = false;
    @Input() paquete: Paquete | null = null;
    @Input() categoria: string = 'PAQUETE_REGULAR';
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    @ViewChild('nivelContainer') nivelContainer!: ElementRef;
    @ViewChild('servicioContainer') servicioContainer!: ElementRef;

    formData: PaqueteRequest = {
        codigo: '',
        nombre: '',
        descripcion: '',
        precioPaquete: 0,
        fechaInicio: '',
        fechaFin: '',
        estado: '1',
        idTipoAtencion: null,
        tipoAtencion: 'AMBULATORIO',
        categoria: 'PAQUETE',
        servicios: [],
        descuento: 0,
        idEmpresa: 1
    };

    precioReal: number = 0;

    serviciosDisponibles: Servicio[] = [];
    servicioSeleccionado: number | null = null;
    listaServiciosAgregados: {
        idServicio: number,
        nombre: string,
        cantidad: number,
        precio: number,
        descuentoMonto: number, // Descuento en soles (S/.) - VALOR EXACTO INGRESADO
        descuentoPorcentaje: number // Descuento en porcentaje (%) - SINCRONIZADO CON MONTO
    }[] = [];

    niveles: Nivel[] = [];
    arbolNiveles: NivelTree[] = [];
    selectedNivelId: number | null = null;
    nivelSeleccionadoNombre: string = 'Todos los Niveles';
    expandedNodes: Set<number> = new Set<number>();
    searchTermNivel: string = '';

    dropdownStyles: { [key: string]: string } = {};

    showServicioDropdown: boolean = false;
    servicioDropdownStyles: { [key: string]: string } = {};

    tiposAtencion: any[] = [];

    searchTerm: string = '';
    searchResults: Servicio[] = [];
    allFilteredResults: Servicio[] = []; // Resultados filtrados totales sin paginar
    isSearching: boolean = false;
    showSearchDropdown: boolean = false;
    private searchTimeout: any = null;

    // Paginación
    currentPage: number = 0;
    pageSize: number = 10;
    totalPages: number = 0;

    private isRecalculating: boolean = false;

    isEditMode: boolean = false;

    constructor(
        private paqueteService: PaqueteService,
        private servicioService: ServicioService,
        private nivelService: NivelService,
        private cdr: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.cargarServicios();
        this.cargarNiveles();
        this.cargarTiposAtencion();
    }

    cargarTiposAtencion(): void {
        this.servicioService.getTiposAtencion().subscribe({
            next: (data) => {
                setTimeout(() => {
                    this.tiposAtencion = data || [];
                    this.onTipoAtencionChange();
                    this.cdr.detectChanges();
                });
            },
            error: (err) => console.error('Error cargando tipos de atención', err)
        });
    }

    cargarNiveles(): void {
        this.nivelService.obtenerTodos(0, 1000).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.niveles = res.data.content || [];
                    this.arbolNiveles = this.buildTree(this.niveles);
                    this.cdr.detectChanges();
                }
            },
            error: (err) => console.error('Error cargando niveles', err)
        });
    }

    private buildTree(niveles: Nivel[]): NivelTree[] {
        const map = new Map<number, NivelTree>();
        const roots: NivelTree[] = [];

        niveles.forEach(n => {
            if (n.idNivel) {
                map.set(n.idNivel, {
                    ...n,
                    idNivel: n.idNivel,
                    nivelJerarquia: n.nivelJerarquia || 0,
                    tieneHijos: false,
                    expandido: false,
                    hijos: []
                } as NivelTree);
            }
        });

        niveles.forEach(n => {
            if (n.idNivel) {
                const node = map.get(n.idNivel)!;
                if (n.idNivelPadre && map.has(n.idNivelPadre)) {
                    const parent = map.get(n.idNivelPadre)!;
                    parent.hijos = parent.hijos || [];
                    parent.hijos.push(node);
                    parent.tieneHijos = true;
                } else {
                    roots.push(node);
                }
            }
        });

        return roots;
    }

    calcularPosicionDropdown(): void { }

    cerrarNivelDropdown(): void {
        this.cdr.detectChanges();
    }

    // ====== SERVICE SEARCH DROPDOWN LOGIC ======

    toggleServicioDropdown(open: boolean): void {
        this.showServicioDropdown = open;
        this.cdr.detectChanges();
    }

    calcularPosicionServicioDropdown(): void { }

    cerrarServicioDropdown(): void {
        this.showServicioDropdown = false;
        this.cdr.detectChanges();
    }

    toggleNode(event: Event, nodo: NivelTree, hermanos: NivelTree[]): void {
        event.stopPropagation();
        const idNivel = nodo.idNivel;

        if (this.expandedNodes.has(idNivel)) {
            this.expandedNodes.delete(idNivel);
        } else {
            // Comportamiento de acordeón: colapsar otros hermanos expandidos
            if (hermanos) {
                hermanos.forEach(h => {
                    if (h.idNivel !== idNivel) {
                        this.collapseRecursive(h);
                    }
                });
            }
            this.expandedNodes.add(idNivel);
        }
    }

    private collapseRecursive(nodo: NivelTree): void {
        this.expandedNodes.delete(nodo.idNivel);
        if (nodo.hijos) {
            nodo.hijos.forEach(h => this.collapseRecursive(h));
        }
    }

    seleccionarNivel(nodo: NivelTree | null): void {
        if (nodo) {
            this.selectedNivelId = nodo.idNivel;
            this.nivelSeleccionadoNombre = nodo.nombre;
        } else {
            this.selectedNivelId = null;
            this.nivelSeleccionadoNombre = 'Todos los Niveles';
        }

        this.buscarServicios(true);
    }

    cerrarDrawerServicios(): void {
        this.showServicioDropdown = false;
    }

    onSearchServicioChange(): void {
        this.buscarServicios(false);
    }

    toggleServicioEnPaquete(servicio: Servicio): void {
        const index = this.listaServiciosAgregados.findIndex(s => s.idServicio === servicio.id);

        if (index !== -1) {

            this.eliminarServicio(index);
        } else {

            this.seleccionarServicioDesdeBusqueda(servicio);
        }
    }

    isServicioSeleccionado(idServicio: number | undefined): boolean {
        if (!idServicio) return false;
        return this.listaServiciosAgregados.some(s => s.idServicio === idServicio);
    }

    onSearchNivelChange(): void {
        const term = this.searchTermNivel.trim().toLowerCase();
        if (!term) {
            return;
        }

    }

    private filterTree(nodes: NivelTree[], term: string): NivelTree[] {
        return nodes.map(node => ({ ...node }))
            .filter(node => {
                const nombreMatch = node.nombre.toLowerCase().includes(term);
                const hijosMatch = node.hijos && node.hijos.length > 0 &&
                    (node.hijos = this.filterTree(node.hijos, term)).length > 0;

                return nombreMatch || hijosMatch;
            });
    }

    onTipoAtencionChange(): void {
        const id = this.formData.idTipoAtencion;
        if (id) {
            const selected = this.tiposAtencion.find(t => t.id === Number(id));
            if (selected) {
                this.formData.tipoAtencion = selected.nombre;
            }
        } else {
            this.formData.tipoAtencion = 'AMBULATORIO';
        }
    }

    cargarServicios(): void {
        this.buscarServicios(true);
    }

    buscarServicios(immediate: boolean = false): void {
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }

        const term = this.searchTerm.trim();

        const executeSearch = () => {
            this.isSearching = true;

            this.servicioService.listarServiciosActivos().subscribe({
                next: (data) => {
                    let filtered = data || [];

                    // 1. Filtrar solo los que son de tipo SERVICIO (o si no tiene tipo definido)
                    filtered = filtered.filter(s =>
                        !s.tipo ||
                        s.tipo.toUpperCase() === 'SERVICIO'
                    );

                    // 2. Filtrar por Nivel si hay uno seleccionado (distinto de null/undefined)
                    if (this.selectedNivelId !== null && this.selectedNivelId !== undefined) {
                        const targetId = Number(this.selectedNivelId);
                        filtered = filtered.filter(s =>
                            s.idNivel === targetId ||
                            (s.nivel && (s.nivel.id === targetId || s.nivel.idNivel === targetId))
                        );
                    }

                    // 3. Filtrar por término de búsqueda
                    if (term) {
                        const lowerTerm = term.toLowerCase();
                        filtered = filtered.filter(s =>
                            (s.nombreServicio && s.nombreServicio.toLowerCase().includes(lowerTerm)) ||
                            (s.codigo && s.codigo.toLowerCase().includes(lowerTerm))
                        );
                    }

                    // 4. Guardar resultados totales y calcular paginación
                    this.allFilteredResults = filtered;
                    this.totalPages = Math.ceil(this.allFilteredResults.length / this.pageSize);

                    // Asegurar que la página actual sea válida
                    if (this.currentPage >= this.totalPages && this.totalPages > 0) {
                        this.currentPage = this.totalPages - 1;
                    }

                    this.paginateResults();
                    this.isSearching = false;
                    this.cdr.markForCheck();
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error fetching services', err);
                    this.isSearching = false;
                    this.searchResults = [];
                    this.allFilteredResults = [];
                    this.totalPages = 0;
                    this.cdr.detectChanges();
                }
            });
        };

        if (immediate) {
            executeSearch();
        } else {
            this.searchTimeout = setTimeout(executeSearch, 300);
        }
    }

    seleccionarServicioDesdeBusqueda(servicio: Servicio): void {
        const existing = this.listaServiciosAgregados.find(s => s.idServicio === servicio.id);

        if (existing) {
            existing.cantidad++;
        } else {
            this.listaServiciosAgregados.push({
                idServicio: servicio.id!,
                nombre: servicio.nombreServicio,
                cantidad: 1,
                precio: servicio.precioConIgv || 0,
                descuentoMonto: 0,
                descuentoPorcentaje: 0
            });
        }

        this.recalcularPrecioReal();
    }

    paginateResults(): void {
        const start = this.currentPage * this.pageSize;
        const end = start + this.pageSize;
        this.searchResults = this.allFilteredResults.slice(start, end);
        this.cdr.markForCheck();
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.paginateResults();
    }

    onPageSizeChange(size: number): void {
        this.pageSize = size;
        this.currentPage = 0;
        this.totalPages = Math.ceil(this.allFilteredResults.length / this.pageSize);
        this.paginateResults();
    }

    cerrarDropdown(): void {
        setTimeout(() => {
            this.showSearchDropdown = false;
        }, 200);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isVisible'] && this.isVisible) {
            this.seleccionarNivel(null); // Seleccionar 'Todos los Niveles' por defecto
            if (this.paquete) {
                this.isEditMode = true;
                this.cargarDatosParaEdicion();
            } else {
                this.isEditMode = false;
                this.resetForm();
            }
        }
    }

    private cargarDatosParaEdicion(): void {
        if (!this.paquete) return;

        this.formData = {
            codigo: this.paquete.codigo,
            nombre: this.paquete.nombre,
            descripcion: this.paquete.descripcion || '',
            precioPaquete: this.paquete.precioPaquete,
            fechaInicio: this.paquete.fechaInicio || '',
            fechaFin: this.paquete.fechaFin || '',
            estado: this.paquete.estado,
            idTipoAtencion: this.paquete.idTipoAtencion || null,
            tipoAtencion: this.paquete.tipoAtencion || 'AMBULATORIO',
            categoria: this.paquete.categoria || this.categoria,
            servicios: [],
            descuento: this.paquete.descuento || 0,
            idEmpresa: this.paquete.idEmpresa || 1
        } as PaqueteRequest;

        this.listaServiciosAgregados = (this.paquete.servicios || []).map(servicio => {
            const precioUnitario = servicio.precioUnitario || servicio.precio || 0;
            const cantidad = servicio.cantidad || 1;
            const descuentoPorcentaje = servicio.descuentoPorcentaje || 0;

            let descuentoMonto = 0;

            if (servicio.descuentoMonto !== undefined && servicio.descuentoMonto !== null) {
                descuentoMonto = Number(servicio.descuentoMonto);
            } else if (descuentoPorcentaje > 0) {
                const precioTotal = precioUnitario * cantidad;
                descuentoMonto = (precioTotal * descuentoPorcentaje) / 100;

                descuentoMonto = Math.round(descuentoMonto * 100) / 100;
            }

            return {
                idServicio: servicio.idServicio,
                nombre: servicio.nombreServicio,
                cantidad: cantidad,
                precio: precioUnitario,
                descuentoMonto: descuentoMonto,
                descuentoPorcentaje: descuentoPorcentaje
            };
        });

        this.recalcularPrecioRealDesdeServicios();
    }

    private recalcularPrecioRealDesdeServicios(): void {

        const sumaPreciosReales = this.listaServiciosAgregados.reduce((acc, item) => {
            return acc + (item.precio * item.cantidad);
        }, 0);

        const sumaPreciosFinales = this.listaServiciosAgregados.reduce((acc, item) => {
            return acc + this.calcularPrecioFinal(item);
        }, 0);

        this.precioReal = Math.round(sumaPreciosReales * 100) / 100;

        const precioOfertaCalculado = Math.round(sumaPreciosFinales * 100) / 100;

        if (Math.abs(this.formData.precioPaquete - precioOfertaCalculado) > 0.01) {
            this.formData.precioPaquete = precioOfertaCalculado;
        }
    }

    onPrecioRealFocus(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (this.precioReal === 0) {
            this.precioReal = null as any;
            setTimeout(() => {
                input.setSelectionRange(0, 0);
            }, 0);
        }
    }

    onPrecioOfertaFocusMejorado(event: Event): void {
        const input = event.target as HTMLInputElement;

        setTimeout(() => {
            input.setSelectionRange(0, 0);
        }, 0);
    }

    onDescuentoPorcentajeTotalFocusMejorado(event: Event): void {
        const input = event.target as HTMLInputElement;

        setTimeout(() => {
            input.setSelectionRange(0, 0);
        }, 0);
    }

    onDescuentoFocusMejorado(item: any, event: Event): void {
        const input = event.target as HTMLInputElement;

        if (item.descuentoMonto === 0 || !item.descuentoMonto) {

            item.descuentoMonto = null as any;
        }

        setTimeout(() => {
            input.setSelectionRange(0, 0);
        }, 0);
    }
    onDescuentoPorcentajeFocusMejorado(item: any, event: Event): void {
        const input = event.target as HTMLInputElement;

        if (item.descuentoPorcentaje === 0 || !item.descuentoPorcentaje) {

            item.descuentoPorcentaje = null as any;
        }

        setTimeout(() => {
            input.setSelectionRange(0, 0);
        }, 0);
    }

    onPrecioOfertaFocus(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (this.formData.precioPaquete === 0) {
            this.formData.precioPaquete = null as any;
            setTimeout(() => {
                input.setSelectionRange(0, 0);
            }, 0);
        }
    }

    close(): void {
        this.onClose.emit();
    }

    save(): void {
        if (!this.validarFormulario()) return;

        const precioRealValue = this.precioReal || 0;
        const precioOfertaValue = this.formData.precioPaquete || 0;
        const descuentoCalculado = Math.max(0, precioRealValue - precioOfertaValue);

        const serviciosRequest = this.listaServiciosAgregados.map(s => ({
            idServicio: s.idServicio,
            cantidad: s.cantidad,
            descuentoPorcentaje: s.descuentoPorcentaje || 0
        }));

        const request: PaqueteRequest = {
            ...this.formData,
            descuento: descuentoCalculado,
            servicios: serviciosRequest
        };

        if (this.isEditMode && this.paquete?.idPaquete) {
            this.paqueteService.actualizarPaquete(this.paquete.idPaquete, request).subscribe({
                next: () => {
                    this.handleSuccess('Actualizado', 'Paquete actualizado correctamente');
                },
                error: (err) => this.handleError('Error', 'No se pudo actualizar el paquete')
            });
        } else {
            this.paqueteService.crearPaquete(request).subscribe({
                next: () => {
                    this.handleSuccess('Creado', 'Paquete creado correctamente');
                },
                error: (err) => this.handleError('Error', 'No se pudo crear el paquete')
            });
        }
    }

    validarFormulario(): boolean {
        if (!this.formData.nombre) {
            this.alertasService.warning('Advertencia', 'El nombre del paquete es obligatorio');
            return false;
        }

        if (!this.formData.precioPaquete || this.formData.precioPaquete <= 0) {
            this.alertasService.warning('Advertencia', 'El precio oferta es obligatorio y debe ser mayor a 0');
            return false;
        }

        const precioRealValue = this.precioReal || 0;
        if (this.formData.precioPaquete > precioRealValue) {
            this.alertasService.warning('Error de Precio', 'El Precio Oferta no puede ser mayor que el Precio Real.');
            return false;
        }

        return true;
    }

    agregarServicio(): void {
        if (!this.servicioSeleccionado) return;

        const servicio = this.serviciosDisponibles.find(s => s.id == this.servicioSeleccionado);
        if (!servicio) return;

        const existing = this.listaServiciosAgregados.find(s => s.idServicio === servicio.id);

        if (existing) {
            existing.cantidad++;
        } else {
            this.listaServiciosAgregados.push({
                idServicio: servicio.id!,
                nombre: servicio.nombreServicio,
                cantidad: 1,
                precio: servicio.precioConIgv || 0,
                descuentoMonto: 0,
                descuentoPorcentaje: 0
            });
        }

        this.recalcularPrecioReal();
        this.servicioSeleccionado = null;
    }

    eliminarServicio(index: number): void {
        this.listaServiciosAgregados.splice(index, 1);
        this.recalcularPrecioReal();
    }

    recalcularPrecioReal(): void {
        if (this.isRecalculating) return;

        const sumaPreciosReales = this.listaServiciosAgregados.reduce((acc, item) => {
            return acc + (item.precio * item.cantidad);
        }, 0);

        const sumaPreciosFinales = this.listaServiciosAgregados.reduce((acc, item) => {
            return acc + this.calcularPrecioFinal(item);
        }, 0);

        this.precioReal = Math.round(sumaPreciosReales * 100) / 100;
        this.formData.precioPaquete = Math.round(sumaPreciosFinales * 100) / 100;
    }

    calcularDescuentoMonto(item: any): number {
        return item.descuentoMonto || 0;
    }

    calcularDescuentoPorcentaje(item: any): number {
        const precioReal = item.precio * item.cantidad;
        if (precioReal === 0) return 0;

        const porcentaje = (item.descuentoMonto / precioReal) * 100;
        return Math.round(porcentaje * 100) / 100;
    }

    calcularDescuentoPorcentajeTotal(): number {
        if (!this.precioReal || this.precioReal === 0) return 0;

        const descuentoTotal = this.precioReal - (this.formData.precioPaquete || 0);
        const porcentaje = (descuentoTotal / this.precioReal) * 100;

        return Math.max(0, Math.round(porcentaje * 100) / 100);
    }

    calcularPrecioFinal(item: any): number {
        const precioReal = item.precio * item.cantidad;
        const descuento = item.descuentoMonto || 0;
        return Math.max(0, precioReal - descuento);
    }

    validarDescuento(item: any): void {

        if (item.descuentoMonto === '' || item.descuentoMonto === null) {
            item.descuentoPorcentaje = 0;
            this.recalcularPrecioReal();
            return;
        }

        let monto = Number(item.descuentoMonto);

        if (monto < 0) monto = 0;

        const precioReal = item.precio * item.cantidad;
        if (monto > precioReal) {
            monto = precioReal;
        }

        item.descuentoMonto = Math.round(monto * 100) / 100;

        if (precioReal > 0) {
            item.descuentoPorcentaje = Math.round((item.descuentoMonto / precioReal) * 10000) / 100;
        } else {
            item.descuentoPorcentaje = 0;
        }

        this.recalcularPrecioReal();
    }

    onDescuentoFocus(item: any): void {
        if (item.descuentoMonto === 0) {
            item.descuentoMonto = '';
        }
    }

    onDescuentoBlur(item: any): void {
        if (!item.descuentoMonto || item.descuentoMonto === '') {
            item.descuentoMonto = 0;
        }
        this.validarDescuento(item);
    }

    handleSuccess(title: string, text: string): void {
        // Envolvemos en timeout para asegurar limpieza de ciclo de vida
        setTimeout(() => {
            this.onSave.emit();
            this.close();
            this.cdr.detectChanges();
            this.alertasService.success(title, text);
        });
    }

    handleError(title: string, text: string): void {
        console.error(text);
        this.alertasService.error(title, text);
    }

    private getToday(): string {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    resetForm(): void {
        this.formData = {
            codigo: '',
            nombre: '',
            descripcion: '',
            precioPaquete: 0,
            fechaInicio: this.getToday(),
            fechaFin: '',
            estado: '1',
            idTipoAtencion: null,
            tipoAtencion: 'AMBULATORIO',
            categoria: this.categoria,
            servicios: [],
            descuento: 0,
            idEmpresa: 1
        };
        this.precioReal = 0;
        this.listaServiciosAgregados = [];
        this.servicioSeleccionado = null;

        this.searchTerm = '';
        this.searchResults = [];
        this.showSearchDropdown = false;
    }

    onPrecioOfertaChange(): void {
        if (this.isRecalculating) return;

        const precioRealValue = this.precioReal || 0;
        const precioOfertaValue = this.formData.precioPaquete || 0;

        if (precioOfertaValue > precioRealValue) {
            this.formData.precioPaquete = precioRealValue;
            return;
        }

        const descuentoTotal = Math.max(0, precioRealValue - precioOfertaValue);

        this.distribuirDescuentoProporcional(descuentoTotal);
    }
    onDescuentoPorcentajeTotalChange(value: any): void {
        if (this.isRecalculating) return;

        let porcentaje = Number(value);

        if (isNaN(porcentaje) || porcentaje < 0) porcentaje = 0;
        if (porcentaje > 100) porcentaje = 100;

        this.aplicarDescuentoPorcentajeGlobal(porcentaje);
    }

    private distribuirDescuentoProporcional(descuentoTotal: number): void {
        this.isRecalculating = true;

        const precioRealValue = this.precioReal || 0;

        if (precioRealValue === 0) {
            this.isRecalculating = false;
            return;
        }

        this.listaServiciosAgregados.forEach(item => {
            const precioServicio = item.precio * item.cantidad;
            const proporcion = precioServicio / precioRealValue;

            const descuentoServicio = descuentoTotal * proporcion;
            item.descuentoMonto = Math.round(descuentoServicio * 100) / 100;

            if (precioServicio > 0) {
                item.descuentoPorcentaje = Math.round((item.descuentoMonto / precioServicio) * 10000) / 100;
            } else {
                item.descuentoPorcentaje = 0;
            }
        });

        this.isRecalculating = false;
    }

    private aplicarDescuentoPorcentajeGlobal(porcentaje: number): void {
        this.isRecalculating = true;

        this.listaServiciosAgregados.forEach(item => {
            const precioServicio = item.precio * item.cantidad;

            item.descuentoMonto = Math.round((precioServicio * porcentaje / 100) * 100) / 100;

            item.descuentoPorcentaje = porcentaje;
        });

        const sumaPreciosFinales = this.listaServiciosAgregados.reduce((acc, item) => {
            return acc + this.calcularPrecioFinal(item);
        }, 0);

        this.formData.precioPaquete = Math.round(sumaPreciosFinales * 100) / 100;

        this.isRecalculating = false;
    }

    onDescuentoPorcentajeServicioChange(item: any, porcentaje: number): void {
        if (this.isRecalculating) return;

        if (porcentaje < 0) porcentaje = 0;
        if (porcentaje > 100) porcentaje = 100;

        const precioServicio = item.precio * item.cantidad;

        item.descuentoMonto = Math.round((precioServicio * porcentaje / 100) * 100) / 100;
        item.descuentoPorcentaje = porcentaje;

        this.recalcularPrecioReal();
    }

    onDescuentoMontoServicioChange(item: any, monto: number): void {
        if (this.isRecalculating) return;

        const precioTotalServicio = item.precio * item.cantidad;
        if (monto < 0) monto = 0;
        if (monto > precioTotalServicio) monto = precioTotalServicio;

        item.descuentoMonto = monto;
        item.descuentoPorcentaje = precioTotalServicio > 0 
            ? Math.round((monto / precioTotalServicio * 100) * 100) / 100 
            : 0;

        this.recalcularPrecioReal();
    }

    get arbolFiltrado(): any[] {
        return this.filterTree(this.arbolNiveles, this.searchTermNivel);
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        // Nivel Sidebar logic (no longer needed for closing but kept for consistency)


        // Servicio Dropdown logic
        if (this.showServicioDropdown && this.servicioContainer) {
            const clickedInsideServicio = this.servicioContainer.nativeElement.contains(event.target);
            if (!clickedInsideServicio) {
                this.cerrarServicioDropdown();
            }
        }
    }

    @HostListener('window:scroll')
    onWindowScroll(): void {
        if (this.showServicioDropdown) {
            this.calcularPosicionServicioDropdown();
        }
    }

    @HostListener('window:resize')
    onWindowResize(): void {
        if (this.showServicioDropdown) {
            this.calcularPosicionServicioDropdown();
        }
    }
}
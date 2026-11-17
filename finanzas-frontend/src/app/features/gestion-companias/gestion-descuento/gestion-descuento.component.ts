import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Nivel, NivelTree } from '../../../models/nivel.model';
import { NivelService } from '../../../services/nivel.service';
import { Servicio } from '../../../models/servicio.model';
import { ServicioService } from '../../../services/servicio.service';
import {
  TablaGeneralComponent,
  Columna,
} from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { AlertasService } from '../../../services/alertas.service';

@Component({
  selector: 'app-gestion-descuento',
  standalone: true,
  imports: [CommonModule, FormsModule, TablaGeneralComponent, PaginationComponent, SearchGenericComponent],
  templateUrl: './gestion-descuento.component.html',
})
export class GestionDescuentoComponent implements OnInit {
  @Input() plan: any;
  @Output() onBack = new EventEmitter<void>();

  descuentoFijo: number | null = null;
  descuentoPorcentaje: number | null = null;
  precioFijo: number | null = null;

  codigoBusqueda: string = '';
  private searchSubject = new Subject<string>();
  nombreEntidadSeleccionada: string = '';
  entidadSeleccionada: { type: 'SERVICIO' | 'NIVEL'; id: any; data: any } | null = null;
  nodoSeleccionado: any = null;
  isSearching: boolean = false;
  isSaving: boolean = false;

  mostrarArbol: boolean = true;
  arbolNiveles: NivelTree[] = [];
  arbolLoading: boolean = false;

  descuentosTableData: any[] = [];
  filteredDescuentosData: any[] = []; // NEW: For client-side filtering
  paginatedDescuentosData: any[] = []; // NEW: For client-side pagination
  mostrarTabla: boolean = false;
  rawServiciosHijos: any[] = [];
  allDiscounts: any[] = [];
  descuentosLoading: boolean = false;

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
    {
      field: 'nivelNombre',
      header: 'Nombre de Servicio',
      tipo: 'text',
      subField: [],
      align: 'left',
    },
    {
      field: 'tipoAtencionNombre',
      header: 'Tipos de Atención',
      tipo: 'text',
      subField: [],
      align: 'center',
    },
    { field: 'servicioNombre', header: 'Precio Real', tipo: 'text', subField: [], align: 'left' },
    {
      field: 'descuentoFijo',
      header: 'Desc. Fijo',
      tipo: 'input-number',
      subField: [],
      align: 'center',
      disabledField: 'checked',
    },
    {
      field: 'precioFijo',
      header: 'Tarifa Plana',
      tipo: 'input-number',
      subField: [],
      align: 'center',
      disabledField: 'checked',
    },
    {
      field: 'descuentoPorcentaje',
      header: 'Desc. %',
      tipo: 'input-number',
      subField: [],
      align: 'center',
      disabledField: 'checked',
    },
    {
      field: 'precioFinal',
      header: 'Precio Final',
      tipo: 'text',
      subField: [],
      align: 'center',
    },
  ];

  get hasSelectedItems(): boolean {
    return this.filteredDescuentosData.some(item => item.checked);
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
    private cd: ChangeDetectorRef,
    private alertasService: AlertasService
  ) { }

  ngOnInit(): void {
    if (this.plan && this.plan.idPlan) {
      // PROGRAMADOR SENIOR: Herencia automática de valores globales del Plan
      this.descuentoPorcentaje = this.plan.porcentajeCoaseguro || 0;
      this.precioFijo = this.plan.copago || null;

      this.cargarDescuentosSilencioso();
      this.cargarArbolNiveles();
    }

    this.searchSubject.pipe(debounceTime(500), distinctUntilChanged()).subscribe((term) => {
      this.buscarEntidad(term);
    });
  }

  cargarArbolNiveles() {
    this.arbolLoading = true;
    this.nivelService.obtenerArbolCompleto().subscribe({
      next: (res) => {
        this.arbolNiveles = res.data || [];
        this.updatePaginationArbol();
        this.arbolLoading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.arbolLoading = false;
        this.cd.detectChanges();
      },
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
    let targetTerm = entidad.numNivel || entidad.nombre;
    this.codigoBusqueda = targetTerm;
    this.nombreEntidadSeleccionada = entidad.nombre;

    this.buscarEntidad(targetTerm);
  }

  onCodigoChange(term: string) {
    this.searchSubject.next(term);
  }

  cargarDescuentosSilencioso(): void {
    this.servicioService.obtenerDescuentos(this.plan.idPlan).subscribe({
      next: (data) => {
        this.allDiscounts = data;
        // PROGRAMADOR SENIOR: Sincronización en carga inicial/silenciosa
        if (this.entidadSeleccionada && this.rawServiciosHijos.length > 0) {
          this.procesarTablaDescuentos();
        }
      },
    });
  }

  cargarDescuentos(): void {
    this.descuentosLoading = true;
    this.servicioService.obtenerDescuentos(this.plan.idPlan).subscribe({
      next: (data) => {
        this.allDiscounts = data;

        // PROGRAMADOR SENIOR: Sincronización de datos base
        // Si hay una búsqueda activa, refrescamos los servicios del servidor para que 
        // rawServiciosHijos tenga los montosDescuentoFijo actualizados del backend.
        if (this.entidadSeleccionada && this.codigoBusqueda) {
          this.buscarEntidadSilencioso(this.codigoBusqueda);
        } else if (this.entidadSeleccionada && this.rawServiciosHijos.length > 0) {
          this.procesarTablaDescuentos();
        } else {
          // Si no hay búsqueda, mapeamos allDiscounts directamente
          this.descuentosTableData = data.map((item) => {
            let tipoAtencionStr = '-';
            let precioStr = '-';
            let nombreItem = item.nivel ? item.nivel.nombre : 'General';
            let codigoVal = item.servicio ? item.servicio.codigo : '-';

            if (item.servicio) {
              nombreItem = item.servicio.nombreServicio;
              tipoAtencionStr =
                item.servicio.tipoAtencionNombre ||
                (item.tipoAtencion ? item.tipoAtencion.nombre : '-');
              precioStr =
                item.servicio.precioConIgv != null
                  ? (item.servicio.moneda === 'USD' ? '$ ' : 'S/ ') +
                  Number(item.servicio.precioConIgv).toFixed(2)
                  : 'S/ 0.00';
            } else {

              if (item.tipoAtencion) {
                tipoAtencionStr = item.tipoAtencion.nombre;
              }
              if (tipoAtencionStr === 'TODOS' || !tipoAtencionStr) tipoAtencionStr = '-';
            }

            return {
              ...item,
              checked: false,
              codigo: codigoVal,
              nivelNombre: nombreItem,
              tipoAtencionNombre: tipoAtencionStr,
              servicioNombre: precioStr,
              descuentoFijo: Number(item.descuentoFijo || 0),
              descuentoPorcentaje: Number(item.descuentoPorcentaje || 0),
              precioFijo: item.precioFijo != null ? Number(item.precioFijo) : null,
            };
          });
        }
        this.descuentosLoading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando descuentos', err);
        this.descuentosLoading = false;
        this.cd.detectChanges();
      },
    });
  }

  buscarEntidad(term: string): void {
    if (!term) {
      this.limpiarSeleccion();
      return;
    }

    this.isSearching = true;
    this.nombreEntidadSeleccionada = 'Buscando...';
    this.currentPage = 0;

    this.servicioService.search(term, this.currentPage, this.pageSize, 'id', 'ASC', this.plan.idPlan).subscribe({
      next: (response) => {
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
            next: (res) => {
              if (res.data) {
                this.seleccionarEntidad('NIVEL', res.data);
              } else {
                this.handleNotFound();
              }
              this.isSearching = false;
            },
            error: (err) => {
              this.handleNotFound();
              this.isSearching = false;
            },
          });
        }
      },
      error: () => {

        this.nivelService.obtenerPorCodigo(term).subscribe({
          next: (res) => {
            if (res.data) {
              this.seleccionarEntidad('NIVEL', res.data);
            } else {
              this.handleNotFound();
            }
            this.isSearching = false;
          },
          error: () => {
            this.handleNotFound();
            this.isSearching = false;
          },
        });
      },
    });
  }

  // PROGRAMADOR SENIOR: Refresco silencioso para sincronizar rawServiciosHijos sin parpadeos
  buscarEntidadSilencioso(term: string): void {
    this.servicioService.search(term, 0, this.pageSize, 'id', 'ASC', this.plan.idPlan).subscribe({
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
    // Si no hay entidad seleccionada, buscamos todos pasando un string vacío "" al backend
    if (!this.entidadSeleccionada) {
      this.descuentosLoading = true;
      this.servicioService.search('', this.currentPage, this.pageSize, 'id', 'ASC', this.plan.idPlan).subscribe({
        next: (res) => {
          const servicios = res.content || [];
          this.totalElements = res.totalElements || 0;
          this.totalPages = res.totalPages || 0;

          this.rawServiciosHijos = servicios;
          this.mostrarArbol = false;
          this.mostrarTabla = true;
          this.procesarTablaDescuentos();

          if (servicios.length === 0) {
            this.alertasService.info(
              'Información',
              'No hay servicios asignados en el catálogo.',
            );
          }

          this.descuentosLoading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          this.rawServiciosHijos = [];
          console.error('Error al cargar servicios', err);
          this.alertasService.error('Error', 'No se pudieron cargar los servicios del catálogo.');
          this.descuentosLoading = false;
          this.cd.detectChanges();
        },
      });
      return;
    }

    if (this.entidadSeleccionada.type === 'NIVEL') {
      const nombreNivel = this.entidadSeleccionada.data?.nombre || '';
      this.descuentosLoading = true;

      this.servicioService.search(nombreNivel, this.currentPage, this.pageSize, 'id', 'ASC', this.plan.idPlan).subscribe({
        next: (res) => {
          const servicios = res.content || [];
          this.totalElements = res.totalElements || 0;
          this.totalPages = res.totalPages || 0;

          this.rawServiciosHijos = servicios;
          this.mostrarArbol = false;
          this.mostrarTabla = true;
          this.procesarTablaDescuentos();

          if (servicios.length === 0) {
            this.alertasService.info(
              'Información',
              'Este nivel no tiene servicios asignados en el catálogo.',
            );
          }

          this.descuentosLoading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          this.rawServiciosHijos = [];
          console.error('Error al cargar servicios del nivel', err);
          this.alertasService.error('Error', 'No se pudieron cargar los servicios del catálogo.');
          this.descuentosLoading = false;
          this.cd.detectChanges();
        },
      });
    } else {

      if (this.rawServiciosHijos && this.rawServiciosHijos.length > 0) {
        this.mostrarArbol = false;
        this.mostrarTabla = true;
        this.procesarTablaDescuentos();
      }
    }
  }

  // --- Filtering Logic for Table ---
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
  // -------------------------

  onPageChange(page: number) {
    if (!this.entidadSeleccionada || this.entidadSeleccionada?.type === 'NIVEL') {
      // Si es un nivel que está paginando desde el servidor o si listamos todo
      this.currentPage = page;
      this.listarHijos();
    } else {
      // Paginacion cliente
      this.currentPage = page;
      this.updatePagination();
    }
  }

  onPageSizeChange(size: number) {
    if (!this.entidadSeleccionada || this.entidadSeleccionada?.type === 'NIVEL') {
      this.pageSize = size;
      this.currentPage = 0;
      this.listarHijos();
    } else {
      this.pageSize = size;
      this.currentPage = 0;
      this.updatePagination();
    }
  }

  onDescuentoChange(tipo: 'FIJO' | 'PORCENTAJE' | 'TARIFA_PLANA') {
    if (tipo === 'TARIFA_PLANA') {
      this.descuentoPorcentaje = null;
      this.descuentoFijo = null;
    } else if (tipo === 'FIJO') {
      this.descuentoPorcentaje = null;
      this.precioFijo = null;
    } else {
      this.descuentoFijo = null;
      this.precioFijo = null;
    }
    if (this.rawServiciosHijos.length > 0) {
      this.procesarTablaDescuentos();
    }
  }

  procesarTablaDescuentos() {
    this.descuentosTableData = this.rawServiciosHijos.map((servicio: any) => {


      const sId = Number(servicio.id);
      const sNivelId = servicio.idNivel ? Number(servicio.idNivel) : null;

      // Buscar Servicio
      let discountRecord = this.allDiscounts.find(d => Number(d.idServicio) === sId);

      // Si no hay por servicio, buscar por Nivel
      if (!discountRecord && sNivelId) {
        discountRecord = this.allDiscounts.find(d => Number(d.idNivel) === sNivelId && !d.idServicio);
      }

      // Si aún no hay, buscar el Global (idNivel 0 o null y sin idServicio)
      if (!discountRecord) {
        discountRecord = this.allDiscounts.find(d =>
          !d.idServicio &&
          (Number(d.idNivel) === 0 || d.idNivel === null) &&
          (d.tipoAtencion?.nombre === 'TODOS' || d.tipoAtencion === 'TODOS' || d.codigoPlan === this.plan.codigo)
        );
      }

      // 2. VALORES BASE (AUDITORÍA SENIOR DE HERENCIA MULTINIVEL)
      // Si no hay registro en BD (discountRecord), heredamos los "Smart Defaults" del Plan.
      // NOTA: Solo heredamos el Coaseguro (%). El Copago (Tarifa Plana) NO se hereda por defecto 
      // a todos los servicios para evitar que el Precio Final suba por encima del Precio Real.
      // El componente padre podría pasar el porcentaje con el símbolo "%". Lo limpiamos.
      let fallbackPorcVal = parseFloat(String(this.plan.porcentajeCoaseguro || '0').replace('%', ''));
      
      let baseFijo = 0;
      let basePorc = discountRecord ? Number(discountRecord.descuentoPorcentaje || 0) : (isNaN(fallbackPorcVal) ? 0 : fallbackPorcVal);
      let baseTarifa = discountRecord && discountRecord.precioFijo != null 
                        ? Number(discountRecord.precioFijo) 
                        : null;

      // 3. ESTADO UI (OVERRIDE)
      const existingItem = this.descuentosTableData.find(d => d.id === servicio.id);
      let isChecked = existingItem ? existingItem.checked : false;

      let finalFijo = baseFijo;
      let finalPorc = basePorc;
      let finalTarifa = baseTarifa;

      let precioVal = servicio.precioConIgv != null ? Number(servicio.precioConIgv) : 0;
      let precioStr = (servicio.moneda === 'USD' ? '$ ' : 'S/ ') + precioVal.toFixed(2);

      // 4. APLICAR LÓGICA DE PREVISUALIZACIÓN MASIVA (CABECERA)
      if (isChecked) {
        if (this.precioFijo !== null && this.precioFijo >= 0) {
          finalTarifa = Number(this.precioFijo);
          finalPorc = 0;
          finalFijo = 0;
        } else if (this.descuentoPorcentaje !== null && this.descuentoPorcentaje >= 0) {
          finalPorc = Number(this.descuentoPorcentaje);
          finalFijo = Number((precioVal * (finalPorc / 100)).toFixed(2));
          finalTarifa = null;
        } else if (this.descuentoFijo !== null && this.descuentoFijo >= 0) {
          finalFijo = Number(this.descuentoFijo);
          finalPorc = precioVal > 0 ? Number(((finalFijo / precioVal) * 100).toFixed(2)) : 0;
          finalTarifa = null;
        } else if (existingItem) {
          // Respetar edición manual por fila si no hay masivo en cabecera
          finalFijo = existingItem.descuentoFijo ?? baseFijo;
          finalPorc = existingItem.descuentoPorcentaje ?? basePorc;
          finalTarifa = existingItem.precioFijo !== undefined ? existingItem.precioFijo : baseTarifa;
        }
      }

      // 5. NORMALIZACIÓN FINAL
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
        descuentoFijo: finalFijo,
        descuentoPorcentaje: finalPorc,
        precioFijo: finalTarifa,
        precioFinal: 'S/ ' + calcFinal.toFixed(2),
      };
    });

    this.applyTableFilter(); // Refrescar filtros/paginacion
  }

  handleTableInputChange(event: { item: any; col: any; value: any }) {
    const { item, col, value } = event;
    const precioReal = this.rawServiciosHijos.find(s => s.id === item.id)?.precioConIgv || 0;
    const precioVal = Number(precioReal);

    if (col.field === 'descuentoFijo') {
      const nuevoFijo = Number(value) || 0;
      item.descuentoFijo = nuevoFijo; // PROGRAMADOR SENIOR: Guardar el valor editado
      const nuevoPorc = precioVal > 0 ? (nuevoFijo / precioVal) * 100 : 0;
      item.descuentoPorcentaje = Number(nuevoPorc.toFixed(2));
      item.precioFijo = null;
      item.precioFinal = 'S/ ' + Math.max(0, precioVal - nuevoFijo).toFixed(2);
    } else if (col.field === 'descuentoPorcentaje') {
      const nuevoPorc = Number(value) || 0;
      item.descuentoPorcentaje = nuevoPorc; // PROGRAMADOR SENIOR: Guardar el valor editado
      const nuevoFijo = precioVal * (nuevoPorc / 100);
      item.descuentoFijo = Number(nuevoFijo.toFixed(2));
      item.precioFijo = null;
      item.precioFinal = 'S/ ' + Math.max(0, precioVal - nuevoFijo).toFixed(2);
    } else if (col.field === 'precioFijo') {
      const nuevaTarifa = Number(value) || 0;
      item.precioFijo = nuevaTarifa;
      item.descuentoPorcentaje = 0;
      item.descuentoFijo = 0;
      item.precioFinal = 'S/ ' + nuevaTarifa.toFixed(2);
    }
    this.cd.detectChanges();
  }

  handleCheckboxChange(event: { item: any; checked: boolean }) {
    this.procesarTablaDescuentos();
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

  limpiarSeleccion(): void {
    this.codigoBusqueda = '';
    this.entidadSeleccionada = null;
    this.nombreEntidadSeleccionada = '';
    this.rawServiciosHijos = [];
    this.descuentosTableData = [];
    this.mostrarTabla = false;
    this.mostrarArbol = true;
    this.currentPage = 0;
    this.currentPageArbol = 0;
    if (this.plan && this.plan.idPlan) {
      this.cargarDescuentosSilencioso();
    }
  }

  aplicarDescuento() {
    if (!this.validarRequisitosBasicos()) return;

    let payload: any = {
      idPlan: this.plan.idPlan,
      descuentoFijo: this.descuentoFijo || 0,
      descuentoPorcentaje: this.descuentoPorcentaje || 0,
      precioFijo: this.precioFijo,
    };

    const checkedServices = this.filteredDescuentosData.filter(d => d.checked);

    if (checkedServices.length > 0) {
      this.aplicarDescuentosSeleccionados(checkedServices);
      return;
    }

    if (this.entidadSeleccionada) {
      if (this.entidadSeleccionada.type === 'SERVICIO') {
        payload.tipoAplicacion = 'SERVICIO';
        payload.idServicio = this.entidadSeleccionada.id;
      } else {
        payload.tipoAplicacion = 'NIVEL';
        payload.idNivel = this.entidadSeleccionada.id;
        payload.idTipoAtencion = null;
      }
    } else {
      payload.tipoAplicacion = 'TODOS';
      payload.idTipoAtencion = null;
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
        'Ingrese al menos un valor de descuento o tarifa plana.',
      );
      return false;
    }
    return true;
  }

  private enviarSolicitudDescuento(payload: any, pregunta: string) {
    this.alertasService.confirm(
      'Confirmar',
      pregunta,
      'Sí, confirmar',
      'Cancelar',
      'bg-green-600 hover:bg-green-700 shadow-green-600/30',
      'success'
    ).then((result) => {
      if (result) {
        this.servicioService.aplicarDescuento(payload).subscribe({
          next: (res) => {
            this.alertasService.success('Éxito', 'Descuentos aplicados correctamente.');
            this.limpiarDespuesDeAplicar();
            this.cargarDescuentos();
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
      'Sí, confirmar',
      'Cancelar',
      'bg-green-600 hover:bg-green-700 shadow-green-600/30',
      'success'
    ).then((result) => {
      if (result) {
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
      this.cargarDescuentos();
      return;
    }

    const item = items[0];
    // PROGRAMADOR SENIOR: Estandarización de contrato para persistencia individual alineada con el Backend
    const payload = {
      idPlan: Number(this.plan.idPlan),
      descuentoFijo: Number(item.descuentoFijo || 0),
      descuentoPorcentaje: Number(item.descuentoPorcentaje || 0),
      precioFijo: item.precioFijo !== undefined && item.precioFijo !== null ? Number(item.precioFijo) : null,
      tipoAplicacion: 'SERVICIO',
      idServicio: Number(item.id),
      idNivel: null,
      idTipoAtencion: null
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
    this.descuentosTableData.forEach(item => item.checked = false);
    this.applyTableFilter();
  }

  goBack() {
    this.onBack.emit();
  }
}

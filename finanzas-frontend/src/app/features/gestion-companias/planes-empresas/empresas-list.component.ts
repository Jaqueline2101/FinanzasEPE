import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmpresaService, Empresa } from '../../../services/empresa.service';
import { ConvenioService } from '../../../services/convenio.service';
import { ServicioService } from '../../../services/servicio.service';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { forkJoin } from 'rxjs';
import { AlertasService } from '../../../services/alertas.service';


import { FormsModule } from '@angular/forms';
import { ModalVincularEmpComponent } from './modal-vincularEmp/modal-vincular-emp.component';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-empresas-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TablaGeneralComponent, ModalVincularEmpComponent, PaginationComponent, SearchGenericComponent, ModalComponent],
  templateUrl: './empresas-list.component.html'
})
export class EmpresasListComponent implements OnChanges {
  @Input() companiaId: number | null = null;
  @Input() companiaNombre: string = '';
  empresas: Empresa[] = [];
  empresasTableData: any[] = []; // Full Data
  filteredTableData: any[] = []; // Filtered Data (before pagination)
  paginatedData: any[] = []; // Sliced Data for current page

  // Search Configuration
  searchOptions = [
    { label: 'Nombre Plan', value: 'nombrePlan' },
    { label: 'Código', value: 'codigo' }
  ];
  currentSearchQuery: string = '';
  currentSearchType: string = 'nombrePlan';

  // Pagination Configuration
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;

  // Variables para el modal de vinculación
  showLinkModal: boolean = false;

  // Variables para el modal de edición
  showEditModal: boolean = false;
  editingPlan: any = {};

  // Bulk Discount Properties
  descuentoFijo: number | null = null;
  descuentoPorcentaje: number | null = null;
  isApplyingBulk: boolean = false;

  loading: boolean = false;

  // Configuration for Tabla General
  columnas: Columna[] = [
    { field: 'checked', header: '', tipo: 'checkbox', subField: [], align: 'center' },
    { field: 'index', header: '#', tipo: 'index', subField: [], align: 'center' },
    { field: 'codigo', header: 'Código', tipo: 'text', subField: [], align: 'center' },
    { field: 'nombrePlan', header: 'Nombre de Plan', tipo: 'text', subField: [], align: 'center' },
    {
      field: 'nombre',
      header: 'Empresas',
      tipo: 'title-subtitle',
      subField: ['ruc'],
      align: 'center',
      badgeConfig: {
        text: 'text-gray-500',
        container: ''
      }
    },
    {
      field: 'estado',
      header: 'Estado',
      tipo: 'badge',
      subField: [],
      align: 'center',
      badgeConfig: {
        container: 'bg-green-100 text-green-800 border-green-200',
        text: 'text-green-800'
      }
    },
    { field: 'fechaInicio', header: 'Fecha Inicio', tipo: 'date', subField: [], align: 'center' },
    { field: 'fechaFin', header: 'Fecha Fin', tipo: 'date', subField: [], align: 'center' },
    {
      field: 'copago',
      header: 'Copago',
      tipo: 'currency',
      subField: [],
      align: 'center',
      badgeConfig: { text: 'text-indigo-600 font-bold', container: 'bg-indigo-50 border-indigo-100' }
    },
    {
      field: 'deducible',
      header: 'Deducible',
      tipo: 'currency',
      subField: [],
      align: 'center',
      badgeConfig: { text: 'text-orange-600 font-bold', container: 'bg-orange-50 border-orange-100' }
    },
    {
      field: 'porcentajeCoaseguro',
      header: 'Coaseguro',
      tipo: 'badge',
      subField: [],
      align: 'center',
      badgeConfig: { text: 'text-emerald-800 font-bold', container: 'bg-emerald-100 border-emerald-200 px-2 py-0.5 rounded-full border' }
    },
    {
      field: 'gestionarDescuentos',
      header: 'Gestionar Descuentos',
      tipo: 'custom-button-action',
      subField: [],
      align: 'center',
      actionIcon: 'fa-tags',
      actionLabel: 'Gestionar Descuentos',
      actionClass: '!bg-green-50 !text-green-600 hover:!bg-green-100 !border-green-200 !rounded-full !shadow-sm !font-bold !px-3'
    }
  ];

  constructor(
    private empresaService: EmpresaService,
    private convenioService: ConvenioService,
    private servicioService: ServicioService,
    private cd: ChangeDetectorRef,
    private alertasService: AlertasService
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['companiaId'] && this.companiaId) {
      this.loadEmpresas();
    }
  }

  loadEmpresas(): void {
    if (!this.companiaId) return;
    this.loading = true;

    // We fetch both lists to link them
    forkJoin({
      empresas: this.empresaService.getEmpresasPorCompania(this.companiaId),
      planes: this.convenioService.listarPlanesPorCompania(this.companiaId)
    }).subscribe({
      next: (response: any) => {
        const empresas = response.empresas || [];
        const planes = response.planes || [];

        // Map for quick empresa lookup - Ensure Number keys
        const empresasMap = new Map(empresas.map((e: any) => [Number(e.idEmpresa), e]));

        // Primary iteration is now over PLANES
        console.log('Planes recibidos del servidor:', planes);
        this.empresasTableData = planes.map((plan: any) => {
          // Find the company associated with this plan - Ensure Number key
          const empresa: any = empresasMap.get(Number(plan.idEmpresa));

          return {
            // Plan Data (The Row Identity)
            checked: false,
            idPlan: plan.idPlan,
            codigo: plan.codigo || '-',
            nombrePlan: plan.nombre || 'Sin Nombre',
            fechaInicio: plan.fechaInicio,
            fechaFin: plan.fechaFin,
            estado: plan.estado === '1' ? 'Activo' : 'Inactivo', // Plan Status for display

            // Insurance Fields - Crucial for Edit & Logic
            copago: plan.copago,
            deducible: plan.deducible,
            porcentajeCoaseguro: plan.porcentajeCoaseguro !== null && plan.porcentajeCoaseguro !== undefined ? plan.porcentajeCoaseguro + '%' : '0%',

            // Company Data (Enriched Info)
            idEmpresa: plan.idEmpresa, // Important for edit/link actions
            nombre: plan.nombreEmpresa || (empresa ? empresa.nombre : 'Empresa No Encontrada'),
            ruc: empresa ? empresa.ruc : '-',
            // We use 'nombre' field for the specific column that expects 'Empresas' header

            // Full Objects for potential use
            rawPlan: plan,
            rawEmpresa: empresa
          };
        });

        // Optional: Sort by Plan ID descending (newest plans first)
        this.empresasTableData.sort((a, b) => b.idPlan - a.idPlan);

        this.applyFilter(); // Initial filter & pagination

        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error loading data', err);
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  // --- Filtering Logic ---
  handleSearch(event: { q: string; type: string }): void {
    this.currentSearchQuery = event.q.toLowerCase();
    this.currentSearchType = event.type;
    this.currentPage = 0; // Reset to first page when searching
    this.applyFilter();
  }

  applyFilter(): void {
    if (!this.currentSearchQuery) {
      this.filteredTableData = [...this.empresasTableData];
    } else {
      this.filteredTableData = this.empresasTableData.filter(item => {
        const value = item[this.currentSearchType]?.toString().toLowerCase() || '';
        return value.includes(this.currentSearchQuery);
      });
    }
    this.updatePagination();
  }
  // -----------------------

  // --- Pagination Logic ---
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredTableData.length / this.pageSize);
    if (this.currentPage >= this.totalPages) {
      this.currentPage = Math.max(0, this.totalPages - 1);
    }
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedData = this.filteredTableData.slice(startIndex, endIndex);
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.updatePagination();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0; // Reset to first page
    this.updatePagination();
  }
  // -------------------------

  @Output() onGestionarDescuentos = new EventEmitter<any>();

  handleLinkAction(event: any): void {
    const item = event;
    console.log('Gestionar Descuentos clicked for:', item);
    this.onGestionarDescuentos.emit(item);
  }



  // Métodos del Modal de Vinculación
  openLinkModal(): void {
    this.showLinkModal = true;
  }

  closeLinkModal(): void {
    this.showLinkModal = false;
  }

  onVinculacionExitosa(): void {
    // Reset to first page to show new items (assuming sorting by newest)
    this.currentPage = 0;
    this.loadEmpresas();

    this.closeLinkModal();
    this.alertasService.success('Creación exitosa', 'Plan registrado correctamente.');
  }

  getLinkedIds(): Set<number> {
    return new Set(this.empresasTableData.map(e => e.idEmpresa || e.empresa?.idEmpresa));
  }

  onCheckboxChange(event: { item: any; checked: boolean }): void {
    // Selection state is already updated in item (since it's an object reference)
    // but we can add secondary logic here if needed.
  }

  toggleTodos(checked: boolean): void {
    this.paginatedData.forEach(item => item.checked = checked);
    this.cd.detectChanges();
  }

  seleccionarTodoBoton(): void {
    const todosMarcados = this.paginatedData.every(item => item.checked);
    this.toggleTodos(!todosMarcados);
  }

  aplicarDescuentoMasivo(): void {
    const selectedPlans = this.empresasTableData.filter(p => p.checked);

    if (selectedPlans.length === 0) {
      this.alertasService.error('Atención', 'Seleccione al menos un plan de la tabla.');
      return;
    }

    if (this.descuentoFijo === null && this.descuentoPorcentaje === null) {
      this.alertasService.error('Atención', 'Ingrese un monto fijo o porcentaje de descuento.');
      return;
    }

    this.alertasService.confirm(
      '¿Confirmar aplicación?',
      `Se aplicará el descuento a ${selectedPlans.length} planes seleccionados.`,
      'Sí, aplicar',
      'Cancelar',
      'bg-green-600 hover:bg-green-700 shadow-green-600/30',
      'success'
    ).then((result: boolean) => {
      if (result) {
        this.isApplyingBulk = true;
        this.procesarDescuentosPlanesSecuencial(selectedPlans);
      }
    });
  }

  private procesarDescuentosPlanesSecuencial(plans: any[]): void {
    if (plans.length === 0) {
      this.isApplyingBulk = false;
      this.alertasService.success('Éxito', 'Descuento aplicado masivamente a los planes.');
      this.descuentoFijo = null;
      this.descuentoPorcentaje = null;
      // Uncheck all after success
      this.empresasTableData.forEach(p => p.checked = false);
      this.cd.detectChanges();
      return;
    }

    const plan = plans[0];
    // PROGRAMADOR SENIOR: Estandarización de payload para persistencia garantizada
    // Se asegura que los valores sean numéricos y se incluya el contrato completo que el backend espera
    const payload = {
      idPlan: Number(plan.idPlan),
      descuentoFijo: Number(this.descuentoFijo || 0),
      descuentoPorcentaje: Number(this.descuentoPorcentaje || 0),
      tipoAplicacion: 'TODOS',
      idNivel: null,
      idServicio: null,
      idTipoAtencion: null
    };

    this.servicioService.aplicarDescuento(payload).subscribe({
      next: () => this.procesarDescuentosPlanesSecuencial(plans.slice(1)),
      error: (err: any) => {
        console.error(`Error aplicando descuento al plan ${plan.idPlan}:`, err);
        // Continue with next plan even if one fails
        this.procesarDescuentosPlanesSecuencial(plans.slice(1));
      }
    });
  }

  onDescuentoValueChange(tipo: 'FIJO' | 'PORCENTAJE'): void {
    if (tipo === 'FIJO') this.descuentoPorcentaje = null;
    else this.descuentoFijo = null;
  }

  eliminar(item: any): void {
    if (!this.companiaId) return;

    this.alertasService.deleteConfirm(
      '¿Estás seguro?',
      `¿Deseas desvincular la empresa ${item.nombre}? Esta acción no se puede deshacer.`
    ).then((result) => {
      if (result) {
        if (item.idPlan) {
          this.convenioService.eliminarConvenio(item.idPlan).subscribe({
            next: () => {
              this.loadEmpresas();
              this.alertasService.success('Desvincular', 'La empresa ha sido desvinculada correctamente.');
            },
            error: (err) => {
              console.error('Error eliminando convenio', err);
              this.alertasService.error('Error', 'No se pudo desvincular la empresa. Inténtalo de nuevo.');
            }
          });
        } else {
          this.alertasService.error('Error', 'No se identificó el ID del convenio.');
        }
      }
    });
  }

  // Métodos del Modal de Edición (Refactorizados para Plan)
  openEditModal(item: any): void {
    // The item comes fully enriched from table data
    // We bind to editingPlan for the form
    this.editingPlan = {
      idPlan: item.idPlan,
      idEmpresa: item.idEmpresa,
      nombreEmpresa: item.nombre, // For display only
      ruc: item.ruc, // For display only

      // Editable fields - matching "Vincular Empresa" structure
      codigo: item.codigo !== '-' ? item.codigo : '',
      nombre: item.nombrePlan,
      estado: item.estado === 'Activo' ? '1' : '0',
      fechaInicio: item.fechaInicio,
      fechaFin: item.fechaFin,
      // Insurance fields
      copago: item.copago,
      deducible: item.deducible,
      porcentajeCoaseguro: typeof item.porcentajeCoaseguro === 'string'
        ? Number(item.porcentajeCoaseguro.replace('%', ''))
        : item.porcentajeCoaseguro
    };

    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingPlan = {};
  }

  saveEdit(): void {
    if (!this.editingPlan.idPlan) return;

    // Prepare Request Object for ConvenioService
    const request: any = {
      idEmpresa: this.editingPlan.idEmpresa,
      idCompania: this.companiaId,
      codigo: this.editingPlan.codigo,
      nombre: this.editingPlan.nombre,
      estado: this.editingPlan.estado,
      fechaInicio: this.editingPlan.fechaInicio,
      fechaFin: this.editingPlan.fechaFin,
      // Insurance fields
      copago: Number(this.editingPlan.copago || 0),
      deducible: Number(this.editingPlan.deducible || 0),
      porcentajeCoaseguro: Number(this.editingPlan.porcentajeCoaseguro || 0)
    };

    this.convenioService.actualizarConvenio(this.editingPlan.idPlan, request).subscribe({
      next: (updated) => {
        // Programador Senior: Diferimos la ejecución al siguiente micro-ciclo 
        // para evitar el error NG0100 ExpressionChangedAfterItHasBeenCheckedError
        setTimeout(() => {
          this.closeEditModal();
          this.loadEmpresas(); 
          this.alertasService.success('Actualizado', 'Plan actualizado correctamente.');
          this.cd.detectChanges();
        });
      },
      error: (err) => {
        console.error('Error updating plan', err);
        this.alertasService.error('Error', 'No se pudo actualizar el plan.');
      }
    });
  }
}
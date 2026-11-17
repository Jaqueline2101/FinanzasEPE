import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { KardexService, KardexResponse } from '../../services/kardex.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { Sucursal } from '../../models/sucursal.model';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';
import { ModalKardexComponent } from './modal-kardex/modal-kardex.component';

import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PageHeaderComponent,
    BreadcrumbComponent,
    TablaGeneralComponent,
    PaginationComponent,
    BarraFiltrosComponent,
    ModalKardexComponent
  ],
  providers: [DatePipe],
  templateUrl: './kardex.component.html',
  styleUrls: ['./kardex.component.css'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms ease-in', style({ opacity: 1 }))
      ])
    ])
  ]
})
export class KardexComponent implements OnInit {
  breadcrumbItems = [
    { label: 'Inicio', route: '/admin/dashboard' },
    { label: 'Reportes', route: '/admin/reportes/ventas' },
    { label: 'Kardex de Inventario' }
  ];

  totalPages: number = 0;
  isLoading = false;
  movimientos: any[] = [];
  
  // Modal state
  modalVisible = false;
  selectedMovimiento: any = null;

  sucursales: Sucursal[] = [];

  get sucursalOptions() {
    return [
      { label: 'TODOS LOS ESTABLECIMIENTOS', value: 'null' },
      ...this.sucursales.map(s => ({ label: s.nombreSucursal, value: s.idSucursal.toString() }))
    ];
  }

  // Pagination Movimientos
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;

  columnas: Columna[] = [
    { field: 'index', header: '#', tipo: 'index', align: 'center' },
    { header: 'Fecha', field: 'fechaFormateada', tipo: 'text', align: 'left' },
    { header: 'Operación / Detalle', field: 'operacionDisplay', tipo: 'title-subtitle', subField: ['detalle'], align: 'left' },
    { header: 'Producto / Bien', field: 'nombreProducto', tipo: 'title-subtitle', subField: ['idCatalogoStr'], align: 'left' },
    { header: 'Movimiento', field: 'cantidadMovimiento', tipo: 'badge', align: 'center' },
    { header: 'Saldo Cant.', field: 'saldoCantidad', tipo: 'badge', badgeConfig: { from: 'from-slate-100', to: 'to-slate-200', text: 'text-slate-700', border: 'border-slate-300' }, align: 'right' },
    { header: 'Costo PMP', field: 'saldoCostoUnitario', tipo: 'currency', align: 'right' },
    { header: 'Saldo Total', field: 'saldoCostoTotal', tipo: 'currency', align: 'right' }
  ];


  constructor(
    private kardexService: KardexService,
    private sucursalService: SucursalService,
    private datePipe: DatePipe,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {

    this.isLoading = true;

    forkJoin({
      sucursales: this.sucursalService.getAll(0, 100),
      historial: this.kardexService.getTodos(this.currentPage, this.pageSize)
    }).subscribe({
      next: (result: any) => {
        if (result.sucursales.success && result.sucursales.data) {
          this.sucursales = result.sucursales.data.content;
        }
        this.procesarRespuestaHistorial(result.historial);
        this.isLoading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando datos iniciales', err);
        this.isLoading = false;
        this.cd.detectChanges();
      }
    });
  }

  private procesarRespuestaHistorial(data: any): void {
    const content = data.content || [];
    this.movimientos = content.map((m: any) => this.mapKardexData(m));
    this.totalElements = data.totalElements || 0;
    this.totalPages = data.totalPages || 0;
  }

  private mapKardexData(m: any): any {
    return {
      ...m,
      fechaFormateada: this.datePipe.transform(m.fecha, 'dd/MM/yyyy HH:mm'),
      fechaVencFormateada: m.fechaVenc ? 'Vence: ' + this.datePipe.transform(m.fechaVenc, 'MM/yyyy') : 'Sin vencimiento',
      cantidadMovimiento: `${m.signo} ${Number(m.cantidad).toFixed(2)} ${m.presentacion || ''}`,
      nroLote: m.nroLote || 'N/A',
      operacionDisplay: m.operacion.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (l: string) => l.toUpperCase()),
      nombreProducto: m.catalogo?.nombre || 'N/A',
      idCatalogoStr: `ID: ${m.idCatalogo} ${m.nroLote ? '| Lote: ' + m.nroLote : ''}`,
      badgeConfig: m.signo === '+'
        ? { from: 'from-emerald-50', to: 'to-emerald-100', text: 'text-emerald-700', border: 'border-emerald-200' }
        : { from: 'from-rose-50', to: 'to-rose-100', text: 'text-rose-700', border: 'border-rose-200' }
    };
  }

  handleFilter(criteria: FilterCriteria) {
    console.log('Filtros aplicados:', criteria);
    this.onRefresh();
  }

  verDetalles(movimiento: any) {
    this.selectedMovimiento = movimiento;
    this.modalVisible = true;
    this.cd.detectChanges();
  }

  exportarExcel() {
    if (this.movimientos.length === 0) {
      Swal.fire('Atención', 'No hay datos para exportar', 'warning');
      return;
    }

    const dataToExport = this.movimientos.map((m, index) => ({
      '#': index + 1,
      'FECHA': m.fechaFormateada,
      'OPERACIÓN': m.operacionDisplay,
      'DETALLE': m.detalle,
      'PRODUCTO': m.nombreProducto,
      'ID/LOTE': m.idCatalogoStr,
      'MOVIMIENTO': m.cantidadMovimiento,
      'SALDO CANT.': m.saldoCantidad,
      'COSTO PMP': m.saldoCostoUnitario,
      'SALDO TOTAL': m.saldoCostoTotal
    }));

    const worksheet = XLSX.utils.json_to_sheet(dataToExport);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Kardex');

    const wscols = [
      { wch: 5 },  // #
      { wch: 20 }, // Fecha
      { wch: 20 }, // Operación
      { wch: 30 }, // Detalle
      { wch: 35 }, // Producto
      { wch: 20 }, // ID/Lote
      { wch: 15 }, // Movimiento
      { wch: 15 }, // Saldo Cant
      { wch: 15 }, // Costo PMP
      { wch: 15 }  // Saldo Total
    ];
    worksheet['!cols'] = wscols;

    XLSX.writeFile(workbook, `Kardex_Inventario_${new Date().getTime()}.xlsx`);
  }

  onRefresh(): void {
    this.currentPage = 0;
    this.cargarHistorial();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.cargarHistorial();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.cargarHistorial();
  }

  cargarHistorial(): void {
    this.isLoading = true;
    this.kardexService.getTodos(this.currentPage, this.pageSize).subscribe({
      next: (data: any) => {
        this.procesarRespuestaHistorial(data);
        this.isLoading = false;
        this.cd.detectChanges();
      },
      error: (err: any) => {
        console.error('Error cargando Kardex', err);
        Swal.fire('Error', 'No se pudo cargar el historial de movimientos', 'error');
        this.isLoading = false;
        this.cd.detectChanges();
      }
    });
  }

  getStatusBadgeConfig(fechaVenc: string | undefined): any {
    if (!fechaVenc) return { from: 'from-slate-100', to: 'to-slate-200', text: 'text-slate-600' };
    const today = new Date();
    const venc = new Date(fechaVenc);
    const diffDays = Math.ceil((venc.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays <= 0) return { from: 'from-rose-100', to: 'to-rose-200', text: 'text-rose-700', border: 'border-rose-300' };
    if (diffDays <= 30) return { from: 'from-orange-100', to: 'to-orange-200', text: 'text-orange-700', border: 'border-orange-300' };
    if (diffDays <= 90) return { from: 'from-amber-100', to: 'to-amber-200', text: 'text-amber-700', border: 'border-amber-300' };
    return { from: 'from-emerald-100', to: 'to-emerald-200', text: 'text-emerald-700', border: 'border-emerald-300' };
  }
}

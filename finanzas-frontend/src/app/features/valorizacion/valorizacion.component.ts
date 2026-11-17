import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { ValorizacionService } from '../../services/valorizacion.service';
import { ValorizacionDTO, ValorizacionResumenDTO } from '../../models/valorizacion.model';
import { AuthService } from '../auth/services/auth.service';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-valorizacion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PageHeaderComponent,
    BreadcrumbComponent,
    TablaGeneralComponent,
    PaginationComponent,
    BarraFiltrosComponent
  ],
  providers: [DatePipe, CurrencyPipe],
  templateUrl: './valorizacion.component.html'
})
export class ValorizacionComponent implements OnInit {

  // --- UI STATE ---
  breadcrumbItems = [
    { label: 'Admin', route: '/admin' },
    { label: 'Reportes', route: '/admin/reportes/valorizacion' },
    { label: 'Valorización', route: '/admin/reportes/valorizacion' }
  ];
  isLoading: boolean = false;

  // --- DATA ---
  resumen: ValorizacionResumenDTO = {
    totalProductos: 0,
    valorTotalInventario: 0,
    productosSinStock: 0,
    productosStockBajo: 0,
    productosProximosVencer: 0,
    valorizacionPorCategoria: {},
    topProductosValorizados: {}
  };

  detalle: ValorizacionDTO[] = [];

  // --- FILTERS ---
  idSucursal: number = 1; // Debería venir de un servicio de auth/sucursal
  searchTerm: string = '';
  categoriaFilter: string = '';
  estadoStockFilter: string = '';

  // --- PAGINATION ---
  paginaActual: number = 0;
  tamanoPagina: number = 10;
  totalElementos: number = 0;
  totalPaginas: number = 0;

  // --- TABLE COLUMNS ---
  columnasTabla: Columna[] = [
    { header: 'Código', field: 'codigo', align: 'center' },
    { header: 'Producto', field: 'nombre', align: 'left' },
    { header: 'Categoría', field: 'categoria', align: 'center' },
    { header: 'Lote', field: 'lote', align: 'center' },
    { header: 'Stock', field: 'stock', align: 'center' },
    { header: 'Mínimo', field: 'stockMinimo', align: 'center' },
    { header: 'Costo Unit.', field: 'costoUnitario', tipo: 'currency', align: 'right' },
    { header: 'Venta Unit.', field: 'precioVenta', tipo: 'currency', align: 'right' },
    { header: 'Valor Total', field: 'valorTotal', tipo: 'currency', align: 'right' },
    { header: 'Vencimiento', field: 'fechaVencimiento', tipo: 'date', align: 'center' },
    { header: 'Estado', field: 'estado', tipo: 'order-status', align: 'center' }
  ];

  searchOptions = [
    { label: 'Todas las Categorías', value: '' },
    // Aquí se podrían cargar dinámicamente
  ];

  estadoOptions = [
    { label: 'Todos los Estados', value: 'TODOS' },
    { label: 'Sin Stock', value: 'SIN_STOCK' },
    { label: 'Stock Bajo', value: 'STOCK_BAJO' },
    { label: 'Normal', value: 'NORMAL' }
  ];

  constructor(
    private valorizacionService: ValorizacionService,
    private authService: AuthService,
    private cd: ChangeDetectorRef,
    private datePipe: DatePipe,
    private currencyPipe: CurrencyPipe
  ) { }

  ngOnInit() {
    // Obtener sucursal actual desde AuthService
    const sucursalId = this.authService.getSucursalIdFromToken();
    if (sucursalId) {
      this.idSucursal = sucursalId;
    } else {
      // Fallback a localStorage si no está en el token
      const user = this.authService.currentUser();
      if (user && user.sucursalActualId) {
        this.idSucursal = user.sucursalActualId;
      }
    }
    
    this.cargarReporte();
  }

  cargarReporte(resetPage: boolean = true) {
    if (resetPage) this.paginaActual = 0;

    this.isLoading = true;
    this.valorizacionService.getReporte(
      this.idSucursal,
      this.searchTerm,
      this.categoriaFilter,
      this.estadoStockFilter,
      this.paginaActual,
      this.tamanoPagina
    ).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.resumen = res.data.resumen;
          this.detalle = res.data.detalle.content;
          this.totalElementos = res.data.detalle.totalElements;
          this.totalPaginas = res.data.detalle.totalPages;
        }
        this.isLoading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error cargando reporte:', err);
        Swal.fire('Error', 'No se pudo cargar el reporte de valorización', 'error');
        this.cd.detectChanges();
      }
    });
  }

  handleFilter(criteria: FilterCriteria) {
    this.searchTerm = criteria.term;
    this.estadoStockFilter = criteria.estado;
    this.categoriaFilter = criteria.searchType === 'ALL' ? '' : criteria.searchType;
    this.cargarReporte(true);
  }

  onPageChange(page: number) {
    this.paginaActual = page;
    this.cargarReporte(false);
  }

  onPageSizeChange(size: number) {
    this.tamanoPagina = size;
    this.cargarReporte(true);
  }

  exportarExcel() {
    if (this.detalle.length === 0) return;

    const data = this.detalle.map(item => ({
      'Código': item.codigo,
      'Producto': item.nombre,
      'Categoría': item.categoria,
      'Lote': item.lote,
      'Stock': item.stock,
      'Stock Mínimo': item.stockMinimo,
      'Costo Unitario': item.costoUnitario,
      'Precio Venta': item.precioVenta,
      'Valor Total': item.valorTotal,
      'Vencimiento': item.fechaVencimiento,
      'Estado': item.estado
    }));

    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Valorización');
    XLSX.writeFile(wb, `Valorizacion_Inventario_${new Date().getTime()}.xlsx`);
  }

  exportarPDF() {
    const doc = new jsPDF('l', 'mm', 'a4');

    doc.setFontSize(18);
    doc.text('Reporte de Valorización de Inventario', 14, 22);

    doc.setFontSize(11);
    doc.text(`Fecha: ${new Date().toLocaleDateString()}`, 14, 30);
    doc.text(`Total Valorización: ${this.currencyPipe.transform(this.resumen.valorTotalInventario, 'PEN')}`, 14, 36);

    const head = [['Código', 'Producto', 'Stock', 'Costo', 'Venta', 'Total', 'Vencimiento']];
    const body = this.detalle.map(item => [
      item.codigo,
      item.nombre,
      item.stock,
      this.currencyPipe.transform(item.costoUnitario, 'PEN'),
      this.currencyPipe.transform(item.precioVenta, 'PEN'),
      this.currencyPipe.transform(item.valorTotal, 'PEN'),
      item.fechaVencimiento || '---'
    ]);

    autoTable(doc, {
      head: head,
      body: body,
      startY: 45,
      theme: 'striped',
      headStyles: { fillColor: [11, 65, 101] }
    });

    doc.save(`Valorizacion_${new Date().getTime()}.pdf`);
  }

  imprimir() {
    window.print();
  }

  // Helpers para los gráficos (en este caso representados con barras de progreso o círculos)
  getCategorias() {
    return Object.keys(this.resumen.valorizacionPorCategoria || {});
  }

  getTopProductos() {
    return Object.keys(this.resumen.topProductosValorizados || {});
  }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as XLSX from 'xlsx';
import Swal from 'sweetalert2';
import { RepPagoPersonalService } from '../../services/rep-pago-personal.service';
import { DashboardPagoPersonalDto } from '../../models/rep-pago-personal.model';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { TablaGeneralComponent } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { PagoPersonalService } from '../../services/pago-personal.service';
import { finalize } from 'rxjs/operators';
import { PagoComprobante } from '../../models/pago-honorarios.model';

@Component({
  selector: 'app-rep-pago-personal',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    BreadcrumbComponent, 
    PageHeaderComponent, 
    BarraFiltrosComponent,
    TablaGeneralComponent,
    PaginationComponent
  ],
  templateUrl: './rep-pago-personal.component.html',
  styleUrls: ['./rep-pago-personal.component.css']
})
export class RepPagoPersonalComponent implements OnInit {
  breadcrumbItems = [
    { label: 'Reportes', ruta: '/admin/reportes' },
    { label: 'Pagos de Personal', ruta: '/admin/reportes/pagos' }
  ];

  dashboardData?: DashboardPagoPersonalDto;
  recentPagos: PagoComprobante[] = [];
  loading = false;
  anioActual = new Date().getFullYear();
  anios = [2023, 2024, 2025, 2026];

  // Pagination
  paginaActual = 0;
  tamanoPagina = 10;
  totalPaginas = 0;
  allPagos: PagoComprobante[] = [];

  columnas = [
    { field: 'fechaPago', header: 'Fecha', tipo: 'date' as const },
    { field: 'numeroDocumento', header: 'Documento' },
    { field: 'nombreRazonSocial', header: 'Trabajador' },
    { field: 'areaTrabajador', header: 'Área', tipo: 'area-badge' as const },
    { field: 'periodoMes', header: 'Mes' },
    { field: 'montoNeto', header: 'Monto Neto', tipo: 'currency' as const },
    { field: 'estadoPago', header: 'Estado', tipo: 'order-status' as const }
  ];

  constructor(
    private reportService: RepPagoPersonalService,
    private pagoService: PagoPersonalService,
    private cd: ChangeDetectorRef
  ) {}

  handleFilter(criteria: FilterCriteria): void {
    if (criteria.fechaDesde) {
      const year = new Date(criteria.fechaDesde).getFullYear();
      if (year !== this.anioActual) {
        this.anioActual = year;
        this.consultar();
      }
    }
  }

  ngOnInit(): void {
    this.consultar();
  }

  consultar(): void {
    this.loading = true;
    this.reportService.getDashboard(this.anioActual)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (data: DashboardPagoPersonalDto) => {
          this.dashboardData = data;
          this.cd.detectChanges();
        },
        error: (err: any) => console.error('Error al cargar dashboard:', err)
      });

    // También cargamos los últimos pagos para la tabla inferior
    this.pagoService.listarPagos().subscribe({
      next: (response: any) => {
        // El servicio devuelve ApiResponse<PagoComprobante[]>
        const dataArr: PagoComprobante[] = response?.data || [];
        
        this.allPagos = dataArr
          .filter((p: PagoComprobante) => p.periodoAnio && p.periodoAnio.toString() === this.anioActual.toString())
          .sort((a: PagoComprobante, b: PagoComprobante) => {
            const dateA = a.fechaRegistro ? new Date(a.fechaRegistro).getTime() : 0;
            const dateB = b.fechaRegistro ? new Date(b.fechaRegistro).getTime() : 0;
            return dateB - dateA;
          });
        
        this.totalPaginas = Math.ceil(this.allPagos.length / this.tamanoPagina);
        this.updatePaginatedData();
        this.cd.detectChanges();
      }
    });
  }

  updatePaginatedData(): void {
    const start = this.paginaActual * this.tamanoPagina;
    const end = start + this.tamanoPagina;
    this.recentPagos = this.allPagos.slice(start, end);
  }

  onPageChange(page: number): void {
    this.paginaActual = page;
    this.updatePaginatedData();
  }

  onPageSizeChange(size: number): void {
    this.tamanoPagina = size;
    this.paginaActual = 0;
    this.totalPaginas = Math.ceil(this.allPagos.length / this.tamanoPagina);
    this.updatePaginatedData();
    this.cd.detectChanges();
  }

  exportarExcel(): void {
    if (!this.allPagos || this.allPagos.length === 0) {
      Swal.fire('Atención', 'No hay datos para exportar', 'warning');
      return;
    }
    const datePipe = new DatePipe('en-US');
    const dataToExport = this.allPagos.map((item, index) => ({
      '#': index + 1,
      'FECHA': item.fechaPago ? datePipe.transform(item.fechaPago, 'dd/MM/yyyy') : '---',
      'DOCUMENTO': item.numeroDocumento || '---',
      'TRABAJADOR': item.nombreRazonSocial || '---',
      'ÁREA': item.areaTrabajador || '---',
      'MES': item.periodoMes || '---',
      'MONTO NETO': item.montoNeto,
      'ESTADO': item.estadoPago || '---'
    }));
    const ws = XLSX.utils.json_to_sheet(dataToExport);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Pagos Personal');
    XLSX.writeFile(wb, `Reporte_Pagos_Personal_${this.anioActual}.xlsx`);
  }

  getMaxValor(grafico: any[] | undefined): number {
    if (!grafico || grafico.length === 0) return 1;
    return Math.max(...grafico.map(d => d.valor));
  }

  getPorcentaje(valor: number, max: number): number {
    if (max === 0) return 0;
    return (valor / max) * 100;
  }
}

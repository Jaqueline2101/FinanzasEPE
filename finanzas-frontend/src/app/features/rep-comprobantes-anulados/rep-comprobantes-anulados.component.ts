import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/services/auth.service';

@Component({
  selector: 'app-rep-comprobantes-anulados',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    PageHeaderComponent, 
    BreadcrumbComponent, 
    TablaGeneralComponent, 
    BarraFiltrosComponent, 
    PaginationComponent
  ],
  providers: [DatePipe],
  templateUrl: './rep-comprobantes-anulados.component.html',
})
export class RepComprobantesAnuladosComponent implements OnInit {
  
  breadcrumbItems = [
    { label: 'Reportes', route: '/admin/reportes' },
    { label: 'Comprobantes Anulados', route: '/admin/reportes/comprobantes-anulados' },
  ];

  // Columnas para la tabla
  columnas: Columna[] = [
    { header: 'Fecha', field: 'fecha', align: 'center', tipo: 'date' },
    { header: 'Comprobante', field: 'comprobante', align: 'left' },
    { header: 'Paciente', field: 'paciente', align: 'left' },
    { header: 'Método Pago', field: 'metodoPago', align: 'center' },
    { header: 'Total', field: 'total', align: 'right', tipo: 'currency' },
    { header: 'Motivo Anulación', field: 'motivoAnulacion', align: 'left' },
    { header: 'Usuario', field: 'usuarioAnulacion', align: 'left' }
  ];

  // Datos y estado
  datos: any[] = [];
  isLoading = false;
  totalAnulado = 0;
  totalElements = 0;

  // Filtros y Paginación
  paginaActual = 0;
  tamanoPagina = 10;
  totalPages = 0;
  
  searchOptions = [
    { label: 'Todos', value: 'ALL' },
    { label: 'Comprobante', value: 'DOC' },
    { label: 'Paciente', value: 'PAC' }
  ];

  filtros: any = {
    fechaInicio: '',
    fechaFin: '',
    query: '',
    idSucursal: null
  };

  constructor(
    private http: HttpClient,
    private cd: ChangeDetectorRef,
    private authService: AuthService
  ) {
    // Inicializar fechas con el día de hoy (tiempo local)
    const hoy = this.getHoy();
    this.filtros.fechaInicio = hoy;
    this.filtros.fechaFin = hoy;
    
    // Capturar sucursal del usuario
    const user = this.authService.currentUser();
    if (user && user.sucursalActualId) {
      this.filtros.idSucursal = user.sucursalActualId;
    }
  }

  private getHoy(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  ngOnInit(): void {
    this.cargarReporte();
  }

  cargarReporte(): void {
    this.isLoading = true;
    
    const body = {
      fechaInicio: this.filtros.fechaInicio,
      fechaFin: this.filtros.fechaFin,
      idSucursal: this.filtros.idSucursal,
      query: this.filtros.query
    };

    const url = `${environment.apiUrl}/reportes/comprobantes-anulados/generar?page=${this.paginaActual}&size=${this.tamanoPagina}`;

    this.http.post<any>(url, body).subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          const resData = resp.data;
          this.datos = resData.detalleAnulados.content || [];
          this.totalElements = resData.detalleAnulados.totalElements || 0;
          this.totalPages = resData.detalleAnulados.totalPages || 0;
          this.totalAnulado = resData.totalAnulado || 0;
        }
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
    this.filtros.fechaInicio = criteria.fechaDesde || this.getHoy();
    this.filtros.fechaFin = criteria.fechaHasta || this.getHoy();
    this.filtros.query = criteria.term || '';
    
    // Mantener la sucursal actual si no se provee otra
    if (!this.filtros.idSucursal) {
      const user = this.authService.currentUser();
      if (user && user.sucursalActualId) {
        this.filtros.idSucursal = user.sucursalActualId;
      }
    }

    this.paginaActual = 0;
    this.cargarReporte();
  }

  onPageChange(page: number): void {
    this.paginaActual = page;
    this.cargarReporte();
  }

  onPageSizeChange(size: number): void {
    this.tamanoPagina = size;
    this.paginaActual = 0;
    this.cargarReporte();
  }
}

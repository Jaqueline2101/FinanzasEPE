import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { OtrosPagosService } from '../../services/otros-pagos.service';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../auth/services/auth.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-rep-otros-pagos',
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
  providers: [DatePipe],
  templateUrl: './rep-otros-pagos.component.html'
})
export class RepOtrosPagosComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Reportes', route: '/admin/reportes' },
    { label: 'Otros Pagos' }
  ];

  columnas: Columna[] = [
    { field: 'fechaMovimiento', header: 'Fecha', tipo: 'date', align: 'center' },
    { field: 'beneficiario', header: 'Beneficiario', align: 'left' },
    { field: 'tipoPersona', header: 'Tipo', tipo: 'badge', align: 'center' },
    { field: 'dni', header: 'DNI', align: 'center' },
    { field: 'concepto', header: 'Concepto', align: 'left' },
    { field: 'metodoPago', header: 'Método', align: 'center' },
    { field: 'monto', header: 'Monto', tipo: 'currency', align: 'right' }
  ];

  datos: any[] = [];
  loading = false;
  totalRecords = 0;
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  searchOptions = [
    { label: 'Nombre', value: 'beneficiario' },
    { label: 'DNI', value: 'dni' }
  ];

  currentSucursalId = 1;

  constructor(
    private otrosPagosService: OtrosPagosService,
    private authService: AuthService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.currentSucursalId = user.sucursalActualId || 1;
    }
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading = true;
    const params = {
      idSucursal: this.currentSucursalId.toString(),
      page: this.currentPage.toString(),
      size: this.pageSize.toString()
    };

    this.otrosPagosService.obtenerOtrosPagos(params)
      .pipe(finalize(() => {
        this.loading = false;
        this.cd.detectChanges();
      }))
      .subscribe({
        next: (res) => {
          this.datos = (res.content || []).map((item: any) => ({
            ...item,
            // Aseguramos que la fechaMovimiento sea un objeto Date para que el pipe funcione
            fechaMovimiento: item.fechaMovimiento ? new Date(item.fechaMovimiento) : null
          }));
          this.totalRecords = res.totalElements || 0;
          this.totalPages = res.totalPages || 0;
          this.cd.detectChanges();
        },
        error: (err) => console.error('Error al cargar otros pagos:', err)
      });
  }

  handleFilter(criteria: FilterCriteria): void {
    // Para este reporte, el backend actualmente maneja paginación simple.
    // Si se implementan filtros de fecha en el backend, se añadirían aquí.
    this.currentPage = 0;
    this.cargarDatos();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.cargarDatos();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.cargarDatos();
  }

  exportarExcel(): void {
    if (this.datos.length === 0) {
      return;
    }

    const dataToExport = this.datos.map((item, index) => ({
      '#': index + 1,
      'FECHA': item.fechaMovimiento,
      'BENEFICIARIO': item.beneficiario,
      'TIPO': item.tipoPersona,
      'DNI': item.dni,
      'CONCEPTO': item.concepto,
      'MÉTODO': item.metodoPago,
      'MONTO': item.monto
    }));

    const worksheet = XLSX.utils.json_to_sheet(dataToExport);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Otros Pagos');

    const wscols = [
      { wch: 5 },  // #
      { wch: 15 }, // Fecha
      { wch: 30 }, // Beneficiario
      { wch: 15 }, // Tipo
      { wch: 15 }, // DNI
      { wch: 40 }, // Concepto
      { wch: 15 }, // Método
      { wch: 15 }  // Monto
    ];
    worksheet['!cols'] = wscols;

    XLSX.writeFile(workbook, `Reporte_Otros_Pagos_${new Date().getTime()}.xlsx`);
  }
}

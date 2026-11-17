import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RepEmpresasAtendidasService, EmpresaAtendida } from '../../services/rep-empresas-atendidas.service';
import { AlertasService } from '../../services/alertas.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';

@Component({
  selector: 'app-rep-empresas-atendidas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PageHeaderComponent,
    BreadcrumbComponent,
    BarraFiltrosComponent
  ],
  templateUrl: './rep-empresas-atendidas.component.html',
  styleUrls: ['./rep-empresas-atendidas.component.css']
})
export class RepEmpresasAtendidasComponent implements OnInit {
  private repService = inject(RepEmpresasAtendidasService);
  private alertasService = inject(AlertasService);
  private cd = inject(ChangeDetectorRef);

  empresas: EmpresaAtendida[] = [];
  empresasFiltradas: EmpresaAtendida[] = [];
  loading = false;

  fechaInicio = '';
  fechaFin = '';
  lastFechaInicio = '';
  lastFechaFin = '';
  searchTerm = '';

  searchOptions = [
    { label: 'Empresa', value: 'ALL' }
  ];

  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Reportes', route: '/admin/reportes' },
    { label: 'Empresas más atendidas', route: '/admin/reportes/empresas-atendidas' }
  ];

  ngOnInit(): void {
    this.fechaInicio = this.getDefaultInicio();
    this.fechaFin = this.getDefaultFin();
    setTimeout(() => {
      this.buscar();
    });
  }

  getDefaultInicio(): string {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    return this.formatDate(firstDay);
  }

  getDefaultFin(): string {
    return this.formatDate(new Date());
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  buscar(): void {
    if (!this.fechaInicio || !this.fechaFin) {
      this.alertasService.warning('Fechas requeridas', 'Por favor selecciona fecha de inicio y fin');
      return;
    }

    // Si las fechas son las mismas que ya cargamos, solo filtramos localmente
    if (this.fechaInicio === this.lastFechaInicio && this.fechaFin === this.lastFechaFin) {
      this.aplicarFiltroLocal();
      return;
    }

    this.loading = true;
    this.repService.getEmpresasMasAtendidas(this.fechaInicio, this.fechaFin).subscribe({
      next: (data) => {
        this.empresas = data;
        this.lastFechaInicio = this.fechaInicio;
        this.lastFechaFin = this.fechaFin;
        this.aplicarFiltroLocal();
        this.loading = false;
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar reporte:', err);
        this.alertasService.error('Error', 'No se pudo cargar el reporte de empresas más atendidas');
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  aplicarFiltroLocal(): void {
    if (!this.searchTerm.trim()) {
      this.empresasFiltradas = [...this.empresas];
    } else {
      const query = this.searchTerm.toLowerCase().trim();
      this.empresasFiltradas = this.empresas.filter(empresa => 
        (empresa.nombreCompania || '').toLowerCase().includes(query)
      );
    }
    this.cd.detectChanges();
  }

  onFilter(criteria: FilterCriteria): void {
    this.fechaInicio = criteria.fechaDesde || this.getDefaultInicio();
    this.fechaFin = criteria.fechaHasta || this.getDefaultFin();
    this.searchTerm = criteria.term || '';
    setTimeout(() => {
      this.buscar();
    });
  }
}

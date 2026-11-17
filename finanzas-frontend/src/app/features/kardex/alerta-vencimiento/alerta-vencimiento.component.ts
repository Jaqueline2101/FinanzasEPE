import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../../../services/inventory.service';
import { Producto } from '../../../models/producto.model';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';

@Component({
  selector: 'app-alerta-vencimiento',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TablaGeneralComponent,
    PaginationComponent
  ],
  providers: [DatePipe],
  templateUrl: './alerta-vencimiento.component.html',
  styleUrls: ['./alerta-vencimiento.component.css']
})
export class AlertaVencimientoComponent implements OnInit {
  @Output() countChanged = new EventEmitter<number>();
  
  isLoadingAlertas = false;
  alertas: any[] = [];
  diasFiltro: number = 90;

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  
  columnasAlertas: Columna[] = [
    { header: 'Código / Lote', field: 'codigo', tipo: 'title-subtitle', subField: ['registroInvimaLote'], align: 'left' },
    { header: 'Producto', field: 'nombreProducto', tipo: 'title-subtitle', subField: ['categoria'], align: 'left' },
    { header: 'Stock Actual', field: 'stockDisplay', tipo: 'badge', badgeConfig: { from: 'from-indigo-100', to: 'to-indigo-200', text: 'text-indigo-700', border: 'border-indigo-300' }, align: 'right' },
    { header: 'Vencimiento', field: 'fechaVencimiento', tipo: 'date', align: 'center' },
    { header: 'Estado', field: 'estadoLabel', tipo: 'badge' }
  ];

  constructor(
    private inventoryService: InventoryService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarAlertas();
  }

  cargarAlertas(): void {
    this.isLoadingAlertas = true;
    this.inventoryService.getAlertasVencimiento(this.diasFiltro).subscribe({
      next: (data: Producto[]) => {
        this.alertas = data.map(p => ({
            ...p,
            codigo: p.codigo || p.catalogo?.codigo || 'N/A',
            nombreProducto: p.nombreProducto || p.catalogo?.nombreServicio || 'N/A',
            categoria: p.categoria || p.catalogo?.tipoMedicamento || 'N/A',
            stockDisplay: `${p.stockReal} ${p.unidadMedida || p.catalogo?.presentacion || ''}`,
            estadoLabel: this.getStatusLabel(p.fechaVencimiento || ''),
            badgeConfig: this.getStatusBadgeConfig(p.fechaVencimiento || '')
        }));
        this.totalElements = this.alertas.length;
        this.isLoadingAlertas = false;
        this.countChanged.emit(this.alertas.length);
        this.cd.detectChanges();
      },
      error: (err: any) => {
        console.error('Error cargando alertas', err);
        this.isLoadingAlertas = false;
        this.cd.detectChanges();
      }
    });
  }

  getStatusClass(fechaVenc: string): string {
    const today = new Date();
    const venc = new Date(fechaVenc);
    const diffDays = Math.ceil((venc.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays <= 0) return 'status-expired';
    if (diffDays <= 30) return 'status-critical';
    if (diffDays <= 90) return 'status-warning';
    return 'status-safe';
  }

  getStatusLabel(fechaVenc: string): string {
    const today = new Date();
    const venc = new Date(fechaVenc);
    const diffDays = Math.ceil((venc.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays <= 0) return 'VENCIDO';
    if (diffDays <= 30) return 'CRÍTICO';
    if (diffDays <= 90) return 'PRÓXIMO';
    return 'SEGURO';
  }

  getStatusBadgeConfig(fechaVenc: string|undefined): any {
    if (!fechaVenc) return { from: 'from-slate-100', to: 'to-slate-200', text: 'text-slate-600' };
    const today = new Date();
    const venc = new Date(fechaVenc);
    const diffDays = Math.ceil((venc.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays <= 0) return { from: 'from-rose-100', to: 'to-rose-200', text: 'text-rose-700', border: 'border-rose-300' };
    if (diffDays <= 30) return { from: 'from-orange-100', to: 'to-orange-200', text: 'text-orange-700', border: 'border-orange-300' };
    if (diffDays <= 90) return { from: 'from-amber-100', to: 'to-amber-200', text: 'text-amber-700', border: 'border-amber-300' };
    return { from: 'from-emerald-100', to: 'to-emerald-200', text: 'text-emerald-700', border: 'border-emerald-300' };
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.cd.detectChanges();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.cd.detectChanges();
  }

  get paginatedAlertas(): any[] {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    return this.alertas.slice(start, end);
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / this.pageSize);
  }
}

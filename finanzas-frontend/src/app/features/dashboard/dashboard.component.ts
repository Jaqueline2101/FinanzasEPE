import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardResponse } from './dashboard.service';
import { NgApexchartsModule, ChartComponent, ApexAxisChartSeries, ApexChart, ApexXAxis, ApexDataLabels, ApexTooltip, ApexStroke, ApexYAxis, ApexTitleSubtitle, ApexFill, ApexLegend, ApexPlotOptions, ApexResponsive, ApexGrid } from 'ng-apexcharts';
import { FormsModule } from '@angular/forms';

export type ChartOptions = {
  series: ApexAxisChartSeries | any;
  chart: ApexChart | any;
  xaxis: ApexXAxis | any;
  stroke: ApexStroke | any;
  tooltip: ApexTooltip | any;
  dataLabels: ApexDataLabels | any;
  yaxis: ApexYAxis | any;
  fill: ApexFill | any;
  legend: ApexLegend | any;
  labels: string[] | any;
  plotOptions: ApexPlotOptions | any;
  responsive: ApexResponsive[] | any;
  title: ApexTitleSubtitle | any;
  grid: ApexGrid | any;
  colors: string[] | any;
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgApexchartsModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  @ViewChild('chart') chart!: ChartComponent;
  
  public salesChartOptions: Partial<ChartOptions> | any;
  public insuranceChartOptions: Partial<ChartOptions> | any;
  public valChartOptions: Partial<ChartOptions> | any;
  public discountChartOptions: Partial<ChartOptions> | any;
  public heatmapOptions: Partial<ChartOptions> | any;

  data: DashboardResponse | null = null;
  loading = true;
  selectedPeriod = 'Este Mes';
  salesChartType: 'area' | 'bar' | 'line' = 'area';

  constructor(
    private dashboardService: DashboardService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  changePeriod(period: string): void {
    this.selectedPeriod = period;
    this.loadData();
  }

  changeChartType(type: 'area' | 'bar' | 'line'): void {
    this.salesChartType = type;
    this.initCharts();
    this.cd.detectChanges();
  }

  loadData(): void {
    this.loading = true;
    this.data = null; // Limpiar datos previos para forzar re-renderizado
    this.cd.detectChanges(); 
    
    const sucursalId = this.dashboardService.getCurrentSucursalId();

    this.dashboardService.getResumen(sucursalId).subscribe({
      next: (res) => {
        if (res && res.data) {
          this.data = res.data;
          this.initCharts();
          
          // Forzar desactivación de loading y detección de cambios
          this.loading = false;
          this.cd.markForCheck();
          this.cd.detectChanges();

          // Un segundo ciclo por seguridad para ApexCharts
          setTimeout(() => {
            this.cd.detectChanges();
          }, 100);
        }
      },
      error: (err) => {
        console.error('Error loading dashboard', err);
        this.loading = false;
        this.cd.detectChanges();
      }
    });
  }

  initCharts(): void {
    if (!this.data) return;

    const g = this.data.graficos;

    // 1. Ventas vs Compras (Lineas)
    this.salesChartOptions = {
      series: [
        { name: 'Ventas', data: g.dataVentas.map(v => Math.round(v)) },
        { name: 'Compras', data: g.dataCompras.map(v => Math.round(v)) }
      ],
      chart: { height: 350, type: this.salesChartType, toolbar: { show: false }, zoom: { enabled: false }, fontFamily: 'Inter, sans-serif' },
      dataLabels: { enabled: false },
      stroke: { curve: 'smooth', width: 3 },
      xaxis: { categories: g.labelsMeses },
      yaxis: { labels: { formatter: (val: number) => 'S/ ' + val } },
      tooltip: { y: { formatter: (val: number) => 'S/ ' + val } },
      colors: ['#3b82f6', '#ef4444'],
      fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.45, opacityTo: 0.05, stops: [20, 100] } },
      grid: { borderColor: '#f1f5f9', strokeDashArray: 4 }
    };

    // 2. Top Aseguradoras (Barras Horizontales)
    this.insuranceChartOptions = {
      series: [{ name: 'Pacientes', data: Object.values(g.topAseguradoras) }],
      chart: { type: 'bar', height: 300, toolbar: { show: false } },
      plotOptions: { bar: { horizontal: true, borderRadius: 6, barHeight: '60%' } },
      dataLabels: { enabled: true, formatter: (val: number) => val + ' pac.' },
      xaxis: { categories: Object.keys(g.topAseguradoras) },
      colors: ['#0f172a'],
      grid: { borderColor: '#f1f5f9', strokeDashArray: 4 }
    };

    // 3. Valorización por Categoría (Donut)
    this.valChartOptions = {
      series: Object.values(g.valorizacionPorCategoria),
      chart: { type: 'donut', height: 320 },
      labels: Object.keys(g.valorizacionPorCategoria),
      colors: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'],
      legend: { position: 'bottom' },
      plotOptions: { pie: { donut: { size: '75%', labels: { show: true, total: { show: true, label: 'Total', formatter: () => 'S/ ' + this.data?.kpis.stockActual } } } } }
    };

    // 4. Descuentos (Radial Bar) - Datos Reales del Backend
    const d = g.descuentosPorTipo;
    this.discountChartOptions = {
      series: Object.values(d),
      chart: { height: 350, type: 'radialBar', fontFamily: 'Inter, sans-serif' },
      plotOptions: { 
        radialBar: { 
          hollow: { size: '40%' },
          dataLabels: { 
            name: { show: true, fontSize: '14px', color: '#64748b' }, 
            value: { show: true, fontSize: '20px', fontWeight: 700, color: '#0f172a' }, 
            total: { show: true, label: 'Promedio', formatter: () => 'Real' } 
          } 
        } 
      },
      labels: Object.keys(d),
      colors: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
    };

    // 5. Heatmap (Movimientos de Inventario Reales)
    this.heatmapOptions = {
      series: g.heatmapKardex,
      chart: { height: 350, type: 'heatmap', toolbar: { show: false } },
      dataLabels: { enabled: false },
      colors: ["#3b82f6"],
      xaxis: { type: 'category', categories: ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab', 'Dom'] },
      title: { text: 'Densidad de Movimientos (Semanal)', style: { fontSize: '14px', fontWeight: 'bold', color: '#64748b' } },
      grid: { padding: { right: 20 } }
    };
  }

  // Eliminamos generateHeatmapData ya que ahora es real desde el backend

  formatCurrency(val: number | undefined): string {
    if (val === undefined) return 'S/ 0.00';
    return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(val);
  }
}

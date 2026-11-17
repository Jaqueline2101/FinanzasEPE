import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DashboardKPIs {
  ventasTotales: number;
  comprasTotales: number;
  stockActual: number;
  gananciaNeta: number;
  comprobantesEmitidos: number;
  comprobantesAnulados: number;
  pacientesAtendidos: number;
  aseguradorasActivas: number;
  variacionVentas: number;
  variacionCompras: number;
  variacionStock: number;
  variacionGanancia: number;
}

export interface DashboardCharts {
  labelsMeses: string[];
  dataVentas: number[];
  dataCompras: number[];
  descuentosPorTipo: { [key: string]: number };
  anuladosPorMes: { [key: string]: number };
  usuariosAnuladores: { [key: string]: number };
  topAseguradoras: { [key: string]: number };
  valorizacionPorCategoria: { [key: string]: number };
  topProductosValorizados: { [key: string]: number };
  heatmapKardex: any[];
}

export interface VentaReciente {
  fecha: string;
  cliente: string;
  monto: number;
  estado: string;
}

export interface CompraReciente {
  fecha: string;
  proveedor: string;
  producto: string;
  total: number;
}

export interface DashboardResponse {
  kpis: DashboardKPIs;
  graficos: DashboardCharts;
  ventasRecientes: VentaReciente[];
  comprasRecientes: CompraReciente[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getResumen(idSucursal?: number): Observable<any> {
    let params = new HttpParams();
    if (idSucursal) {
      params = params.set('idSucursal', idSucursal.toString());
    }
    return this.http.get<any>(`${this.apiUrl}/resumen`, { params });
  }

  getCurrentSucursalId(): number | undefined {
    const sucursalId = localStorage.getItem('sucursalId');
    return sucursalId ? parseInt(sucursalId) : undefined;
  }
}

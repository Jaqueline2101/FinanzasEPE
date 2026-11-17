import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EmpresaAtendida {
  idCompania: number;
  nombreCompania: string;
  totalAtenciones: number;
  totalMonto: number;
}

@Injectable({
  providedIn: 'root'
})
export class RepEmpresasAtendidasService {
  private apiUrl = `${environment.apiUrl}/rep-empresas-atendidas`;
  private http = inject(HttpClient);

  getEmpresasMasAtendidas(fechaInicio: string, fechaFin: string): Observable<EmpresaAtendida[]> {
    const params = { fechaInicio, fechaFin };
    return this.http.get<EmpresaAtendida[]>(this.apiUrl, { params });
  }
}

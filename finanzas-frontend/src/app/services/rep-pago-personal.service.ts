import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardPagoPersonalDto } from '../models/rep-pago-personal.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RepPagoPersonalService {
  private apiUrl = `${environment.apiUrl}/reportes/pagos-personal`;

  constructor(private http: HttpClient) { }

  getDashboard(anio?: number): Observable<DashboardPagoPersonalDto> {
    const params: any = {};
    if (anio) params.anio = anio;
    return this.http.get<DashboardPagoPersonalDto>(`${this.apiUrl}/dashboard`, { params });
  }
}

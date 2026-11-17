import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, ValorizacionResponse } from '../models/valorizacion.model';

@Injectable({
  providedIn: 'root'
})
export class ValorizacionService {
  private readonly API_URL = `${environment.apiUrl}/reportes/valorizacion`;

  constructor(private http: HttpClient) { }

  getReporte(
    idSucursal: number,
    search?: string,
    categoria?: string,
    estadoStock?: string,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<ValorizacionResponse>> {
    let params = new HttpParams()
      .set('idSucursal', idSucursal.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);
    if (categoria) params = params.set('categoria', categoria);
    if (estadoStock) params = params.set('estadoStock', estadoStock);

    return this.http.get<ApiResponse<ValorizacionResponse>>(this.API_URL, { params });
  }
}

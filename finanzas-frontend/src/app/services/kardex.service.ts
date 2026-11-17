import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface KardexResponse {
  idArticuloKardex: string;
  idAlmArticulo: number;
  idCatalogo: number;
  idSucursal: number;
  idUsuario: number;
  numDoc: string;
  fecha: string;
  fechaVenc: string;
  nroLote: string;
  operacion: string;
  detalle: string;
  signo: string;
  cantidad: number;
  costoUnitario: number;
  costoTotal: number;
  saldoCantidad: number;
  saldoCostoUnitario: number;
  saldoCostoTotal: number;
  origenTipo: string;
  presentacion: string;
  catalogo?: { id: number; nombre: string };
  sucursal?: { idSucursal: number; nombreSucursal: string };
  usuario?: { id: number; nombre: string };
}

@Injectable({
  providedIn: 'root'
})
export class KardexService {
  private apiUrl = `${environment.apiUrl}/kardex`;

  constructor(private http: HttpClient) {}

  getHistorial(idCatalogo?: number, idAlmArticulo?: number): Observable<KardexResponse[]> {
    let params = new HttpParams();
    if (idCatalogo) params = params.set('idCatalogo', idCatalogo.toString());
    if (idAlmArticulo) params = params.set('idAlmArticulo', idAlmArticulo.toString());
    
    return this.http.get<KardexResponse[]>(this.apiUrl, { params });
  }

  getTodos(page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/todos`, { params });
  }
}

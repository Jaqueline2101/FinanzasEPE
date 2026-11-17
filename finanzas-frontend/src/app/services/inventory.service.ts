import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Producto } from '../models/producto.model';

export interface LoteSugerido {
  idProducto: number;
  nroLote: string;
  fechaVencimiento: string;
  cantidadATomar: number;
  stockRestanteEnLote: number;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private apiUrl = `${environment.apiUrl}/inventario`;

  constructor(private http: HttpClient) { }

  getSugerenciaFEFO(idCatalogo: number, cantidad: number): Observable<LoteSugerido[]> {
    return this.http.get<LoteSugerido[]>(`${this.apiUrl}/sugerir-fefo/${idCatalogo}`, {
      params: { cantidad: cantidad.toString() }
    });
  }

  getAlertasVencimiento(dias?: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/alertas-vencimiento`, {
      params: dias ? { dias: dias.toString() } : {}
    });
  }
}

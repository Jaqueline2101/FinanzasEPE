import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Nivel, NivelTree } from '../models/nivel.model';
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../shared/modals/response.model';



@Injectable({
  providedIn: 'root'
})
export class NivelService {
  private apiUrl = `${environment.apiUrl}/niveles`;

  constructor(private http: HttpClient) { }

  // CRUD Básico
  crear(nivel: Nivel): Observable<ApiResponse<Nivel>> {
    return this.http.post<ApiResponse<Nivel>>(this.apiUrl, nivel);
  }

  obtenerTodos(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Nivel>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<Nivel>>>(this.apiUrl, { params });
  }

  obtenerPorId(id: number): Observable<ApiResponse<Nivel>> {
    return this.http.get<ApiResponse<Nivel>>(`${this.apiUrl}/${id}`);
  }

  obtenerPorCodigo(codigo: string): Observable<ApiResponse<Nivel>> {
    return this.http.get<ApiResponse<Nivel>>(`${this.apiUrl}/codigo/${codigo}`);
  }

  actualizar(id: number, nivel: Nivel): Observable<ApiResponse<Nivel>> {
    return this.http.put<ApiResponse<Nivel>>(`${this.apiUrl}/${id}`, nivel);
  }

  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  // Jerarquía
  obtenerNivelesRaiz(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Nivel>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PageResponse<Nivel>>>(`${this.apiUrl}/raiz`, { params });
  }

  obtenerHijos(idNivelPadre: number): Observable<ApiResponse<Nivel[]>> {
    return this.http.get<ApiResponse<Nivel[]>>(`${this.apiUrl}/${idNivelPadre}/hijos`);
  }

  obtenerArbolCompleto(): Observable<ApiResponse<NivelTree[]>> {
    return this.http.get<ApiResponse<NivelTree[]>>(`${this.apiUrl}/arbol`);
  }

  obtenerArbolDesde(idNivel: number): Observable<ApiResponse<NivelTree[]>> {
    return this.http.get<ApiResponse<NivelTree[]>>(`${this.apiUrl}/${idNivel}/arbol`);
  }

  // Tipos
  obtenerTipos(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/tipos`);
  }

  // Operaciones especiales
  moverNivel(idNivel: number, idNivelPadreNuevo?: number, ordenNuevo?: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/mover`, {
      idNivel,
      idNivelPadreNuevo,
      ordenNuevo
    });
  }

  cambiarOrden(idNivel: number, nuevoOrden: number): Observable<ApiResponse<void>> {
    const params = new HttpParams().set('nuevoOrden', nuevoOrden.toString());
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${idNivel}/orden`, null, { params });
  }

  // Búsquedas
  buscar(filtros: any, page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<Nivel>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filtros.nombre) params = params.set('nombre', filtros.nombre);
    if (filtros.tipo) params = params.set('tipo', filtros.tipo);
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.idNivelPadre) params = params.set('idNivelPadre', filtros.idNivelPadre);

    return this.http.get<ApiResponse<PageResponse<Nivel>>>(`${this.apiUrl}/buscar`, { params });
  }

  obtenerEstadisticas(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/estadisticas`);
  }
}
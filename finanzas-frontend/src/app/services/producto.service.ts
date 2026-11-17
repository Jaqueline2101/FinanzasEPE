import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { Producto } from '../models/producto.model';
import { ApiResponse } from '../shared/modals/response.model';

@Injectable({
    providedIn: 'root'
})
export class ProductoService {
    private apiUrl = `${environment.apiUrl}/productos`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.apiUrl);
    }

    getBySucursal(idSucursal: number): Observable<Producto[]> {
        return this.http.get<Producto[]>(`${this.apiUrl}/sucursal/${idSucursal}`);
    }

    getById(id: number): Observable<Producto> {
        return this.http.get<Producto>(`${this.apiUrl}/${id}`);
    }

    create(producto: Producto): Observable<Producto> {
        return this.http.post<Producto>(this.apiUrl, producto);
    }

    update(id: number, producto: Producto): Observable<Producto> {
        return this.http.put<Producto>(`${this.apiUrl}/${id}`, producto);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getTipoTemperaturas(): Observable<any[]> {
        return this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/tipo-temperatura`).pipe(
            map(res => res.data || [])
        );
    }
}

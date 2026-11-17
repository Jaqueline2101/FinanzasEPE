import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Compra } from '../models/compra.model';
import { Page } from '../models/page.model'; // Assuming a generic Page model exists, if not I'll use any

@Injectable({
    providedIn: 'root'
})
export class CompraService {
    private apiUrl = `${environment.apiUrl}/compras`;

    constructor(private http: HttpClient) { }

    registrar(compra: Compra): Observable<Compra> {
        return this.http.post<Compra>(this.apiUrl, compra);
    }

    getById(id: number): Observable<Compra> {
        return this.http.get<Compra>(`${this.apiUrl}/${id}`);
    }

    listarPorSucursal(
        idSucursal: number,
        page: number = 0,
        size: number = 10,
        filters?: any,
        sort: string = 'fechaEmision',
        direction: string = 'desc'
    ): Observable<any> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sortBy', sort)
            .set('direction', direction);

        if (filters) {
            if (filters.term) params = params.set('term', filters.term);
            if (filters.searchType) params = params.set('searchType', filters.searchType);
            if (filters.estado && filters.estado !== 'TODOS') params = params.set('estado', filters.estado);
            if (filters.fechaDesde) params = params.set('fechaDesde', filters.fechaDesde);
            if (filters.fechaHasta) params = params.set('fechaHasta', filters.fechaHasta);
        }

        return this.http.get<any>(`${this.apiUrl}/sucursal/${idSucursal}`, { params });
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}

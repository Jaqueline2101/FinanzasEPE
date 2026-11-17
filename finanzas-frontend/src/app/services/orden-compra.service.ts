import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { OrdenCompra } from '../models/orden-compra.model';

@Injectable({
    providedIn: 'root'
})
export class OrdenCompraService {
    private apiUrl = `${environment.apiUrl}/ordenes-compra`;

    constructor(private http: HttpClient) { }

    listar(): Observable<OrdenCompra[]> {
        return this.http.get<OrdenCompra[]>(this.apiUrl);
    }

    obtenerPorId(id: number): Observable<OrdenCompra> {
        return this.http.get<OrdenCompra>(`${this.apiUrl}/${id}`);
    }

    registrar(orden: OrdenCompra): Observable<OrdenCompra> {
        return this.http.post<OrdenCompra>(this.apiUrl, orden);
    }

    actualizar(id: number, orden: OrdenCompra): Observable<OrdenCompra> {
        return this.http.put<OrdenCompra>(`${this.apiUrl}/${id}`, orden);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    aprobar(id: number, usuario: string): Observable<OrdenCompra> {
        return this.http.patch<OrdenCompra>(`${this.apiUrl}/${id}/aprobar?usuario=${usuario}`, {});
    }

    anular(id: number, usuario: string, motivo: string): Observable<OrdenCompra> {
        return this.http.patch<OrdenCompra>(`${this.apiUrl}/${id}/anular?usuario=${usuario}`, { motivo });
    }
}

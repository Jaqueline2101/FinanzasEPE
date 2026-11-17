import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Proveedor } from '../models/proveedor.model';

@Injectable({
    providedIn: 'root'
})
export class ProveedorService {
    private apiUrl = `${environment.apiUrl}/proveedores`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<Proveedor[]> {
        return this.http.get<Proveedor[]>(this.apiUrl);
    }

    buscarPorNombre(query: string): Observable<Proveedor[]> {
        return this.http.get<Proveedor[]>(`${this.apiUrl}/search?query=${query}`);
    }

    getById(id: number): Observable<Proveedor> {
        return this.http.get<Proveedor>(`${this.apiUrl}/${id}`);
    }

    create(proveedor: Proveedor): Observable<Proveedor> {
        return this.http.post<Proveedor>(this.apiUrl, proveedor);
    }

    update(id: number, proveedor: Proveedor): Observable<Proveedor> {
        return this.http.put<Proveedor>(`${this.apiUrl}/${id}`, proveedor);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    checkDoc(num: string, excludeId?: number): Observable<boolean> {
        let url = `${this.apiUrl}/check-doc?num=${num}`;
        if (excludeId) url += `&excludeId=${excludeId}`;
        return this.http.get<boolean>(url);
    }

    checkRazonSocial(valor: string, excludeId?: number): Observable<boolean> {
        let url = `${this.apiUrl}/check-razon-social?valor=${valor}`;
        if (excludeId) url += `&excludeId=${excludeId}`;
        return this.http.get<boolean>(url);
    }
}

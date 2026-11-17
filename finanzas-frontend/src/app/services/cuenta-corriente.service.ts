import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CuentaCorriente {
    idCuenta: number;
    nombreTitular: string;
    banco: string;
    numeroCuenta: string;
    cci?: string;
    moneda: string; // PEN, USD
    alias?: string;
    saldo: number;
    estado: string; // A, I
    createdAt?: string;
    updatedAt?: string;
}

@Injectable({
    providedIn: 'root'
})
export class CuentaCorrienteService {

    private apiUrl = `${environment.apiUrl}/cuentas-corrientes`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<CuentaCorriente[]> {
        return this.http.get<CuentaCorriente[]>(`${this.apiUrl}`);
    }

    getActive(): Observable<CuentaCorriente[]> {
        return this.http.get<CuentaCorriente[]>(`${this.apiUrl}/activas`);
    }

    getById(id: number): Observable<CuentaCorriente> {
        return this.http.get<CuentaCorriente>(`${this.apiUrl}/${id}`);
    }

    create(cuenta: CuentaCorriente): Observable<CuentaCorriente> {
        return this.http.post<CuentaCorriente>(`${this.apiUrl}`, cuenta);
    }

    update(id: number, cuenta: CuentaCorriente): Observable<CuentaCorriente> {
        return this.http.put<CuentaCorriente>(`${this.apiUrl}/${id}`, cuenta);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}

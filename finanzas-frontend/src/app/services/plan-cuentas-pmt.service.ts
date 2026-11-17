import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { PlanCuentasRegistro, HistorialMovimiento } from '../models/plan-cuentas-pmt.model';

@Injectable({
    providedIn: 'root'
})
export class PlanCuentasPmtService {
    private apiUrl = `${environment.apiUrl}/plan-cuentas`;

    constructor(private http: HttpClient) { }

    registrar(dto: PlanCuentasRegistro): Observable<PlanCuentasRegistro> {
        return this.http.post<PlanCuentasRegistro>(this.apiUrl, dto);
    }

    actualizar(id: number, dto: PlanCuentasRegistro): Observable<PlanCuentasRegistro> {
        // El backend maneja tanto registro como actualización vía POST en la raíz
        // ya que el método registrar en el service de Java hace un findById orElse new.
        return this.http.post<PlanCuentasRegistro>(this.apiUrl, dto);
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    obtenerPorId(id: number, tipo?: string): Observable<PlanCuentasRegistro> {
        const url = tipo ? `${this.apiUrl}/${id}?tipo=${tipo}` : `${this.apiUrl}/${id}`;
        return this.http.get<PlanCuentasRegistro>(url);
    }

    obtenerHistorial(id: number): Observable<HistorialMovimiento[]> {
        return this.http.get<HistorialMovimiento[]>(`${this.apiUrl}/${id}/historial`);
    }

    listar(): Observable<PlanCuentasRegistro[]> {
        return this.http.get<PlanCuentasRegistro[]>(this.apiUrl);
    }
}

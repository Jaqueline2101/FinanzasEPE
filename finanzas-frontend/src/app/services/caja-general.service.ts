import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CajaGeneralSaldoResponse {
    metodoPago: string;
    saldo: number;
}

export interface CajaGeneralResponse {
    id: number;
    nombre: string;
    idSucursal: number;
    saldoActual: number;
    saldosPorMetodo: CajaGeneralSaldoResponse[];
    fechaActualizacion: string;
}

export interface CajaGeneralMovimientoRequest {
    idSucursal: number;
    tipo: 'INGRESO' | 'EGRESO' | 'TRANSFERENCIA_ENTRE_METODOS' | 'TRANSFERENCIA_RECIBIDA';
    monto: number;
    descripcion: string;
    referencia: string;
    metodoPago: string;
    metodoPagoOrigen?: string;
    metodoPagoDestino?: string;
    usuario: string;
}

export interface CajaGeneralMovimientoResponse {
    id: number;
    tipo: 'INGRESO' | 'EGRESO' | 'TRANSFERENCIA_ENTRE_METODOS' | 'TRANSFERENCIA_RECIBIDA';
    monto: number;
    descripcion: string;
    referencia: string;
    metodoPago: string;
    fecha: string;
    usuario: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

@Injectable({
    providedIn: 'root'
})
export class CajaGeneralService {
    private apiUrl = `${environment.apiUrl}/caja-general`;
    private http = inject(HttpClient);

    obtenerPorSucursal(idSucursal: number): Observable<ApiResponse<CajaGeneralResponse>> {
        return this.http.get<ApiResponse<CajaGeneralResponse>>(`${this.apiUrl}/sucursal/${idSucursal}`);
    }

    registrarMovimiento(request: CajaGeneralMovimientoRequest): Observable<ApiResponse<CajaGeneralMovimientoResponse>> {
        return this.http.post<ApiResponse<CajaGeneralMovimientoResponse>>(`${this.apiUrl}/movimiento`, request);
    }

    listarMovimientos(idSucursal: number, metodoPago?: string, page: number = 0, size: number = 20): Observable<ApiResponse<any>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
            
        if (metodoPago) {
            params = params.set('metodoPago', metodoPago);
        }

        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/sucursal/${idSucursal}/movimientos`, { params });
    }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CajaChica {
    id: number;
    nombre: string;
    saldoActual: number;
    estado: string;
    saldoInicial: number;
    saldoCierreReal: number;
    idPuntoVenta: number;
    idUsuarioCajero: string;
    fechaCierre: string;
    createdAt: string;
}

export interface CajaChicaRequest {
    nombre: string;
    idSucursal: number;
    idPuntoVenta: number;
    saldoInicial: number;
}

export interface CajaChicaMovimientoRequest {
    cajaChicaId: number | null;
    tipo: 'INGRESO' | 'EGRESO';
    monto: number;
    descripcion: string;
    referencia: string;
    metodoPago: string;
}

export interface CajaChicaMovimientoResponse {
    id: number;
    tipo: 'INGRESO' | 'EGRESO';
    monto: number;
    descripcion: string;
    referencia: string;
    metodoPago: string;
    fecha: string;
    usuario: string;
}

export interface CajaResumenDTO {
    cajaId: number;
    nombre: string;
    saldoInicial: number;
    ingresos: { metodoPago: string, monto: number }[];
    egresos: { metodoPago: string, monto: number }[];
    totalIngresos: number;
    totalEgresos: number;
    saldoFinalTeorico: number;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

@Injectable({
    providedIn: 'root'
})
export class CajaChicaService {
    private apiUrl = `${environment.apiUrl}/caja-chica`;
    private http = inject(HttpClient);

    obtenerCajaAbierta(): Observable<ApiResponse<CajaChica>> {
        return this.http.get<ApiResponse<CajaChica>>(`${this.apiUrl}/abierta`);
    }

    crearCaja(request: CajaChicaRequest): Observable<ApiResponse<CajaChica>> {
        return this.http.post<ApiResponse<CajaChica>>(`${this.apiUrl}/cajas`, request);
    }

    registrarMovimiento(request: CajaChicaMovimientoRequest): Observable<ApiResponse<CajaChicaMovimientoResponse>> {
        return this.http.post<ApiResponse<CajaChicaMovimientoResponse>>(`${this.apiUrl}/movimientos`, request);
    }

    listarMovimientos(cajaId: number, page: number = 0, size: number = 20): Observable<ApiResponse<any>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', 'fecha,desc');

        return this.http.get<ApiResponse<any>>(`${this.apiUrl}/movimientos/${cajaId}`, { params });
    }

    obtenerResumen(cajaId: number): Observable<ApiResponse<CajaResumenDTO>> {
        return this.http.get<ApiResponse<CajaResumenDTO>>(`${this.apiUrl}/resumen/${cajaId}`);
    }

    cerrarCaja(cajaId: number, saldoCierreReal: number, transferir: boolean): Observable<ApiResponse<CajaChica>> {
        const params = new HttpParams()
            .set('saldoCierreReal', saldoCierreReal.toString())
            .set('transferirACajaGeneral', transferir.toString());

        return this.http.put<ApiResponse<CajaChica>>(`${this.apiUrl}/cajas/${cajaId}/cerrar`, null, { params });
    }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MovimientoFinanciero, BalanceDTO, MovimientoDTO, TransferenciaDTO } from '../models/finanza.model';

@Injectable({
    providedIn: 'root'
})
export class FinanzasService {
    private apiUrl = `${environment.apiUrl}/finanzas`;
    private http = inject(HttpClient);

    constructor() { }

    registrarMovimiento(dto: MovimientoDTO): Observable<MovimientoFinanciero> {
        return this.http.post<MovimientoFinanciero>(`${this.apiUrl}/movimiento`, dto);
    }

    listarMovimientos(idSucursal: number, page: number = 0, size: number = 20, inicio?: string, fin?: string, idCuenta?: number, idCuentaExcluir?: number, idUsuario?: number): Observable<any> {
        let params = new HttpParams()
            .set('idSucursal', idSucursal.toString())
            .set('page', page.toString())
            .set('size', size.toString());

        if (inicio) params = params.set('inicio', inicio);
        if (fin) params = params.set('fin', fin);
        if (idCuenta) params = params.set('idCuenta', idCuenta.toString());
        if (idCuentaExcluir) params = params.set('idCuentaExcluir', idCuentaExcluir.toString());
        if (idUsuario) params = params.set('idUsuario', idUsuario.toString());

        return this.http.get<any>(`${this.apiUrl}/movimientos`, { params });
    }

    obtenerBalance(idSucursal: number, inicio?: string, fin?: string, idCuenta?: number, idUsuario?: number): Observable<BalanceDTO> {
        let params = new HttpParams().set('idSucursal', idSucursal.toString());

        if (inicio) params = params.set('inicio', inicio);
        if (fin) params = params.set('fin', fin);
        if (idCuenta) params = params.set('idCuenta', idCuenta.toString());
        if (idUsuario) params = params.set('idUsuario', idUsuario.toString());

        return this.http.get<BalanceDTO>(`${this.apiUrl}/balance`, { params });
    }

    anularMovimiento(id: number, idUsuario: number, motivo: string): Observable<void> {
        const params = new HttpParams()
            .set('idUsuario', idUsuario.toString())
            .set('motivo', motivo);

        return this.http.delete<void>(`${this.apiUrl}/movimiento/${id}`, { params });
    }

    realizarTransferencia(dto: TransferenciaDTO): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/transferencia`, dto);
    }
}

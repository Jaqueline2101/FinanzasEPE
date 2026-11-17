import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ArqueoCajaDTO {
    id?: number;
    fechaHora?: string;
    idUsuario?: number;
    nombreUsuario?: string;
    idSucursal?: number;
    nombreSucursal?: string;
    idPunto?: number;
    nombrePunto?: string;
    saldoEsperado: number;
    saldoFisico: number;
    diferencia: number;
    observaciones?: string;
    estado?: string;
}

@Injectable({
    providedIn: 'root'
})
export class ArqueoCajaService {
    private apiUrl = `${environment.apiUrl}/arqueos`;
    private http = inject(HttpClient);

    obtenerSaldoEsperado(): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/saldo-esperado`);
    }

    registrarArqueo(dto: ArqueoCajaDTO): Observable<ArqueoCajaDTO> {
        return this.http.post<ArqueoCajaDTO>(`${this.apiUrl}/registrar`, dto);
    }

    listarArqueos(inicio?: string, fin?: string, page: number = 0, size: number = 20): Observable<any> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (inicio) params = params.set('inicio', inicio);
        if (fin) params = params.set('fin', fin);

        return this.http.get<any>(`${this.apiUrl}/historial`, { params });
    }
}

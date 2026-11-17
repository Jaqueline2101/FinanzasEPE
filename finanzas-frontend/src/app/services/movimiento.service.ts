import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Movimiento } from '../models/movimiento.model';

@Injectable({
    providedIn: 'root'
})
export class MovimientoService {

    private apiUrl = `${environment.apiUrl}/finanzas`;

    constructor(private http: HttpClient) { }

    create(movimiento: Partial<Movimiento>, idCuenta: number | null, idUsuario: number = 1, idSucursal: number = 1): Observable<any> {
        const payload = {
            tipo: movimiento.tipoMovimiento,
            monto: movimiento.monto,
            concepto: movimiento.descripcion,
            idUsuario: idUsuario,
            idSucursal: idSucursal,
            idCuenta: idCuenta,
            fechaMovimiento: movimiento.fechaEmision ? `${movimiento.fechaEmision}T${new Date().toTimeString().split(' ')[0]}` : new Date().toISOString(),
            origen: 'MANUAL',
            metodoPago: 'EFECTIVO',
            tipoComprobante: movimiento.tipoComprobante,
            numeroComprobante: movimiento.numeroComprobante,
            codigoContable: movimiento.codigoContable,
            beneficiario: movimiento.beneficiario
        };
        return this.http.post<any>(`${this.apiUrl}/movimiento`, payload);
    }

    getNextCorrelative(idSucursal: number, tipo: string): Observable<string> {
        return this.http.get(`${this.apiUrl}/correlativo/siguiente`, {
            params: { idSucursal: idSucursal.toString(), tipo: tipo },
            responseType: 'text'
        });
    }

    getAll(): Observable<Movimiento[]> {
        return new Observable(observer => {
            observer.next([]);
            observer.complete();
        });
    }

    delete(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/movimiento/${id}`, {
            params: {
                idUsuario: '1',
                motivo: 'Eliminado desde registro'
            }
        });
    }
}

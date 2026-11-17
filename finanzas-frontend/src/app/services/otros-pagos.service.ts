import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface OtroPago {
    id?: number;
    fecha: string;
    beneficiario: string;
    tipoPersona: 'TRABAJADOR' | 'MEDICO' | 'EXTERNO';
    dni?: string;
    monto: number;
    metodoPago: 'EFECTIVO' | 'TRANSFERENCIA' | 'YAPE';
    concepto: string;
    idCuenta?: number;
    idUsuario?: number;
    idSucursal?: number;
    // Extended fields
    telefono?: string;
    email?: string;
    nombreCampana?: string;
    esAnonimo?: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class OtrosPagosService {
    private apiUrl = `${environment.apiUrl}/otros-pagos`;

    constructor(private http: HttpClient) { }

    registrarOtroPago(pago: OtroPago): Observable<any> {
        return this.http.post(`${environment.apiUrl}/otros-pagos`, pago);
    }

    obtenerOtrosPagos(params: any): Observable<any> {
        return this.http.get<any>(`${environment.apiUrl}/otros-pagos`, { params });
    }

    actualizarOtroPago(id: number, pago: OtroPago): Observable<any> {
        return this.http.put(`${environment.apiUrl}/otros-pagos/${id}`, pago);
    }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EmpleadoDTO {
    id?: number;
    nombre: string;
    apepat: string;
    apemat: string;
    numdoc: string;
    ruc?: string;
    direcc?: string;
    fono1?: string;
    email?: string;
    nacfec?: string;
    sexo?: string;
    estadoCivil?: string;
    idOcupacion?: number;
    nombreOcupacion?: string;
    fechaIngreso?: string;
    centroLabor?: string;
    modalidadTrabajo?: string;
    tipoContrato?: string;
    fechaFin?: string;
    renovacion?: boolean;
    sueldoBase?: number;
    estado?: boolean;
    cantidadFamiliares?: number;
    roles?: string[];
}

@Injectable({
    providedIn: 'root'
})
export class EmpleadoService {
    private apiUrl = `${environment.apiUrl}/rrhh/empleados`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<EmpleadoDTO[]> {
        return this.http.get<EmpleadoDTO[]>(this.apiUrl);
    }

    getById(id: number): Observable<EmpleadoDTO> {
        return this.http.get<EmpleadoDTO>(`${this.apiUrl}/${id}`);
    }

    getByDni(dni: string): Observable<EmpleadoDTO> {
        return this.http.get<EmpleadoDTO>(`${this.apiUrl}/busqueda/${dni}`);
    }

    create(empleado: EmpleadoDTO): Observable<EmpleadoDTO> {
        return this.http.post<EmpleadoDTO>(this.apiUrl, empleado);
    }

    update(id: number, empleado: EmpleadoDTO): Observable<EmpleadoDTO> {
        return this.http.put<EmpleadoDTO>(`${this.apiUrl}/${id}`, empleado);
    }

    updateState(id: number, activo: boolean): Observable<void> {
        return this.http.patch<void>(`${this.apiUrl}/${id}/estado?activo=${activo}`, {});
    }
}

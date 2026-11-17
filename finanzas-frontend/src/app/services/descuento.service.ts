import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class DescuentoService {
    private apiUrl = `${environment.apiUrl}/tipos-paciente`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    /**
     * Obtener configuración completa de descuentos familiares
     * Retorna el objeto TipoPaciente completo con su lista de servicios
     */
    obtenerConfiguracionFamiliar(): Observable<any> {
        return this.http.get<any>(this.apiUrl, { headers: this.getHeaders() })
            .pipe(
                map(response => {
                    const tipos = response.data || [];
                    const familiar = tipos.find((t: any) => t.codigo === 'PERSONAL_REUMA');
                    // Return the raw object or null if not found, to let component handle creation if needed
                    return familiar || null;
                })
            );
    }

    /**
     * Guardar configuración completa de descuentos familiares
     * Recibe el objeto con la lista de servicios actualizada
     */
    guardarConfiguracionFamiliar(data: any): Observable<any> {
        // If it has an ID, it's an update
        if (data.idTipoPaciente) {
            // Strip ID from body as it is not in request DTO
            const { idTipoPaciente, ...body } = data;
            return this.http.put<any>(`${this.apiUrl}/${data.idTipoPaciente}`, body, { headers: this.getHeaders() });
        } else {
            // Create new structure if it doesn't exist
            const request = {
                // Spread data first
                ...data,
                // Override with defaults if missing (or undefined in data)
                codigo: data.codigo || 'PERSONAL_REUMA',
                nombre: data.nombre || 'Personal de Reumacenter',
                estado: data.estado || '1'
            };
            return this.http.post<any>(this.apiUrl, request, { headers: this.getHeaders() });
        }
    }

    /**
     * Aplicar descuento masivo a múltiples servicios
     */
    aplicarDescuentoMasivo(idTipoPaciente: number, request: any): Observable<any> {
        return this.http.post<any>(
            `${this.apiUrl}/${idTipoPaciente}/descuentos/masivo`,
            request,
            { headers: this.getHeaders() }
        );
    }

    /**
     * Aplicar descuentos específicos en lote
     */
    aplicarDescuentoBatch(idTipoPaciente: number, request: any[]): Observable<any> {
        return this.http.post<any>(
            `${this.apiUrl}/${idTipoPaciente}/descuentos/batch`,
            request,
            { headers: this.getHeaders() }
        );
    }

    /**
     * Obtener servicios con descuentos especiales para venta-servicios
     * Usado para mostrar precios con descuento en el módulo de ventas
     */
    getServiciosConDescuentosEspeciales(idTipoPaciente: number): Observable<any[]> {
        return this.http.get<any>(
            `${this.apiUrl}/${idTipoPaciente}/servicios-con-descuento`,
            { headers: this.getHeaders() }
        ).pipe(
            map(response => response.data || [])
        );
    }

    private getDescuentoPorTipo(servicios: any[], tipo: string): number {
        const servicio = servicios.find(s => s.tipoServicio === tipo);
        return servicio ? Number(servicio.descuento) : 0;
    }
}

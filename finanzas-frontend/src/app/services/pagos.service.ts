import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CronogramaPago } from '../models/cronograma-pago.model';
import { Page } from '../models/page.model';

@Injectable({
    providedIn: 'root'
})
export class PagosService {
    private apiUrl = `${environment.apiUrl}/cronograma-pagos`;

    constructor(private http: HttpClient) { }

    listarDeudas(params: any = {}): Observable<Page<CronogramaPago>> {
        let httpParams = new HttpParams();
        Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined) {
                httpParams = httpParams.append(key, params[key]);
            }
        });
        return this.http.get<Page<CronogramaPago>>(this.apiUrl, { params: httpParams });
    }

    registrarPago(id: number, numeroOperacion: string, voucher?: File): Observable<CronogramaPago> {
        const formData = new FormData();
        formData.append('numeroOperacion', numeroOperacion);
        if (voucher) {
            formData.append('voucher', voucher);
        }
        return this.http.post<CronogramaPago>(`${this.apiUrl}/${id}/pagar`, formData);
    }

    registrarPagosMasivos(ids: number[], numeroOperacion: string, voucher?: File): Observable<CronogramaPago[]> {
        const formData = new FormData();
        formData.append('ids', ids.join(','));
        formData.append('numeroOperacion', numeroOperacion);
        if (voucher) {
            formData.append('voucher', voucher);
        }
        return this.http.post<CronogramaPago[]>(`${this.apiUrl}/pagar-masivo`, formData);
    }
}

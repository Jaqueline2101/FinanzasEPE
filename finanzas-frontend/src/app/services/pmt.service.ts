import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { PmtMovimiento, PmtResumen } from '../models/pmt.model';

@Injectable({
    providedIn: 'root'
})
export class PmtService {
    private apiUrl = `${environment.apiUrl}/pmt`;

    constructor(private http: HttpClient) { }

    getTitulares(): Observable<PmtResumen[]> {
        return this.http.get<PmtResumen[]>(`${this.apiUrl}/titulares`);
    }

    getLibroAuxiliar(titular: string): Observable<PmtMovimiento[]> {
        return this.http.get<PmtMovimiento[]>(`${this.apiUrl}/libro-auxiliar?titular=${encodeURIComponent(titular)}`);
    }
}

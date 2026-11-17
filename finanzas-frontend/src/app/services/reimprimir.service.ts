import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../shared/modals/response.model';
import { DocumentoReimpresion, ReimpresionFilter } from '../models/reimprimir.model';

@Injectable({
    providedIn: 'root'
})
export class ReimpresionService {
    private readonly API_URL = `${environment.apiUrl}/reimpresiones`;

    constructor(private http: HttpClient) { }

    buscarDocumentos(filtros: ReimpresionFilter): Observable<ApiResponse<DocumentoReimpresion[]>> {
        return this.http.post<ApiResponse<DocumentoReimpresion[]>>(`${this.API_URL}/buscar`, filtros);
    }
}

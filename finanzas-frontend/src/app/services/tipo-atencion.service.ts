import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class TipoAtencionService {
    private apiUrl = `${environment.apiUrl}/tipos-atencion`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    listar(): Observable<any[]> {
        return this.http.get<any>(this.apiUrl, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    guardar(tipo: any): Observable<any> {
        return this.http.post<any>(this.apiUrl, tipo, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    actualizar(id: number, tipo: any): Observable<any> {
        return this.http.put<any>(`${this.apiUrl}/${id}`, tipo, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }

    eliminar(id: number): Observable<any> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() })
            .pipe(map(response => response.data));
    }
}

import { Injectable, signal, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import SockJS from 'sockjs-client';
import { Client, Stomp } from '@stomp/stompjs';

export interface AppNotification {
    userId?: string;
    modulo: string;
    titulo: string;
    contenido: string;
    tipo: 'SUCCESS' | 'ERROR' | 'WARNING' | 'INFO';
}

@Injectable({
    providedIn: 'root'
})
export class NotificationSocketService implements OnDestroy {
    private stompClient: any;
    private notificationSubject = new Subject<AppNotification>();
    
    // Observable para que el Header o cualquier componente se suscriba
    notifications$ = this.notificationSubject.asObservable();
    
    // Estado de conexión
    connected = signal<boolean>(false);

    constructor() {
        this.initializeWebSocketConnection();
    }

    ngOnDestroy() {
        this.disconnect();
    }

    private initializeWebSocketConnection() {
        this.connect();
    }

    // Método para ser llamado desde el componente para iniciar la escucha
    connect() {
        const wsUrl = `${environment.apiUrl}/ws-notifications`;
        const socket = new SockJS(wsUrl);
        this.stompClient = Stomp.over(socket);
        
        console.log('Iniciando conexión WebSocket a:', wsUrl);

        this.stompClient.connect({}, (frame: any) => {
            this.connected.set(true);
            console.log('Conectado a WebSocket: ' + frame);
            
            // Suscribirse al tópico global
            this.stompClient.subscribe('/topic/notifications', (message: any) => {
                if (message.body) {
                    const notification: AppNotification = JSON.parse(message.body);
                    this.notificationSubject.next(notification);
                }
            });
        }, (error: any) => {
            console.error('Error en WebSocket:', error);
            this.connected.set(false);
            // Reintento de conexión tras 5 segundos
            setTimeout(() => this.connect(), 5000);
        });
    }

    disconnect() {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.disconnect();
        }
    }
}

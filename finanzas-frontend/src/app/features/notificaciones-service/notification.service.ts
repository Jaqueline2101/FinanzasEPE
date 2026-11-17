import { Injectable, OnDestroy } from '@angular/core';
import { Client, Message, Stomp } from '@stomp/stompjs';
// Eliminada importación de sockjs-client para usar WebSockets puros
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import { NgZone } from '@angular/core';

export interface NotificationPayload {
  id?: string;
  userId?: string;
  modulo: string;
  titulo: string;
  contenido: any;
  tipo: 'info' | 'success' | 'warning' | 'error';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService implements OnDestroy {
  private stompClient: Client | null = null;
  
  private notificationsSubject = new BehaviorSubject<NotificationPayload[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();
  
  private activeNotifications: NotificationPayload[] = [];
  
  // URL del microservicio de notificaciones usando WebSockets Nativos (ws:// en lugar de http://)
  private readonly brokerUrl = 'ws://localhost:8086/ws-notifications-native';

  constructor(private router: Router, private ngZone: NgZone) {
    // Usamos setTimeout para no bloquear el hilo principal de la aplicación durante el inicio
    setTimeout(() => {
      this.connect();
    }, 1000);
  }

  private connect() {
    this.stompClient = new Client({
      brokerURL: this.brokerUrl,
      debug: (msg: string) => console.log('STOMP DEBUG:', msg),
      reconnectDelay: 5000,
      onConnect: (frame) => {
        console.log('✅ Conectado al servicio de notificaciones');
        
        // Suscribirse al canal global
        this.stompClient?.subscribe('/topic/notifications', (message: Message) => {
          this.handleNotification(JSON.parse(message.body));
        });

        // Suscribirse al canal privado del usuario (si hay ID)
        const userId = localStorage.getItem('userId');
        if (userId) {
          this.stompClient?.subscribe(`/user/${userId}/queue/notifications`, (message: Message) => {
            this.handleNotification(JSON.parse(message.body));
          });
        }
      },
      onStompError: (frame) => {
        console.error('❌ Error de STOMP:', frame.headers['message']);
        console.error('Detalles:', frame.body);
      }
    });

    this.stompClient.activate();
  }

  private handleNotification(payload: NotificationPayload) {
    console.log('🔔 Nueva notificación recibida:', payload);
    
    // Generar un ID si no viene
    if (!payload.id) payload.id = Math.random().toString(36).substr(2, 9);

    this.activeNotifications = [payload, ...this.activeNotifications];
    this.notificationsSubject.next(this.activeNotifications);

    // Auto-remove after 2 seconds for success/error notifications (except for PLANES and PAQUETES)
    if ((payload.tipo === 'success' || payload.tipo === 'error') && payload.modulo !== 'PLANES' && payload.modulo !== 'PAQUETES') {
        const idToRemove = payload.id;
        setTimeout(() => {
            this.removeNotification(idToRemove);
        }, 2000);
    }

    // Mostrar alerta visual inmediata usando SweetAlert2
    this.showToast(payload);
  }

  public removeNotification(id: string) {
    this.activeNotifications = this.activeNotifications.filter(n => n.id !== id);
    this.notificationsSubject.next(this.activeNotifications);
  }

  public removeNotificationByContentId(modulo: string, contentId: number) {
    this.activeNotifications = this.activeNotifications.filter(n => {
      if (n.modulo === modulo && typeof n.contenido === 'object' && n.contenido !== null) {
        if (modulo === 'AUTORIZACION_DESCUENTO' && n.contenido.idAutorizacion === contentId) {
          return false;
        }
      }
      return true;
    });
    this.notificationsSubject.next(this.activeNotifications);
  }

  public clearNotifications() {
    this.activeNotifications = [];
    this.notificationsSubject.next(this.activeNotifications);
  }

  private showToast(payload: NotificationPayload) {
    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: (payload.tipo === 'warning' || payload.modulo === 'PLANES' || payload.modulo === 'PAQUETES') ? undefined : 5000,
      timerProgressBar: (payload.tipo === 'warning' || payload.modulo === 'PLANES' || payload.modulo === 'PAQUETES') ? false : true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
        
        // Agregar evento click para redireccionar a la pantalla correspondiente
        toast.style.cursor = 'pointer';
        toast.addEventListener('click', () => {
          if (payload.modulo === 'AUTORIZACION_DESCUENTO') {
            this.ngZone.run(() => {
              const id = typeof payload.contenido === 'object' && payload.contenido !== null 
                  ? payload.contenido.idAutorizacion : null;
              
              if (id) {
                this.router.navigate(['/admin/configuraciones/autorizar-descuentos'], { queryParams: { openId: id } });
              } else {
                this.router.navigate(['/admin/configuraciones/autorizar-descuentos']);
              }
            });
            Swal.close();
          }
        });
      }
    });

    let textoMostrar = payload.modulo;
    if (typeof payload.contenido === 'string') {
        textoMostrar = payload.contenido;
    } else if (typeof payload.contenido === 'object' && payload.contenido !== null) {
        if (payload.modulo === 'AUTORIZACION_DESCUENTO' && payload.contenido.cajeroNombre) {
            textoMostrar = `El cajero ${payload.contenido.cajeroNombre} solicita S/ ${payload.contenido.montoDescuento} para ${payload.contenido.pacienteNombre}`;
        } else {
            textoMostrar = 'Nueva notificación recibida';
        }
    }

    Toast.fire({
      icon: payload.tipo,
      title: payload.titulo,
      text: textoMostrar
    });
  }

  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}

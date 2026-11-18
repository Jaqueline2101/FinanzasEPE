import { Component, Input, OnInit, OnDestroy, Output, EventEmitter, ChangeDetectorRef, ChangeDetectionStrategy, inject, effect, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { interval, Subscription, fromEvent, merge } from 'rxjs';
import { throttleTime, filter } from 'rxjs/operators';

import { HttpClientModule } from '@angular/common/http';
import { AuthService, TokenInfo } from '../../features/auth/services/auth.service';
import { SidebarService } from '../sidebar/sidebar.service';
import { ConfiguracionService } from '../../core/services/configuracion.service';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../services/theme.service';  
import { NotificationService, NotificationPayload } from '../../features/notificaciones-service/notification.service';


@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  // ✅ CRÍTICO: REMOVER AuthService de aquí para usar el singleton global
  // providers: [AuthService],  ❌ ESTO CAUSABA EL PROBLEMA
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() breadcrumb: string = 'Inicio';
  @Output() onCerrarSesion = new EventEmitter<void>();
  themeService = inject(ThemeService);  // Inyectar
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }


  tiempoRestanteMs: number = 0;
  tiempoFormateado: string = '30:00';

  private timerSubscription: Subscription | undefined;
  private syncSubscription: Subscription | undefined;

  usuarioActual: string = 'Usuario';
  primerNombre: string = 'Usuario';
  userGender: string = 'null';
  bien: string = '';
  sucursalNombre: string = '';
  puntoNombre: string = '';
  menuUsuarioAbierto: boolean = false;

  isAdmin: boolean = false;
  showConfigConfig: boolean = false;
  isLoadingConfig: boolean = false;

  private readonly UMBRAL_ADVERTENCIA = 300000;
  private advertenciaMostrada = false;

  // Notificaciones en tiempo real
  notificationService = inject(NotificationService);
  notificacionesActivas: NotificationPayload[] = [];
  private notificationSub?: Subscription;

  constructor(
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    public sidebarService: SidebarService,
    private configuracionService: ConfiguracionService,
    private activatedRoute: ActivatedRoute
  ) {
    this.cargarSucursalInicial();

    // ✅ EFFECT: Reaccionar a cambios en la sucursal (Signal)
    effect(() => {
      const nuevaSucursal = this.authService.currentSucursal();
      if (nuevaSucursal && nuevaSucursal !== this.sucursalNombre) {
        console.log('⚡ [HEADER] Actualizando sucursal por Signal:', nuevaSucursal);
        this.sucursalNombre = nuevaSucursal;

        // Guardar en LS por si acaso
        const userId = localStorage.getItem('userId');
        if (userId) {
          localStorage.setItem(`sucursalActual_${userId}`, nuevaSucursal);
        }

        this.cdr.detectChanges();

        // Compatibilidad con CambioSucursalComponent
        setTimeout(() => {
          this.authService.confirmSucursalUpdated();
        }, 0);
      }
    });

    // ✅ EFFECT: Reaccionar a cambios en el Punto de Venta (Signal)
    effect(() => {
      const nuevoPunto = this.authService.currentPunto();
      if (nuevoPunto !== this.puntoNombre) {
        console.log('⚡ [HEADER] Actualizando punto por Signal:', nuevoPunto);
        this.puntoNombre = nuevoPunto || '';
        this.cdr.detectChanges();
      }
    });
  }

  private activitySubscription: Subscription | undefined;
  private sucursalChangeSubscription: Subscription | undefined;
  private routerSubscription: Subscription | undefined;

  private cargarSucursalInicial(): void {
    const sucursalDelToken = this.authService.getSucursalFromToken();
    const puntoDelToken = this.authService.getPuntoNombreFromToken();

    // ✅ Leer de localStorage con clave específica por usuario
    const userId = localStorage.getItem('userId');
    const sucursalLocalStorage = userId
      ? localStorage.getItem(`sucursalActual_${userId}`)
      : null;
    const puntoLocalStorage = userId
      ? localStorage.getItem(`puntoActual_${userId}`)
      : null;

    this.sucursalNombre = sucursalDelToken || sucursalLocalStorage || '';
    this.puntoNombre = puntoDelToken || puntoLocalStorage || '';
    console.log('🏗️ [HEADER CONSTRUCTOR] Sucursal inicial:', this.sucursalNombre, 'Punto:', this.puntoNombre);
  }

  ngOnInit() {
    this.iniciarTemporizador();
    this.cargarPerfil();
    this.setupActivityDetection();
    this.setupSucursalChangeListener();
    this.setupRouterListener();

    this.cdr.detectChanges();
    this.setupNotificationListener();
  }

  private setupNotificationListener(): void {
    this.notificationSub = this.notificationService.notifications$.subscribe(list => {
      this.notificacionesActivas = list;
      this.cdr.markForCheck();
      this.cdr.detectChanges();
    });
  }

  onNotificationClick(notif: NotificationPayload): void {
    if (notif.modulo === 'AUTORIZACION_DESCUENTO') {
      const id = typeof notif.contenido === 'object' && notif.contenido !== null 
          ? notif.contenido.idAutorizacion : null;
      
      if (id) {
        this.router.navigate(['/admin/configuraciones/autorizar-descuentos'], { queryParams: { openId: id } });
      } else {
        this.router.navigate(['/admin/configuraciones/autorizar-descuentos']);
      }
    }
    
    // Remover la notificación al hacer clic
    if (notif.id) {
      this.notificationService.removeNotification(notif.id);
    }
  }

  removerNotificacion(notif: NotificationPayload): void {
    if (notif.id) {
      this.notificationService.removeNotification(notif.id);
    }
  }

  getContenidoTexto(notif: NotificationPayload): string {
    if (typeof notif.contenido === 'string') {
      return notif.contenido;
    } else if (typeof notif.contenido === 'object' && notif.contenido !== null) {
      if (notif.modulo === 'AUTORIZACION_DESCUENTO' && notif.contenido.cajeroNombre) {
        return `El cajero ${notif.contenido.cajeroNombre} solicita S/ ${notif.contenido.montoDescuento} para ${notif.contenido.pacienteNombre}`;
      }
    }
    return 'Nueva notificación recibida';
  }

  setupActivityDetection(): void {
    const activityEvents$ = merge(
      fromEvent(document, 'mousemove'),
      fromEvent(document, 'click'),
      fromEvent(document, 'keydown'),
      fromEvent(document, 'scroll')
    );

    this.activitySubscription = activityEvents$
      .pipe(throttleTime(60000))
      .subscribe(() => {
        this.sincronizarConServidor();
      });
  }

  setupSucursalChangeListener(): void {
    // Usar effect para reaccionar a cambios en la señal
    // Nota: Como estamos en un contexto de inyección (constructor o campo), podemos usar effect.
    // Pero ngOnInit no es un contexto de inyección. Moveremos esto al constructor.
  }


  setupRouterListener(): void {
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        console.log('🧭 [HEADER] Navegación detectada, verificando sucursal...');
        const sucursalDelToken = this.authService.getSucursalFromToken();

        // ✅ Leer de localStorage con clave específica por usuario
        const userId = localStorage.getItem('userId');
        const sucursalLocalStorage = userId
          ? localStorage.getItem(`sucursalActual_${userId}`)
          : null;

        const nuevaSucursal = sucursalDelToken || sucursalLocalStorage || '';

        if (nuevaSucursal && nuevaSucursal !== this.sucursalNombre) {
          console.log('🔄 [HEADER] Actualizando sucursal por navegación:', nuevaSucursal);
          this.sucursalNombre = nuevaSucursal;
          this.cdr.detectChanges();
        }

        this.updateBreadcrumb();
      });
  }

  private updateBreadcrumb(): void {
    let route = this.activatedRoute;
    while (route.firstChild) {
      route = route.firstChild;
    }
    if (route.snapshot.data['title']) {
      this.breadcrumb = route.snapshot.data['title'];
      this.cdr.detectChanges();
    }
  }

  cargarPerfil(): void {
    const userStorage = localStorage.getItem('currentUser');
    if (userStorage) {
      try {
        const user = JSON.parse(userStorage);
        this.usuarioActual = user.firstName + ' ' + (user.lastName || '');
        this.primerNombre = user.firstName || 'Usuario';
        this.userGender = user.sexo || 'null';

        if (this.userGender === 'M') {
          this.bien = 'Bienvenido';
        } else if (this.userGender === 'F') {
          this.bien = 'Bienvenida';
        } else {
          this.bien = 'Bienvenide';
        }
        this.isAdmin = (user.roles && user.roles.includes('ADMINISTRADOR')) || user.rol === 'ADMINISTRADOR';
      } catch (e) {
        this.usuarioActual = 'Usuario';
        this.bien = 'Bienvenide';
      }
    }

    const sucursalDelToken = this.authService.getSucursalFromToken();
    if (sucursalDelToken && sucursalDelToken !== this.sucursalNombre) {
      this.sucursalNombre = sucursalDelToken;

      // ✅ Guardar con clave específica por usuario
      const userId = localStorage.getItem('userId');
      if (userId) {
        localStorage.setItem(`sucursalActual_${userId}`, sucursalDelToken);
      }
    }
  }

  getProfileImage(): string {
    const user = this.authService.currentUser();

    if (user?.fotoPerfil) {
      return user.fotoPerfil;
    }

    if (this.userGender === 'M') {
      return '/male-avatar.jpg';
    } else if (this.userGender === 'F') {
      return '/female-avatar.png';
    } else {
      return '/default-avatar.png';
    }
  }

  iniciarTemporizador(): void {
    const savedTimeout = localStorage.getItem('sessionTimeout');
    if (savedTimeout) {
      this.tiempoRestanteMs = parseInt(savedTimeout);
    } else {
      this.tiempoRestanteMs = 1800000;
      localStorage.setItem('sessionTimeout', '1800000');
    }
    this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);

    this.timerSubscription = interval(1000).subscribe(() => {
      if (this.tiempoRestanteMs > 0) {
        this.tiempoRestanteMs -= 1000;
        this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
        this.cdr.detectChanges();

        if (this.tiempoRestanteMs <= 0) {
          // Auto-renovar sesión para que el usuario no sea expulsado automáticamente
          this.tiempoRestanteMs = 1800000;
          this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
          this.cdr.detectChanges();
        }
      }
    });

    this.syncSubscription = interval(60000).subscribe(() => {
      this.sincronizarConServidor();
    });
  }

  sincronizarConServidor(): void {
    this.authService.refreshToken().subscribe({
      next: (response) => {
        if (response?.success && response.data) {
          const tokenInfo: TokenInfo = response.data;
          this.tiempoRestanteMs = tokenInfo.timeRemainingMs || 1800000;
          this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        // En caso de error de red o backend, renovar localmente para no cerrar la sesión del usuario
        this.tiempoRestanteMs = 1800000;
        this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
        this.cdr.detectChanges();
      }
    });
  }

  mostrarAdvertencia(): void {
    // Silenciado para no interrumpir al usuario
  }

  manejarSesionExpirada(): void {
    // Auto-renovar para permanecer siempre en el sistema
    this.tiempoRestanteMs = 1800000;
    this.tiempoFormateado = this.formatearTiempo(this.tiempoRestanteMs);
    this.cdr.detectChanges();
  }

  detenerTemporizador(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
    if (this.syncSubscription) {
      this.syncSubscription.unsubscribe();
    }
  }

  formatearTiempo(ms: number): string {
    const totalSegundos = Math.floor(ms / 1000);
    const minutos = Math.floor(totalSegundos / 60);
    const segundos = totalSegundos % 60;

    const minutosStr = String(minutos).padStart(2, '0');
    const segundosStr = String(segundos).padStart(2, '0');

    return `${minutosStr}:${segundosStr} `;
  }

  ngOnDestroy(): void {
    this.detenerTemporizador();
    if (this.activitySubscription) {
      this.activitySubscription.unsubscribe();
    }
    if (this.sucursalChangeSubscription) {
      this.sucursalChangeSubscription.unsubscribe();
    }
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
    if (this.notificationSub) {
      this.notificationSub.unsubscribe();
    }
    // document.removeEventListener('click', this.cerrarMenuUsuarioFuera.bind(this)); // REMOVED: Using @HostListener
  }

  toggleMenuUsuario() {
    this.menuUsuarioAbierto = !this.menuUsuarioAbierto;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.menuUsuarioAbierto) return;

    const target = event.target as HTMLElement;
    const profileContainer = document.getElementById('profile-menu-container');

    // Si el clic no fue dentro del contenedor del perfil y el menú, lo cerramos
    if (profileContainer && !profileContainer.contains(target)) {
      this.menuUsuarioAbierto = false;
      this.cdr.markForCheck(); // CRÍTICO para OnPush
    }
  }

  cerrarMenuUsuarioFuera(event: MouseEvent) {
    // Mantengo el método por si se llama desde otro lado, pero refactorizado
    this.onDocumentClick(event);
  }

  verPerfil() {
    this.menuUsuarioAbierto = false;
    this.router.navigate(['/admin/configuraciones/mi-perfil']);
  }

  cambiarContrasena() {
    this.menuUsuarioAbierto = false;
    this.router.navigate(['/admin/configuraciones/cambio-clave']);
  }

  cerrarSesion() {
    this.menuUsuarioAbierto = false;
    this.authService.logout();
    this.onCerrarSesion.emit();
  }

  abrirConfiguracion() {
    this.showConfigConfig = true;
    if (this.isAdmin) {
      this.loadGlobalConfig();
    }
  }

  cancelarConfiguracion() {
    this.showConfigConfig = false;
  }

  globalTimeValue: number = 30;

  loadGlobalConfig() {
    this.configuracionService.getGlobalSessionTimeout().subscribe({
      next: (data) => {
        if (data && data.minutes) {
          this.globalTimeValue = data.minutes;
        }
      },
      error: (err) => {
        console.error('Error al cargar configuración global', err);
      }
    });
  }

  guardarConfiguracion() {
    if (this.globalTimeValue < 15) {
      alert('⚠️ El tiempo mínimo de sesión es de 15 minutos.');
      this.globalTimeValue = 15;
      return;
    }

    this.isLoadingConfig = true;

    if (this.isAdmin) {
      this.configuracionService.updateGlobalSessionTimeout(this.globalTimeValue).subscribe({
        next: () => {
          alert('✅ Configuración actualizada. Cierra sesión y vuelve a iniciar para aplicar los cambios.');
          this.isLoadingConfig = false;
          this.showConfigConfig = false;
        },
        error: (err) => {
          console.error(err);
          alert('❌ Error al actualizar configuración global.');
          this.isLoadingConfig = false;
        }
      });
    } else {
      this.isLoadingConfig = false;
      this.showConfigConfig = false;
    }
  }
}
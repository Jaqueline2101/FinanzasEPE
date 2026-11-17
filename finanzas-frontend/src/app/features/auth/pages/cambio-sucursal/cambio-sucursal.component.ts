import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PuntoService } from '../../../../core/services/punto.service';
import { Sucursal } from '../../../../models/sucursal.model';
import { Punto } from '../../../../models/punto.model';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../../shared/components/breadcrumb/breadcrumb';

@Component({
  selector: 'app-cambio-sucursal',
  standalone: true,
  imports: [CommonModule, FormsModule, BreadcrumbComponent],
  templateUrl: './cambio-sucursal.component.html',
  styleUrls: ['./cambio-sucursal.component.css'],
})
export class CambioSucursalComponent implements OnInit {
  usuario: string = '';
  nuevaContrasena: string = '';

  sucursalSeleccionada: string = '';
  puntoSeleccionado: string = '';

  sucursales: Sucursal[] = [];
  puntosVenta: Punto[] = [];
  cargandoPuntos: boolean = false;
  loading: boolean = false;

  alertaMostrada: boolean = false;
  alertaTipo: 'success' | 'error' = 'success';
  alertaMensaje: string = '';

  mostrandoTransicion: boolean = false;
  nombreSucursalSeleccionada: string = '';
  nombrePuntoSeleccionado: string = '';

  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuraciones', route: '/admin/configuraciones' },
    { label: 'Cambio Punto de Atención' }
  ];

  private puntoService = inject(PuntoService);

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  esCajero: boolean = false;
  bloqueoCajero: boolean = false; // Nueva bandera para bloquear la UI

  ngOnInit() {
    // ... (previous localstorage logic) ...
    const userStorage = localStorage.getItem('currentUser');
    if (userStorage) {
      try {
        const user = JSON.parse(userStorage);
        this.usuario = user.username || '';
      } catch (e) {
        console.error('Error al cargar usuario', e);
      }
    }

    let userId = localStorage.getItem('userId');
    if (!userId) {
      const currentUser = this.authService.currentUser();
      if (currentUser?.id) {
        userId = currentUser.id.toString();
        localStorage.setItem('userId', userId);
      }
    }

    // Verificar Rol primero
    const currentUser = this.authService.currentUser();
    const userAny = currentUser as any;
    this.esCajero = currentUser?.rol === AuthService.ROLE_CAJERO || (userAny?.roles && userAny.roles.includes(AuthService.ROLE_CAJERO)) || (currentUser?.permissions?.includes('CAJA_LECTURA') || false);

    // ✅ LÓGICA DE BLOQUEO DE CAJERO
    // Si es cajero Y ya tiene un punto seleccionado (en AuthService), BLOQUEAR.
    const puntoActual = this.authService.currentPunto();
    if (this.esCajero && puntoActual) {
      this.bloqueoCajero = true;
      // Mostrar alerta persistente
      setTimeout(() => {
        this.mostrarAlerta('error', '🔒 SU CAJA ESTÁ ASIGNADA: Para cambiar de punto de venta, debe Cerrar Sesión.');
      }, 500);
    }

    if (userId) {
      this.cargarSucursales(parseInt(userId));
    } else {
      console.error('❌ [CAMBIO SUCURSAL] No se pudo obtener el ID del usuario. No se cargarán las sucursales.');
      this.mostrarAlerta('error', 'Error de sesión: No se identificó al usuario.');
    }

    // ✅ NUEVO: Cargar sucursal actual del usuario desde el token
    const sucursalActualDelToken = this.authService.getSucursalFromToken();
    if (sucursalActualDelToken) {
      this.sucursalSeleccionada = sucursalActualDelToken;
    }

    // 🔄 RESTAURACIÓN DINÁMICA: Cargar el punto si ya está guardado en el perfil del usuario (Token)
    const puntoActualDelToken = this.authService.getPuntoNombreFromToken();
    if (puntoActualDelToken) {
      this.puntoSeleccionado = puntoActualDelToken;
    }

    // 🚨 REVISAR SI VIENE CON ERROR DE GUARD
    this.route.queryParams.subscribe(params => {
      if (params['error'] === 'punto_requerido') {
        // Forzar reinicio de la alerta si ya estaba mostrada o no
        this.alertaMostrada = false;
        this.cdr.detectChanges();

        // Pequeño timeout para asegurar que la UI se refresque y muestre la animación
        setTimeout(() => {
          this.mostrarAlerta('error', '⚠️ ALERTA DE SEGURIDAD: Como Cajero, DEBE seleccionar un Punto de Venta para continuar.');
        }, 100);
      }
    });
  }

  cargarSucursales(userId: number) {
    console.log('Cargando sucursales para userId:', userId);

    // El rol ya se verificó en ngOnInit

    this.authService.getSucursalesAutorizadas(userId).subscribe({
      next: (response) => {
        console.log('Response completo:', response);
        if (response.success && response.data) {
          this.sucursales = response.data as Sucursal[];

          // Si ya hay una sucursal seleccionada (del token), cargar sus puntos
          if (this.sucursalSeleccionada) {
            const suc = this.sucursales.find(s => s.nombreSucursal === this.sucursalSeleccionada);
            if (suc) {
              this.cargarPuntos(suc.idSucursal);
            }
          }

          console.log('Sucursales cargadas:', this.sucursales);
          this.cdr.detectChanges();
        } else {
          console.warn('Response no exitoso o sin data:', response);
          this.sucursales = []; // Usuario sin sucursales asignadas
        }
      },
      error: (err) => {
        // Si es 404 o 403, el usuario simplemente no tiene sucursales asignadas
        if (err.status === 404 || err.status === 403) {
          console.log('Usuario sin sucursales asignadas');
          this.sucursales = [];
        } else {
          // Solo mostrar error para otros códigos HTTP
          console.error('Error al cargar sucursales:', err);
          this.mostrarAlerta('error', 'Error al cargar sucursales');
        }
      },
    });
  }

  onSucursalChange() {
    this.puntoSeleccionado = '';
    this.puntosVenta = [];

    if (!this.sucursalSeleccionada) return;

    const sucursal = this.sucursales.find(s => s.nombreSucursal === this.sucursalSeleccionada);
    if (sucursal) {
      this.cargarPuntos(sucursal.idSucursal);
    }
  }

  cargarPuntos(sucursalId: number) {
    this.cargandoPuntos = true;
    this.puntoService.listarActivosPorSucursal(sucursalId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.puntosVenta = response.data;

          if (this.puntoSeleccionado) {
            const existe = this.puntosVenta.some(p => p.nombre === this.puntoSeleccionado);
            if (!existe) this.puntoSeleccionado = '';
          }

        } else {
          this.puntosVenta = [];
        }
        this.cargandoPuntos = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar puntos:', err);
        this.puntosVenta = [];
        this.cargandoPuntos = false;
        this.cdr.detectChanges();
      }
    });
  }

  private alertTimeout: any;

  mostrarAlerta(tipo: 'success' | 'error', mensaje: string) {
    // Limpiar timeout anterior si existe
    if (this.alertTimeout) {
      clearTimeout(this.alertTimeout);
    }

    this.alertaTipo = tipo;
    this.alertaMensaje = mensaje;
    this.alertaMostrada = true;
    this.cdr.detectChanges();

    if (tipo === 'error') {
      this.alertTimeout = setTimeout(() => {
        this.alertaMostrada = false;
        this.cdr.detectChanges();
      }, 4000);
    }
  }

  guardando: boolean = false;

  guardarCambios() {
    if (this.guardando) return; // Prevención adicional

    if (!this.sucursalSeleccionada) {
      this.mostrarAlerta('error', 'Por favor seleccione una sucursal');
      return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) {
      this.mostrarAlerta('error', 'Usuario no encontrado');
      return;
    }

    const sucursal = this.sucursales.find((s) => s.nombreSucursal === this.sucursalSeleccionada);
    if (!sucursal) {
      this.mostrarAlerta('error', 'Sucursal no válida');
      return;
    }

    let puntoId: number | undefined;

    if (this.puntosVenta.length > 0) {
      // Si es cajero, OBLIGATORIO seleccionar punto
      if (this.esCajero && !this.puntoSeleccionado) {
        this.mostrarAlerta('error', 'Como Cajero, es OBLIGATORIO seleccionar un Punto de Venta');
        return;
      }

      // Si no es cajero, es opcional (pero si selecciona uno, usarlo)
      if (this.puntoSeleccionado) {
        const punto = this.puntosVenta.find(p => p.nombre === this.puntoSeleccionado);
        if (punto) {
          puntoId = punto.idPunto;
        }
      }
    }

    if (!this.nuevaContrasena) {
      this.mostrarAlerta('error', 'Confirma tus datos');
      return;
    }

    this.guardando = true; // Deshabilitar botón

    this.authService
      .seleccionarSucursal(parseInt(userId), sucursal.idSucursal, this.nuevaContrasena, puntoId)
      .subscribe({
        next: (response) => {
          if (response.success) {
            console.log('✅ Sucursal seleccionada exitosamente');
            this.nombreSucursalSeleccionada = sucursal.nombreSucursal;
            this.nombrePuntoSeleccionado = response.data?.puntoNombre || '';

            // ✅ SOLUCIÓN: No esperar al Header. Proceder inmediatamente.
            console.log('✅ [CAMBIO SUCURSAL] Cambio exitoso. Mostrando bienvenida.');
            this.alertaMostrada = false;
            this.mostrandoTransicion = true;
            this.cdr.detectChanges();
          } else {
            this.guardando = false; // Reactivar si no fue success (aunque api retorna error usu.)
            this.cdr.detectChanges();
          }
        },
        error: (err) => {
          console.error('Error al seleccionar sucursal:', err);
          const errorMsg = err.error?.message || 'Credenciales incorrectas';
          this.mostrarAlerta('error', errorMsg);
          this.guardando = false; // Reactivar botón en error
          this.cdr.detectChanges();
        },
      });
  }

  volverASeleccion() {
    this.mostrandoTransicion = false;
    this.alertaMostrada = false;
    this.cdr.detectChanges();
  }

  cerrarSesion() {
    console.log('Cerrando sesión desde cambio-sucursal component');
  }
}

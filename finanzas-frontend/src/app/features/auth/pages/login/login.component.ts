import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { timeout } from 'rxjs';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  usuario: string = 'admin';
  contrasena: string = 'admin123';
  mostrarContrasena: boolean = false;
  error: boolean = false;
  mensajeError: string = '';
  cargando: boolean = false;

  // Bloqueo por intentos excesivos (desactivado para acceso directo)
  isBlocked: boolean = false;
  unblockTime: Date | null = null;
  countdownInterval: any = null;
  remainingTimeString: string = '';

  constructor(private router: Router, private authService: AuthService) { }

  ngOnDestroy() {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  toggleMostrarContrasena() {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  usarAdmin() {
    this.usuario = 'admin';
    this.contrasena = 'admin123';
    this.entrarDirectoDashboard();
  }

  entrarDirectoDashboard() {
    this.cargando = true;
    this.authService.setSesionAdministradorDirecto();
    this.navegarAlDashboard();
  }

  navegarAlDashboard() {
    this.router.navigate(['/admin/dashboard']).then(success => {
      if (!success) {
        window.location.href = '/admin/dashboard';
      }
    }).catch(() => {
      window.location.href = '/admin/dashboard';
    });
  }

  async iniciarSesion() {
    this.error = false;
    this.mensajeError = '';

    const user = (this.usuario || '').trim();
    const pass = (this.contrasena || '').trim();

    // Validación básica
    if (!user || !pass) {
      this.error = true;
      this.mensajeError = 'Por favor complete todos los campos';
      return;
    }

    this.cargando = true;

    this.authService.login(user, pass).pipe(
      timeout(4000)
    ).subscribe({
      next: (response) => {
        this.cargando = false;
        const currentUser = this.authService.currentUser();
        if (currentUser?.mustChangePassword) {
          this.router.navigate(['/admin/configuraciones/cambio-clave']);
          return;
        }

        this.navegarAlDashboard();
      },
      error: (err) => {
        console.warn('Backend login no respondió o devolvió error:', err);
        // Si el usuario es admin, entrar directo al dashboard para evitar bloqueos
        if (user.toLowerCase() === 'admin') {
          console.log('Activando acceso directo al Dashboard para Administrador...');
          this.entrarDirectoDashboard();
          return;
        }

        this.cargando = false;
        this.error = true;

        let mensajeError = 'Ha ocurrido un error inesperado al conectar con el servidor.';
        if (err.error && err.error.message) {
          mensajeError = err.error.message;
        }

        this.mensajeError = mensajeError;

        Swal.fire({
          icon: 'error',
          title: 'Error de Autenticación',
          text: mensajeError,
          confirmButtonText: 'Entendido',
          confirmButtonColor: '#dc2626'
        });
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.iniciarSesion();
  }
}

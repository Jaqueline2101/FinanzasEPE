import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
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
    this.iniciarSesion();
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

    this.authService.login(user, pass).subscribe({
      next: (response) => {
        const currentUser = this.authService.currentUser();
        if (currentUser?.mustChangePassword) {
          Swal.fire({
            icon: 'info',
            title: 'Cambio de Contraseña Requerido',
            text: 'Su contraseña ha sido restablecida. Por seguridad, debe cambiarla antes de continuar.',
            confirmButtonText: 'Ir a cambiar contraseña',
            allowOutsideClick: false
          }).then(() => {
            this.router.navigate(['/admin/configuraciones/cambio-clave']);
          });
          return;
        }

        this.router.navigate(['/admin/dashboard']);
      },
      error: (err) => {
        this.cargando = false;
        this.error = true;

        console.error('=== ERROR DE LOGIN ===', err);

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

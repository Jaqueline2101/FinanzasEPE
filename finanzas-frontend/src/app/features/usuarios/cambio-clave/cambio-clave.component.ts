import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { UserService } from '../../../services/user.service';

@Component({
    selector: 'app-cambio-clave',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, BreadcrumbComponent],
    templateUrl: './cambio-clave.component.html'
})
export class CambioClaveComponent implements OnInit {
    changePasswordForm: FormGroup;
    loading = false;
    username = '';
    userId: number | null = null;

    // Visibility toggles
    showOldPassword = false;
    showNewPassword = false;
    showConfirmPassword = false;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuraciones', route: '/admin/configuraciones' },
        { label: 'Cambiar contraseña' }
    ];

    togglePasswordVisibility(field: 'old' | 'new' | 'confirm') {
        if (field === 'old') this.showOldPassword = !this.showOldPassword;
        else if (field === 'new') this.showNewPassword = !this.showNewPassword;
        else if (field === 'confirm') this.showConfirmPassword = !this.showConfirmPassword;
    }

    cerrarSesion() {
        localStorage.clear();
        this.router.navigate(['/login']);
    }

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        private router: Router
    ) {
        this.changePasswordForm = this.fb.group({
            oldPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, {
            validators: this.passwordMatchValidator
        });
    }

    ngOnInit(): void {
        // Obtener datos del usuario logueado
        const currentUserStr = localStorage.getItem('currentUser');
        const userIdStr = localStorage.getItem('userId');

        if (currentUserStr) {
            const user = JSON.parse(currentUserStr);
            this.username = user.username || '';
        }

        if (userIdStr) {
            this.userId = parseInt(userIdStr, 10);
        }
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('newPassword')?.value === g.get('confirmPassword')?.value
            ? null : { mismatch: true };
    }

    onSubmit() {
        if (this.changePasswordForm.invalid) {
            return;
        }

        if (!this.userId) {
            Swal.fire('Error', 'No se pudo identificar al usuario', 'error');
            return;
        }

        this.loading = true;
        const { oldPassword, newPassword, confirmPassword } = this.changePasswordForm.value;

        this.userService.changePassword(this.userId, {
            oldPassword,
            newPassword,
            confirmPassword
        }).subscribe({
            next: () => {
                this.loading = false;
                Swal.fire({
                    title: '¡Contraseña actualizada!',
                    text: 'Tu contraseña se cambió exitosamente. Por seguridad, debes iniciar sesión nuevamente.',
                    icon: 'success',
                    confirmButtonText: 'Iniciar sesión',
                    confirmButtonColor: '#2563EB',
                    allowOutsideClick: false,
                    allowEscapeKey: false
                }).then(() => {
                    // Cerrar sesión automáticamente
                    localStorage.clear();
                    this.router.navigate(['/login']);
                });
            },
            error: (err) => {
                this.loading = false;
                let msg = 'Ocurrió un error al cambiar la contraseña';
                if (err.error && err.error.message) {
                    msg = err.error.message;
                }
                Swal.fire('Error', msg, 'error');
            }
        });
    }
}

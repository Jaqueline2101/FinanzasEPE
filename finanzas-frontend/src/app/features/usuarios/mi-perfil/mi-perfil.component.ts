import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { UserService } from '../../../services/user.service';
import { UserResponse } from '../../../models/user.model';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';

import { AuthService } from '../../auth/services/auth.service';

@Component({
    selector: 'app-mi-perfil',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        BreadcrumbComponent
    ],
    templateUrl: './mi-perfil.component.html',
    styleUrls: ['./mi-perfil.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MiPerfilComponent implements OnInit {
    profileForm: FormGroup;
    userProfile: UserResponse | null = null;
    loading: boolean = true;
    saving: boolean = false;
    previewUrl: string | ArrayBuffer | null = null;
    selectedFile: File | null = null;
    tempFile: File | null = null;
    originalPhotoUrl: string | null = null;

    breadcrumbItems = [
        { label: 'Configuraciones', url: '/admin/configuraciones' },
        { label: 'Mi Perfil', url: '/admin/configuraciones/mi-perfil' }
    ];

    // Permisos (esto debería venir del backend según el rol del usuario)
    canEditCargo = false;
    canEditSucursal = false;

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        private router: Router,
        public sidebarService: SidebarService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef
    ) {
        this.profileForm = this.fb.group({
            // Información Personal
            nombres: [{ value: '', disabled: true }],
            apellidos: [{ value: '', disabled: true }],
            dni: [{ value: '', disabled: true }],
            fechaNacimiento: [{ value: '', disabled: true }],

            // Información de Contacto
            email: [{ value: '', disabled: true }],
            telefono: [{ value: '', disabled: true }],
            direccion: [{ value: '', disabled: true }],

            // Información Profesional
            profesion: [{ value: '', disabled: true }],
            cargo: [{ value: '', disabled: true }],

            // Información Laboral
            sucursal: [{ value: '', disabled: true }],
            fechaIngreso: [{ value: '', disabled: true }],

            // Seguridad
            login: [{ value: '', disabled: true }],
            ultimaConexion: [{ value: '', disabled: true }]
        });
    }

    ngOnInit(): void {
        this.loadUserProfile();
        this.checkPermissions();
    }

    loadUserProfile(): void {
        this.loading = true;
        const userId = localStorage.getItem('userId');

        if (!userId) {
            Swal.fire('Error', 'No se pudo identificar al usuario', 'error');
            this.router.navigate(['/login']);
            return;
        }

        this.userService.getUserById(parseInt(userId, 10)).subscribe({
            next: (response: any) => {
                this.userProfile = response.data || null;
                this.populateForm();
                this.loading = false;
            },
            error: (err) => {
                this.loading = false;
                Swal.fire('Error', 'No se pudo cargar el perfil del usuario', 'error');
            }
        });
    }

    populateForm(): void {
        if (!this.userProfile) return;

        this.profileForm.patchValue({
            nombres: this.userProfile.firstName,
            apellidos: this.userProfile.lastName,
            dni: this.userProfile.numdoc || '',
            fechaNacimiento: this.userProfile.nacfec || '',
            email: this.userProfile.email,
            telefono: this.userProfile.phone,
            direccion: this.userProfile.direcc || '',
            profesion: this.userProfile.nomOcupacion || '',
            cargo: this.userProfile.roleName || '',
            sucursal: this.userProfile.sucursal || '',
            fechaIngreso: this.userProfile.fechaIng || '',
            login: this.userProfile.username,
            ultimaConexion: this.userProfile.lastLogin
        });

        // Cargar foto de perfil si existe
        if (this.userProfile.fotoPerfil) {
            this.previewUrl = this.userProfile.fotoPerfil;
        }
    }

    checkPermissions(): void {
        // Aquí deberías verificar los permisos del usuario desde el backend
        const userRole = localStorage.getItem('userRole');
        this.canEditCargo = userRole === 'ADMIN';
        this.canEditSucursal = userRole === 'ADMIN';
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            // Validar tipo de archivo
            if (!file.type.match(/image\/(png|jpg|jpeg)/)) {
                Swal.fire('Error', 'Solo se permiten imágenes PNG, JPG o JPEG', 'error');
                return;
            }

            // Validar tamaño (max 2MB)
            if (file.size > 2 * 1024 * 1024) {
                Swal.fire('Error', 'La imagen no debe superar los 2MB', 'error');
                return;
            }

            // Set file directly
            this.tempFile = file;

            // Guardar URL original si no existe aún
            if (!this.originalPhotoUrl && typeof this.previewUrl === 'string') {
                this.originalPhotoUrl = this.previewUrl;
            }

            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.previewUrl = e.target.result;
                this.cdr.markForCheck(); // Mark for check since we are inside async callback
            };
            reader.readAsDataURL(file);
        }
    }

    cancelarFoto(): void {
        this.tempFile = null;
        this.previewUrl = this.originalPhotoUrl;
        // Reset file input
        const fileInput = document.getElementById('file-upload') as HTMLInputElement;
        if (fileInput) {
            fileInput.value = '';
        }
    }

    guardarFoto(): void {
        if (!this.tempFile) return;

        const userId = localStorage.getItem('userId');
        if (!userId) return;

        this.loading = true;
        this.userService.uploadPhoto(parseInt(userId, 10), this.tempFile).subscribe({
            next: (response) => {
                this.loading = false;
                if (response.success && response.data) {
                    Swal.fire({
                        title: '¡Foto actualizada!',
                        text: 'Tu foto de perfil ha sido actualizada exitosamente',
                        icon: 'success',
                        timer: 1500,
                        showConfirmButton: false
                    });

                    // Actualizar estado local
                    if (this.userProfile && response.data.fotoPerfil) {
                        this.userProfile.fotoPerfil = response.data.fotoPerfil;
                        this.originalPhotoUrl = response.data.fotoPerfil; // Actualizar original
                        // Actualizar Sidebar y Header via AuthService
                        this.authService.updateCurrentUserPhoto(response.data.fotoPerfil);
                    }

                    this.tempFile = null; // Limpiar archivo temporal
                }
            },
            error: (err) => {
                this.loading = false;
                console.error('Error uploading photo', err);
                Swal.fire('Error', 'No se pudo subir la foto de perfil', 'error');
            }
        });
    }

    requestChange(fieldName: string): void {
        Swal.fire({
            title: 'Solicitar cambio',
            html: `
        <p class="text-sm text-gray-600 mb-4">
          No tienes permisos para editar este campo directamente. 
          ¿Deseas solicitar un cambio al administrador?
        </p>
        <textarea 
          id="swal-textarea" 
          class="swal2-textarea w-full" 
          placeholder="Describe el cambio que necesitas..."
        ></textarea>
      `,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Enviar solicitud',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#2563EB',
            preConfirm: () => {
                const textarea = document.getElementById('swal-textarea') as HTMLTextAreaElement;
                if (!textarea.value) {
                    Swal.showValidationMessage('Por favor describe el cambio que necesitas');
                    return false;
                }
                return textarea.value;
            }
        }).then((result) => {
            if (result.isConfirmed) {
                // Aquí enviarías la solicitud al backend
                this.sendChangeRequest(fieldName, result.value);
            }
        });
    }



    sendChangeRequest(fieldName: string, description: string): void {
        this.loading = true;

        // TODO: Implementar endpoint en el backend para solicitudes de cambio
        // Por ahora solo mostramos un mensaje de éxito
        setTimeout(() => {
            this.loading = false;
            Swal.fire({
                title: '¡Solicitud enviada!',
                text: 'Tu solicitud de cambio ha sido enviada al administrador. Te notificaremos cuando sea procesada.',
                icon: 'success',
                confirmButtonColor: '#2563EB'
            });
        }, 1000);
    }

    onSubmit(): void {
        if (this.profileForm.invalid) {
            Swal.fire('Error', 'Por favor completa todos los campos requeridos correctamente', 'error');
            return;
        }

        Swal.fire({
            title: '¿Guardar cambios?',
            text: 'Se actualizará tu información de perfil',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Sí, guardar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#2563EB'
        }).then((result) => {
            if (result.isConfirmed) {
                this.saveProfile();
            }
        });
    }

    saveProfile(): void {
        this.loading = true;
        const userId = localStorage.getItem('userId');

        if (!userId) {
            Swal.fire('Error', 'No se pudo identificar al usuario', 'error');
            return;
        }

        const formData = new FormData();

        // Agregar datos del formulario
        Object.keys(this.profileForm.controls).forEach(key => {
            const control = this.profileForm.get(key);
            if (control && !control.disabled) {
                formData.append(key, control.value || '');
            }
        });

        // Agregar foto si se seleccionó una nueva
        if (this.selectedFile) {
            formData.append('fotoPerfil', this.selectedFile);
        }

        // TODO: Implementar endpoint en el backend para actualizar perfil
        // this.userService.updateProfile(parseInt(userId, 10), formData).subscribe({
        //   next: (response) => {
        //     this.loading = false;
        //     Swal.fire('¡Éxito!', 'Tu perfil ha sido actualizado', 'success');
        //     this.loadUserProfile(); // Recargar datos
        //   },
        //   error: (err) => {
        //     this.loading = false;
        //     Swal.fire('Error', 'No se pudo actualizar el perfil', 'error');
        //   }
        // });

        // Simulación temporal
        setTimeout(() => {
            this.loading = false;
            Swal.fire({
                title: '¡Perfil actualizado!',
                text: 'Tus cambios han sido guardados exitosamente',
                icon: 'success',
                confirmButtonColor: '#2563EB'
            });
        }, 1500);
    }

    cambiarContrasena(): void {
        this.router.navigate(['/admin/configuraciones/cambio-clave']);
    }

    cerrarSesion(): void {
        localStorage.clear();
        this.router.navigate(['/login']);
    }

    getInitials(): string {
        if (!this.userProfile) return 'U';
        const nombres = this.userProfile.firstName || '';
        const apellidos = this.userProfile.lastName || '';
        return `${nombres.charAt(0)}${apellidos.charAt(0)}`.toUpperCase();
    }
}

import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { SearchGenericComponent } from '../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { ModalComponent } from '../../shared/components/modal/modal';
import { ApiResponse } from '../../shared/modals/response.model';
import Swal from 'sweetalert2';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-gestion-claves',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TablaGeneralComponent,
        BreadcrumbComponent,
        PageHeaderComponent,
        SearchGenericComponent,
        PaginationComponent,
        ModalComponent
    ],
    templateUrl: './gestion-claves.component.html'
})
export class GestionClavesComponent implements OnInit {
    private userService = inject(UserService);
    private cd = inject(ChangeDetectorRef);

    // State
    users: any[] = [];
    isLoading = false;
    searchTerm = '';
    
    // Pagination
    currentPage = 0;
    pageSize = 10;
    totalElements = 0;
    totalPages = 0;

    // Modal state
    isModalVisible = false;
    selectedUser: any = null;
    newPassword = '';
    mustChangePassword = true;
    isSaving = false;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuraciones', route: '/admin/configuraciones' },
        { label: 'Seguridad', route: '/admin/configuraciones/seguridad' },
        { label: 'Gestión de Claves' }
    ];

    columns: Columna[] = [
        { field: '', header: '#', tipo: 'index', align: 'center' },
        { field: 'username', header: 'Usuario' },
        { field: 'firstName', header: 'Nombres' },
        { field: 'lastName', header: 'Apellidos' },
        { field: 'numdoc', header: 'DNI/Doc' },
        { field: 'active', header: 'Estado', tipo: 'status' }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' }
    ];

    ngOnInit(): void {
        this.loadUsers();
    }

    handleSearch(event: { q: string, type: string }): void {
        this.searchTerm = event.q;
        this.currentPage = 0;
        this.loadUsers();
    }

    loadUsers(): void {
        this.isLoading = true;
        
        const request = (this.searchTerm && this.searchTerm.length >= 2)
            ? this.userService.searchUsers(this.searchTerm, this.currentPage, this.pageSize)
            : this.userService.getAllUsers(this.currentPage, this.pageSize);

        request.pipe(finalize(() => {
                this.isLoading = false;
                this.cd.detectChanges();
            }))
            .subscribe({
                next: (res: ApiResponse<any>) => {
                    if (res.success && res.data) {
                        this.users = res.data.content || [];
                        this.totalElements = res.data.totalElements || 0;
                        this.totalPages = res.data.totalPages || 0;
                    }
                },
                error: (err: any) => {
                    Swal.fire({ icon: 'error', title: 'Error', text: 'Error al cargar usuarios' });
                }
            });
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.loadUsers();
    }

    openResetModal(user: any): void {
        this.selectedUser = user;
        this.newPassword = '';
        this.mustChangePassword = true;
        this.isModalVisible = true;
    }

    closeModal(): void {
        this.isModalVisible = false;
        this.selectedUser = null;
    }

    confirmReset(): void {
        if (!this.newPassword || this.newPassword.length < 6) {
            Swal.fire({ icon: 'warning', title: 'Contraseña inválida', text: 'La contraseña debe tener al menos 6 caracteres' });
            return;
        }

        if (!this.selectedUser) return;

        this.isSaving = true;
        this.userService.resetPassword(this.selectedUser.id, this.newPassword, this.mustChangePassword)
            .pipe(finalize(() => this.isSaving = false))
            .subscribe({
                next: (success: boolean) => {
                    if (success) {
                        Swal.fire({ 
                            icon: 'success', 
                            title: 'Éxito', 
                            text: `Contraseña de ${this.selectedUser.username} restablecida correctamente` 
                        });
                        this.closeModal();
                    }
                },
                error: (err: any) => {
                    Swal.fire({ 
                        icon: 'error', 
                        title: 'Error', 
                        text: err.error?.message || 'No se pudo restablecer la contraseña' 
                    });
                }
            });
    }

    generateRandomPassword(): void {
        const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        let pass = "";
        for (let i = 0; i < 10; i++) {
            pass += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        this.newPassword = pass;
    }
}

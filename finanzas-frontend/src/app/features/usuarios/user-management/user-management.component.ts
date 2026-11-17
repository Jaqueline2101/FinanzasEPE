import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../services/user.service';
import { RoleService } from '../../../services/role.service';
import { HeaderComponent } from '../../../shared/header/header.component';
import { SidebarComponent } from '../../../shared/sidebar/sidebar.component';
import {
  BreadcrumbComponent,
  BreadcrumbItem,
} from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';
import { Role } from '../../../models/role.model';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { AlertasService } from '../../../services/alertas.service';
import { User, UserStats } from '../../../models/user.model';
import { SucursalService } from '../../../core/services/sucursal.service';
import { Sucursal } from '../../../models/sucursal.model';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { ModalManagementComponent } from './modal-management/modal-management.component';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    PageHeaderComponent,
    PaginationComponent,
    SearchGenericComponent,
    SearchGenericComponent,
    ModalManagementComponent,
    AlertasComponent
  ],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css'],
})
export class UserManagementComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Gestión de Usuarios' },
  ];

  // Listas
  users: User[] = [];
  availableRoles: Role[] = [];
  availableSucursales: Sucursal[] = [];

  // Estado del modal
  showModal = false;
  selectedUser: User | null = null;

  // Modal Visualización (si se mantiene)
  showViewModal = false;
  viewUser: User | null = null;

  // Estados UI
  loading = false;

  // Search & Filter state
  searchQuery = '';
  searchType = 'ALL';
  showActiveOnly = false;

  // Paginación
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Mensajes
  message = '';
  messageType: 'success' | 'error' | 'info' | 'warning' = 'info';

  // Estadísticas
  stats: UserStats = {
    total: 0,
    active: 0,
    inactive: 0
  };

  // Ordenamiento
  sortField: string = 'login';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(
    private userService: UserService,
    private roleService: RoleService,
    private cdRef: ChangeDetectorRef,
    public sidebarService: SidebarService,
    private sucursalService: SucursalService,
    private alertasService: AlertasService
  ) { }

  ngOnInit(): void {
    this.executeSearch();
    this.loadRoles();
    this.loadAvailableSucursales();
    this.loadStats();
  }

  handleSearch(event: { q: string; type: string }) {
    this.searchQuery = event.q;
    this.searchType = event.type;
    this.currentPage = 0;
    this.executeSearch();
  }

  executeSearch(): void {
    this.loading = true;
    this.cdRef.detectChanges();

    let request;

    if (this.searchQuery && this.searchQuery.trim().length > 0) {
      request = this.userService.searchByFilter(
        this.searchQuery,
        this.searchType,
        this.currentPage,
        this.pageSize
      );
    } else {
      request = this.showActiveOnly
        ? this.userService.getActiveUsers(this.currentPage, this.pageSize, this.sortField, this.sortDirection)
        : this.userService.getAllUsers(this.currentPage, this.pageSize, this.sortField, this.sortDirection);
    }

    request
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdRef.detectChanges();
        })
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.users = response.data.content;
            this.totalPages = response.data.totalPages;
            this.totalElements = response.data.totalElements;
            this.currentPage = response.data.number;
          } else {
            this.alertasService.warning('Atención', response.message || 'No se pudo cargar la lista de usuarios');
          }
        },
        error: (error) => {
          console.error('Error al cargar usuarios:', error);
          this.alertasService.error('Error', error.error?.message || 'Error de conexión al intentar obtener los usuarios');
        },
      });
  }

  loadUsers(): void {
    this.executeSearch();
  }

  loadRoles(): void {
    this.roleService.getActiveRoles(0, 100).subscribe({
      next: (response) => {
        if (response.success && response.data?.content) {
          this.availableRoles = response.data.content;
        } else {
          this.alertasService.warning('Atención', response.message || 'No se pudieron cargar los roles disponibles');
        }
      },
      error: (error) => {
        console.error('Error al cargar roles:', error);
      },
    });
  }

  loadAvailableSucursales(): void {
    this.sucursalService.getAll().subscribe({
      next: (response) => {
        if (response.success && response.data && response.data.content) {
          this.availableSucursales = response.data.content;
        }
      },
      error: (error) => {
        console.error('❌ Error al cargar sucursales:', error);
      },
    });
  }

  loadStats(): void {
    this.userService.getUserStats().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.stats = response.data;
        }
      },
      error: (error) => {
        console.error('❌ Error al cargar estadísticas:', error);
      }
    });
  }

  // ========== MODAL ACTIONS ==========

  openCreateModal(): void {
    this.selectedUser = null;
    this.showModal = true;
  }

  openEditModal(user: User): void {
    this.selectedUser = user;
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedUser = null;
  }

  onUserSaved(): void {
    this.loadUsers();
    this.loadStats();
  }

  // ========== GESTIÓN DE ESTADO (ACTIVAR/DESACTIVAR) ==========
  toggleStatusConfirm(user: User): void {
    const action = user.active ? 'desactivar' : 'activar';
    const title = user.active ? '¿Desactivar acceso?' : '¿Activar acceso?';
    const message = `¿Realmente deseas ${action} al usuario "${user.username}"?`;
    const confirmText = user.active ? 'SÍ, DESACTIVAR' : 'SÍ, ACTIVAR';
    const confirmClass = user.active 
      ? 'bg-rose-500 hover:bg-rose-600 shadow-rose-500/30' 
      : 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-500/30';

    this.alertasService.confirm(
      title,
      message,
      confirmText,
      'CANCELAR',
      confirmClass
    ).then((confirmed) => {
      if (confirmed) {
        this.loading = true;
        this.userService.toggleUserStatus(user.id, !user.active).subscribe({
          next: (response) => {
            this.loading = false;
            if (response.success) {
              const successMsg = user.active ? 'Usuario desactivado correctamente' : 'Usuario activado correctamente';
              this.alertasService.success('¡Operación exitosa!', successMsg);
              this.loadUsers();
              this.loadStats();
            } else {
              this.alertasService.warning('Atención', response.message);
            }
          },
          error: (error) => {
            this.loading = false;
            this.alertasService.error('Error', error.error?.message || `Error al ${action} usuario`);
          },
        });
      }
    });
  }

  // ========== UTILIDADES ==========

  changePage(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  changePageSize(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 0;
    this.loadUsers();
  }

  sortBy(field: string): void {
    let backendField = field;
    if (field === 'username') backendField = 'login';
    if (field === 'firstName') backendField = 'nombre';

    if (this.sortField === backendField) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = backendField;
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }

  getStatusBadgeClass(user: User): string {
    if (!user.active) return 'bg-red-100 text-red-800 border-red-200';
    if (user.useDirectPermissions) return 'bg-purple-100 text-purple-800 border-purple-200';
    return 'bg-green-100 text-green-800 border-green-200';
  }

  getStatusText(user: User): string {
    if (!user.active) return 'Inactivo';
    if (user.useDirectPermissions) return 'Permisos Directos';
    return 'Activo';
  }

  getInitials(user: User): string {
    return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
  }

  getRoleColor(index: number): string {
    const colors = [
      'bg-blue-100 text-blue-700 border-blue-200',
      'bg-green-100 text-green-700 border-green-200',
      'bg-purple-100 text-purple-700 border-purple-200',
      'bg-yellow-100 text-yellow-700 border-yellow-200',
      'bg-pink-100 text-pink-700 border-pink-200',
      'bg-indigo-100 text-indigo-700 border-indigo-200',
    ];
    return colors[index % colors.length];
  }
}

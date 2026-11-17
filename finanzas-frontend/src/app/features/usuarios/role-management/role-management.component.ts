import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoleService } from '../../../services/role.service';
import { Role, RoleRequest } from '../../../models/role.model';

import {
  BreadcrumbComponent,
  BreadcrumbItem,
} from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { AlertasService } from '../../../services/alertas.service';
import { PermissionService } from '../../../services/permission.service';
import { Permission } from '../../../models/permission.model';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';

import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    PageHeaderComponent,
    ModalComponent,
    PaginationComponent,
    SearchGenericComponent,
    AlertasComponent,
  ],
  templateUrl: './role-management.component.html',
  styleUrls: ['./role-management.component.css'],
})
export class RoleManagementComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Gestión de Roles' },
  ];

  roles: Role[] = [];
  permissions: Permission[] = [];
  showModal = false;
  isEditMode = false;
  selectedRole: Role | null = null;
  formData: RoleRequest = this.getEmptyForm();
  selectedPermissions: Set<number> = new Set();
  loading = false;
  saving = false;

  // Search & Filter state
  searchQuery = '';
  searchType = 'ALL';
  showActiveOnly = true;
  currentRolePage = 0;
  rolePageSize = 10;
  totalRoleElements = 0;
  totalRolePages = 0; // Se asegura una línea

  // Opciones de búsqueda estables
  roleSearchOptions = [
    { label: 'Todos', value: 'ALL' },
    { label: 'Nombre', value: 'NOMBRE' }
  ];

  permissionSearchOptions = [
    { label: 'Todos', value: 'ALL' },
    { label: 'Nombre', value: 'NOMBRE' },
    { label: 'Código', value: 'CODIGO' }
  ];

  // Permission Search state
  searchPermissionQuery = '';
  searchPermissionType = 'ALL';
  currentPermissionPage = 0;
  permissionPageSize = 10;
  totalPermissionElements = 0;
  totalPermissionPages = 0;
  currentModulo = 'Sistema'; // Contexto actual de Finanzas

  constructor(
    private roleService: RoleService,
    private permissionService: PermissionService,
    private alertasService: AlertasService,
    public sidebarService: SidebarService,
    private cdRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.executeSearch(0, this.rolePageSize);
  }

  handleSearch(event: { q: string; type: string }) {
    this.searchQuery = event.q;
    this.searchType = event.type;
    this.currentRolePage = 0;
    this.executeSearch(0, this.rolePageSize);
  }

  executeSearch(page: number = 0, size: number = 10): void {
    setTimeout(() => {
      this.loading = true;
      this.cdRef.detectChanges();
    });
    let request;

    if (this.searchQuery && this.searchQuery.trim().length > 0) {
      request = this.roleService.searchByFilter(this.searchQuery, this.searchType, page, size, this.currentModulo);
    } else {
      request = this.showActiveOnly
        ? this.roleService.getActiveRoles(page, size, 'name', this.currentModulo)
        : this.roleService.getAllRoles(page, size, 'name', this.currentModulo);
    }

    request.subscribe({
      next: (response) => {
        setTimeout(() => {
          if (response.success && response.data) {
            this.roles = response.data.content;
            this.totalRoleElements = response.data.totalElements;
            this.totalRolePages = response.data.totalPages;
            this.currentRolePage = response.data.number;
          } else {
            this.alertasService.warning('Atención', response.message || 'No se pudieron cargar los roles');
          }
          this.loading = false;
          this.cdRef.detectChanges();
        });
      },
      error: (error) => {
        setTimeout(() => {
          this.loading = false;
          this.cdRef.detectChanges();

          this.alertasService.error(
            'Error al cargar roles',
            error.error?.message || error.message || 'No se pudo conectar con el servidor'
          );
        });
      },
    });
  }

  // Alias para compatibilidad
  loadRoles(page: number = 0, size: number = 10): void {
    this.executeSearch(page, size);
  }

  handlePermissionSearch(event: { q: string; type: string }) {
    this.searchPermissionQuery = event.q;
    this.searchPermissionType = event.type;
    this.currentPermissionPage = 0;
    this.executePermissionSearch(0, this.permissionPageSize);
  }

  executePermissionSearch(page: number = 0, size: number = 10): void {

    let request;
    if (this.searchPermissionQuery && this.searchPermissionQuery.trim().length > 0) {
      request = this.permissionService.searchByFilter(
        this.searchPermissionQuery,
        this.searchPermissionType,
        page,
        size,
        this.currentModulo
      );
    } else {
      request = this.permissionService.getActivePermissions(page, size, 'name', this.currentModulo);
    }

    request.subscribe({
      next: (response) => {
        setTimeout(() => {
          if (response.success && response.data) {
            this.permissions = response.data.content;
            this.totalPermissionElements = response.data.totalElements;
            this.totalPermissionPages = response.data.totalPages;
            this.currentPermissionPage = response.data.number;
          } else {
            this.alertasService.warning('Atención', response.message || 'No se pudieron cargar los permisos');
          }
          this.cdRef.detectChanges();
        });
      },
      error: (error) => {
        setTimeout(() => {
          this.cdRef.detectChanges();
          this.alertasService.error(
            'Error al cargar permisos',
            error.error?.message || error.message || 'No se pudo conectar con el servidor'
          );
        });
      },
    });
  }

  // Alias
  loadPermissions(page: number = 0, size: number = 10): void {
    this.executePermissionSearch(page, size);
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedRole = null;
    this.formData = this.getEmptyForm();
    this.selectedPermissions.clear();
    this.currentPermissionPage = 0;

    setTimeout(() => {
      this.showModal = true;
      this.loadPermissions(0, this.permissionPageSize);
      this.cdRef.detectChanges();
    });
  }

  openEditModal(role: Role): void {
    this.isEditMode = true;
    this.selectedRole = role;
    this.formData = {
      name: role.name,
      description: role.description,
      permissionIds: role.permissions.map((p) => p.id),
      active: role.active,
    };
    this.selectedPermissions = new Set(role.permissions.map((p) => p.id));
    this.currentPermissionPage = 0;

    setTimeout(() => {
      this.showModal = true;
      this.loadPermissions(0, this.permissionPageSize);
      this.cdRef.detectChanges();
    });
  }

  closeModal(): void {
    this.showModal = false;
    this.isEditMode = false;
    this.selectedRole = null;
    this.formData = this.getEmptyForm();
    this.selectedPermissions.clear();
    this.currentPermissionPage = 0;
    this.permissions = [];
  }

  saveRole(): void {
    if (!this.formData.name.trim()) {
      this.alertasService.warning('Atención', 'El nombre del rol es obligatorio');
      return;
    }

    if (this.selectedPermissions.size === 0) {
      this.alertasService.warning('Atención', 'Debe seleccionar al menos un permiso');
      return;
    }

    this.formData.permissionIds = Array.from(this.selectedPermissions);

    // Defer saving state to avoid NG0100
    setTimeout(() => {
      this.saving = true;
      this.cdRef.detectChanges();
    });

    const request = this.isEditMode && this.selectedRole
      ? this.roleService.updateRole(this.selectedRole.id, this.formData)
      : this.roleService.createRole(this.formData);

    request.subscribe({
      next: (response) => {
        setTimeout(() => {
          this.saving = false;
          if (response.success) {
            this.closeModal();
            this.loadRoles(this.currentRolePage, this.rolePageSize);
            this.alertasService.success(
              '¡Éxito!',
              response.message || (this.isEditMode ? 'Rol actualizado exitosamente' : 'Rol creado exitosamente')
            );
          } else {
            this.alertasService.error('Error', response.message || 'No se pudo guardar el rol');
          }
          this.cdRef.detectChanges();
        });
      },
      error: (error) => {
        setTimeout(() => {
          this.saving = false;
          this.alertasService.error('Error al guardar', error.error?.message || 'Error de conexión');
          this.cdRef.detectChanges();
        });
      },
    });
  }

  openDeleteConfirm(role: Role): void {
    this.alertasService.confirm(
      '¿Estás seguro?',
      `Vas a eliminar el rol: ${role.name}`,
      'SÍ, ELIMINAR',
      'CANCELAR',
      'bg-rose-500 hover:bg-rose-600 shadow-rose-500/30'
    ).then((confirmed) => {
      if (confirmed) {
        this.confirmDelete(role.id);
      }
    });
  }

  confirmDelete(roleId: number): void {
    this.saving = true;

    this.roleService.deleteRole(roleId).subscribe({
      next: (response) => {
        this.saving = false;
        if (response.success) {
          this.alertasService.success('¡Éxito!', response.message || 'Rol eliminado exitosamente');
          this.loadRoles(this.currentRolePage, this.rolePageSize);
        } else {
          this.alertasService.error('Error', response.message || 'No se pudo eliminar el rol');
        }
      },
      error: (error) => {
        this.saving = false;
        this.alertasService.error('Error', error.error?.message || error.message || 'Error al eliminar');
      },
    });
  }

  togglePermission(permissionId: number): void {
    if (this.selectedPermissions.has(permissionId)) {
      this.selectedPermissions.delete(permissionId);
    } else {
      this.selectedPermissions.add(permissionId);
    }
  }

  hasPermission(permissionId: number): boolean {
    return this.selectedPermissions.has(permissionId);
  }

  getEmptyForm(): RoleRequest {
    return {
      name: '',
      description: '',
      permissionIds: [],
      active: true,
    };
  }

  getPermissionShortName(permissionName: string): string {
    return permissionName.replace(/_/g, ' ');
  }

  getPermissionActionType(permissionName: string): string {
    const name = permissionName.toUpperCase();
    if (name.includes('READ') || name.includes('VIEW') || name.includes('LEER')) return 'read';
    if (name.includes('CREATE') || name.includes('CREAR')) return 'create';
    if (name.includes('UPDATE') || name.includes('EDIT') || name.includes('ACTUALIZAR'))
      return 'update';
    if (name.includes('DELETE') || name.includes('ELIMINAR')) return 'delete';
    if (name.includes('BLOQUEAR')) return 'block';
    return 'other';
  }

  onRolePageChange(page: number): void {
    this.loadRoles(page, this.rolePageSize);
    this.cdRef.detectChanges();
  }

  onRolePageSizeChange(size: number): void {
    this.rolePageSize = size;
    this.currentRolePage = 0;
    this.loadRoles(0, size);
  }

  onPermissionPageChange(page: number): void {
    this.loadPermissions(page, this.permissionPageSize);
    this.cdRef.detectChanges();
  }

  onPermissionPageSizeChange(size: number): void {
    this.permissionPageSize = size;
    this.currentPermissionPage = 0;
    this.loadPermissions(0, size);
  }
}

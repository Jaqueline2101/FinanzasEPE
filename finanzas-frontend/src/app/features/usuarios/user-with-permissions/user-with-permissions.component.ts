// src/app/components/user-permission-management/user-permission-management.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserPermissionService } from '../../../services/user-permission.service';
import { UserService } from '../../../services/user.service';
import {
  AssignDirectPermissionsRequest,
  UserWithPermissionsResponse,
} from '../../../models/user-permission.model';
import {
  BreadcrumbItem,
  BreadcrumbComponent,
} from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { PermissionService } from '../../../services/permission.service';
import Swal from 'sweetalert2';
import { Permission } from '../../../models/permission.model';
import { UserResponse } from '../../../models/user.model';

interface ModulePermissionsGroup {
  module: string;
  displayName: string;
  icon: string;
  permissions: Permission[];
}

@Component({
  selector: 'app-user-permission-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    PageHeaderComponent,
  ],
  templateUrl: './user-with-permissions.component.html',
  styleUrls: ['./user-with-permissions.component.css'],
})
export class UserPermissionManagementComponent implements OnInit {
  // #region Breadcrumb
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuraciones', route: '/configuraciones' },
    { label: 'Usuarios' },
  ];
  // #endregion
  // #region header
  totalElements: number = 0;
  tituloPagina: string = 'Permisos Directos por Usuario ';
  nombre: string = '';

  // #endregion
  // Listas
  users: UserResponse[] = [];
  allPermissions: Permission[] = [];
  moduleGroups: ModulePermissionsGroup[] = [];
  selectedUser: UserWithPermissionsResponse | null = null;
  selectedPermissions: Set<number> = new Set();
  originalPermissions: Set<number> = new Set();
  loading = false;
  saving = false;
  loadingUser = false;
  useDirectPermissions = false;
  searchTerm = '';
  showActiveOnly = true;
  permissionReason = '';
  expirationDate: string | null = null;
  message = '';
  messageType: 'success' | 'error' | 'info' | 'warning' = 'info';
  showClearConfirm = false;
  showModeToggleConfirm = false;

  constructor(
    private userPermissionService: UserPermissionService,
    private userService: UserService,
    private permissionService: PermissionService,
    private cdRef: ChangeDetectorRef,
    public sidebarService: SidebarService
  ) { }

  ngOnInit(): void {
    this.loadUsers();
    this.loadPermissions();
  }

  // ========== CARGA DE DATOS ==========

  loadUsers(): void {
    this.loading = true;
    const request = this.showActiveOnly
      ? this.userService.getActiveUsers(0, 100) // Ajustado a 100 por restricción de API
      : this.userService.getAllUsers(0, 100);

    request.subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.users = response.data.content;
          this.totalElements = response.data.totalElements;
        }
        this.loading = false;
        this.cdRef.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.toastError(err.error?.message || 'Error al cargar usuarios');
      },
    });
  }

  loadPermissions(): void {
    this.permissionService.getAllPermissions(0, 100).subscribe({
      next: (response) => {
        this.allPermissions = response.data.content;
        this.groupPermissionsByModule();
      },
      error: (err) =>
        this.toastError(err.error?.message || 'No se pudieron cargar los permisos base'),
    });
  }

  loadUserDetails(id: number): void {
    this.loadingUser = true;
    this.userPermissionService.getUserWithPermissions(id).subscribe({
      next: (response) => {
        this.loadingUser = false;
        if (response.success && response.data) {
          this.selectedUser = response.data;
          this.useDirectPermissions = this.selectedUser.useDirectPermissions;

          // Mapeamos los permisos directos actuales
          const permissionsArray = this.selectedUser.directPermissions || [];
          this.selectedPermissions = new Set(permissionsArray.map((dp: any) => dp.permission.id));
          this.originalPermissions = new Set(this.selectedPermissions);

          this.cdRef.detectChanges();
        } else {
          // Bloque de Atención si success es false
          Swal.fire({
            icon: 'warning',
            title: 'Atención',
            text: response.message || 'No se pudo obtener la información detallada del usuario',
          });
        }
      },
      error: (err) => {
        this.loadingUser = false;
        console.error('Error al cargar detalles:', err);

        // Bloque de Error técnico
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text:
            err.error?.message || 'Error de conexión al intentar cargar los detalles del usuario',
        });
      },
    });
  }

  groupPermissionsByModule(): void {
    // 1. Verificación de seguridad: si no hay permisos, limpiar grupos y salir
    if (!this.allPermissions) {
      this.moduleGroups = [];
      return;
    }

    const grouped = new Map<string, Permission[]>();

    this.allPermissions.forEach((permission) => {
      // Aseguramos que el módulo siempre sea un string, incluso si viene null de la DB
      const moduleName = permission.module || 'general';

      if (!grouped.has(moduleName)) {
        grouped.set(moduleName, []);
      }
      grouped.get(moduleName)!.push(permission);
    });

    this.moduleGroups = Array.from(grouped.entries())
      .map(([module, permissions]) => ({
        module: module,
        displayName: this.getModuleDisplayName(module),
        icon: this.getModuleIcon(module),
        // Verificación extra para evitar errores en localeCompare
        permissions: (permissions || []).sort((a, b) => (a.name || '').localeCompare(b.name || '')),
      }))
      .sort((a, b) => (a.displayName || '').localeCompare(b.displayName || ''));
  }

  // ========== SELECCIÓN DE USUARIO ==========

  selectUser(user: UserResponse): void {
    console.log('Usuario seleccionado:', user);

    // Limpiamos estados previos antes de cargar el nuevo
    this.selectedUser = null;
    this.selectedPermissions = new Set();
    this.originalPermissions = new Set();
    this.permissionReason = '';
    this.expirationDate = null;

    // Validación preventiva
    if (!user || !user.id) {
      Swal.fire({
        icon: 'warning',
        title: 'Atención',
        text: 'El usuario seleccionado no tiene un identificador válido.',
      });
      return;
    }

    // Llamamos a la carga de detalles (que ya tiene sus propios Swal configurados)
    this.loadUserDetails(user.id);
  }

  // ========== GESTIÓN DE PERMISOS ==========

  hasPermission(permissionId: number): boolean {
    return this.selectedPermissions.has(permissionId);
  }

  areAllModulePermissionsSelected(module: ModulePermissionsGroup): boolean {
    return module.permissions.every((p) => this.selectedPermissions.has(p.id));
  }

  areSomeModulePermissionsSelected(module: ModulePermissionsGroup): boolean {
    const selectedCount = module.permissions.filter((p) =>
      this.selectedPermissions.has(p.id)
    ).length;
    return selectedCount > 0 && selectedCount < module.permissions.length;
  }

  hasChanges(): boolean {
    if (this.selectedPermissions.size !== this.originalPermissions.size) {
      return true;
    }

    for (const id of this.selectedPermissions) {
      if (!this.originalPermissions.has(id)) {
        return true;
      }
    }

    return false;
  }

  // ========== GUARDAR PERMISOS ==========

  savePermissions(): void {
    if (!this.selectedUser || !this.useDirectPermissions) return;

    // Validación de motivo con formato Atención
    if (!this.permissionReason.trim()) {
      Swal.fire({
        icon: 'warning',
        title: 'Atención',
        text: 'Debes proporcionar un motivo para asignar permisos especiales',
      });
      return;
    }

    Swal.fire({
      title: '¿Guardar cambios?',
      text: 'Se actualizarán los permisos directos del usuario.',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, guardar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#3b82f6',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (result.isConfirmed) {
        this.executeSave();
      }
    });
  }

  private executeSave(): void {
    // Mostrar loading mientras se procesa la petición
    Swal.fire({
      title: 'Procesando...',
      text: 'Actualizando privilegios del usuario',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });

    this.saving = true;
    const request: AssignDirectPermissionsRequest = {
      userId: this.selectedUser!.id,
      permissionIds: Array.from(this.selectedPermissions),
      reason: this.permissionReason,
      expiresAt: this.expirationDate || undefined,
    };

    this.userPermissionService.assignDirectPermissions(request).subscribe({
      next: (response: any) => {
        this.saving = false;

        if (response.success) {
          Swal.fire({
            icon: 'success',
            title: '¡Actualizado!',
            text: response.message || 'Permisos actualizados correctamente',
            timer: 2000,
            showConfirmButton: false,
          });

          this.selectedUser = response.data;
          this.originalPermissions = new Set(this.selectedPermissions);
          this.loadUsers(); // Refrescar lista general
        } else {
          // Manejo de respuesta success: false (Atención)
          Swal.fire({
            icon: 'warning',
            title: 'Atención',
            text: response.message || 'El servidor no pudo procesar la actualización',
          });
        }
      },
      error: (err) => {
        this.saving = false;
        console.error('Error al guardar permisos:', err);

        // Bloque de Error técnico
        Swal.fire({
          icon: 'error',
          title: 'Error en la operación',
          text: err.error?.message || 'Error de conexión al intentar guardar los cambios',
        });
      },
    });
  }
  cancelChanges(): void {
    this.selectedPermissions = new Set(this.originalPermissions);
  }

  // ========== CAMBIAR MODO ==========

  toggleMode(): void {
    if (!this.selectedUser) return;

    const willUseDirect = !this.useDirectPermissions;

    // Validación: No permitir modo ROL si el usuario no tiene roles asignados
    if (!willUseDirect && (!this.selectedUser.roles || this.selectedUser.roles.length === 0)) {
      Swal.fire({
        icon: 'warning',
        title: 'Atención',
        text: 'El usuario no tiene roles asignados. No se puede activar el modo "POR ROL" sin una base de permisos.',
        confirmButtonColor: '#1e293b',
      });
      return;
    }

    Swal.fire({
      title: 'Cambiar modo de permisos',
      text: `¿Deseas cambiar al modo ${willUseDirect ? 'DIRECTO' : 'POR ROL'}?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, cambiar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#8b5cf6',
      cancelButtonColor: '#64748b',
      reverseButtons: true,
    }).then((result) => {
      if (result.isConfirmed) {
        this.confirmToggleMode(willUseDirect);
      }
    });
  }

  private confirmToggleMode(newMode: boolean): void {
    // Mostrar Loading estético
    Swal.fire({
      title: 'Cambiando modo...',
      text: 'Espere un momento por favor',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });

    this.saving = true;
    this.userPermissionService
      .togglePermissionMode({
        userId: this.selectedUser!.id,
        useDirectPermissions: newMode,
      })
      .subscribe({
        next: (response: any) => {
          this.saving = false;

          if (response.success) {
            Swal.fire({
              icon: 'success',
              title: 'Modo Actualizado',
              text: `El modo se cambió exitosamente a: ${newMode ? 'DIRECTO' : 'ROL'}`,
              timer: 2000,
              showConfirmButton: false,
            });

            this.selectedUser = response.data;
            this.useDirectPermissions = newMode;
            if (this.selectedUser) {
              this.loadUserDetails(this.selectedUser.id);
            }
          } else {
            // Manejo de Atención si success es false
            Swal.fire({
              icon: 'warning',
              title: 'Atención',
              text: response.message || 'No se pudo completar el cambio de modo',
            });
          }
        },
        error: (err: any) => {
          this.saving = false;
          console.error('Error al cambiar modo:', err);

          // Manejo de Error técnico
          Swal.fire({
            icon: 'error',
            title: 'Error en la operación',
            text: err.error?.message || 'Error de conexión al intentar cambiar el modo de permisos',
          });
        },
      });
  }

  cancelToggleMode(): void {
    this.showModeToggleConfirm = false;
  }

  // ========== LIMPIAR PERMISOS ==========

  openClearConfirm(): void {
    this.showClearConfirm = true;
  }

  confirmClearPermissions(): void {
    if (!this.selectedUser) return;

    Swal.fire({
      title: '¿Limpiar todos los permisos?',
      text: `Se eliminarán permanentemente todos los permisos directos asignados a "${this.selectedUser.fullName}". El usuario volverá a depender únicamente de sus roles.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#64748b',
      confirmButtonText: 'Sí, limpiar todo',
      cancelButtonText: 'Cancelar',
      reverseButtons: true,
    }).then((result) => {
      if (result.isConfirmed) {
        this.executeClearPermissions();
      }
    });
  }

  private executeClearPermissions(): void {
    // Mostrar Loading
    Swal.fire({
      title: 'Limpiando...',
      text: 'Eliminando privilegios directos',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });

    this.saving = true;
    this.userPermissionService.clearAllDirectPermissions(this.selectedUser!.id).subscribe({
      next: (response: any) => {
        this.saving = false;

        if (response.success) {
          Swal.fire({
            icon: 'success',
            title: '¡Limpieza completada!',
            text: response.message || 'Todos los permisos directos han sido eliminados',
            timer: 2000,
            showConfirmButton: false,
          });

          this.loadUserDetails(this.selectedUser!.id);
          this.showClearConfirm = false; // Solo si usas un modal adicional
          this.loadUsers(); // Refrescar lista general
        } else {
          // Bloque de Atención
          Swal.fire({
            icon: 'warning',
            title: 'Atención',
            text: response.message || 'No se pudo completar la limpieza de permisos',
          });
        }
      },
      error: (error: any) => {
        this.saving = false;
        console.error('Error al limpiar permisos:', error);

        // Bloque de Error Técnico
        Swal.fire({
          icon: 'error',
          title: 'Error en la operación',
          text: error.error?.message || 'Error de conexión al intentar limpiar los permisos',
        });
      },
    });
  }

  cancelClearPermissions(): void {
    this.showClearConfirm = false;
  }

  // ========== UTILIDADES ==========

  get filteredUsers(): UserResponse[] {
    return this.users.filter(
      (user) =>
        user.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.firstName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  getPermissionShortName(permissionName: string): string {
    const parts = permissionName.split('_');
    return parts.length > 1 ? parts.slice(1).join(' ') : permissionName;
  }

  getPermissionActionType(permissionName: string): string {
    if (permissionName.includes('READ') || permissionName.includes('VIEW')) return 'read';
    if (permissionName.includes('CREATE')) return 'create';
    if (permissionName.includes('UPDATE') || permissionName.includes('EDIT')) return 'update';
    if (permissionName.includes('DELETE')) return 'delete';
    if (permissionName.includes('VALIDATE')) return 'validate';
    if (permissionName.includes('ENTRY')) return 'entry';
    return 'other';
  }

  getModuleDisplayName(module: string): string {
    const names: Record<string, string> = {
      patient: 'Pacientes',
      order: 'Órdenes',
      result: 'Resultados',
      lab_area: 'Niveles',
      exam: 'Exámenes',
      equipment: 'Equipos',
      user: 'Usuarios',
      role: 'Roles',
      permission: 'Permisos',
      report: 'Reportes',
      audit: 'Auditoría',
      config: 'Configuración',
      unit: 'Unidades',
    };
    return names[module] || module;
  }

  getModuleIcon(module: string): string {
    const icons: Record<string, string> = {
      patient: '👤',
      order: '📝',
      result: '🧪',
      lab_area: '🏢',
      exam: '🔬',
      equipment: '⚙️',
      user: '👥',
      role: '🔐',
      permission: '🔑',
      report: '📊',
      audit: '📋',
      config: '⚡',
      unit: '📏',
    };
    return icons[module] || '⚡';
  }

  showMessage(text: string, type: 'success' | 'error' | 'info' | 'warning'): void {
    this.message = text;
    this.messageType = type;

    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  toggleActiveFilter(): void {
    this.showActiveOnly = !this.showActiveOnly;
    this.loadUsers();
  }

  getStatusBadgeClass(user: UserResponse): string {
    if (user.useDirectPermissions) {
      return 'bg-purple-100 text-purple-800';
    }
    return 'bg-blue-100 text-blue-800';
  }

  getStatusText(user: UserResponse): string {
    return user.useDirectPermissions ? 'Permisos Directos' : 'Por Rol';
  }
  /**
   * Verifica si un permiso viene del rol del usuario
   */
  isPermissionFromRole(permissionId: number): boolean {
    return this.selectedUser?.rolePermissions?.some((rp: any) => rp.id === permissionId) || false;
  }

  /**
   * Verifica si un permiso es solo directo (no viene del rol)
   */
  isPermissionOnlyDirect(permissionId: number): boolean {
    return this.hasPermission(permissionId) && !this.isPermissionFromRole(permissionId);
  }

  /**
   * Obtiene el origen del permiso (ROL, DIRECTO, o AMBOS)
   */
  getPermissionOrigin(permissionId: number): 'ROL' | 'DIRECTO' | 'AMBOS' | 'NINGUNO' {
    const hasFromRole = this.isPermissionFromRole(permissionId);
    const hasDirectPermission = this.selectedPermissions.has(permissionId);

    if (hasFromRole && hasDirectPermission) return 'AMBOS';
    if (hasFromRole) return 'ROL';
    if (hasDirectPermission) return 'DIRECTO';
    return 'NINGUNO';
  }

  /**
   * Verifica si el checkbox debe estar deshabilitado
   * Se deshabilita si el permiso viene del rol y NO estamos en modo híbrido
   */
  isPermissionDisabled(permissionId: number): boolean {
    // Si viene del rol, el checkbox se deshabilita (solo lectura)
    return this.isPermissionFromRole(permissionId);
  }

  /**
   * Toggle de permiso modificado para no permitir desmarcar permisos de rol
   */
  togglePermission(permissionId: number): void {
    if (this.isPermissionFromRole(permissionId)) return;

    if (this.selectedPermissions.has(permissionId)) {
      this.selectedPermissions.delete(permissionId);
    } else {
      this.selectedPermissions.add(permissionId);
    }
  }

  /**
   * Toggle de todos los permisos de un módulo (modificado)
   */
  toggleAllPermissionsInModule(module: ModulePermissionsGroup, event: any): void {
    const checked = event.target.checked;

    module.permissions.forEach((permission) => {
      // Solo afectar permisos que NO vienen del rol
      if (!this.isPermissionFromRole(permission.id)) {
        if (checked) {
          this.selectedPermissions.add(permission.id);
        } else {
          this.selectedPermissions.delete(permission.id);
        }
      }
    });
  }

  /**
   * Verifica si hay permisos del rol en el módulo
   */
  hasRolePermissionsInModule(module: ModulePermissionsGroup): boolean {
    return module.permissions.some((p) => this.isPermissionFromRole(p.id));
  }

  /**
   * Cuenta cuántos permisos del módulo vienen del rol
   */
  getRolePermissionsCount(module: ModulePermissionsGroup): number {
    return module.permissions.filter((p) => this.isPermissionFromRole(p.id)).length;
  }

  /**
   * Cuenta cuántos permisos directos tiene el módulo (sin contar los del rol)
   */
  getDirectPermissionsCount(module: ModulePermissionsGroup): number {
    return module.permissions.filter(
      (p) => this.selectedPermissions.has(p.id) && !this.isPermissionFromRole(p.id)
    ).length;
  }
  private toastSuccess(msg: string) {
    Swal.fire({
      icon: 'success',
      title: msg,
      timer: 2000,
      showConfirmButton: false,
      toast: true,
      position: 'top-end',
    });
  }

  private toastError(msg: string) {
    Swal.fire({ icon: 'error', title: 'Error', text: msg, confirmButtonColor: '#3b82f6' });
  }

  private toastWarning(msg: string) {
    Swal.fire({ icon: 'warning', title: 'Atención', text: msg, confirmButtonColor: '#f59e0b' });
  }
}

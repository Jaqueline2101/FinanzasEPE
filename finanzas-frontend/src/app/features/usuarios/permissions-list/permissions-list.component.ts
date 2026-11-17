import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  BreadcrumbComponent,
  BreadcrumbItem,
} from '../../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { SidebarService } from '../../../shared/sidebar/sidebar.service';
import { PermissionService } from '../../../services/permission.service';
import { Permission } from '../../../models/permission.model';
import Swal from 'sweetalert2';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { forkJoin } from 'rxjs';

interface PermissionGroup {
  module: string;
  displayName: string;
  icon: string;
  permissions: Permission[];
  expanded: boolean;
}

import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';

@Component({
  selector: 'app-permissions-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BreadcrumbComponent,
    PageHeaderComponent,
    PaginationComponent,
    SearchGenericComponent,
  ],
  templateUrl: './permissions-list.component.html',
  styleUrls: ['./permissions-list.component.css'],
})
export class PermissionsListComponent implements OnInit {
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Seguridad', route: '/configuraciones' },
    { label: 'Permisos del Sistema' },
  ];

  permissionGroups: PermissionGroup[] = [];
  loading = false;
  searchTerm = '';
  searchType = 'ALL';
  selectedModule: string | null = null;
  moduleList: string[] = [];
  // Estadísticas
  totalPermissions = 0;
  totalModules = 0;
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;

  constructor(
    private permissionService: PermissionService,
    public sidebarService: SidebarService,
    private cdRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadModules();
    this.loadPermissions();
  }

  loadModules(): void {
    this.permissionService.getAllModules().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.moduleList = response.data;
          this.totalModules = this.moduleList.length;
          this.cdRef.detectChanges();
        }
      },
      error: (error) => console.error('Error loading modules', error),
    });
  }

  // Al cambiar el select en el HTML
  onModuleChange(): void {
    this.currentPage = 0; // Resetear página al filtrar
    this.loadPermissions(); // Llamar al método de carga principal
  }

  handleSearch(event: { q: string; type: string }): void {
    this.searchTerm = event.q;
    this.searchType = event.type;
    this.currentPage = 0;
    this.loadPermissions();
  }

  loadPermissions(): void {
    this.loading = true;
    let permissionsRequest$;

    if (this.searchTerm && this.searchTerm.trim().length > 0) {
      permissionsRequest$ = this.permissionService.searchByFilter(
        this.searchTerm,
        this.searchType,
        this.currentPage,
        this.pageSize
      );
    } else if (this.selectedModule) {
      permissionsRequest$ = this.permissionService.getPermissionsByModule(
        this.selectedModule,
        this.currentPage,
        this.pageSize
      );
    } else {
      permissionsRequest$ = this.permissionService.getAllPermissions(
        this.currentPage,
        this.pageSize
      );
    }

    permissionsRequest$.subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.groupPermissions(response.data.content);
          this.totalPermissions = response.data.totalElements;
          this.totalPages = response.data.totalPages;
          this.currentPage = response.data.number;
        }
        this.cdRef.detectChanges();
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading permissions', error);
        this.cdRef.detectChanges();
      },
    });
  }

  groupPermissions(permissions: Permission[]): void {
    const grouped = new Map<string, Permission[]>();

    permissions.forEach((permission) => {
      const moduleName = permission.module || 'general';
      if (!grouped.has(moduleName)) {
        grouped.set(moduleName, []);
      }
      grouped.get(moduleName)!.push(permission);
    });

    this.permissionGroups = Array.from(grouped.entries())
      .map(([module, perms]) => ({
        module,
        displayName: this.getModuleDisplayName(module),
        icon: this.getModuleIcon(module),
        permissions: perms.sort((a, b) => a.name.localeCompare(b.name)),
        expanded: true, // Expandir por defecto para ver resultados de búsqueda mejor
      }))
      .sort((a, b) => a.displayName.localeCompare(b.displayName));
  }

  // filteredGroups getter removed as filtering is now backend-side
  toggleModule(group: PermissionGroup): void {
    group.expanded = !group.expanded;
  }

  expandAll(): void {
    this.permissionGroups.forEach((g) => (g.expanded = true));
  }

  collapseAll(): void {
    this.permissionGroups.forEach((g) => (g.expanded = false));
  }

  get filteredGroups(): PermissionGroup[] {
    return this.permissionGroups;
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

  getActionColor(type: string): string {
    const colors: Record<string, string> = {
      read: 'bg-blue-100 text-blue-700 border-blue-200',
      create: 'bg-green-100 text-green-700 border-green-200',
      update: 'bg-yellow-100 text-yellow-700 border-yellow-200',
      delete: 'bg-red-100 text-red-700 border-red-200',
      validate: 'bg-purple-100 text-purple-700 border-purple-200',
      entry: 'bg-indigo-100 text-indigo-700 border-indigo-200',
      other: 'bg-gray-100 text-gray-700 border-gray-200',
    };
    return colors[type] || colors['other'];
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
    return icons[module] || '📦';
  }

  exportPermissions(): void {
    console.log('Exportar permisos');
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadPermissions();
  }
  changePageSize(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 0;
    this.loadPermissions();
  }
}

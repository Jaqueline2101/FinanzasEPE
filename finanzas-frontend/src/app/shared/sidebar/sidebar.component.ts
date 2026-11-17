import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { SidebarService } from './sidebar.service';
import { filter } from 'rxjs/operators';
import { MenuItemDTO } from './menu-dinamico.service';
import { PermissionService2 } from '../../features/auth/services/permission2.service';
import { MenuDinamicoService } from './menu-dinamico.service';
import { AuthService } from '../../features/auth/services/auth.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  usuarioActual: string = '';
  userGender: string = 'null';
  bien: string = 'Bienvenide';
  rutaActual: string = '';
  menuDinamicoSanitizado: MenuItemDTO[] = [];
  loadingMenu: boolean = false;
  menuCargado: boolean = false;

  grupoExpandido: { [key: number]: boolean } = {};
  submenuExpandido: { [key: number]: boolean } = {};

  constructor(
    private router: Router,
    public sidebarService: SidebarService,
    private cdRef: ChangeDetectorRef,
    public permissionService: PermissionService2,
    private menuService: MenuDinamicoService,
    private authService: AuthService,
    private sanitizer: DomSanitizer
  ) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.rutaActual = event.url;
      this.expandirMenuSegunRuta(event.url);
    });
  }

  ngOnInit() {
    this.cargarPerfil();
    this.rutaActual = this.router.url;

    // Usar effect para reaccionar a cambios en el signal currentUser
    // Pero como estamos en un componente, usaremos una suscripción al signal de forma indirecta
    // o simplemente confiaremos en la carga inicial y el login success que refresca el componente
    
    const userStorage = localStorage.getItem('currentUser');
    if (userStorage) {
      try {
        const userData = JSON.parse(userStorage);
        this.permissionService.setUser(userData);
        this.cargarPerfil();
        const idUsuario = userData.userId || userData.id;
        if (idUsuario) {
          this.cargarMenuDinamico(idUsuario);
        }
      } catch (e) {
        console.error('Error al parsear usuario:', e);
      }
    }
    this.cdRef.detectChanges();
  }

  cargarMenuDinamico(usuarioId: number): void {
    this.loadingMenu = true;
    this.menuCargado = false;
    
    this.menuService.cargarMenu(usuarioId).subscribe({
      next: (menu) => {
        this.menuDinamicoSanitizado = this.sanitizarIconosRecursivo(menu);
        this.expandirMenuSegunRuta(this.rutaActual);
        this.loadingMenu = false;
        this.menuCargado = true;
        this.cdRef.detectChanges();
      },
      error: () => {
        this.menuService.cargarMenuDesdeStorage();
        const menu = this.menuService.getMenu() || [];
        this.menuDinamicoSanitizado = this.sanitizarIconosRecursivo(menu);
        this.expandirMenuSegunRuta(this.rutaActual);
        this.loadingMenu = false;
        this.menuCargado = true;
        this.cdRef.detectChanges();
      }
    });
  }

  private sanitizarIconosRecursivo(menu: MenuItemDTO[]): MenuItemDTO[] {
    return menu.map(item => {
      const newItem = { ...item };
      if (newItem.icono && typeof newItem.icono === 'string' && newItem.icono.includes('<svg')) {
        newItem.icono = this.sanitizer.bypassSecurityTrustHtml(newItem.icono);
      }
      if (newItem.children && newItem.children.length > 0) {
        newItem.children = this.sanitizarIconosRecursivo(newItem.children);
      }
      return newItem;
    }).sort((a, b) => (a.orden || 999) - (b.orden || 999));
  }

  expandirMenuSegunRuta(url: string): void {
    Object.keys(this.grupoExpandido).forEach(id => this.grupoExpandido[+id] = false);
    Object.keys(this.submenuExpandido).forEach(id => this.submenuExpandido[+id] = false);

    this.menuDinamicoSanitizado.forEach(grupo => {
      if (this.verificarRutaEnGrupo(grupo, url)) {
        this.grupoExpandido[grupo.id] = true;
        if (grupo.children) {
          grupo.children.forEach(child => {
            if (child.tipo === 'SUBMENU' && this.verificarRutaEnGrupo(child, url)) {
              this.submenuExpandido[child.id] = true;
            }
          });
        }
      }
    });
  }

  private verificarRutaEnGrupo(item: MenuItemDTO, url: string): boolean {
    if (item.ruta && url.includes(item.ruta)) return true;
    return item.children ? item.children.some((child: MenuItemDTO) => this.verificarRutaEnGrupo(child, url)) : false;
  }

  toggleGrupo(grupoId: number): void {
    const yaExpandido = this.grupoExpandido[grupoId];

    // Si vamos a expandir este grupo, cerramos todos los demás (comportamiento acordeón)
    if (!yaExpandido) {
      Object.keys(this.grupoExpandido).forEach(id => {
        this.grupoExpandido[+id] = false;
      });
      // Opcional: Cerrar submenús abiertos al cambiar de grupo principal
      Object.keys(this.submenuExpandido).forEach(id => {
        this.submenuExpandido[+id] = false;
      });
    }

    this.grupoExpandido[grupoId] = !yaExpandido;
  }

  toggleSubmenu(submenuId: number): void {
    const yaExpandido = this.submenuExpandido[submenuId];

    // Si vamos a abrir un submenú, cerramos los demás del mismo nivel
    if (!yaExpandido) {
      Object.keys(this.submenuExpandido).forEach(id => {
        this.submenuExpandido[+id] = false;
      });
    }

    this.submenuExpandido[submenuId] = !yaExpandido;
  }

  verificarSubmenuActivo(submenu: MenuItemDTO): boolean {
    return submenu.children ? submenu.children.some((child: MenuItemDTO) => this.rutaActual === child.ruta) : false;
  }

  navegarA(ruta: string | undefined): void {
    if (ruta) this.router.navigate([ruta]);
  }

  cargarPerfil(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.usuarioActual = user.firstName + ' ' + (user.lastName || '');
      this.userGender = user.sexo || 'null';
      this.bien = this.userGender === 'M' ? 'Bienvenido' : this.userGender === 'F' ? 'Bienvenida' : 'Bienvenide';
      this.cdRef.detectChanges();
    }
  }

  getProfileImage(): string {
    const user = this.authService.currentUser();
    console.log('🖼️ Cargando imagen de perfil para:', user?.username, 'Foto:', user?.fotoPerfil);
    if (user?.fotoPerfil) return user.fotoPerfil;
    return this.userGender === 'M' ? '/male-avatar.jpg' : this.userGender === 'F' ? '/female-avatar.png' : '/default-avatar.png';
  }

  verPerfil(): void {
    this.router.navigate(['/admin/configuraciones/mi-perfil']);
  }

  cerrarSesion(): void {
    this.permissionService.clearPermisos();
    this.menuService.clearMenu();
    this.authService.logout();
  }
}

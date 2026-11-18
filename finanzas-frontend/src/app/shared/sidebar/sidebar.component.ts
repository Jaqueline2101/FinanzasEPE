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
      newItem.icono = this.resolverIcono(newItem.icono, newItem.titulo);
      if (newItem.children && newItem.children.length > 0) {
        newItem.children = this.sanitizarIconosRecursivo(newItem.children);
      }
      return newItem;
    }).sort((a, b) => (a.orden || 999) - (b.orden || 999));
  }

  private resolverIcono(icono: any, titulo?: string): SafeHtml {
    const raw = typeof icono === 'string' ? icono.trim() : '';

    if (raw.startsWith('<svg') || raw.includes('<svg')) {
      return this.sanitizer.bypassSecurityTrustHtml(raw);
    }

    const t = (titulo || '').toLowerCase();
    const ic = raw.toLowerCase();

    // Lucide SVG Icons correspondientes
    if (t.includes('dashboard') || ic.includes('chart') || ic.includes('dashboard')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-layout-dashboard"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>`
      );
    }
    if (t.includes('reporte') || ic.includes('file-invoice') || ic.includes('clipboard') || ic.includes('report')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-clipboard-plus"><rect width="8" height="4" x="8" y="2" rx="1" ry="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M9 14h6"/><path d="M12 17v-6"/></svg>`
      );
    }
    if (t.includes('config') || ic.includes('gear') || ic.includes('settings')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-settings"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.1a2 2 0 0 1-1-1.72v-.51a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>`
      );
    }
    if (t.includes('proceso') || ic.includes('cpu')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-cpu"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><path d="M15 2v2"/><path d="M15 20v2"/><path d="M2 15h2"/><path d="M2 9h2"/><path d="M20 15h2"/><path d="M20 9h2"/><path d="M9 2v2"/><path d="M9 20v2"/></svg>`
      );
    }
    if (t.includes('caja') || ic.includes('wallet')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-wallet"><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/><path d="M18 12a2 2 0 0 0 0 4h4v-4Z"/></svg>`
      );
    }
    if (t.includes('rr') || t.includes('emplead') || ic.includes('users') || ic.includes('user')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-users"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`
      );
    }
    if (t.includes('venta') || ic.includes('shopping-cart') || ic.includes('cart')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-shopping-cart"><circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/><path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/></svg>`
      );
    }
    if (t.includes('compra') || ic.includes('shopping-bag') || ic.includes('bag')) {
      return this.sanitizer.bypassSecurityTrustHtml(
        `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-shopping-bag"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/><path d="M3 6h18"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>`
      );
    }

    if (raw.includes('fa-') || raw.startsWith('fa')) {
      return this.sanitizer.bypassSecurityTrustHtml(`<i class="${raw}"></i>`);
    }

    return this.sanitizer.bypassSecurityTrustHtml(
      `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-folder"><path d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"/></svg>`
    );
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

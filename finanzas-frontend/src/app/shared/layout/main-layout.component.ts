import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';
import { SidebarService } from '../sidebar/sidebar.service';
import { NotificationService } from '../../features/notificaciones-service/notification.service';
import { inject } from '@angular/core';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="flex min-h-screen bg-gray-50">
      <app-sidebar></app-sidebar>
      <div class="flex-1 flex flex-col transition-all duration-300 pt-16" [style.padding-left]="(sidebarService.isOpen$ | async) ? '260px' : '0'"> <!-- Ajuste exacto al sidebar -->
        <app-header></app-header>
        <main class="flex-1">
            <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `
})
export class MainLayoutComponent {
  protected sidebarService = inject(SidebarService); 
  private notificationService = inject(NotificationService); // Se inyecta para activar la escucha de WebSockets
}

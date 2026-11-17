import { Component, signal, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './services/theme.service';
import { AlertasComponent } from './shared/components/alertas/alertas';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AlertasComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('finanzas');
  private themeService = inject(ThemeService);

  ngOnInit(): void {
    // El servicio ya aplica el tema en su constructor, pero puedes forzar aquí si es necesario
  }
}
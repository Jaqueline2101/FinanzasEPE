import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'app-theme';

  // Signal reactivo para el estado del tema
  isDarkMode = signal<boolean>(this.getInitialTheme());

  constructor() {
    // Aplicar tema inicial inmediatamente
    const initialTheme = this.getInitialTheme();
    this.applyTheme(initialTheme);

    // Efecto reactivo para cambios de tema
    effect(() => {
      const isDark = this.isDarkMode();
      console.log('🎨 [ThemeService] Cambio de tema:', isDark ? 'OSCURO' : 'CLARO');
      this.applyTheme(isDark);
    });
  }

  /**
   * Alterna entre modo claro y oscuro
   */
  toggleTheme(): void {
    const newValue = !this.isDarkMode();
    console.log('🔄 [ThemeService] Toggleando tema a:', newValue ? 'OSCURO' : 'CLARO');
    this.isDarkMode.set(newValue);
    localStorage.setItem(this.THEME_KEY, String(newValue));
  }

  /**
   * Establece el tema directamente
   */
  setTheme(isDark: boolean): void {
    console.log('⚙️ [ThemeService] Estableciendo tema:', isDark ? 'OSCURO' : 'CLARO');
    this.isDarkMode.set(isDark);
    localStorage.setItem(this.THEME_KEY, String(isDark));
  }

  /**
   * Obtiene el tema inicial desde localStorage
   * CORREGIDO: Lee correctamente el valor guardado
   */
  private getInitialTheme(): boolean {
    if (typeof window === 'undefined' || typeof localStorage === 'undefined') {
      return false; // Default: modo claro en SSR
    }

    try {
      const saved = localStorage.getItem(this.THEME_KEY);

      // Si no hay nada guardado, usar modo claro por defecto
      if (saved === null || saved === undefined) {
        console.log('💡 [ThemeService] No hay tema guardado, usando modo claro por defecto');
        return false;
      }

      // Parsear el valor guardado (puede ser 'true', 'false', '"true"', '"false"')
      const isDark = saved === 'true' || saved === '"true"';

      console.log('💾 [ThemeService] Tema cargado desde localStorage:', isDark ? 'OSCURO' : 'CLARO');
      return isDark;

    } catch (e) {
      console.error('❌ [ThemeService] Error al leer tema de localStorage:', e);
      return false; // Default: modo claro en caso de error
    }
  }

  /**
   * Aplica el tema al documento
   */
  private applyTheme(isDark: boolean): void {
    if (typeof window === 'undefined' || typeof document === 'undefined') {
      return;
    }

    const html = document.documentElement;
    const body = document.body;

    // Remover clase anterior y aplicar nueva al HTML
    if (isDark) {
      html.classList.add('dark');
    } else {
      html.classList.remove('dark');
    }

    // Aplicar también al body para asegurar propagación
    if (body) {
      if (isDark) {
        body.classList.add('dark');
      } else {
        body.classList.remove('dark');
      }
    }

    // Forzar reflow para asegurar que los estilos se apliquen inmediatamente
    void html.offsetHeight;
    if (body) {
      void body.offsetHeight;
    }

    console.log('✅ [ThemeService] Tema aplicado:', isDark ? 'OSCURO' : 'CLARO');
    console.log('📋 [ThemeService] Clases HTML:', html.className);
    console.log('📋 [ThemeService] Clases BODY:', body?.className || 'N/A');
  }

  /**
   * Obtiene el estado actual del tema
   */
  getCurrentTheme(): boolean {
    return this.isDarkMode();
  }

  /**
   * Verifica si el modo oscuro está activo
   */
  isDark(): boolean {
    return this.isDarkMode();
  }

  /**
   * Resetea el tema al valor por defecto (modo claro)
   */
  resetTheme(): void {
    console.log('🔄 [ThemeService] Reseteando tema a modo claro');
    this.setTheme(false);
  }
}
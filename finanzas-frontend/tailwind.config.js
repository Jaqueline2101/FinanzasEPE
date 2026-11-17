/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class', // CRÍTICO: Usa la clase 'dark' en el HTML
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Lato', 'sans-serif'],
        display: ['Montserrat', 'sans-serif'],
        body: ['Lato', 'sans-serif'],
      },
      colors: {
        // Nueva paleta corporativa (Figma)
        'corp': {
          'primary': '#1E293B',      // Fondo principal
          'secondary': '#1D4ED8',    // Botón primario
          'dark': '#0F172A',         // Texto principal / Fondo modo oscuro
          'gray-medium': '#475569',  // Texto secundario
          'gray-light': '#CBD5E1',   // Bordes/Inputs
          'gray-disabled': '#E5E7EB', // Deshabilitado
          'hover': '#1E40AF',        // Hover botón
        },
        // Colores legacy (mantener compatibilidad)
        'primary-blue': '#1e3a8a',
        'primary-green': '#16a34a',
        'primary-red': '#dc2626',
      },
      backgroundColor: {
        // Backgrounds personalizados para modo oscuro
        'dark-primary': '#0f172a',
        'dark-secondary': '#1e293b',
        'dark-tertiary': '#334155',
      },
      textColor: {
        // Textos personalizados para modo oscuro
        'dark-primary': '#f1f5f9',
        'dark-secondary': '#cbd5e1',
        'dark-muted': '#94a3b8',
      },
      borderColor: {
        // Bordes personalizados para modo oscuro
        'dark-primary': '#334155',
        'dark-secondary': '#475569',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-in': 'slideIn 0.3s ease-out',
        'slide-in-right': 'slideInRight 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideIn: {
          '0%': { opacity: '0', transform: 'translateX(-20px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        slideInRight: {
          '0%': { opacity: '0', transform: 'translateX(20px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        }
      }
    },
  },
  plugins: [],
}
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="isOpen"
      class="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn"
      (click)="onBackdropClick($event)">
      
      <div [class]="getModalClasses()" (click)="$event.stopPropagation()">
        
        <!-- Header optimizado -->
        <div class="px-5 py-3 border-b border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/20">
          <div class="flex justify-between items-center">
            <div class="flex-1">
              <ng-container *ngIf="!customHeader; else headerSlot">
                <div class="flex items-center gap-2.5">
                  <div *ngIf="showIcon" class="bg-blue-50 dark:bg-blue-900/30 p-1.5 rounded-lg border border-blue-200 dark:border-blue-800">
                    <svg class="w-4 h-4 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" [attr.d]="getIconPath()" />
                    </svg>
                  </div>
                  <h3 class="text-base font-bold text-slate-900 dark:text-slate-50">{{ title }}</h3>
                </div>
              </ng-container>
              <ng-template #headerSlot>
                <ng-content select="[header]"></ng-content>
              </ng-template>
            </div>
            
            <button *ngIf="showCloseButton" (click)="close()"
              class="text-slate-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-900/20 p-1.5 rounded-lg transition-all duration-200 ml-4">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Contenido -->
        <div class="p-5 max-h-[85vh] overflow-y-auto custom-scroll bg-white dark:bg-slate-800">
          <ng-content></ng-content>
        </div>

        <!-- Footer -->
        <div *ngIf="showFooter" class="flex justify-end gap-2 px-5 py-3 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-200 dark:border-slate-700">
          <ng-content select="[footer]"></ng-content>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes fadeIn { 
      from { opacity: 0; } 
      to { opacity: 1; } 
    }
    @keyframes slideUp { 
      from { transform: translateY(10px) scale(0.98); opacity: 0; } 
      to { transform: translateY(0) scale(1); opacity: 1; } 
    }
    .animate-fadeIn { 
      animation: fadeIn 0.2s ease-out; 
    }
    .animate-slideUp { 
      animation: slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1); 
    }
    
    /* Scrollbar delgado */
    .custom-scroll::-webkit-scrollbar { 
      width: 4px; 
    }
    .custom-scroll::-webkit-scrollbar-track { 
      background: transparent; 
    }
    .custom-scroll::-webkit-scrollbar-thumb { 
      background: #CBD5E1; 
      border-radius: 10px; 
    }
    .custom-scroll::-webkit-scrollbar-thumb:hover { 
      background: #94A3B8; 
    }
    .dark .custom-scroll::-webkit-scrollbar-thumb { 
      background: #475569; 
    }
    .dark .custom-scroll::-webkit-scrollbar-thumb:hover { 
      background: #64748B; 
    }

    /* PLACEHOLDERS: letra delgada y color gris claro */
    :host ::ng-deep input::placeholder,
    :host ::ng-deep textarea::placeholder,
    :host ::ng-deep select::placeholder {
      color: #94A3B8 !important;
      opacity: 1 !important;
      font-weight: 300 !important;
      font-size: 14px !important;
    }
    
    :host ::ng-deep .dark input::placeholder,
    :host ::ng-deep .dark textarea::placeholder,
    :host ::ng-deep .dark select::placeholder {
      color: #94A3B8 !important;
      opacity: 1 !important;
      font-weight: 300 !important;
      font-size: 14px !important;
    }

    /* INPUTS: texto en NEGRITA y NEGRO cuando se escribe */
    :host ::ng-deep input,
    :host ::ng-deep textarea,
    :host ::ng-deep select {
      border-width: 1px !important;
      font-weight: 600 !important;
      font-size: 14px !important;
      color: #1e293b !important;
    }

    :host ::ng-deep .dark input,
    :host ::ng-deep .dark textarea,
    :host ::ng-deep .dark select {
      color: #f1f5f9 !important;
    }

    /* Labels en NEGRITA */
    :host ::ng-deep label {
      font-weight: 600 !important;
      color: #1e293b !important;
    }

    :host ::ng-deep .dark label {
      color: #f1f5f9 !important;
    }
  `]
})
export class ModalComponent {
  @Input() isOpen: boolean = false;
  @Input() title: string = '';
  @Input() size: ModalSize = 'md';
  @Input() showCloseButton: boolean = true;
  @Input() showIcon: boolean = true;
  @Input() iconType: 'document' | 'edit' | 'delete' | 'info' | 'warning' | 'success' | 'custom' = 'document';
  @Input() customIconPath: string = '';
  @Input() closeOnBackdrop: boolean = false;
  @Input() showFooter: boolean = false;
  @Input() customHeader: boolean = false;

  @Output() modalClose = new EventEmitter<void>();

  close(): void {
    this.modalClose.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (this.closeOnBackdrop) this.close();
  }

  getModalClasses(): string {
    const baseClasses = 'bg-white dark:bg-slate-800 rounded-xl shadow-xl w-full overflow-hidden animate-slideUp border border-slate-200 dark:border-slate-700';

    const sizeClasses: Record<ModalSize, string> = {
      'sm': 'max-w-sm',
      'md': 'max-w-lg',
      'lg': 'max-w-2xl',
      'xl': 'max-w-4xl',
      '2xl': 'max-w-5xl',
      '3xl': 'max-w-7xl'
    };

    return `${baseClasses} ${sizeClasses[this.size]}`;
  }

  getIconPath(): string {
    if (this.iconType === 'custom' && this.customIconPath) return this.customIconPath;
    const icons: Record<string, string> = {
      'document': 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
      'edit': 'M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z',
      'delete': 'M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16',
      'info': 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
      'warning': 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      'success': 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
    };
    return icons[this.iconType] || icons['document'];
  }
}
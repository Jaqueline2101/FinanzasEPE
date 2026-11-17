import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CuentaCorriente } from '../../../models/cuenta-corriente.model';

@Component({
    selector: 'app-detalle-corrientes',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './detalle-corrientes.component.html',
    styles: [`
    .animate-fade-in { animation: fadeIn 0.2s ease-out; }
    .animate-slide-up { animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
    
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    
    @keyframes slideUp {
      from { transform: translateY(20px) scale(0.95); opacity: 0; }
      to { transform: translateY(0) scale(1); opacity: 1; }
    }
  `]
})
export class DetalleCorrientesComponent {
    @Input() isOpen = false;
    @Input() data: Partial<CuentaCorriente> = {};
    @Output() onClose = new EventEmitter<void>();

    close() {
        this.onClose.emit();
    }
}

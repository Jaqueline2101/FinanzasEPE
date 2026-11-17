import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-detalle',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html',
    styleUrls: ['./modal-detalle.component.css']
})
export class ModalDetalleComponent {
    @Input() show: boolean = false;
    @Input() movimiento: any = null;
    @Output() close = new EventEmitter<void>();

    onClose() {
        this.close.emit();
    }

    getTipoColor(tipo: string): string {
        return tipo === 'INGRESO' ? 'text-emerald-600' : 'text-red-600';
    }

    getBadgeClass(tipo: string): string {
        return tipo === 'INGRESO'
            ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
            : 'bg-red-50 text-red-700 border-red-100';
    }
}

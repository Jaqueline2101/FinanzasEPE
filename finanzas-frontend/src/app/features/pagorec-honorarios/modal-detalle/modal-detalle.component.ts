import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { PagoComprobante } from '../../../models/pago-honorarios.model';

@Component({
    selector: 'app-modal-detalle-honorarios',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleHonorariosComponent {
    @Input() isOpen = false;
    @Input() pago?: PagoComprobante;
    @Output() onClose = new EventEmitter<void>();

    close() {
        this.onClose.emit();
    }

    getMesNombre(mes?: number | string): string {
        if (!mes) return '-';
        if (typeof mes === 'string') return mes; // Ya es el nombre del mes
        const meses = [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ];
        return meses[mes - 1] || '-';
    }
}

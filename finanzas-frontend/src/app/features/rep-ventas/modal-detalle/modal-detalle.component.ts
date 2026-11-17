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
    @Input() venta: any | null = null;
    @Output() onClose = new EventEmitter<void>();

    cerrar() {
        this.onClose.emit();
    }

    get items() {
        return this.venta?.detalles || [];
    }

    get medicoNombre(): string {
        if (this.venta?.medicoNombre) return this.venta.medicoNombre;
        const firstWithMedico = this.venta?.detalles?.find((d: any) => d.medicoNombre);
        return firstWithMedico?.medicoNombre || 'NO ESPECIFICADO';
    }
}

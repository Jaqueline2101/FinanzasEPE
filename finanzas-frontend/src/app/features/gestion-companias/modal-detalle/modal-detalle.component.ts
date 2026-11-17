import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { Compania } from '../../../models/compania.model';

@Component({
    selector: 'app-modal-detalle-compania',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html',
    styles: []
})
export class ModalDetalleCompaniaComponent {
    @Input() isVisible: boolean = false;
    @Input() compania: Compania | null = null;
    @Output() onClose = new EventEmitter<void>();

    close(): void {
        this.onClose.emit();
    }
}

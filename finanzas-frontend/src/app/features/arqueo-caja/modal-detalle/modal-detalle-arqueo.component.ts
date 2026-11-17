import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { ArqueoCajaDTO } from '../../../services/arqueo-caja.service';

@Component({
    selector: 'app-modal-detalle-arqueo',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle-arqueo.component.html',
    styles: [``]
})
export class ModalDetalleArqueoComponent {
    @Input() isOpen = false;
    @Input() arqueo: ArqueoCajaDTO | null = null;
    @Output() modalClose = new EventEmitter<void>();

    close() {
        this.modalClose.emit();
    }
}

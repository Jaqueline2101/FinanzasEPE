import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { CredCtaCte } from '../../../models/cred-ctacte.model';

@Component({
    selector: 'app-modal-detalle-cuenta',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle-cuenta.component.html',
    styleUrls: ['./modal-detalle-cuenta.component.css']
})
export class ModalDetalleCuentaComponent {
    @Input() isOpen = false;
    @Input() cuenta: CredCtaCte | null = null;
    @Output() onClose = new EventEmitter<void>();

    close() {
        this.onClose.emit();
    }
}

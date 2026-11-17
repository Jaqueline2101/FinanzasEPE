import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Empresa } from '../../../services/empresa.service';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-detalle-empresa',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle-empresa.component.html'
})
export class ModalDetalleEmpresaComponent {
    @Input() isVisible: boolean = false;
    @Input() empresa: Empresa | null = null;
    @Output() onClose = new EventEmitter<void>();

    close(): void {
        this.onClose.emit();
    }
}

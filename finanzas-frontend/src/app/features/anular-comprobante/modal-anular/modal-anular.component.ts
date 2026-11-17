import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VentaResumen } from '../anular-comprobante.component';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-anular',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-anular.component.html',
})
export class ModalAnularComponent {

    @Input() venta!: VentaResumen;

    @Input() anulando = false;

    @Output() confirmar = new EventEmitter<string>();

    @Output() cancelar = new EventEmitter<void>();

    motivo = '';

    get motivoValido(): boolean {
        return this.motivo.trim().length >= 10;
    }

    get motivoChars(): number {
        return this.motivo.trim().length;
    }

    onConfirmar(): void {
        if (!this.motivoValido || this.anulando) return;
        this.confirmar.emit(this.motivo.trim());
    }

    onCancelar(): void {
        if (this.anulando) return;
        this.motivo = '';
        this.cancelar.emit();
    }

    onOverlayClick(): void {
        this.onCancelar();
    }
}

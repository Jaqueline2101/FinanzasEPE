import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-cerrar-caja-modal',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './cerrar-caja-modal.component.html'
})
export class CerrarCajaModalComponent implements OnChanges {
    @Input() isOpen: boolean = false;
    @Input() saldoTeorico: number = 0;
    @Output() modalClose = new EventEmitter<void>();
    @Output() confirmar = new EventEmitter<{ saldoReal: number; transferir: boolean }>();

    saldoReal: number = 0;
    transferir: boolean = true;
    guardando: boolean = false;

    constructor(private alertasService: AlertasService) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isOpen']?.currentValue === true) {
            this.saldoReal = this.saldoTeorico;
            this.transferir = true;
            this.guardando = false;
        }
    }

    cerrar(): void {
        if (this.guardando) return;
        this.modalClose.emit();
    }

    onSubmit(): void {
        if (this.saldoReal < 0) {
            this.alertasService.error('Validación', 'El saldo real no puede ser negativo.');
            return;
        }
        this.guardando = true;
        this.confirmar.emit({ saldoReal: this.saldoReal, transferir: this.transferir });
    }
}

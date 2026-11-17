import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-abrir-caja-modal',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './abrir-caja-modal.component.html'
})
export class AbrirCajaModalComponent implements OnChanges {
    @Input() isOpen: boolean = false;
    @Output() modalClose = new EventEmitter<void>();
    @Output() confirmar = new EventEmitter<number>();

    saldoInicial: number | null = null;
    guardando: boolean = false;

    constructor(private alertasService: AlertasService) {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isOpen']?.currentValue === true) {
            this.saldoInicial = null;
            this.guardando = false;
        }
    }

    cerrar(): void {
        if (this.guardando) return;
        this.modalClose.emit();
    }

    onSubmit(): void {
        const saldo = this.saldoInicial ?? 0;
        if (saldo < 0) {
            this.alertasService.error('Validación', 'El saldo inicial debe ser 0 o mayor.');
            return;
        }
        this.guardando = true;
        this.confirmar.emit(saldo);
    }
}

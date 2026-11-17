import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { OrdenCompra } from '../../../models/orden-compra.model';
import { OrdenCompraService } from '../../../services/orden-compra.service';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-anular-compra',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule, ModalComponent],
    templateUrl: './modal-anular-compra.component.html',
    styles: [`
        .reason-field {
            font-family: inherit;
            width: 100%;
            min-height: 120px;
            padding: 1rem;
            border-radius: 1rem;
            border: 1.5px solid #e2e8f0;
            background-color: #f8fafc;
            color: #1e293b;
            font-size: 0.875rem;
            font-weight: 500;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            resize: none;
            outline: none;
        }
        .reason-field:focus {
            background-color: #ffffff;
            border-color: #ef4444;
            box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
        }
        .reason-field::placeholder {
            color: #94a3b8;
            font-weight: 400;
        }
    `]
})
export class ModalAnularCompraComponent {
    @Input() orden?: OrdenCompra;
    @Output() onClose = new EventEmitter<void>();
    @Output() onConfirm = new EventEmitter<void>();

    motivoAnulacion: string = '';
    anulando: boolean = false;

    constructor(
        private ordenService: OrdenCompraService,
        private alertasService: AlertasService
    ) {}

    close() {
        if (!this.anulando) {
            this.onClose.emit();
        }
    }

    confirmar() {
        if (!this.orden?.id || !this.motivoAnulacion.trim()) return;

        // Senior Pattern: Confirmación secundaria para acciones críticas
        this.alertasService.confirm(
            '¿Confirmar anulación?',
            `Se anulará la orden ${this.orden.numeroOrden} de forma permanente.`
        ).then((confirmed) => {
            if (confirmed) {
                this.ejecutarAnulacion();
            }
        });
    }

    private ejecutarAnulacion() {
        this.anulando = true;
        this.ordenService.anular(this.orden!.id!, 'ADMIN', this.motivoAnulacion).subscribe({
            next: () => {
                this.anulando = false;
                this.alertasService.success('¡Anulada!', 'La orden de compra ha sido anulada correctamente.');
                this.onConfirm.emit();
            },
            error: (err) => {
                this.anulando = false;
                console.error('Error anulando orden', err);
                const msg = err.error?.message || 'No se pudo procesar la anulación en este momento.';
                this.alertasService.error('Error de Proceso', msg);
            }
        });
    }
}

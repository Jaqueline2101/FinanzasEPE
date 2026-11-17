import { Component, EventEmitter, Input, Output, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { ImpresionPagosComponent } from '../impresion-pagos/impresion-pagos.component';

@Component({
    selector: 'app-modal-detalle',
    standalone: true,
    imports: [CommonModule, ModalComponent, ImpresionPagosComponent],
    templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleComponent implements OnChanges {
    @Input() isOpen = false;
    @Input() payment: any | null = null;
    @Output() onClose = new EventEmitter<void>();

    // Printing State
    showPrintModal = false;
    displayDate: Date | null = null;

    ngOnChanges() {
        if (this.payment) {
            // Backend mismatch: 'fecha' vs 'fechaMovimiento'
            const raw = this.payment.fecha || this.payment.fechaMovimiento;

            if (raw) {
                if (Array.isArray(raw)) {
                    // [yyyy, mm, dd]
                    this.displayDate = new Date(raw[0], raw[1] - 1, raw[2]);
                } else {
                    // yyyy-mm-dd or timestamp
                    const parts = raw.toString().split('-');
                    if (parts.length === 3) {
                        // Force YYYY, MM-1, DD to avoid timezone issues with string parsing
                        const dayPart = parts[2].split('T')[0];
                        this.displayDate = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(dayPart));
                    } else {
                        this.displayDate = new Date(raw);
                    }
                }
            } else {
                this.displayDate = null;
            }
        }
    }

    close() {
        this.isOpen = false;
        this.onClose.emit();
    }

    printTicket() {
        this.showPrintModal = true;
    }

    closePrint() {
        this.showPrintModal = false;
    }
}

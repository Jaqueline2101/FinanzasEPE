import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { PlanCuentasRegistro } from '../../../models/plan-cuentas-pmt.model';

@Component({
    selector: 'app-modal-detalle',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleComponent {
    @Input() isOpen = false;
    @Input() registro?: PlanCuentasRegistro;
    @Output() onClose = new EventEmitter<void>();

    close() {
        this.onClose.emit();
    }

    getBadgeClass(tipo: string | undefined): string {
        switch (tipo) {
            case 'PROVEEDOR': return 'bg-purple-100 text-purple-700 border-purple-200';
            case 'MEDICO': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'TRABAJADOR': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            default: return 'bg-slate-100 text-slate-700 border-slate-200';
        }
    }
}

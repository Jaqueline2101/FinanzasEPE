import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrdenCompra } from '../../../models/orden-compra.model';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-detalle-orden',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle-orden.component.html',
    styles: [`
        .detail-row {
            display: flex;
            flex-direction: column;
            gap: 2px;
            padding-top: 4px;
            padding-bottom: 4px;
        }
        .detail-label {
            font-size: 9px;
            font-weight: 800;
            color: #64748b; /* text-slate-500 */
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        .detail-value {
            font-size: 13px;
            font-weight: 700;
            color: #1e293b; /* text-slate-800 */
        }
        .section-card {
            background-color: #f8fafc; /* bg-slate-50 */
            border: 1px solid #f1f5f9; /* border-slate-100 */
            border-radius: 0.75rem;
            padding: 0.75rem;
        }
    `]
})
export class ModalDetalleOrdenComponent {
    @Input() isOpen = false;
    @Input() orden?: OrdenCompra;
    @Output() onClose = new EventEmitter<void>();

    getStatusClass(estado?: string): string {
        switch (estado) {
            case 'BORRADOR': return 'bg-slate-100 text-slate-600 border-slate-200';
            case 'APROBADO': return 'bg-emerald-50 text-emerald-600 border-emerald-100';
            case 'ANULADO': return 'bg-rose-50 text-rose-600 border-rose-100';
            case 'RECIBIDO': return 'bg-blue-50 text-blue-600 border-blue-100';
            default: return 'bg-slate-50 text-slate-400 border-slate-100';
        }
    }

    close() {
        this.onClose.emit();
    }
}

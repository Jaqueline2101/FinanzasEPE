import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../../../shared/components/modal/modal';
import { LabAuditoriaDto } from '../../../models/audit.model';

@Component({
    selector: 'app-audit-detail-modal',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './audit-detail-modal.component.html'
})
export class AuditDetailModalComponent {
    @Input() isOpen = false;
    @Input() audit: LabAuditoriaDto | null = null;
    @Output() close = new EventEmitter<void>();

    onClose() {
        this.close.emit();
    }

    historyChanges: { field: string; oldValue: string; newValue: string }[] = [];

    ngOnChanges() {
        if (this.isOpen && this.audit) {
            this.parseChanges();
        }
    }

    private parseChanges() {
        this.historyChanges = [];
        if (!this.audit) return;

        const oldData = this.safeParse(this.audit.datoAnterior);
        const newData = this.safeParse(this.audit.datoNuevo);

        if (!oldData && !newData) return;

        // Caso 1: INSERT (Solo newData)
        if (!oldData && newData) {
            Object.keys(newData).forEach(key => {
                this.historyChanges.push({
                    field: this.formatFieldName(key),
                    oldValue: '—',
                    newValue: this.formatValue(newData[key])
                });
            });
            return;
        }

        // Caso 2: DELETE (Solo oldData)
        if (oldData && !newData) {
            Object.keys(oldData).forEach(key => {
                this.historyChanges.push({
                    field: this.formatFieldName(key),
                    oldValue: this.formatValue(oldData[key]),
                    newValue: '—'
                });
            });
            return;
        }

        // Caso 3: UPDATE (Comparar ambos)
        const allKeys = Array.from(new Set([...Object.keys(oldData), ...Object.keys(newData)]));
        allKeys.forEach(key => {
            const oldVal = oldData[key];
            const newVal = newData[key];

            // Solo mostrar si cambiaron
            if (!this.areValuesEqual(oldVal, newVal)) {
                this.historyChanges.push({
                    field: this.formatFieldName(key),
                    oldValue: oldVal !== undefined ? this.formatValue(oldVal) : '—',
                    newValue: newVal !== undefined ? this.formatValue(newVal) : '—'
                });
            }
        });
    }

    private safeParse(json: string | null): any {
        if (!json) return null;
        try {
            return JSON.parse(json);
        } catch {
            return null;
        }
    }

    private formatFieldName(key: string): string {
        // Simple formatter: camelCase to Title Case
        return key.replace(/([A-Z])/g, ' $1')
            .replace(/^./, (str) => str.toUpperCase())
            .trim();
    }

    private formatValue(value: any): string {
        if (value === null || value === undefined) return '—';
        if (typeof value === 'boolean') return value ? 'Sí' : 'No';
        if (typeof value === 'object') return JSON.stringify(value);
        return String(value);
    }

    private areValuesEqual(val1: any, val2: any): boolean {
        return JSON.stringify(val1) === JSON.stringify(val2);
    }

    getActionBadgeClass(action: string): string {
        switch (action?.toUpperCase()) {
            case 'INSERT': return 'bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-semibold border border-green-200';
            case 'UPDATE': return 'bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded text-xs font-semibold border border-yellow-200';
            case 'DELETE': return 'bg-red-100 text-red-700 px-2 py-0.5 rounded text-xs font-semibold border border-red-200';
            case 'LOGIN': return 'bg-blue-100 text-blue-700 px-2 py-0.5 rounded text-xs font-semibold border border-blue-200';
            case 'LOGOUT': return 'bg-slate-100 text-slate-700 px-2 py-0.5 rounded text-xs font-semibold border border-slate-200';
            default: return 'bg-gray-100 text-gray-700 px-2 py-0.5 rounded text-xs font-semibold border border-gray-200';
        }
    }

    getStatusBadgeClass(status: string): string {
        return status === 'Éxito'
            ? 'bg-blue-500 text-white px-2 py-0.5 rounded text-xs font-semibold'
            : 'bg-red-500 text-white px-2 py-0.5 rounded text-xs font-semibold';
    }
}

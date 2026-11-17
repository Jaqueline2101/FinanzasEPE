import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { Proveedor } from '../../../models/proveedor.model';

@Component({
    selector: 'app-modal-detalle-proveedor',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleProveedorComponent {
    @Input() isOpen = false;
    @Input() proveedor: Proveedor | null = null;
    @Output() onClose = new EventEmitter<void>();

    close(): void {
        this.onClose.emit();
    }

    // Helper to format full location
    splitValues(value: string | undefined): string[] {
        if (!value) return [];
        return value.split('|').map(v => v.trim()).filter(v => v.length > 0);
    }

    getUbicacion(): string {
        if (!this.proveedor) return '-';
        const parts = [
            this.proveedor.departamento,
            this.proveedor.provincia,
            this.proveedor.distrito
        ].filter(p => p);

        return parts.length > 0 ? parts.join(' / ') : '-';
    }
    getDireccionesDetalle(): { sede: string, direccion: string }[] {
        if (!this.proveedor) return [];

        const direcciones = this.proveedor.direccion ? this.proveedor.direccion.split('|').map(d => d.trim()) : [];
        const sedes = this.proveedor.sedes ? this.proveedor.sedes.split('|').map(s => s.trim()) : [];

        const count = Math.max(direcciones.length, sedes.length);
        const result = [];

        for (let i = 0; i < count; i++) {
            result.push({
                direccion: direcciones[i] || '',
                sede: sedes[i] || ''
            });
        }

        return result.filter(item => item.direccion.length > 0);
    }
}

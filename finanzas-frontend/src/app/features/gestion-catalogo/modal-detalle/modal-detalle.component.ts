import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Servicio } from '../../../models/servicio.model';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-detalle-servicio',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html',
    styleUrls: ['./modal-detalle.component.css']
})
export class ModalDetalleServicioComponent implements OnChanges {
    @Input() isVisible: boolean = false;
    @Input() servicio: Servicio | null = null;
    @Output() onClose = new EventEmitter<void>();

    categorias: any[] = [
        { id: 1, nombre: 'Antibióticos' },
        { id: 2, nombre: 'Material de Curación' },
        { id: 3, nombre: 'Equipos Médicos' },
        { id: 4, nombre: 'Insumos Médicos' },
        { id: 5, nombre: 'Material Odontológico' },
        { id: 6, nombre: 'Reactivos de Laboratorio' }
    ];

    getCategoryName(id: any): string {
        if (!id) return '-';
        const numericId = Number(id);
        const cat = this.categorias.find(c => Number(c.id) === numericId);
        return cat ? cat.nombre : '-';
    }

    // Analysis Metrics (Computed)
    metrics = {
        costoNeto: 0,
        valorVentaNeto: 0,
        igv: 0,
        gananciaReal: 0,
        margenReal: 0
    };

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['servicio'] && this.servicio) {
            this.calculateMetrics();
        }
    }

    private calculateMetrics() {
        if (!this.servicio) return;

        const pCompra = this.servicio.precioCompra || 0;
        const pConIgv = this.servicio.precioConIgv || 0;

        const costoNeto = pCompra / 1.18;
        const valorVentaNeto = pConIgv / 1.18;
        const igv = pConIgv - valorVentaNeto;
        const gananciaReal = valorVentaNeto - costoNeto;

        let margen = 0;
        if (valorVentaNeto > 0) {
            margen = (gananciaReal / valorVentaNeto) * 100;
        }

        this.metrics = {
            costoNeto,
            valorVentaNeto,
            igv,
            gananciaReal,
            margenReal: margen
        };
    }

    close(): void {
        this.onClose.emit();
    }
}

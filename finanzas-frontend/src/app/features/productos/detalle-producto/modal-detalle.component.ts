import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { Producto } from '../../../models/producto.model';

@Component({
    selector: 'app-modal-detalle-producto',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleProductoComponent implements OnChanges {
    @Input() isOpen = false;
    @Input() producto: Producto | null = null;
    @Output() onClose = new EventEmitter<void>();

    getAlmacenamientoNombre(): string {
        const id = this.producto?.idTipoTemperatura || this.producto?.catalogo?.idTipoTemperatura;
        if (!id) return 'Normal';
        
        switch (Number(id)) {
            case 2: return 'Refrigerado';
            case 3: return 'Congelado';
            default: return 'Normal';
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        // No metrics needed for inventory-only view
    }

    get stockEnCajas(): number {
        if (!this.producto || !this.producto.factorCompra) return 0;
        return this.producto.stockReal / this.producto.factorCompra;
    }

    close(): void {
        this.onClose.emit();
    }

    getMarca(detalle: string | undefined): string {
        if (this.producto?.marcaLaboratorio) return this.producto.marcaLaboratorio;
        if (this.producto?.catalogo?.modelo) return this.producto.catalogo.modelo;
        if (!detalle) return '-';
        const marcaPart = detalle.split('|')[0];
        return marcaPart.replace('Marca:', '').trim() || '-';
    }

    getUbicacion(detalle: string | undefined): string {
        // Prioridad 1: Campo directo en el producto
        if (this.producto?.ubicacion && this.producto.ubicacion !== '-') return this.producto.ubicacion;
        
        // Prioridad 2: Buscar en el detalle del catálogo (Legacy format)
        if (detalle) {
            const parts = detalle.split('|');
            const ubicPart = parts.find(p => p.toLowerCase().includes('ubicación') || p.toLowerCase().includes('ubicacion'));
            if (ubicPart) {
                return ubicPart.split(':')[1]?.trim() || '-';
            }
        }
        
        return '-';
    }
}

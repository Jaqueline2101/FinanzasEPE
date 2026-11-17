import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { Paquete } from '../../../models/paquete.model';

@Component({
    selector: 'app-modal-detalle',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './modal-detalle.component.html',
})
export class ModalDetalleComponent {
    @Input() isVisible: boolean = false;
    @Input() paquete: Paquete | null = null;
    @Output() onClose = new EventEmitter<void>();

    close(): void {
        this.onClose.emit();
    }

    /**
     * Calcula el ahorro total del paquete
     */
    get ahorro(): number {
        if (!this.paquete) return 0;
        return (this.paquete.precioReal || 0) - (this.paquete.precioPaquete || 0);
    }

    /**
     * Calcula el precio real de un servicio (precio unitario * cantidad)
     */
    calcularPrecioRealServicio(item: any): number {
        const precioUnitario = item.precioUnitario || item.precio || 0;
        const cantidad = item.cantidad || 1;
        return precioUnitario * cantidad;
    }

    /**
     * Calcula el monto del descuento en soles para un servicio
     */
    calcularDescuentoMonto(item: any): number {
        const precioReal = this.calcularPrecioRealServicio(item);
        const descuentoPorcentaje = item.descuentoPorcentaje || 0;
        return (precioReal * descuentoPorcentaje) / 100;
    }

    /**
     * Calcula el precio final de un servicio después del descuento
     */
    calcularPrecioFinalServicio(item: any): number {
        const precioReal = this.calcularPrecioRealServicio(item);
        const descuentoMonto = this.calcularDescuentoMonto(item);
        return precioReal - descuentoMonto;
    }

    /**
     * Calcula el total de precios reales (suma de todos los servicios sin descuento)
     */
    calcularTotalPrecioReal(): number {
        if (!this.paquete?.servicios) return 0;

        return this.paquete.servicios.reduce((total, item) => {
            return total + this.calcularPrecioRealServicio(item);
        }, 0);
    }

    /**
     * Calcula el total final (suma de todos los servicios con descuento)
     * Debe coincidir con paquete.precioPaquete
     */
    calcularTotalFinal(): number {
        if (!this.paquete?.servicios) return 0;

        return this.paquete.servicios.reduce((total, item) => {
            return total + this.calcularPrecioFinalServicio(item);
        }, 0);
    }

    /**
     * Calcula el porcentaje del descuento total
     */
    calcularPorcentajeDescuentoTotal(): number {
        const totalReal = this.calcularTotalPrecioReal();
        const totalDescuento = this.paquete?.descuento || 0;

        if (totalReal === 0) return 0;

        const porcentaje = (totalDescuento / totalReal) * 100;
        return Math.round(porcentaje * 100) / 100; // Redondear a 2 decimales
    }
}
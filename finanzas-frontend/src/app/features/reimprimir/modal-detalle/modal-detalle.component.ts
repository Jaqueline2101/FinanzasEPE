import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentoReimpresion } from '../../../models/reimprimir.model';

@Component({
    selector: 'app-modal-detalle',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './modal-detalle.component.html',
    styleUrls: ['./modal-detalle.component.css']
})
export class ModalDetalleComponent {
    @Input() documento: DocumentoReimpresion | null = null;
    @Input() dataAdicional: any = null; // Para datos del backend como HonorarioVentaInfo
    @Input() carrito: any[] = []; // Para mostrar detalles de los servicios u ordenes
    @Output() onClose = new EventEmitter<void>();

    cerrar() {
        this.onClose.emit();
    }

    get moduloLabel(): string {
        if (!this.documento) return '';
        switch (this.documento.moduloOrigen) {
            case 'HONORARIOS': return 'Pago de Honorarios';
            case 'PAGO_PERSONAL': return 'Pago de Personal';
            case 'VENTAS': return 'Venta / Registro';
            default: return this.documento.moduloOrigen;
        }
    }
}

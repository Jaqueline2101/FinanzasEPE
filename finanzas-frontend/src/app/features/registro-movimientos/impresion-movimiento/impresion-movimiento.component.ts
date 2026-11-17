
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Movimiento } from '../../../models/movimiento.model';

@Component({
    selector: 'app-impresion-movimiento',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './impresion-movimiento.component.html',
    styleUrls: ['./impresion-movimiento.component.css']
})
export class ImpresionMovimientoComponent implements OnInit {
    @Input() movimiento: any | null = null;
    @Output() close = new EventEmitter<void>();

    selectedFormat: '80mm' | '58mm' | 'A4' | 'A5' = '80mm';
    currentDate: Date = new Date();

    ngOnInit() {
        this.currentDate = new Date();
    }

    setFormat(format: '80mm' | '58mm' | 'A4' | 'A5') {
        this.selectedFormat = format;
    }

    print() {
        window.print();
    }

    sendWhatsApp() {
        if (!this.movimiento) return;
        const text = `*COMPROBANTE DE ${this.isIngreso ? 'INGRESO' : 'EGRESO'}*%0A` +
            `Empresa: REUMACENTER S.A.C.%0A` +
            `Fecha: ${this.currentDate.toLocaleDateString()}%0A` +
            `Monto: ${this.movimiento.moneda === 'PEN' ? 'S/' : '$'} ${this.movimiento.monto}%0A` +
            `Concepto: ${this.movimiento.concepto || this.movimiento.descripcion}%0A` +
            `Beneficiario: ${this.movimiento.beneficiario || '---'}`;

        window.open(`https://wa.me/?text=${text}`, '_blank');
    }

    sendEmail() {
        if (!this.movimiento) return;
        const subject = `Comprobante de ${this.isIngreso ? 'Ingreso' : 'Egreso'} - ${this.movimiento.numeroComprobante || 'S/N'}`;
        const body = `Estimado usuario,%0A%0AAdjunto el detalle del movimiento:%0A%0A` +
            `Fecha: ${this.currentDate.toLocaleDateString()}%0A` +
            `Monto: ${this.movimiento.moneda === 'PEN' ? 'S/' : '$'} ${this.movimiento.monto}%0A` +
            `Concepto: ${this.movimiento.concepto || this.movimiento.descripcion}%0A%0A` +
            `Atentamente,%0AREUMACENTER S.A.C.`;

        window.open(`mailto:?subject=${subject}&body=${body}`, '_blank');
    }

    onClose() {
        this.close.emit();
    }

    get isIngreso(): boolean {
        return this.movimiento?.tipo === 'INGRESO' || this.movimiento?.tipoMovimiento === 'INGRESO';
    }

    get tituloDocumento(): string {
        if (this.isIngreso) return 'COMPROBANTE DE INGRESO';
        return 'COMPROBANTE DE EGRESO';
    }
}

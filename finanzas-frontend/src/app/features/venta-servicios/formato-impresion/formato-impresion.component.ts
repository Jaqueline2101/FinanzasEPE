import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface VentaInfo {
    ticketNumber: string;
    fecha: Date;
    caja: string;
    usuario: string;
    tipoComprobante: string;
    metodoPago: string;
    montoRecibido: number;
    vuelto: number;
    montoEfectivo?: number;
    montoTarjeta?: number;
    montoTransferencia?: number;
    sede?: string;
    puntoCaja?: string;
    ruc?: string;
    razonSocial?: string;
    numeroDocumento?: string;
    pagoOriginal?: any;
    enlacePdf?: string;
    enlaceXml?: string;
    enlaceCdr?: string;
    codigoHash?: string;
    nombrePlan?: string;

}

@Component({
    selector: 'app-formato-impresion',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './formato-impresion.component.html',
    styleUrls: ['./formato-impresion.component.css']
})
export class FormatoImpresionComponent implements OnInit, OnChanges {
    @Input() formato: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' = 'TICKET_80';
    @Input() modo: 'PACIENTE' | 'COMPANIA' = 'PACIENTE';
    @Input() paciente: any = null;
    @Input() carrito: any[] = [];
    @Input() ventaInfo: VentaInfo | null = null;
    @Input() nombreGrupo: string = '';

    subtotal: number = 0;
    descuentoTotal: number = 0;
    total: number = 0;

    clinicData = {
        nombre: 'REUMACENTER E.I.R.L.',
        ruc: '20123456789',
        direccion: 'Av. Principal 123, Lima',
        telefono: '(01) 456-7890'
    };

    ngOnInit() {
        this.calcularTotales();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['carrito'] || changes['ventaInfo']) {
            this.calcularTotales();
        }
    }

    get docTitle(): string {
        const tipo = this.ventaInfo?.tipoComprobante?.toUpperCase() || '';
        if (tipo.includes('FACTURA')) return 'FACTURA ELECTRÓNICA';
        if (tipo.includes('BOLETA')) return 'BOLETA DE VENTA ELECTRÓNICA';
        return 'NOTA DE VENTA';
    }

    get docNumberLabel(): string {
        const tipo = this.ventaInfo?.tipoComprobante?.toUpperCase() || '';
        if (tipo.includes('FACTURA')) return 'N° Factura:';
        if (tipo.includes('BOLETA')) return 'N° Boleta:';
        return 'N° Ticket:';
    }

    calcularTotales() {
        this.subtotal = this.carrito.reduce((acc, item) => acc + (item.precioBase || item.precioFinal || 0), 0);
        this.descuentoTotal = this.carrito.reduce((acc, item) => acc + (item.descuento || 0), 0);
        this.total = this.subtotal - this.descuentoTotal;
    }
}

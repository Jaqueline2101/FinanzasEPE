import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface HonorarioVentaInfo {
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
    beneficiario?: string;
    numeroDocumento?: string;
}

@Component({
    selector: 'app-pagorec-formato-impresion',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './formato-impresion.component.html',
    styleUrls: ['./formato-impresion.component.css']
})
export class PagorecHonorariosFormatoImpresionComponent implements OnInit, OnChanges {
    @Input() formato: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' | null = 'TICKET_80';
    @Input() pago: any = null; // PagoComprobante
    @Input() ventaInfo: HonorarioVentaInfo | null = null;

    subtotal: number = 0;
    retencion: number = 0;
    otrosDescuentos: number = 0;
    total: number = 0;
    today: Date = new Date();

    clinicData = {
        nombre: 'REUMACENTER E.I.R.L.',
        ruc: '20123456789',
        direccion: 'Av. Principal 123, Lima',
        telefono: '(01) 456-7890'
    };

    meses = [
        { id: 1, nombre: 'Enero' }, { id: 2, nombre: 'Febrero' }, { id: 3, nombre: 'Marzo' },
        { id: 4, nombre: 'Abril' }, { id: 5, nombre: 'Mayo' }, { id: 6, nombre: 'Junio' },
        { id: 7, nombre: 'Julio' }, { id: 8, nombre: 'Agosto' }, { id: 9, nombre: 'Septiembre' },
        { id: 10, nombre: 'Octubre' }, { id: 11, nombre: 'Noviembre' }, { id: 12, nombre: 'Diciembre' }
    ];

    getNombreMes(mesId: number): string {
        const mes = this.meses.find(m => m.id === Number(mesId));
        return mes ? mes.nombre : String(mesId);
    }

    ngOnInit() {
        this.calcularTotales();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['pago'] || changes['ventaInfo']) {
            this.calcularTotales();
        }
    }

    get docTitle(): string {
        return this.pago?.tipoPago || 'PAGO POR HONORARIOS';
    }

    get docNumberLabel(): string {
        return 'N° Recibo:';
    }

    get numeroComprobante(): string {
        if (this.pago?.serieComprobante && this.pago?.numeroRecibo) {
            return `${this.pago.serieComprobante}-${this.pago.numeroRecibo}`;
        }
        return this.pago?.numeroRecibo || '---';
    }

    calcularTotales() {
        if (!this.pago) return;
        this.total = this.pago.montoNeto || 0;
        this.subtotal = this.pago.montoBruto || 0;
        this.retencion = this.pago.retencion || 0;
        this.otrosDescuentos = this.pago.otrosDescuentos || 0;
    }
}

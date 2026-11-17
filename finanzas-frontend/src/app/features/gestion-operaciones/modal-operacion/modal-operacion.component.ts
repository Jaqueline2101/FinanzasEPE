import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoDTO } from '../../../models/finanza.model';
import { CuentaCorriente } from '../../../services/cuenta-corriente.service';
import { AlertasService } from '../../../services/alertas.service';

import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-operacion',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-operacion.component.html',
    styleUrls: ['./modal-operacion.component.css']
})
export class ModalOperacionComponent {
    @Input() show = false;
    @Input() cuentas: CuentaCorriente[] = [];
    @Input() movimiento: MovimientoDTO = {
        tipo: 'INGRESO',
        monto: 0,
        concepto: '',
        idUsuario: 0,
        idSucursal: 0,
        idCuenta: 0,
        metodoPago: 'EFECTIVO',
        tipoComprobante: 'RECIBO',
        numeroComprobante: '',
        beneficiario: ''
    };
    @Input() isLoading = false;
    isDropdownOpen = false;

    constructor(private alertasService: AlertasService) {}

    @Output() close = new EventEmitter<void>();
    @Output() submit = new EventEmitter<void>();

    // Valor de display para el monto
    montoDisplay: string = '0';

    ngOnInit() {
        this.montoDisplay = this.movimiento.monto === 0 ? '0' : this.movimiento.monto.toString();
    }

    onClose() {
        this.close.emit();
    }

    onSubmit() {
        this.submit.emit();
    }

    // Método para obtener el texto de la cuenta sin el saldo
    getCuentaDisplayText(cuenta: CuentaCorriente): string {
        return `${cuenta.alias || cuenta.nombreTitular} - ${cuenta.banco}`;
    }

    getSaldoFormateado(cuenta: CuentaCorriente): string {
        const simbolo = cuenta.moneda === 'PEN' ? 'S/' : '$';
        return `${simbolo} ${cuenta.saldo.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }

    toggleDropdown() {
        this.isDropdownOpen = !this.isDropdownOpen;
    }

    selectCuenta(cuenta: CuentaCorriente) {
        this.movimiento.idCuenta = cuenta.idCuenta;
        this.isDropdownOpen = false;
    }

    getSelectedCuenta(): CuentaCorriente | undefined {
        return this.cuentas.find(c => c.idCuenta === this.movimiento.idCuenta);
    }

    // Cuando el input recibe focus, si el valor es 0, lo limpiamos
    onMontoFocus() {
        if (this.montoDisplay === '0') {
            this.montoDisplay = '';
        }
    }

    // Cuando el input pierde focus, si está vacío, ponemos 0 de nuevo
    onMontoBlur() {
        if (this.montoDisplay === '' || this.montoDisplay === null) {
            this.montoDisplay = '0';
            this.movimiento.monto = 0;
        } else {
            this.movimiento.monto = parseFloat(this.montoDisplay);
        }
    }

    // Cuando cambia el valor del monto
    onMontoChange() {
        if (this.montoDisplay && this.montoDisplay !== '0') {
            this.movimiento.monto = parseFloat(this.montoDisplay);
        }
    }
}
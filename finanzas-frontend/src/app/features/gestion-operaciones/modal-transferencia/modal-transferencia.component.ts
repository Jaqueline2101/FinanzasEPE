import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransferenciaDTO } from '../../../models/finanza.model';
import { CuentaCorriente } from '../../../services/cuenta-corriente.service';
import { AlertasService } from '../../../services/alertas.service';

import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-transferencia',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-transferencia.component.html',
    styleUrls: ['./modal-transferencia.component.css']
})
export class ModalTransferenciaComponent {
    @Input() show = false;
    @Input() cuentas: CuentaCorriente[] = [];
    @Input() transferencia: TransferenciaDTO = {
        idCuentaOrigen: 0,
        idCuentaDestino: 0,
        monto: 0,
        referencia: '',
        descripcion: '',
        idUsuario: 0,
        idSucursal: 0
    };
    @Input() isLoading = false;
    @Input() isCierreCaja = false;

    constructor(private alertasService: AlertasService) {}

    isOriginDropdownOpen = false;
    isDestDropdownOpen = false;

    @Output() close = new EventEmitter<void>();
    @Output() submit = new EventEmitter<void>();

    // Valor de display para el monto
    montoDisplay: string = '0';

    ngOnInit() {
        this.montoDisplay = this.transferencia.monto === 0 ? '0' : this.transferencia.monto.toString();
    }

    onClose() {
        this.close.emit();
    }

    onSubmit() {
        this.submit.emit();
    }

    // Método para obtener el texto de la cuenta incluyendo el banco
    getCuentaDisplayText(cuenta: CuentaCorriente): string {
        return `${cuenta.alias || cuenta.nombreTitular} - ${cuenta.banco}`;
    }

    getSaldoFormateado(cuenta: CuentaCorriente): string {
        const simbolo = cuenta.moneda === 'PEN' ? 'S/' : '$';
        return `${simbolo} ${cuenta.saldo.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }

    toggleOriginDropdown() {
        this.isOriginDropdownOpen = !this.isOriginDropdownOpen;
        if (this.isOriginDropdownOpen) this.isDestDropdownOpen = false;
    }

    toggleDestDropdown() {
        this.isDestDropdownOpen = !this.isDestDropdownOpen;
        if (this.isDestDropdownOpen) this.isOriginDropdownOpen = false;
    }

    selectOriginCuenta(cuenta: CuentaCorriente) {
        this.transferencia.idCuentaOrigen = cuenta.idCuenta;
        this.isOriginDropdownOpen = false;
        // Si el destino es igual al nuevo origen, limpiar destino
        if (this.transferencia.idCuentaDestino === cuenta.idCuenta) {
            this.transferencia.idCuentaDestino = 0;
        }
    }

    selectDestCuenta(cuenta: CuentaCorriente) {
        this.transferencia.idCuentaDestino = cuenta.idCuenta;
        this.isDestDropdownOpen = false;
    }

    getOriginCuenta(): CuentaCorriente | undefined {
        return this.cuentas.find(c => c.idCuenta === this.transferencia.idCuentaOrigen);
    }

    getDestCuenta(): CuentaCorriente | undefined {
        return this.cuentas.find(c => c.idCuenta === this.transferencia.idCuentaDestino);
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
            this.transferencia.monto = 0;
        } else {
            this.transferencia.monto = parseFloat(this.montoDisplay);
        }
    }

    // Cuando cambia el valor del monto
    onMontoChange() {
        if (this.montoDisplay && this.montoDisplay !== '0') {
            this.transferencia.monto = parseFloat(this.montoDisplay);
        }
    }
}
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CuentaCorriente } from '../../../models/cuenta-corriente.model';
import { AlertasService } from '../../../services/alertas.service';

import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-cuenta-corriente-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './modal-corriente-modal.component.html',
})
export class CuentaCorrienteModalComponent {
  @Input() isOpen = false;
  @Input() isEdit = false;
  @Input() data: Partial<CuentaCorriente> = {
    moneda: 'PEN',
    saldo: 0,
    nombreTitular: '',
    estado: 'A'
  };

  @Output() onClose = new EventEmitter<void>();
  @Output() onSave = new EventEmitter<Partial<CuentaCorriente>>();

  // List of known banks for auto-complete
  private readonly BANK_DATA: any[] = [
    { key: 'Caja General', contable: '10.1', sbs: '—', format: null },
    { key: 'Efectivo', contable: '10.1', sbs: '—', format: null },
    // BCP: 191-1234567-0-99 (13 digits) -> ###-#######-#-##
    { key: 'BCP', contable: '10.4.1', sbs: '002', format: '###-#######-#-##' },
    // Interbank: 002-1234567890 (13 digits usually) but let's assume standard account
    { key: 'Interbank', contable: '10.4.2', sbs: '003', format: '###-#############-##' },
    { key: 'BanBif', contable: '10.4.3', sbs: '038', format: '###-########-#' },
    { key: 'Yape', contable: '10.4.4', sbs: '—', format: null },
    { key: 'Plin', contable: '10.4.4', sbs: '—', format: null },
    { key: 'Banco de la Nación', contable: '10.4.5', sbs: '018', format: '####-######' }, // 10 or 11 digits
    { key: 'Nacion', contable: '10.4.5', sbs: '018', format: '####-######' },
    { key: 'BBVA', contable: '10.4.6', sbs: '011', format: '####-####-##########' }, // 18 digits
    { key: 'Scotiabank', contable: '10.4.7', sbs: '009', format: '###-#######' }, // 10 digits
    { key: 'Falabella', contable: '10.4.8', sbs: '051', format: '###########' },
    { key: 'Ripley', contable: '10.4.9', sbs: '053', format: '###########' },
    // Cajas Municipales (Usually 14 digits or similar to BCP? Using generic for now or specific if known)
    { key: 'Caja Arequipa', contable: '10.4.10', sbs: '801', format: '######-########-##' }, // Example pattern
    { key: 'Caja Trujillo', contable: '10.4.11', sbs: '802', format: '###########' },
    { key: 'Caja Piura', contable: '10.4.12', sbs: '803', format: '###########' },
    { key: 'Caja Sullana', contable: '10.4.13', sbs: '805', format: '###########' },
    { key: 'Caja Huancayo', contable: '10.4.14', sbs: '806', format: '###########' },
    { key: 'Caja Tacna', contable: '10.4.15', sbs: '807', format: '###########' },
    { key: 'Caja Ica', contable: '10.4.16', sbs: '808', format: '###########' },
    { key: 'Caja Cusco', contable: '10.4.17', sbs: '809', format: '###########' },
    { key: 'Caja Paita', contable: '10.4.18', sbs: '810', format: '###########' },
    { key: 'Caja Maynas', contable: '10.4.19', sbs: '811', format: '###########' }
  ];

  constructor(private alertasService: AlertasService) {}

  currentBankFormat: string | null = null;
  currentPlaceholder: string = 'Ingrese número de cuenta';

  onBancoChange(valor: string) {
    if (!valor) {
      this.currentBankFormat = null;
      this.currentPlaceholder = 'Ingrese número de cuenta';
      this.data.cuentaContable = '';
      this.data.codigoSBS = '';
      return;
    }
    const upperVal = valor.toUpperCase();

    const found = this.BANK_DATA.find(b => upperVal.includes(b.key.toUpperCase()));

    if (found) {
      this.data.cuentaContable = found.contable;
      this.data.codigoSBS = found.sbs;
      this.currentBankFormat = found.format;
      this.currentPlaceholder = found.format ? found.format.replace(/#/g, 'X') : 'Ingrese número de cuenta';
    } else {
      this.data.cuentaContable = '';
      this.data.codigoSBS = '';
      this.currentBankFormat = null;
      this.currentPlaceholder = 'Ingrese número de cuenta';
    }
  }

  onCuentaInput(event: any) {
    let val = event.target.value.replace(/\D/g, ''); // Remove non-digit chars
    if (this.currentBankFormat) {
      this.data.numeroCuenta = this.applyMask(val, this.currentBankFormat);
    } else {
      this.data.numeroCuenta = val;
    }
  }

  onCCIInput(event: any) {
    // Standard CCI format: ###-###-############-## (20 digits)
    const cciFormat = '###-###-############-##';
    let val = event.target.value.replace(/\D/g, '');
    this.data.cci = this.applyMask(val, cciFormat);
  }

  private applyMask(value: string, mask: string): string {
    let i = 0;
    let v = 0;
    let result = '';

    while (i < mask.length && v < value.length) {
      if (mask[i] === '#') {
        result += value[v];
        v++;
      } else {
        result += mask[i];
        // If the user typed the separator, skip it in value
        if (value[v] === mask[i]) {
          v++;
        }
      }
      i++;
    }
    return result;
  }

  close() {
    this.onClose.emit();
  }

  save() {
    if (!this.data.banco || !this.data.nombreTitular || !this.data.numeroCuenta) {
      this.alertasService.error('Formulario Incompleto', 'Por favor, complete todos los campos obligatorios antes de continuar.');
      return;
    }
    this.onSave.emit(this.data);
  }
}

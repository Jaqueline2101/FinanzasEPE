import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { OtroPago, OtrosPagosService } from '../../../services/otros-pagos.service';
import { CuentaCorrienteService, CuentaCorriente } from '../../../services/cuenta-corriente.service';
import { ImpresionPagosComponent } from '../impresion-pagos/impresion-pagos.component';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-pagos',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent, ImpresionPagosComponent, AlertasComponent],
    templateUrl: './modal-pagos.component.html'
})
export class ModalPagosComponent implements OnInit {
    @Input() isOpen = false;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    @Input() currentUserId: number = 1;
    @Input() currentSucursalId: number = 1;

    @Input() paymentToEdit: any | null = null;

    guardando = false;
    submitted = false;
    private defaultAccountId: number | null = null;

    showPrintModal = false;
    lastSavedPayment: any = null;

    metodoPagoOptions: ('EFECTIVO' | 'TRANSFERENCIA' | 'YAPE')[] = ['EFECTIVO', 'TRANSFERENCIA', 'YAPE'];

    form: OtroPago = this.resetForm();

    constructor(
        private otrosPagosService: OtrosPagosService,
        private cuentaService: CuentaCorrienteService,
        private alertasService: AlertasService
    ) { }

    ngOnInit() {
        this.cargarCuentas();
    }

    ngOnChanges() {
        if (this.isOpen) {
            this.submitted = false;
            if (this.paymentToEdit) {

                const rawDate = this.paymentToEdit.fecha || this.paymentToEdit.fechaMovimiento;
                let isoDate = new Date().toISOString().split('T')[0];

                if (rawDate) {
                    if (Array.isArray(rawDate)) {
                        const d = new Date(rawDate[0], rawDate[1] - 1, rawDate[2]);
                        isoDate = d.toISOString().split('T')[0];
                    } else {
                        isoDate = rawDate.split('T')[0];
                    }
                }

                this.form = {
                    ...this.paymentToEdit,
                    fecha: isoDate,
                    monto: this.paymentToEdit.monto || 0,
                };
            } else {
                this.form = this.resetForm();
            }
        }
    }

    cargarCuentas() {
        this.cuentaService.getActive().subscribe(res => {
            const cuentas = res || [];
            const found = cuentas.find(c => {
                const alias = (c.alias || '').toLowerCase();
                const titular = (c.nombreTitular || '').toLowerCase();
                const banco = (c.banco || '').toLowerCase();
                return alias.includes('chica') ||
                    alias.includes('ventanilla') ||
                    titular.includes('chica') ||
                    banco.includes('caja chica');
            });

            if (found) {
                this.defaultAccountId = found.idCuenta;
            } else if (cuentas.length > 0) {
                this.defaultAccountId = cuentas[0].idCuenta;
            }
        });
    }

    resetForm(): OtroPago {
        return {
            fecha: new Date().toISOString().split('T')[0],
            beneficiario: '',
            tipoPersona: 'EXTERNO',
            dni: '',
            monto: 0,
            metodoPago: 'EFECTIVO',
            concepto: '',
            telefono: '',
            email: '',
            nombreCampana: '',
            esAnonimo: false
        };
    }

    onMontoFocus() {
        if (this.form.monto === 0) {
            this.form.monto = null as any;
        }
    }

    onMontoBlur() {
        if (this.form.monto === null || this.form.monto === undefined || this.form.monto.toString() === '') {
            this.form.monto = 0;
        }
    }

    close() {
        this.isOpen = false;
        this.paymentToEdit = null;
        this.onClose.emit();
    }

    closePrint() {
        this.showPrintModal = false;
        this.lastSavedPayment = null;
        this.close();
    }

    guardarPago() {
        this.submitted = true;
        if (!this.validarForm()) return;

        this.guardando = true;

        this.form.idUsuario = this.currentUserId;
        this.form.idSucursal = this.currentSucursalId;

        // Silent Link
        if (this.defaultAccountId) {
            this.form.idCuenta = this.defaultAccountId;
        }

        const request$ = this.paymentToEdit && this.paymentToEdit.id
            ? this.otrosPagosService.actualizarOtroPago(this.paymentToEdit.id, this.form)
            : this.otrosPagosService.registrarOtroPago(this.form);

        request$.subscribe({
            next: (response) => {
                this.guardando = false;

                this.alertasService.success(
                    this.paymentToEdit ? 'Pago Actualizado' : 'Pago Registrado',
                    'Los datos se han guardado correctamente.'
                );
                
                this.lastSavedPayment = { ...this.form, ...response };
                this.showPrintModal = true;
                this.onSave.emit();
                this.form = this.resetForm();
                this.submitted = false;
            },
            error: (err) => {
                this.guardando = false;
                this.alertasService.error('Error', err.error?.message || 'No se pudo guardar el pago');
            }
        });
    }

    isDniInvalid(): boolean {
        if (!this.form.dni) return false;
        return !/^\d{8}$/.test(this.form.dni);
    }

    isTelefonoInvalid(): boolean {
        if (!this.form.telefono) return false;
        return !/^9\d{8}$/.test(this.form.telefono.replace(/\s/g, ''));
    }

    isEmailInvalid(): boolean {
        if (!this.form.email) return false;
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
        return !emailRegex.test(this.form.email);
    }

    validarForm(): boolean {
        if (!this.form.beneficiario && !this.form.esAnonimo) {
            this.alertasService.error('Atención', 'Debe ingresar el nombre del beneficiario');
            return false;
        }
        if (this.form.monto <= 0) {
            this.alertasService.error('Atención', 'El monto debe ser mayor a 0');
            return false;
        }

        if (this.isDniInvalid()) {
            this.alertasService.error('Atención', 'El DNI debe tener 8 números');
            return false;
        }
        if (this.isTelefonoInvalid()) {
            this.alertasService.error('Atención', 'El teléfono debe tener 9 números y empezar con 9');
            return false;
        }
        if (this.isEmailInvalid()) {
            this.alertasService.error('Atención', 'El formato del email no es válido');
            return false;
        }

        return true;
    }
}
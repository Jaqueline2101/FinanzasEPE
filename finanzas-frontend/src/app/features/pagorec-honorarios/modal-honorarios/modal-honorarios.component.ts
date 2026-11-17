import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { PagoComprobante } from '../../../models/pago-honorarios.model';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';
import { AlertasService } from '../../../services/alertas.service';
import { inject } from '@angular/core';
import { CajaChicaService, CajaChica } from '../../../services/caja-chica.service';

@Component({
    selector: 'app-modal-honorarios',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule, AlertasComponent],
    templateUrl: './modal-honorarios.component.html',
    styleUrls: ['./modal-honorarios.component.css']
})
export class ModalHonorariosComponent implements OnInit {
    @Input() pago?: PagoComprobante;
    @Output() onSave = new EventEmitter<any>();
    @Output() onPreview = new EventEmitter<any>();
    @Output() onClose = new EventEmitter<void>();
    private alertaService = inject(AlertasService);
    private cajaChicaService = inject(CajaChicaService);
    cajaAbierta: CajaChica | null = null;
    loadingCaja = false;

    form!: FormGroup;
    formatoImpresion: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' | null = null;
    formatOptions: ('TICKET_80' | 'TICKET_58' | 'A4' | 'A5')[] = ['TICKET_80', 'TICKET_58', 'A4', 'A5'];
    meses = [
        { id: 1, nombre: 'Enero' },
        { id: 2, nombre: 'Febrero' },
        { id: 3, nombre: 'Marzo' },
        { id: 4, nombre: 'Abril' },
        { id: 5, nombre: 'Mayo' },
        { id: 6, nombre: 'Junio' },
        { id: 7, nombre: 'Julio' },
        { id: 8, nombre: 'Agosto' },
        { id: 9, nombre: 'Septiembre' },
        { id: 10, nombre: 'Octubre' },
        { id: 11, nombre: 'Noviembre' },
        { id: 12, nombre: 'Diciembre' }
    ];

    constructor(private fb: FormBuilder) {
        this.initForm();
    }

    ngOnInit(): void {
        this.verificarCajaAbierta();
        if (this.pago) {
            this.form.patchValue(this.pago);
            this.handleMedioPagoChanges(this.pago.medioPago);
            if (this.pago.detalles && this.pago.detalles.length > 0) {
                this.setDetalles(this.pago.detalles);
            }
        }
    }

    verificarCajaAbierta() {
        this.loadingCaja = true;
        this.cajaChicaService.obtenerCajaAbierta().subscribe({
            next: (res) => {
                this.cajaAbierta = res.data;
                this.loadingCaja = false;
            },
            error: (err) => {
                console.error('Error al obtener caja abierta', err);
                this.cajaAbierta = null;
                this.loadingCaja = false;
            }
        });
    }

    private initForm() {
        this.form = this.fb.group({
            tipoPago: ['HONORARIO', Validators.required],
            numeroRecibo: [''],
            serieComprobante: [''],
            fechaEmision: [this.getLocalDate(), Validators.required],
            fechaPago: [''],
            periodoMes: [new Date().getMonth() + 1],
            periodoAnio: [new Date().getFullYear()],
            conceptoPago: ['', Validators.required],
            estadoPago: ['PAGADO'],
            estadoValidacion: ['VALIDADO'],
            descripcionAdicional: [''],

            tipoBeneficiario: ['TRABAJADOR', Validators.required],
            numeroDocumento: ['', Validators.required],
            nombreRazonSocial: ['', Validators.required],

            montoBruto: [0, [Validators.required, Validators.min(0)]],
            descuento: [0],
            retencion: [0],
            porcentajeRetencion: [8],
            aporteAfpOnp: [0],
            totalRetenciones: [0],
            montoNeto: [0, Validators.required],
            moneda: ['PEN', Validators.required],
            tipoCambio: [1],

            medioPago: ['TRANSFERENCIA', Validators.required],
            bancoDestino: [''],
            numeroCuenta: [''],
            numeroOperacion: [''],
            codigoTransferencia: [''],
            fechaTransferencia: [''],

            cuentaContable: [''],
            subcuenta: [''],
            tipoGasto: [''],
            numeroAsiento: [''],
            glosaContable: [''],
            periodoContable: [''],

            detalles: this.fb.array([])
        });

        // Cálculos automáticos y lógica de campos
        this.form.valueChanges.subscribe(() => {
            this.calcularTotales();
        });

        // Lógica de deshabilitación de campos bancarios
        this.form.get('medioPago')?.valueChanges.subscribe(value => {
            this.handleMedioPagoChanges(value);
        });

        // Lógica para detectar si es honorario y bloquear campo retención
        this.form.get('tipoPago')?.valueChanges.subscribe(value => {
            this.handleTipoPagoChanges(value);
        });

        // Estado inicial
        this.handleMedioPagoChanges(this.form.get('medioPago')?.value);
        this.handleTipoPagoChanges(this.form.get('tipoPago')?.value);
    }

    private handleTipoPagoChanges(tipoPago: string) {
        if (tipoPago === 'HONORARIO') {
            this.form.get('retencion')?.disable({ emitEvent: false });
            this.calcularTotales();
        } else {
            this.form.get('retencion')?.enable({ emitEvent: false });
        }
    }

    private handleMedioPagoChanges(medioPago: string) {
        const bankFields = ['bancoDestino', 'numeroCuenta', 'codigoTransferencia', 'fechaTransferencia'];
        if (medioPago === 'EFECTIVO') {
            bankFields.forEach(field => {
                this.form.get(field)?.disable({ emitEvent: false });
                this.form.get(field)?.setValue('', { emitEvent: false });
            });
        } else {
            bankFields.forEach(field => {
                this.form.get(field)?.enable({ emitEvent: false });
            });
        }
    }

    get detallesForm() {
        return this.form.get('detalles') as FormArray;
    }

    private setDetalles(detalles: any[]) {
        const detailFGs = detalles.map(d => this.fb.group(d));
        const detailFormArray = this.fb.array(detailFGs);
        this.form.setControl('detalles', detailFormArray);
    }

    calcularTotales() {
        // Enforce the 8% automatic retention rule for "HONORARIO"
        const isHonorario = this.form.get('tipoPago')?.value === 'HONORARIO';
        const bruto = this.form.get('montoBruto')?.value || 0;
        let ret = this.form.get('retencion')?.value || 0;

        if (isHonorario) {
            const porcentaje = this.form.get('porcentajeRetencion')?.value || 0;
            if (bruto > 1500) {
                ret = bruto * (porcentaje / 100);
            } else {
                ret = 0;
            }
            this.form.get('retencion')?.setValue(ret, { emitEvent: false });
        }

        const desc = this.form.get('descuento')?.value || 0;
        const afp = this.form.get('aporteAfpOnp')?.value || 0;

        const totalRet = ret + afp;
        const neto = bruto - desc - totalRet;

        this.form.get('totalRetenciones')?.setValue(totalRet, { emitEvent: false });
        this.form.get('montoNeto')?.setValue(neto, { emitEvent: false });
    }

    save() {
        if (!this.cajaAbierta) {
            this.alertaService.error('Caja Cerrada', 'No se puede realizar el pago. Por favor abra la caja primero.');
            return;
        }

        if (this.form.valid) {
            const montoNeto = this.form.get('montoNeto')?.value || 0;
            const medioPago = this.form.get('medioPago')?.value;

            // If payment is EFECTIVO, check saldo
            if (medioPago === 'EFECTIVO' && this.cajaAbierta.saldoActual < montoNeto) {
                this.alertaService.error('Saldo Insuficiente', `El saldo de la caja es S/ ${this.cajaAbierta.saldoActual.toFixed(2)}, insuficiente para realizar el pago de S/ ${montoNeto.toFixed(2)}.`);
                return;
            }

            this.onSave.emit({
                ...this.form.getRawValue(), // Usa getRawValue para incluir campos disabled, si hubiera
                formatoImpresion: this.formatoImpresion,
                cajaChicaId: this.cajaAbierta.id
            });
        } else {
            this.form.markAllAsTouched();
            this.alertaService.error('Campo requeridos', 'Por favor complete todos los campos obligatorios del formulario.');
        }
    }

    toggleFormato(f: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5') {
        if (this.formatoImpresion === f) {
            this.formatoImpresion = null;
        } else {
            this.formatoImpresion = f;
        }
    }

    previsualizar(f: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5', event: Event) {
        event.stopPropagation();
        this.onPreview.emit({
            ...this.form.getRawValue(),
            formatoImpresion: f
        });
    }

    onFocusInput(controlName: string) {
        const control = this.form.get(controlName);
        if (control && (control.value === 0 || control.value === '0')) {
            control.setValue(null, { emitEvent: false });
        }
    }

    onBlurInput(controlName: string) {
        const control = this.form.get(controlName);
        if (control && (control.value === null || control.value === '')) {
            control.setValue(0, { emitEvent: false });
            this.calcularTotales();
        }
    }

    private getLocalDate(): string {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    close() {
        this.onClose.emit();
    }
}

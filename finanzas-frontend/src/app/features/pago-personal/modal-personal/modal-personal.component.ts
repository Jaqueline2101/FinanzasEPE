import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PagoComprobante } from '../../../models/pago-honorarios.model';
import { PlanillaEngineService } from '../../../core/services/planilla-engine.service';
import { EmpleadoService, EmpleadoDTO } from '../../../services/empleado.service';
import { AlertasService } from '../../../services/alertas.service';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
    selector: 'app-modal-personal',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule],
    templateUrl: './modal-personal.component.html',
    styleUrls: ['./modal-personal.component.css']
})
export class ModalPersonalComponent implements OnInit {
    private _pago?: PagoComprobante;
    @Input() set pago(value: PagoComprobante | undefined) {
        this._pago = value;
        if (value) {
            this.loadPagoData(value);
        }
    }
    get pago() { return this._pago; }

    @Output() onSave = new EventEmitter<any>();
    @Output() onClose = new EventEmitter<void>();

    public MESES = ['ENERO', 'FEBRERO', 'MARZO', 'ABRIL', 'MAYO', 'JUNIO', 'JULIO', 'AGOSTO', 'SEPTIEMBRE', 'OCTUBRE', 'NOVIEMBRE', 'DICIEMBRE'];

    form!: FormGroup;
    
    // Autocompletado de empleados
    allEmpleados: any[] = [];
    filteredEmpleados: any[] = [];
    showSuggestions = false;
    currentSearchField: 'dni' | 'nombre' = 'dni';

    constructor(
        private fb: FormBuilder,
        private planillaEngine: PlanillaEngineService,
        private empleadoService: EmpleadoService,
        private alertasService: AlertasService,
        private cdr: ChangeDetectorRef
    ) {
        this.initForm();
    }

    ngOnInit(): void {
        this.loadEmpleados();
        this.setupAutocomplete();
    }

    private loadEmpleados() {
        this.empleadoService.getAll().subscribe(data => {
            this.allEmpleados = data.filter(e => e.estado !== false).map(e => ({
                ...e,
                nombreCompleto: `${e.nombre} ${e.apepat} ${e.apemat}`.trim(),
                identificacion: e.numdoc || e.ruc || ''
            } as any));
        });
    }

    private setupAutocomplete() {
        // Escuchar cambios en DNI
        this.form.get('numeroDocumento')?.valueChanges
            .pipe(debounceTime(300), distinctUntilChanged())
            .subscribe(val => {
                if (val && val.length > 2 && this.currentSearchField === 'dni') {
                    this.filtrarEmpleados(val, 'dni');
                } else {
                    this.showSuggestions = false;
                }
            });

        // Escuchar cambios en Nombre
        this.form.get('nombreRazonSocial')?.valueChanges
            .pipe(debounceTime(300), distinctUntilChanged())
            .subscribe(val => {
                if (val && val.length > 2 && this.currentSearchField === 'nombre') {
                    this.filtrarEmpleados(val, 'nombre');
                } else {
                    this.showSuggestions = false;
                }
            });
    }

    onFieldFocus(field: 'dni' | 'nombre') {
        this.currentSearchField = field;
    }

    private filtrarEmpleados(query: string, type: 'dni' | 'nombre') {
        const q = query.toLowerCase();
        this.filteredEmpleados = this.allEmpleados.filter(e => {
            if (type === 'dni') {
                return e.identificacion?.includes(q);
            } else {
                return e.nombreCompleto?.toLowerCase().includes(q);
            }
        }).slice(0, 5); // Mostrar max 5 sugerencias

        this.showSuggestions = this.filteredEmpleados.length > 0;
        this.cdr.markForCheck();
    }

    seleccionarEmpleado(emp: any) {
        this.form.patchValue({
            numeroDocumento: emp.identificacion,
            nombreRazonSocial: emp.nombreCompleto
        }, { emitEvent: false });
        
        this.showSuggestions = false;
        this.cdr.markForCheck();
    }

    private loadPagoData(data: PagoComprobante) {
        setTimeout(() => {
            if (this.form) {
                if (typeof data.periodoMes === 'number') {
                    data.periodoMes = this.MESES[data.periodoMes - 1];
                }
                this.form.patchValue(data);
                this.mapearDetalles(data);
                this.cdr.markForCheck();
                this.cdr.detectChanges();
            }
        }, 0);
    }

    private mapearDetalles(pago: PagoComprobante) {
        if (!pago.detalles || pago.detalles.length === 0) return;

        const mappings: { [key: string]: string } = {
            'Sueldo Base': 'sueldoBase'
        };

        const formValues: any = {};
        pago.detalles.forEach(d => {
            const field = mappings[d.nombreConcepto || ''];
            if (field) {
                formValues[field] = d.monto;
            }
        });

        this.form.patchValue(formValues, { emitEvent: false });
        this.calcularTotales();
    }

    private initForm() {
        this.form = this.fb.group({
            // Datos Generales
            tipoPago: ['SUELDO', Validators.required],
            periodoMes: [this.MESES[new Date().getMonth()], Validators.required],
            periodoAnio: [new Date().getFullYear(), Validators.required],
            fechaPago: [this.getLocalDate(), Validators.required],
            conceptoPago: ['', Validators.required],
            estadoPago: ['PAGADO'],
            estadoValidacion: ['VALIDADO'],

            // Datos del Trabajador
            numeroDocumento: ['', Validators.required],
            nombreRazonSocial: ['', Validators.required],

            // Ingresos
            sueldoBase: [0, Validators.min(0)],
            montoNeto: [{ value: 0, disabled: true }],
            moneda: ['PEN', Validators.required],

            // Forma de Pago
            medioPago: ['DEPÓSITO', Validators.required],
            bancoDestino: [''],
            numeroCuenta: [''],
            numeroOperacion: [''],
            fechaDeposito: [''],
            fechaProceso: [this.getLocalDate()]
        });

        this.form.valueChanges.subscribe(() => {
            this.calcularTotales();
        });
    }

    calcularTotales() {
        const f = this.form.getRawValue();
        const sueldoBase = f.sueldoBase || 0;

        this.form.patchValue({
            montoNeto: sueldoBase
        }, { emitEvent: false });
    }

    save() {
        if (this.form.valid) {
            const val = this.form.getRawValue();

            const detalles = [];
            if (val.sueldoBase > 0) {
                detalles.push({ 
                    nombreConcepto: 'Sueldo Base', 
                    tipoConcepto: 'INGRESO', 
                    monto: val.sueldoBase 
                });
            }

            const payload = { 
                ...val, 
                detalles,
                montoBruto: val.sueldoBase,
                totalIngresos: val.sueldoBase,
                totalDescuentos: 0
            };

            // Sanitizar para el backend
            if (payload.fechaDeposito === '') payload.fechaDeposito = null;
            if (payload.fechaProceso === '') payload.fechaProceso = null;

            this.onSave.emit(payload);
        } else {
            this.form.markAllAsTouched();
            this.alertasService.error('Formulario Incompleto', 'Por favor, complete todos los campos obligatorios antes de continuar.');
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
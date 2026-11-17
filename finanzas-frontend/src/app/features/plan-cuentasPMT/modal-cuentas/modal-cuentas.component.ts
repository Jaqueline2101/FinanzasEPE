import { Component, EventEmitter, Input, OnInit, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PlanCuentasRegistro } from '../../../models/plan-cuentas-pmt.model';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-cuentas',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule, ModalComponent],
    templateUrl: './modal-cuentas.component.html',
    styleUrls: ['./modal-cuentas.component.css']
})
export class ModalCuentasComponent implements OnInit, OnChanges {
    @Input() isOpen: boolean = false;
    @Input() registro?: PlanCuentasRegistro;
    @Output() onSave = new EventEmitter<any>();
    @Output() onCancel = new EventEmitter<void>();

    form!: FormGroup;

    constructor(private fb: FormBuilder) {
        this.initForm();
    }

    ngOnInit(): void {
        if (this.registro) {
            this.form.patchValue(this.registro);
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['registro'] && this.form) {
            if (this.registro) {
                console.log('Parcheando registro:', this.registro);
                
                // Limpiar valores previos antes de parchear para evitar contaminar el nuevo objeto
                this.form.reset();
                this.form.patchValue(this.registro);
                
                // Forzar sincronización del tipo de registro con fallback
                const tipoValue = this.registro.tipoRegistro || (this.registro as any).tipo || 'TRABAJADOR';
                this.form.get('tipoRegistro')?.setValue(tipoValue.toUpperCase(), { emitEvent: true });

                // Marcar controles como tocados para forzar la actualización de validaciones visuales
                this.form.markAsUntouched();
                this.form.updateValueAndValidity();
            } else {
                this.form.reset();
                this.form.patchValue({
                    tipoRegistro: 'TRABAJADOR',
                    tipoDocumento: 'DNI',
                    estado: 'ACTIVO',
                    modalidadTrabajo: 'Presencial',
                    modalidadPago: 'Mensual',
                    estadoVinculo: 'ACTIVO'
                });
            }
        }
    }

    private initForm() {
        this.form = this.fb.group({
            id: [null],
            // Generales
            tipoRegistro: ['TRABAJADOR', Validators.required],
            tipoDocumento: ['DNI', Validators.required],
            numeroDocumento: ['', Validators.required],
            nombres: ['', Validators.required],
            apellidoPaterno: ['', Validators.required],
            apellidoMaterno: ['', Validators.required],
            razonSocial: [''],
            telefono: [''],
            correo: ['', [Validators.email]],
            direccion: [''],
            estado: ['ACTIVO'],
            observaciones: [''],

            // Vinculación
            fechaInicio: [''],
            fechaFin: [''],
            cargo: [''],
            funcionPrincipal: [''],
            area: [''],
            tipoContrato: [''],
            modalidadTrabajo: ['Presencial'],
            modalidadPago: ['Mensual'],
            centroCosto: [''],
            jefeInmediato: [''],
            motivoCese: [''],
            estadoVinculo: ['ACTIVO'],
        });
    }


    save() {
        if (this.form.valid) {
            this.onSave.emit(this.form.value);
        } else {
            this.form.markAllAsTouched();
        }
    }

    cancel() {
        this.onCancel.emit();
    }

    isInvalid(controlName: string): boolean {
        const control = this.form.get(controlName);
        return !!(control && control.invalid && (control.touched || control.dirty));
    }
}

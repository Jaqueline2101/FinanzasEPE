import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors, AsyncValidatorFn } from '@angular/forms';
import { Proveedor } from '../../../models/proveedor.model';
import { ModalComponent } from '../../../shared/components/modal/modal';

import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { ProveedorService } from '../../../services/proveedor.service';
import { AlertasService } from '../../../services/alertas.service';
import Swal from 'sweetalert2';
import { Observable, of, timer } from 'rxjs';
import { map, switchMap, catchError, first } from 'rxjs/operators';

@Component({
    selector: 'app-modal-proveedor',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, ModalComponent],
    templateUrl: './modal-proveedor.component.html'
})
export class ModalProveedorComponent implements OnInit {
    @Input() proveedor: Proveedor | null = null;
    @Input() isOpen: boolean = false;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<any>();

    form: FormGroup;
    isEditing = false;

    constructor(
        private fb: FormBuilder,
        private proveedorService: ProveedorService,
        private alertasService: AlertasService
    ) {
        this.form = this.fb.group({
            id: [null],
            sedes: [''],
            direccion: [''],
            tipoDocIdent: ['RUC', Validators.required],
            numDocIdent: ['', {
                validators: [Validators.required],
                asyncValidators: [this.documentUniqueValidator()],
                updateOn: 'blur'
            }],
            razonSocial: ['', {
                validators: [Validators.required],
                asyncValidators: [this.razonSocialUniqueValidator()],
                updateOn: 'blur'
            }],
            nombreComercial: [''],
            direcciones: this.fb.array([]),
            telefono: ['', [Validators.pattern(/^\d{9}$/)]],
            telefono2: [''],
            email: ['', [Validators.email]],
            email2: ['', [Validators.email]],
            departamento: [''],
            provincia: [''],
            distrito: [''],
            representante: [''],
            cargoRepresentante: [''],
            plazo: [null],
            banco: [''],
            cuentaBancaria: [''],
            cci: [''],
            sitioWeb: [''],
            tipoProveedor: ['BIENES'],
            observaciones: [''],
            estado: ['Habilitado', Validators.required],
            fechaRegistro: [new Date().toISOString().split('T')[0]]
        });

        // Escuchar cambios en tipoDocIdent para actualizar validaciones de numDocIdent
        this.form.get('tipoDocIdent')?.valueChanges.subscribe(() => {
            this.updateNumDocValidators();
        });
    }

    private updateNumDocValidators() {
        const tipoDoc = this.form.get('tipoDocIdent')?.value;
        const numDocControl = this.form.get('numDocIdent');

        if (numDocControl) {
            const validators = [Validators.required];
            if (tipoDoc === 'RUC') {
                validators.push(Validators.pattern(/^20\d{9}$/)); // Inicia con 20 y total 11 dígitos
            } else if (tipoDoc === 'DNI') {
                validators.push(Validators.pattern(/^\d{8}$/)); // Exactamente 8 dígitos
            }

            numDocControl.setValidators(validators);
            numDocControl.updateValueAndValidity();
        }
    }

    private documentUniqueValidator(): AsyncValidatorFn {
        return (control: AbstractControl): Observable<ValidationErrors | null> => {
            if (!control.value) return of(null);

            return timer(500).pipe(
                switchMap(() => this.proveedorService.checkDoc(control.value, this.form.get('id')?.value)),
                map(exists => (exists ? { docExists: true } : null)),
                catchError(() => of(null)),
                first()
            );
        };
    }

    private razonSocialUniqueValidator(): AsyncValidatorFn {
        return (control: AbstractControl): Observable<ValidationErrors | null> => {
            if (!control.value) return of(null);

            return timer(500).pipe(
                switchMap(() => this.proveedorService.checkRazonSocial(control.value, this.form.get('id')?.value)),
                map(exists => (exists ? { razonSocialExists: true } : null)),
                catchError(() => of(null)),
                first()
            );
        };
    }

    get direcciones(): FormArray {
        return this.form.get('direcciones') as FormArray;
    }

    ngOnInit(): void {
        this.direcciones.clear();

        if (this.proveedor) {
            this.isEditing = true;
            const phones = this.proveedor.telefono ? this.proveedor.telefono.split('|') : [];
            const emails = this.proveedor.email ? this.proveedor.email.split('|') : [];

            // Mapeo senior: Integer (DB) -> String (UI)
            let estadoUI = 'Habilitado';
            if (this.proveedor.estado === 0 || this.proveedor.estado === '0' || this.proveedor.estado === 'Inhabilitado') {
                estadoUI = 'Inhabilitado';
            }

            this.form.patchValue({
                ...this.proveedor,
                estado: estadoUI,
                telefono: phones[0] ? phones[0].trim() : '',
                telefono2: phones[1] ? phones[1].trim() : '',
                email: emails[0] ? emails[0].trim() : '',
                email2: emails[1] ? emails[1].trim() : '',
                fechaRegistro: this.proveedor.fechaRegistro ? this.proveedor.fechaRegistro.split('T')[0] : new Date().toISOString().split('T')[0]
            });

            this.handleDireccionesLoad(this.proveedor.direccion, this.proveedor.sedes);
        } else {
            this.isEditing = false;
            this.form.reset({
                tipoDocIdent: 'RUC',
                estado: 'Habilitado',
                fechaRegistro: new Date().toISOString().split('T')[0]
            });
            this.addDireccion();
        }
    }

    handleDireccionesLoad(direccionesStr?: string, sedesStr?: string) {
        this.direcciones.clear();

        const dirs = direccionesStr ? direccionesStr.split('|') : [];
        const sds = sedesStr ? sedesStr.split('|') : [];

        if (dirs.length === 0) {
            this.addDireccion();
            return;
        }
        const count = Math.max(dirs.length, sds.length);

        for (let i = 0; i < count; i++) {
            const d = dirs[i] ? dirs[i].trim() : '';
            const s = sds[i] ? sds[i].trim() : '';
            this.addDireccion(s, d);
        }
    }

    addDireccion(sedeVal: string = '', dirVal: string = ''): void {
        const group = this.fb.group({
            sede: [sedeVal],
            direccion: [dirVal, Validators.required]
        });
        this.direcciones.push(group);
    }

    removeDireccion(index: number): void {
        this.direcciones.removeAt(index);
    }

    close(): void {
        this.onClose.emit();
    }

    save(): void {
        this.form.markAllAsTouched();
        if (this.form.invalid || this.form.pending) {
            if (this.form.pending) {
                this.alertasService.warning('Validando', 'Por favor espere a que termine la validación de datos únicos.');
            }
            return;
        }

        // Validar Direcciones Únicas
        const dirValues = this.direcciones.value.map((d: any) => d.direccion?.trim().toLowerCase());
        const uniqueDirs = new Set(dirValues);
        if (uniqueDirs.size !== dirValues.length) {
            this.alertasService.warning('Advertencia', 'No se permiten direcciones duplicadas para el mismo proveedor.');
            return;
        }

        this.proceedToSave();
    }

    proceedToSave(): void {
        const formVal = this.form.getRawValue();
        const cleanItems = formVal.direcciones
            .filter((item: any) => item.direccion && item.direccion.trim().length > 0);

        const direccionesStr = cleanItems
            .map((item: any) => item.direccion.trim())
            .join(' | ');

        const sedesStr = cleanItems
            .map((item: any) => (item.sede ? item.sede.trim() : ''))
            .join(' | ');

        const telefonoStr = [formVal.telefono, formVal.telefono2].filter(t => t && t.trim()).join(' | ');
        const emailStr = [formVal.email, formVal.email2].filter(e => e && e.trim()).join(' | ');

        // Mapeo senior: String (UI) -> Integer (DB)
        const estadoDb = formVal.estado === 'Habilitado' ? 1 : 0;

        const payload = {
            ...formVal,
            estado: estadoDb,
            direccion: direccionesStr,
            sedes: sedesStr,
            telefono: telefonoStr,
            email: emailStr,
            fechaRegistro: formVal.fechaRegistro ? `${formVal.fechaRegistro}T00:00:00` : null
        };
        delete payload.telefono2;
        delete payload.email2;
        delete payload.direcciones;

        this.onSave.emit(payload);
    }

    validarFormulario(): boolean {
        this.form.markAllAsTouched();
        return this.form.valid;
    }
}

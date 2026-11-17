import { Component, EventEmitter, Output, Input, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { GrupoFamiliarService } from '../../../services/grupo-familiar.service';
import { UserService } from '../../../services/user.service';
import { GrupoFamiliarRequest, PARENTESCOS } from '../../../models/grupo-familiar.model';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-registrar-paciente-modal',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        ModalComponent
    ],
    templateUrl: './registrar-paciente-modal.component.html',
    styleUrls: ['./registrar-paciente-modal.component.css']
})
export class RegistrarPacienteModalComponent {

    @Output() pacienteRegistrado = new EventEmitter<any>();
    @Output() cerrarModal = new EventEmitter<void>();

    visible: boolean = false;
    pacienteForm: FormGroup;
    esFamiliar: boolean = false;
    trabajadorValido: boolean = false;
    nombreTrabajador: string = '';
    validandoTrabajador: boolean = false;
    enviando: boolean = false;
    dniRegistrado: boolean = false;
    emailRegistrado: boolean = false;
    validandoDni: boolean = false;
    validandoEmail: boolean = false;

    tiposDocumento = [
        { label: 'DNI', value: '01' },
        { label: 'Carnet de Extranjería', value: '04' },
        { label: 'Pasaporte', value: '07' }
    ];

    sexos = [
        { label: 'Masculino', value: 'M' },
        { label: 'Femenino', value: 'F' }
    ];

    parentescos = PARENTESCOS;

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        private grupoFamiliarService: GrupoFamiliarService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) {
        this.pacienteForm = this.fb.group({
            nombre: ['', Validators.required],
            apepat: ['', Validators.required],
            apemat: [''],
            tipodoc: ['01', Validators.required],
            numdoc: ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
            nacfec: ['', Validators.required],
            sexo: ['', Validators.required],
            fonLocal: [''],
            email: ['', [Validators.email]],
            direcc: [''],
            esFamiliar: [false],
            dniTrabajador: [''],
            parentesco: ['']
        });

        // Watch for changes in esFamiliar checkbox
        this.pacienteForm.get('esFamiliar')?.valueChanges.subscribe(value => {
            this.esFamiliar = value;
            if (value) {
                this.pacienteForm.get('dniTrabajador')?.setValidators([Validators.required, Validators.minLength(8)]);
                this.pacienteForm.get('parentesco')?.setValidators(Validators.required);
            } else {
                this.pacienteForm.get('dniTrabajador')?.clearValidators();
                this.pacienteForm.get('parentesco')?.clearValidators();
                this.trabajadorValido = false;
                this.nombreTrabajador = '';
            }
            this.pacienteForm.get('dniTrabajador')?.updateValueAndValidity();
            this.pacienteForm.get('parentesco')?.updateValueAndValidity();
            this.cd.detectChanges();
        });

        // Watch for changes in numdoc (DNI) to check uniqueness
        this.pacienteForm.get('numdoc')?.valueChanges.subscribe(value => {
            if (this.pacienteForm.get('numdoc')?.valid) {
                this.validandoDni = true;
                this.dniRegistrado = false;
                this.userService.checkNumdocExists(value).subscribe({
                    next: (res) => {
                        this.dniRegistrado = res.data;
                        this.validandoDni = false;
                        this.cd.detectChanges();
                    },
                    error: () => {
                        this.validandoDni = false;
                        this.cd.detectChanges();
                    }
                });
            } else {
                this.dniRegistrado = false;
            }
        });

        // Watch for changes in email to check uniqueness
        this.pacienteForm.get('email')?.valueChanges.subscribe(value => {
            if (this.pacienteForm.get('email')?.valid && value) {
                this.validandoEmail = true;
                this.emailRegistrado = false;
                this.userService.checkEmailExists(value).subscribe({
                    next: (res) => {
                        this.emailRegistrado = res.data;
                        this.validandoEmail = false;
                        this.cd.detectChanges();
                    },
                    error: () => {
                        this.validandoEmail = false;
                        this.cd.detectChanges();
                    }
                });
            } else {
                this.emailRegistrado = false;
            }
        });
    }

    abrir() {
        this.visible = true;
        this.resetForm();
    }

    cerrar() {
        this.visible = false;
        this.resetForm();
        this.cerrarModal.emit();
    }

    resetForm() {
        this.pacienteForm.reset({
            tipodoc: '01',
            esFamiliar: false
        });
        this.esFamiliar = false;
        this.trabajadorValido = false;
        this.nombreTrabajador = '';
        this.enviando = false;
        this.dniRegistrado = false;
        this.emailRegistrado = false;
        this.validandoDni = false;
        this.validandoEmail = false;
    }

    getFormValidationErrors(): string[] {
        const errors: string[] = [];
        Object.keys(this.pacienteForm.controls).forEach(key => {
            const control = this.pacienteForm.get(key);
            if (control && control.errors) {
                Object.keys(control.errors).forEach(errorKey => {
                    errors.push(`${key}: ${errorKey}`);
                });
            }
        });
        return errors;
    }

    validarTrabajador() {
        const dniTrabajador = this.pacienteForm.get('dniTrabajador')?.value;

        if (!dniTrabajador || dniTrabajador.length < 8) {
            this.trabajadorValido = false;
            this.nombreTrabajador = '';
            return;
        }

        this.validandoTrabajador = true;

        this.grupoFamiliarService.validarTrabajador(dniTrabajador).subscribe({
            next: (response) => {
                this.trabajadorValido = response.esValido;
                this.nombreTrabajador = response.nombre || '';
                this.validandoTrabajador = false;

                if (!response.esValido) {
                    // Alert removed to prevent UI blocking, using inline error instead
                    console.warn(response.mensaje || 'DNI no corresponde a trabajador activo');
                }
                this.cd.detectChanges();
            },
            error: (error) => {
                console.error('Error validando trabajador:', error);
                this.trabajadorValido = false;
                this.nombreTrabajador = '';
                this.validandoTrabajador = false;
                // Alert removed
                this.cd.detectChanges();
            }
        });
    }

    guardar() {
        if (this.enviando) return;
        console.log('🔵 Guardar clicked');
        console.log('Form valid:', this.pacienteForm.valid);
        console.log('Form value:', this.pacienteForm.value);
        console.log('Form errors:', this.getFormValidationErrors());

        if (this.pacienteForm.invalid) {
            const errors = this.getFormValidationErrors();
            console.error('❌ Form is invalid:', errors);
            this.alertasService.warning('Atención', 'Por favor complete todos los campos requeridos');
            return;
        }

        if (this.esFamiliar && !this.trabajadorValido) {
            console.error('❌ Trabajador no validado');
            this.alertasService.warning('Validación Requerida', 'Debe validar el DNI del trabajador titular');
            return;
        }

        if (this.dniRegistrado) {
            this.alertasService.warning('Atención', 'El número de documento ya está registrado en el sistema');
            return;
        }

        if (this.emailRegistrado) {
            this.alertasService.warning('Atención', 'El correo electrónico ya está registrado en el sistema');
            return;
        }

        console.log('✅ Validation passed, creating patient...');
        this.enviando = true;

        const formValue = this.pacienteForm.value;

        // 1. Crear el paciente
        // Asegurar que la contraseña tenga al menos 8 caracteres y una letra (requerimiento del backend)
        // Agregamos 'P' al inicio para cumplir con el requisito de tener al menos una letra
        const password = 'P' + formValue.numdoc.padEnd(7, '0');

        const pacienteData = {
            username: formValue.numdoc,
            password: password, // Contraseña con letra + números, cumple requisitos
            firstName: formValue.nombre,
            paternalSurname: formValue.apepat,
            maternalSurname: formValue.apemat || '',
            lastName: `${formValue.apepat} ${formValue.apemat || ''}`.trim(),
            email: formValue.email || `${formValue.numdoc}@temp.com`,
            roleIds: [], // El backend asignará el rol por defecto (USUARIO/PACIENTE)
            nombre: formValue.nombre,
            apepat: formValue.apepat,
            apemat: formValue.apemat,
            tipodoc: formValue.tipodoc,
            numdoc: formValue.numdoc,
            nacfec: formValue.nacfec,
            sexo: formValue.sexo,
            fonLocal: formValue.fonLocal,
            direcc: formValue.direcc,
            active: true
        };

        console.log('📤 Sending patient data:', pacienteData);

        this.userService.createUser(pacienteData).subscribe({
            next: (pacienteCreado) => {
                // 2. Si es familiar, crear la relación
                if (this.esFamiliar) {
                    const familiarRequest: GrupoFamiliarRequest = {
                        idTitular: formValue.dniTrabajador,
                        idFamiliar: formValue.numdoc,
                        parentesco: formValue.parentesco,
                        observaciones: `Registrado desde Caja`
                    };

                    this.grupoFamiliarService.registrarFamiliar(familiarRequest).subscribe({
                        next: () => {
                            this.alertasService.success('¡Éxito!', 'Paciente y relación familiar animados correctamente');
                            this.pacienteRegistrado.emit(pacienteCreado);
                            this.cerrar();
                        },
                        error: (error) => {
                            console.error('Error registrando relación familiar:', error);
                            this.alertasService.warning('Atención', 'Paciente creado pero hubo un error al registrar la relación familiar');
                            this.pacienteRegistrado.emit(pacienteCreado);
                            this.cerrar();
                        }
                    });
                } else {
                    this.alertasService.success('¡Guardado!', 'El paciente ha sido registrado correctamente');
                    this.pacienteRegistrado.emit(pacienteCreado);
                    this.cerrar();
                }
            },
            error: (error) => {
                this.enviando = false;
                console.error('Error creando paciente:', error);
                const errorMsg = error.error?.message || 'Error al registrar paciente';
                this.alertasService.error('Error en el Registro', errorMsg);
            }
        });
    }
}

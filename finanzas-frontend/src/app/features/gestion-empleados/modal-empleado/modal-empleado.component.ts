import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule, FormArray } from '@angular/forms';
import { EmpleadoDTO, EmpleadoService } from '../../../services/empleado.service';
import { GrupoFamiliarService } from '../../../services/grupo-familiar.service';
import { SucursalService } from '../../../core/services/sucursal.service';
import { GrupoFamiliar, GrupoFamiliarRequest, PARENTESCOS } from '../../../models/grupo-familiar.model';
import { UserService } from '../../../services/user.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-empleado',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, ModalComponent],
    templateUrl: './modal-empleado.component.html'
})
export class ModalEmpleadoComponent implements OnInit {
    @Input() isOpen: boolean = false;
    @Input() empleadoId: number | null = null;
    @Output() closeModal = new EventEmitter<boolean>();

    private fb = inject(FormBuilder);
    private empleadoService = inject(EmpleadoService);
    private grupoFamiliarService = inject(GrupoFamiliarService);
    private userService = inject(UserService);
    private cd = inject(ChangeDetectorRef);
    private sucursalService = inject(SucursalService);
    private alertasService = inject(AlertasService);

    activeTab: 'general' | 'familia' = 'general';
    form: FormGroup;
    loading = false;
    isEditMode = false;

    familiares: GrupoFamiliar[] = [];
    nuevoFamiliarDni: string = '';
    nuevoFamiliarParentesco: string = 'HIJO';
    registrandoManual = false;
    nuevoFamiliarNombre = '';
    nuevoFamiliarApePat = '';
    nuevoFamiliarApeMat = '';
    nuevoFamiliarNacFec = '';
    nuevoFamiliarSexo = 'M';

    parentescos = PARENTESCOS;
    buscandoFamiliar = false;

    dniRegistrado: boolean = false;
    emailRegistrado: boolean = false;
    validandoDni: boolean = false;
    validandoEmail: boolean = false;
    originalNumdoc: string = '';
    originalEmail: string = '';

    profesiones = [
        'Médico', 'Enfermero', 'Tecnólogo Médico', 'Psicólogo', 'Nutricionista',
        'Químico Farmacéutico', 'Administrador', 'Contador', 'Economista', 'Ingeniero', 
        'Abogado', 'Técnico Administrativo', 'Técnico Enfermería', 'Otros'
    ];
    
    gradosAcademicos = [
        'Técnico', 'Bachiller', 'Licenciado', 'Magister', 'Doctorado', 'Especialidad'
    ];

    modalidades = ['Presencial', 'Remoto', 'Híbrido', 'Tiempo Completo', 'Tiempo Parcial'];
    condiciones = ['Contratado', 'Nombrado', 'Practicante', 'Temporal', 'Locación de Servicios'];
    sucursales: any[] = [];

    constructor() {
        this.form = this.fb.group({
            id: [null],
            tipoDoc: ['DNI', Validators.required],
            numdoc: ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
            nombre: ['', Validators.required],
            apepat: ['', Validators.required],
            apemat: ['', Validators.required],
            sexo: ['M', Validators.required],
            nacfec: [null, Validators.required],
            direcc: [''],
            fono1: [''],
            email: ['', [Validators.email]],
            ruc: [''],
            estadoCivil: ['S'],
            idOcupacion: [null],
            nombreOcupacion: [''],
            formaciones: this.fb.array([this.crearFormacion()]),
            fechaIngreso: [null, Validators.required],
            fechaFin: [null],
            centroLabor: ['SEDE CENTRAL'],
            modalidadTrabajo: ['Presencial'],
            tipoContrato: ['Contratado'],
            sueldoBase: [0, [Validators.required, Validators.min(0)]],
            renovacion: [false],
            estado: [true]
        });
    }

    get formaciones() {
        return this.form.get('formaciones') as FormArray;
    }

    crearFormacion(grado = 'Bachiller', profesion = 'Otros') {
        return this.fb.group({
            gradoAcademico: [grado, Validators.required],
            profesion: [profesion, Validators.required]
        });
    }

    agregarFormacion() {
        this.formaciones.push(this.crearFormacion());
    }

    eliminarFormacion(index: number) {
        if (this.formaciones.length > 1) {
            this.formaciones.removeAt(index);
        }
    }

    ngOnInit(): void {
        this.loadSucursales();
        if (this.empleadoId) {
            this.isEditMode = true;
            this.loadEmpleado(this.empleadoId);
        } else {
            this.isEditMode = false;
        }
        this.setupValidations();
    }

    loadSucursales(): void {
        this.sucursalService.getAll(0, 1000).subscribe({
            next: (res: any) => {
                this.sucursales = res.data?.content || [];
                if (!this.isEditMode && this.sucursales.length > 0) {
                    this.form.patchValue({ centroLabor: this.sucursales[0].nombreSucursal });
                }
            },
            error: (err) => console.error('Error al cargar sucursales', err)
        });
    }

    setupValidations() {
        this.form.get('tipoDoc')?.valueChanges.subscribe(tipo => {
            const numdocControl = this.form.get('numdoc');
            if (tipo === 'DNI') {
                numdocControl?.setValidators([Validators.required, Validators.pattern('^[0-9]{8}$')]);
            } else if (tipo === 'RUC') {
                numdocControl?.setValidators([Validators.required, Validators.pattern('^[0-9]{11}$')]);
            }
            numdocControl?.updateValueAndValidity();
        });

        this.form.get('numdoc')?.valueChanges.subscribe(value => {
            const tipoDoc = this.form.get('tipoDoc')?.value;
            if (tipoDoc === 'RUC') {
                this.form.get('ruc')?.setValue(value, { emitEvent: false });
            } else {
                this.form.get('ruc')?.setValue('', { emitEvent: false });
            }

            if (this.form.get('numdoc')?.valid) {
                if (this.isEditMode && value === this.originalNumdoc) {
                    this.dniRegistrado = false;
                    return;
                }
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

        this.form.get('email')?.valueChanges.subscribe(value => {
            if (this.form.get('email')?.valid && value) {
                if (this.isEditMode && value === this.originalEmail) {
                    this.emailRegistrado = false;
                    return;
                }
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

    loadEmpleado(id: number) {
        this.loading = true;
        this.empleadoService.getById(id).subscribe({
            next: (data) => {
                if (data.ruc && data.ruc.length === 11) {
                    this.form.patchValue({ ...data, tipoDoc: 'RUC', numdoc: data.ruc });
                } else {
                    this.form.patchValue({ ...data, tipoDoc: 'DNI' });
                }

                if (data.nombreOcupacion && data.nombreOcupacion.includes(' - ')) {
                    const entries = data.nombreOcupacion.split('; ');
                    this.formaciones.clear();
                    entries.forEach(entry => {
                        const parts = entry.split(' - ');
                        if (parts.length === 2) {
                            this.formaciones.push(this.crearFormacion(parts[0], parts[1]));
                        }
                    });
                    if (this.formaciones.length === 0) this.formaciones.push(this.crearFormacion());
                } else if (data.nombreOcupacion) {
                    this.formaciones.clear();
                    this.formaciones.push(this.crearFormacion('Bachiller', data.nombreOcupacion));
                }
                
                this.originalNumdoc = data.numdoc || '';
                this.originalEmail = data.email || '';
                this.loading = false;
                if (data.numdoc) this.loadFamiliares(data.numdoc);
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.alertasService.error('Error', 'No se pudo cargar la información del empleado');
            }
        });
    }

    loadFamiliares(dniTitular: string) {
        this.grupoFamiliarService.obtenerFamiliaresPorTitular(dniTitular).subscribe(data => {
            this.familiares = data;
        });
    }

    buscarPorDni() {
        let doc = this.form.get('numdoc')?.value;
        if (!doc || doc.length < 8) return;
        if (doc.length === 11 && doc.startsWith('10')) doc = doc.substring(2, 10);

        this.loading = true;
        this.empleadoService.getByDni(doc).subscribe({
            next: (data) => {
                this.loading = false;
                this.isEditMode = true;
                this.originalNumdoc = data.numdoc || '';
                this.originalEmail = data.email || '';
                if (data.ruc && data.ruc.length === 11) {
                    this.form.patchValue({ ...data, tipoDoc: 'RUC', numdoc: data.ruc });
                } else {
                    this.form.patchValue({ ...data, tipoDoc: 'DNI', numdoc: data.numdoc });
                }
            },
            error: () => this.loading = false
        });
    }

    save() {
        if (this.dniRegistrado && !this.isEditMode) {
            this.alertasService.warning('Atención', 'El número de documento ya está registrado');
            return;
        }
        if (this.emailRegistrado && !this.isEditMode) {
            this.alertasService.warning('Atención', 'El correo electrónico ya está registrado');
            return;
        }
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading = true;
        const formValue = this.form.getRawValue();
        const dto: EmpleadoDTO = { ...formValue };

        if (formValue.tipoDoc === 'RUC') {
            dto.ruc = formValue.numdoc;
            if (formValue.numdoc.startsWith('10') && formValue.numdoc.length === 11) {
                dto.numdoc = formValue.numdoc.substring(2, 10);
            } else {
                dto.numdoc = formValue.numdoc;
            }
        } else {
            dto.ruc = '';
            dto.numdoc = formValue.numdoc;
        }

        const formacionesVal = formValue.formaciones;
        dto.nombreOcupacion = formacionesVal
            .map((f: any) => `${f.gradoAcademico} - ${f.profesion}`)
            .join('; ');

        const req = this.isEditMode && dto.id
            ? this.empleadoService.update(dto.id, dto)
            : this.empleadoService.create(dto);

        req.subscribe({
            next: () => {
                this.loading = false;
                this.alertasService.success('Guardado', 'Información del empleado guardada correctamente');
                this.closeModal.emit(true);
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.alertasService.error('Error', 'Hubo un problema al intentar guardar los datos');
            }
        });
    }

    agregarFamiliar() {
        if (!this.nuevoFamiliarDni || this.nuevoFamiliarDni.length < 8) {
            this.alertasService.warning('Atención', 'Ingrese un DNI válido para el familiar');
            return;
        }
        const dniTitular = this.form.get('numdoc')?.value;
        if (!dniTitular) {
            this.alertasService.error('Error', 'El empleado debe tener un DNI asignado antes de añadir familiares');
            return;
        }
        this.buscandoFamiliar = true;
        const request: GrupoFamiliarRequest = {
            idTitular: dniTitular,
            idFamiliar: this.nuevoFamiliarDni,
            parentesco: this.nuevoFamiliarParentesco,
            observaciones: 'Registro desde Módulo Empleados'
        };
        this.grupoFamiliarService.registrarFamiliar(request).subscribe({
            next: () => {
                this.buscandoFamiliar = false;
                this.nuevoFamiliarDni = '';
                this.registrandoManual = false;
                this.loadFamiliares(dniTitular);
                this.alertasService.success('Agregado', 'El familiar ha sido vinculado correctamente');
            },
            error: (err) => {
                this.buscandoFamiliar = false;
                this.alertasService.confirm(
                    'Familiar No Encontrado',
                    '¿Desea registrar los datos del familiar manualmente?',
                    'Sí, registrar',
                    'Cancelar'
                ).then((confirmado) => {
                    if (confirmado) {
                        this.registrandoManual = true;
                        this.cd.detectChanges();
                    }
                });
            }
        });
    }

    cancelarRegistroManual() {
        this.registrandoManual = false;
        this.nuevoFamiliarNombre = '';
        this.nuevoFamiliarApePat = '';
        this.nuevoFamiliarApeMat = '';
        this.nuevoFamiliarNacFec = '';
    }

    guardarFamiliarManual() {
        if (!this.nuevoFamiliarNombre || !this.nuevoFamiliarApePat || !this.nuevoFamiliarNacFec) {
            this.alertasService.warning('Atención', 'Complete todos los campos obligatorios del familiar');
            return;
        }
        const dniTitular = this.form.get('numdoc')?.value;
        if (!dniTitular) return;
        this.buscandoFamiliar = true;
        const request: GrupoFamiliarRequest = {
            idTitular: dniTitular,
            idFamiliar: this.nuevoFamiliarDni,
            parentesco: this.nuevoFamiliarParentesco,
            observaciones: 'Registro Manual',
            nombre: this.nuevoFamiliarNombre,
            apepat: this.nuevoFamiliarApePat,
            apemat: this.nuevoFamiliarApeMat,
            nacfec: this.nuevoFamiliarNacFec,
            sexo: this.nuevoFamiliarSexo
        };
        this.grupoFamiliarService.registrarFamiliar(request).subscribe({
            next: () => {
                this.buscandoFamiliar = false;
                this.registrandoManual = false;
                this.nuevoFamiliarDni = '';
                this.loadFamiliares(dniTitular);
                this.cd.detectChanges();
                this.alertasService.success('Registrado', 'Familiar registrado y vinculado correctamente');
            },
            error: () => { 
                this.buscandoFamiliar = false; 
                this.alertasService.error('Error', 'No se pudo completar el registro del familiar');
            }
        });
    }

    eliminarFamiliar(id: number) {
        this.alertasService.confirm(
            '¿Desvincular Familiar?',
            'El familiar ya no tendrá los beneficios asociados en el sistema.',
            'Sí, desvincular',
            'Cancelar'
        ).then((confirmado) => {
            if (confirmado) {
                this.grupoFamiliarService.desactivarFamiliar(id).subscribe(() => {
                    const dni = this.form.get('numdoc')?.value;
                    this.loadFamiliares(dni);
                    this.alertasService.success('Eliminado', 'El familiar ha sido desvinculado correctamente');
                });
            }
        });
    }

    close() {
        this.closeModal.emit(true);
    }
}

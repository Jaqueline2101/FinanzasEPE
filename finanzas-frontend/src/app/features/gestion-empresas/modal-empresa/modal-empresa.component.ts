import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Empresa, EmpresaService } from '../../../services/empresa.service';
import { CompaniaService } from '../../../services/compania.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-empresa',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-empresa.component.html',
    styles: []
})
export class ModalEmpresaComponent implements OnInit, OnChanges {
    @Input() isVisible: boolean = false;
    @Input() empresa: Empresa | null = null;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    formData: any = {
        codigo: '',
        nombre: '',
        abrev: '', // Nombre Comercial
        ruc: '',
        telefono: '',
        direccion: '',
        estado: '1',
        representante: '',
        fechaCre: ''
    };

    isEditMode: boolean = false;

    existingEmpresas: Empresa[] = [];
    errors: any = {
        codigo: '',
        nombre: '',
        abrev: '',
        ruc: '',
        representante: '',
        telefono: '',
        direccion: ''
    };

    constructor(
        private empresaService: EmpresaService,
        private companiaService: CompaniaService,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.loadEmpresas();
    }

    loadEmpresas(): void {
        this.empresaService.getEmpresas().subscribe({
            next: (data) => {
                this.existingEmpresas = data;
            },
            error: (err) => console.error('Error cargando empresas para validación', err)
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isVisible'] && this.isVisible) {
            this.loadEmpresas(); // Reload to ensure freshness
            if (this.empresa) {
                this.isEditMode = true;
                this.formData = {
                    ...this.empresa,
                    abrev: this.empresa.abrev || ''
                };
            } else {
                this.isEditMode = false;
                this.resetForm();
            }
            // Reset errors when opening
            this.errors = {
                codigo: '',
                nombre: '',
                abrev: '',
                ruc: '',
                representante: '',
                telefono: '',
                direccion: ''
            };
        }
    }

    close(): void {
        this.onClose.emit();
    }

    save(): void {
        if (!this.validarFormulario()) return;

        // Final check for existing errors from onBlur validations
        if (Object.values(this.errors).some(x => x !== '')) {
            this.alertasService.error('Error de Validación', 'Por favor corrija los errores marcados en rojo antes de guardar.');
            return;
        }

        this.proceedToSave();
    }

    searchRuc(): void {
        const ruc = this.formData.ruc;
        if (!ruc || ruc.length !== 11) return;

        // 1. Check local duplicates first (existing logic in validateField handles this, but we check here to avoid unnecessary API call)
        const duplicate = this.existingEmpresas.find(e => {
            const rucExisting = e.ruc || e.idPersonalUser;
            return rucExisting === ruc && (!this.isEditMode || e.idEmpresa !== this.empresa?.idEmpresa);
        });

        if (duplicate) {
            this.errors.ruc = 'El RUC ya existe';
            return;
        }

        // 2. Call API to find in cross-population (Compania or Person)
        this.companiaService.getByRuc(ruc).subscribe({
            next: (data: any) => {
                if (data) {
                    // Auto-fill fields if they are empty
                    if (!this.formData.nombre && data.nombre) this.formData.nombre = data.nombre;
                    // For Commercial Name, we prefer 'nombre' from response if it's not the same as Razon Social, otherwise empty or same
                    if (!this.formData.abrev && data.nombre) this.formData.abrev = data.nombre;

                    if (data.personaBase) {
                        if (!this.formData.direccion && data.personaBase.direcc) this.formData.direccion = data.personaBase.direcc;
                        if (!this.formData.telefono && data.personaBase.fonLocal) this.formData.telefono = data.personaBase.fonLocal;
                        // Map email if needed, though Empresa doesn't have email field in this form yet
                    } else if (data.direccion) {
                        if (!this.formData.direccion) this.formData.direccion = data.direccion;
                    }

                    this.alertasService.info(
                        'Datos Encontrados',
                        'Se han completado los datos automáticamente desde los registros existentes.'
                    );
                }
            },
            error: (err) => {
                // Not found is fine, we just don't auto-fill
                console.log('RUC not found in other records', err);
            }
        });
    }

    validateField(field: string): void {
        const value = this.formData[field];
        this.errors[field] = ''; // Reset

        switch (field) {
            case 'codigo':
                if (value) {
                    if (value.length > 20) {
                        this.errors.codigo = 'El código no puede exceder 20 caracteres';
                    } else {
                        const duplicate = this.existingEmpresas.find(e =>
                            e.codigo.toLowerCase() === value.toLowerCase() &&
                            (!this.isEditMode || e.idEmpresa !== this.empresa?.idEmpresa)
                        );
                        if (duplicate) this.errors.codigo = 'El código ya existe';
                    }
                }
                break;

            case 'nombre':
                if (!value) {
                    this.errors.nombre = 'La razón social es obligatoria';
                } else if (value.length > 200) {
                    this.errors.nombre = 'La razón social no puede exceder 200 caracteres';
                } else {
                    const duplicate = this.existingEmpresas.find(e =>
                        e.nombre.toLowerCase() === value.toLowerCase() &&
                        (!this.isEditMode || e.idEmpresa !== this.empresa?.idEmpresa)
                    );
                    if (duplicate) this.errors.nombre = 'La razón social ya existe';
                }
                break;

            case 'abrev':
                if (!value) {
                    this.errors.abrev = 'El nombre comercial es obligatorio';
                }
                break;

            case 'ruc':
                if (!value) {
                    this.errors.ruc = 'El RUC es obligatorio';
                } else if (!/^\d{11}$/.test(value)) {
                    this.errors.ruc = 'El RUC debe tener 11 dígitos numéricos';
                } else if (!value.startsWith('20')) {
                    this.errors.ruc = 'El RUC debe comenzar con "20"';
                } else {
                    // Check duplicate RUC (checking against mapped 'ruc' or 'idPersonalUser')
                    const duplicate = this.existingEmpresas.find(e => {
                        const rucExisting = e.ruc || e.idPersonalUser; // Service maps it to 'ruc' property usually, but let's be safe
                        return rucExisting === value &&
                            (!this.isEditMode || e.idEmpresa !== this.empresa?.idEmpresa);
                    });
                    if (duplicate) this.errors.ruc = 'El RUC ya existe';
                }
                break;

            case 'representante':
                if (!value) this.errors.representante = 'El representante es obligatorio';
                break;

            case 'telefono':
                if (!value) this.errors.telefono = 'El teléfono es obligatorio';
                break;

            case 'direccion':
                if (!value) this.errors.direccion = 'La dirección es obligatoria';
                break;
        }
    }



    proceedToSave(): void {
        const request = {
            ...this.formData,
            estado: this.formData.estado || '1',
            abrev: this.formData.abrev, // Use actual Nombre Comercial
            idPersonalUser: this.formData.ruc
        };

        if (this.isEditMode && this.empresa?.idEmpresa) {
            this.empresaService.updateEmpresa(this.empresa.idEmpresa, request).subscribe({
                next: () => {
                    this.handleSuccess('Actualizado', 'Empresa actualizada correctamente');
                },
                error: (err: any) => {
                    console.error('Error al actualizar:', err);
                    const errorMessage = err.error?.message || 'No se pudo actualizar la empresa';
                    this.handleError('Error', errorMessage);
                }
            });
        } else {
            this.empresaService.createEmpresa(request).subscribe({
                next: () => {
                    this.handleSuccess('Creado', 'Empresa creada correctamente');
                },
                error: (err: any) => {
                    console.error('Error al crear:', err);
                    const errorMessage = err.error?.message || 'No se pudo crear la empresa';
                    this.handleError('Error', errorMessage);
                }
            });
        }
    }

    validarFormulario(): boolean {
        const missingFields: string[] = [];

        if (!this.formData.nombre) missingFields.push('Razón Social');
        if (!this.formData.abrev) missingFields.push('Nombre Comercial');
        if (!this.formData.ruc) missingFields.push('RUC');
        if (!this.formData.codigo) missingFields.push('Código');
        if (!this.formData.representante) missingFields.push('Representante');
        if (!this.formData.telefono) missingFields.push('Teléfono');
        if (!this.formData.direccion) missingFields.push('Dirección');

        if (missingFields.length > 0) {
            const message = `Por favor, complete los siguientes campos obligatorios: ${missingFields.join(', ')}`;
            this.alertasService.warning('Campos Incompletos', message);
            return false;
        }
        return true;
    }

    handleSuccess(title: string, text: string): void {
        this.alertasService.success(title, text);
        this.onSave.emit();
        this.close();
    }

    handleError(title: string, text: string): void {
        console.error(text);
        this.alertasService.error(title, text);
    }

    resetForm(): void {
        this.formData = {
            codigo: '',
            nombre: '',
            ruc: '',
            telefono: '',
            direccion: '',
            estado: '1',
            representante: '',
            fechaCre: new Date().toISOString().split('T')[0]
        };
        this.errors = {
            codigo: '',
            nombre: '',
            ruc: ''
        };
    }
}

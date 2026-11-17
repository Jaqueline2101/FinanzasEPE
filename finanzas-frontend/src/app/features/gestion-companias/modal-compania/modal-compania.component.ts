import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Compania } from '../../../models/compania.model';
import { CompaniaService } from '../../../services/compania.service';
import { AlertasService } from '../../../services/alertas.service';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-modal-compania',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-compania.component.html',
    styles: []
})
export class ModalCompaniaComponent implements OnInit, OnChanges {
    @Input() isVisible: boolean = false;
    @Input() compania: Compania | null = null;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    formData: any = {
        codigo: '',
        nombre: '',
        razSol: '',
        codigoIafa: '',
        idTipoCia: 1,
        diasPlazo: 0,
        facturar: '1',
        estado: '1',
        email: '',
        fonLocal: '',
        direcc: '',
        representante: '',
        financieroNombre: '',
        fechaCreacion: '',
        codEps: '',
        codPlan: '',
        ctacte: '0',
        mostrarDirec: '1',
        mostrarDiasPlazo: '1'
    };

    errors: any = {
        nombre: '',
        razSol: '',
        codigoIafa: '',
        email: '',
        codigo: '',
        representante: '',
        direcc: '',
        codPlan: '',
        diasPlazo: '',
        fechaCreacion: '',
        financieroNombre: '',
        fonLocal: ''
    };

    existingCompanias: Compania[] = [];
    suggestions: any[] = [];
    searchTimeout: any;

    isEditMode: boolean = false;

    tipoCiaOptions = [
        { id: 1, label: 'EPS' },
        { id: 2, label: 'SEGUROS' },
        { id: 3, label: 'CONVENIO' }
    ];

    constructor(
        private companiaService: CompaniaService,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.loadCompanias();
    }

    loadCompanias() {
        this.companiaService.getCompanias().subscribe(data => {
            this.existingCompanias = data;
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isVisible'] && this.isVisible) {
            if (this.compania) {
                this.isEditMode = true;
                // Create a copy to avoid reference issues
                this.formData = { ...this.compania };

                // Ensure correct types for form controls
                // Selects expect strings usually, so converting to be safe
                if (this.formData.ctacte !== undefined && this.formData.ctacte !== null) {
                    this.formData.ctacte = String(this.formData.ctacte);
                } else {
                    this.formData.ctacte = '0';
                }

                if (this.formData.facturar !== undefined && this.formData.facturar !== null) {
                    this.formData.facturar = String(this.formData.facturar);
                } else {
                    this.formData.facturar = '1'; // Default
                }

                if (this.formData.estado !== undefined && this.formData.estado !== null) {
                    this.formData.estado = String(this.formData.estado);
                } else {
                    this.formData.estado = '1';
                }

                // Ensure diasPlazo is a number or null
                if (this.formData.diasPlazo !== undefined && this.formData.diasPlazo !== null) {
                    this.formData.diasPlazo = Number(this.formData.diasPlazo);
                } else {
                    this.formData.diasPlazo = null;
                }

                // Extract direcc from personaBase if present and not directly on compania
                if (!this.formData.direcc && this.compania.personaBase?.direcc) {
                    this.formData.direcc = this.compania.personaBase.direcc;
                }

                // Email and FonLocal from personaBase
                if (!this.formData.email && this.compania.personaBase?.email) {
                    this.formData.email = this.compania.personaBase.email;
                }
                if (!this.formData.fonLocal && this.compania.personaBase?.fonLocal) {
                    this.formData.fonLocal = this.compania.personaBase.fonLocal;
                }

                // Tipo cia
                if (this.formData.idTipoCia) {
                    this.formData.idTipoCia = Number(this.formData.idTipoCia);
                }

                // Mostrar flags
                this.formData.mostrarDirec = this.formData.mostrarDirec || '1';
                this.formData.mostrarDiasPlazo = this.formData.mostrarDiasPlazo || '1';

            } else {
                this.isEditMode = false;
                this.resetForm();
                this.loadCompanias(); // Reload to ensure up-to-date checklist
            }
        }
    }

    // ===== MÉTODOS ELIMINADOS (YA NO SON NECESARIOS) =====
    // Se volvió a usar [(ngModel)] directamente con validación en el campo

    searchRuc(): void {
        const ruc = this.formData.codigoIafa;
        if (!ruc || ruc.length !== 11) return;

        // 1. Check local duplicates first
        const duplicate = this.existingCompanias.find(c =>
            c.codigoIafa === ruc &&
            (!this.isEditMode || c.id !== this.compania?.id)
        );

        if (duplicate) {
            this.errors.codigoIafa = 'El RUC ya está registrado';
            return;
        }

        // 2. Call API to find in cross-population
        this.companiaService.getByRuc(ruc).subscribe({
            next: (data: any) => {
                if (data) {
                    // Auto-fill fields if they are empty
                    if (!this.formData.nombre && data.nombre) this.formData.nombre = data.nombre;
                    if (!this.formData.razSol) {
                        // Use razSol from backend if available, otherwise fall back
                        this.formData.razSol = data.razSol || data.personaBase?.nombre || data.nombre;
                    }

                    if (data.personaBase) {
                        if (!this.formData.direcc && data.personaBase.direcc) this.formData.direcc = data.personaBase.direcc;
                        if (!this.formData.fonLocal && data.personaBase.fonLocal) this.formData.fonLocal = data.personaBase.fonLocal;
                        if (!this.formData.email && data.personaBase.email) this.formData.email = data.personaBase.email;
                    } else if (data.direccion) { // In case it comes from Empresa structure which spans differently
                        if (!this.formData.direcc) this.formData.direcc = data.direccion;
                    }

                    // Map fields that might be at root level (from Compania or Empresa map)
                    if (!this.formData.representante && data.representante) this.formData.representante = data.representante;
                    if (!this.formData.financieroNombre && data.financieroNombre) this.formData.financieroNombre = data.financieroNombre;
                    if (!this.formData.email && data.email) this.formData.email = data.email;

                    this.alertasService.info('Datos Encontrados', 'Se han completado los datos automáticamente desde los registros existentes.');
                }
            },
            error: (err) => {
                console.log('RUC not found in other records', err);
            }
        });
    }

    onRazonSocialInput(event: any): void {
        const term = event.target.value;
        if (this.searchTimeout) clearTimeout(this.searchTimeout);

        if (!term || term.length < 3) {
            this.suggestions = [];
            return;
        }

        this.searchTimeout = setTimeout(() => {
            this.companiaService.buscarSugerencias(term).subscribe({
                next: (data) => {
                    this.suggestions = data;
                },
                error: (err) => console.error('Error searching suggestions', err)
            });
        }, 300);
    }

    selectSuggestion(suggestion: any): void {
        this.formData.razSol = suggestion.razonSocial;
        this.formData.codigoIafa = suggestion.ruc;
        this.suggestions = []; // Clear suggestions
        this.searchRuc(); // Trigger auto-fill logic
    }

    close(): void {
        this.onClose.emit();
    }

    save(): void {
        // Run all validations before saving
        this.validateField('nombre');
        this.validateField('razSol');
        this.validateField('codigoIafa');
        this.validateField('codigo');
        this.validateField('representante');
        this.validateField('direcc');
        this.validateField('codPlan');
        this.validateField('diasPlazo');
        this.validateField('diasPlazo');
        // this.validateField('fechaCreacion'); // Internal field
        this.validateField('financieroNombre');
        this.validateField('financieroNombre');
        this.validateField('fonLocal');
        this.validateField('email');

        // Check if any errors exist
        if (Object.values(this.errors).some(x => x !== '')) {
            this.alertasService.error('Error', 'Por favor corrija los errores en el formulario');
            return;
        }

        if (!this.validarFormulario()) return;

        // Crear una copia del formData y asegurar tipos correctos
        const request = {
            ...this.formData,
            diasPlazo: this.formData.diasPlazo ? parseInt(this.formData.diasPlazo, 10) : 0,
            idTipoCia: parseInt(this.formData.idTipoCia, 10),
            facturar: this.formData.facturar || '1',
            estado: this.formData.estado || '1',
            ctacte: this.formData.ctacte || '0',
            // Ensure these are sent as expected by backend DTO
            direcc: this.formData.direcc,
            financieroNombre: this.formData.financieroNombre,
            fechaCreacion: this.formData.fechaCreacion,
            email: this.formData.email,
            fonLocal: this.formData.fonLocal,
            representante: this.formData.representante,
            mostrarDirec: this.formData.mostrarDirec,
            mostrarDiasPlazo: this.formData.mostrarDiasPlazo
        };

        if (this.isEditMode && this.compania?.id) {
            this.companiaService.updateCompania(this.compania.id, request).subscribe({
                next: () => {
                    this.handleSuccess('Actualizado', 'Compañía actualizada correctamente');
                },
                error: (err: any) => {
                    console.error('Error al actualizar:', err);
                    const errorMessage = err.error?.message || 'No se pudo actualizar la compañía';
                    this.handleError('Error', errorMessage);
                }
            });
        } else {
            this.companiaService.createCompania(request).subscribe({
                next: () => {
                    this.handleSuccess('Creado', 'Compañía creada correctamente');
                },
                error: (err: any) => {
                    console.error('Error al crear:', err);
                    const errorMessage = err.error?.message || 'No se pudo crear la compañía';
                    this.handleError('Error', errorMessage);
                }
            });
        }
    }

    validateField(field: string): void {
        const value = this.formData[field];
        this.errors[field] = ''; // Reset error

        switch (field) {
            case 'nombre':
                if (!value) {
                    this.errors.nombre = 'El nombre comercial es obligatorio';
                } else {
                    const duplicate = this.existingCompanias.find(c =>
                        c.nombre.toLowerCase() === value.toLowerCase() &&
                        (!this.isEditMode || c.id !== this.compania?.id)
                    );
                    if (duplicate) this.errors.nombre = 'El nombre comercial ya existe';
                }
                break;

            case 'razSol':
                if (!value) {
                    this.errors.razSol = 'La razón social es obligatoria';
                } else {
                    const duplicate = this.existingCompanias.find(c =>
                        c.razSol?.toLowerCase() === value.toLowerCase() &&
                        (!this.isEditMode || c.id !== this.compania?.id)
                    );
                    if (duplicate) this.errors.razSol = 'La razón social ya existe';
                }
                break;

            case 'codigoIafa':
                if (!value) {
                    this.errors.codigoIafa = 'El RUC es obligatorio';
                } else if (value.length !== 11) {
                    this.errors.codigoIafa = 'El RUC debe tener 11 dígitos';
                } else if (!value.startsWith('20')) {
                    this.errors.codigoIafa = 'El RUC debe comenzar con 20';
                } else {
                    const duplicate = this.existingCompanias.find(c =>
                        c.codigoIafa === value &&
                        (!this.isEditMode || c.id !== this.compania?.id)
                    );
                    if (duplicate) this.errors.codigoIafa = 'El RUC ya está registrado';
                }
                break;

            case 'email':
                if (!value) {
                    this.errors.email = 'El correo electrónico es obligatorio';
                } else {
                    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
                    if (!emailRegex.test(value)) {
                        this.errors.email = 'Formato de correo inválido';
                    }
                }
                break;

            case 'codigo':
                if (!value) {
                    this.errors.codigo = 'El código interno es obligatorio';
                } else if (!/^\d+$/.test(value)) {
                    this.errors.codigo = 'Solo se permiten números';
                } else if (value.length > 3) {
                    this.errors.codigo = 'Máximo 3 dígitos';
                } else {
                    const duplicate = this.existingCompanias.find(c =>
                        String(c.codigo) === String(value) &&
                        (!this.isEditMode || c.id !== this.compania?.id)
                    );
                    if (duplicate) this.errors.codigo = 'El código interno ya existe';
                }
                break;

            case 'representante':
                if (!value) this.errors.representante = 'El representante es obligatorio';
                break;
            case 'direcc':
                if (!value) this.errors.direcc = 'La dirección es obligatoria';
                break;
            case 'codPlan':
                if (!value) this.errors.codPlan = 'El código de plan es obligatorio';
                break;
            case 'diasPlazo':
                if (value === null || value === undefined || value === '') {
                    this.errors.diasPlazo = 'Los días de plazo son obligatorios';
                } else if (value < 0) {
                    this.errors.diasPlazo = 'No se permiten números negativos';
                } else if (String(value).length > 5) {
                    this.errors.diasPlazo = 'Máximo 5 dígitos';
                }
                break;
            // case 'fechaCreacion':
            //     if (!value) this.errors.fechaCreacion = 'La fecha de creación es obligatoria';
            //     break;
            case 'financieroNombre':
                if (!value) this.errors.financieroNombre = 'El responsable financiero es obligatorio';
                break;
            case 'fonLocal':
                if (!value) this.errors.fonLocal = 'El teléfono es obligatorio';
                break;
        }
    }

    validarFormulario(): boolean {
        if (!this.formData.nombre) {
            this.alertasService.warning('Advertencia', 'El nombre comercial es obligatorio');
            return false;
        }

        if (!this.formData.razSol) {
            this.alertasService.warning('Advertencia', 'La razón social es obligatoria');
            return false;
        }
        if (!this.formData.codigoIafa) {
            this.alertasService.warning('Advertencia', 'El RUC / Código IAFA es obligatorio');
            return false;
        }

        if (this.formData.codigoIafa.length !== 11) {
            this.alertasService.warning('Advertencia', 'El RUC debe tener 11 dígitos');
            return false;
        }

        if (!this.formData.codigoIafa.startsWith('20')) {
            this.alertasService.warning('Advertencia', 'El RUC debe comenzar con 20');
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
        this.errors = {
            nombre: '',
            razSol: '',
            codigoIafa: '',
            email: '',
            codigo: '',
            representante: '',
            direcc: '',
            codPlan: '',
            diasPlazo: '',
            fechaCreacion: '',
            financieroNombre: '',
            fonLocal: ''
        };
        this.formData = {
            codigo: '',
            nombre: '',
            razSol: '',
            codigoIafa: '',
            idTipoCia: 1,
            diasPlazo: null,
            facturar: '1',
            estado: '1',
            email: '',
            fonLocal: '',
            direcc: '',
            representante: '',
            financieroNombre: '',
            fechaCreacion: new Date().toISOString().split('T')[0],
            codEps: '',
            codPlan: '',
            ctacte: '0',
            mostrarDirec: '1',
            mostrarDiasPlazo: '1'
        };
    }
}
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Nivel } from '../../../models/nivel.model';
import { NivelService } from '../../../services/nivel.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-nivel',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-nivel.component.html',
})
export class ModalNivelComponent {
    @Input() tiposArea: any[] = [];
    @Output() onSaved = new EventEmitter<void>();

    showModal: boolean = false;
    isEditMode: boolean = false;
    selectedNivel: Nivel | null = null;

    formData: Nivel = {
        nombre: '',
        numNivel: '',
        tipo: '',
        estado: 1,
        idNivelPadre: undefined,
        nombrePadre: undefined,
        centroCosto: undefined,
        centCostLimite: undefined,
        idTipoAte: undefined,
    };

    constructor(
        private nivelService: NivelService,
        private alertasService: AlertasService
    ) { }

    openCreate(nivelPadre?: Nivel): void {
        this.isEditMode = false;
        this.selectedNivel = null;

        this.formData = {
            nombre: '',
            numNivel: '',
            tipo: '',
            estado: 1,
            idNivelPadre: nivelPadre?.idNivel,
            nombrePadre: nivelPadre?.nombre,
            centroCosto: undefined,
            centCostLimite: undefined,
            idTipoAte: undefined,
        };

        this.showModal = true;
    }

    openEdit(nivel: Nivel): void {
        this.isEditMode = true;
        this.selectedNivel = nivel;
        this.formData = { ...nivel };
        this.showModal = true;
    }

    close(): void {
        this.showModal = false;
        this.selectedNivel = null;
        this.formData = {
            nombre: '',
            numNivel: '',
            tipo: '',
            estado: 1,
            idNivelPadre: undefined,
            nombrePadre: undefined,
            centroCosto: undefined,
            centCostLimite: undefined,
            idTipoAte: undefined,
        };
    }

    save(): void {
        if (!this.formData.nombre || !this.formData.nombre.trim()) {
            this.mostrarError('El nombre es obligatorio');
            return;
        }

        if (!this.isEditMode && this.esNivelRaiz()) {
            if (!this.formData.numNivel || !this.formData.numNivel.trim()) {
                this.mostrarError('El código es obligatorio para niveles raíz');
                return;
            }

            if (!/^[0-9]+$/.test(this.formData.numNivel)) {
                this.mostrarError('El código debe contener solo números');
                return;
            }
        }

        if (this.esNivelHijo()) {
            this.formData.numNivel = '';
        }

        if (this.isEditMode && this.selectedNivel) {
            this.nivelService.actualizar(this.selectedNivel.idNivel!, this.formData).subscribe({
                next: (response) => {
                    if (response.success) {
                        this.mostrarExito('Nivel actualizado exitosamente');
                        this.close();
                        this.onSaved.emit();
                    }
                },
                error: (error) => {
                    console.error('Error al actualizar:', error);
                    this.mostrarError(
                        error.error?.message || 'Error al actualizar el nivel'
                    );
                },
            });
        } else {
            this.nivelService.crear(this.formData).subscribe({
                next: (response) => {
                    if (response.success) {
                        const codigoGenerado = response.data?.numNivel;
                        const mensaje = this.esNivelHijo()
                            ? `Nivel creado exitosamente con código: ${codigoGenerado}`
                            : 'Nivel raíz creado exitosamente';

                        this.mostrarExito(mensaje);
                        this.close();
                        this.onSaved.emit();
                    }
                },
                error: (error) => {
                    console.error('Error al crear:', error);
                    this.mostrarError(error.error?.message || 'Error al crear el nivel');
                },
            });
        }
    }

    esNivelRaiz(): boolean {
        return !this.isEditMode && !this.formData.idNivelPadre;
    }

    esNivelHijo(): boolean {
        return !this.isEditMode && !!this.formData.idNivelPadre;
    }

    getTituloModal(): string {
        if (this.isEditMode) {
            return 'Actualizar Nivel';
        }
        return this.esNivelHijo()
            ? `Nuevo Subnivel de "${this.formData.nombrePadre}"`
            : 'Nuevo Nivel';
    }

    getPlaceholderCodigo(): string {
        if (this.isEditMode) {
            return 'Código no editable';
        }
        if (this.esNivelRaiz()) {
            return 'Ejemplo: 100';
        }
        return 'Se generará automáticamente';
    }

    private mostrarExito(mensaje: string): void {
        setTimeout(() => {
            this.alertasService.success('¡Logrado!', mensaje);
        });
    }

    private mostrarError(mensaje: string): void {
        this.alertasService.error('Error', mensaje);
    }
}

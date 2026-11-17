import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Nivel } from '../../../models/nivel.model';
import { NivelService } from '../../../services/nivel.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-subnivel',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-subnivel.component.html',
})
export class ModalSubnivelComponent {
    @Input() tiposArea: any[] = [];
    @Output() onSaved = new EventEmitter<void>();

    showModal: boolean = false;

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

    open(nivelPadre: { idNivel: number; nombre: string }): void {
        this.formData = {
            nombre: '',
            numNivel: '',
            tipo: '',
            estado: 1,
            idNivelPadre: nivelPadre.idNivel,
            nombrePadre: nivelPadre.nombre,
            centroCosto: undefined,
            centCostLimite: undefined,
            idTipoAte: undefined,
        };
        this.showModal = true;
    }

    close(): void {
        this.showModal = false;
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

        // Para subniveles, el numNivel lo genera el backend
        this.formData.numNivel = '';

        this.nivelService.crear(this.formData).subscribe({
            next: (response) => {
                if (response.success) {
                    const codigoGenerado = response.data?.numNivel;
                    this.mostrarExito(`Subnivel creado exitosamente con código: ${codigoGenerado}`);
                    this.close();
                    this.onSaved.emit();
                }
            },
            error: (error) => {
                console.error('Error al crear subnivel:', error);
                this.mostrarError(error.error?.message || 'Error al crear el subnivel');
            },
        });
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

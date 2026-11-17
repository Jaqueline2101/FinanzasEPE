import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TipoAtencion } from '../../../models/tipo-atencion.model';
import { TipoAtencionService } from '../../../services/tipo-atencion.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-tipo-atencion',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './tipo-atencion.component.html',
    styleUrls: ['./tipo-atencion.component.css']
})
export class TipoAtencionComponent implements OnInit {
    tiposAtencion: TipoAtencion[] = [];
    showModal: boolean = false;
    isEditMode: boolean = false;

    formData: TipoAtencion = {
        codigo: '',
        nombre: '',
        orden: 1,
        estado: 1
    };

    constructor(
        private tipoAtencionService: TipoAtencionService,
        private cdr: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.cargarTipos();
    }

    cargarTipos(): void {
        this.tipoAtencionService.listar().subscribe({
            next: (data) => {
                this.tiposAtencion = data;
                this.cdr.detectChanges();
            },
            error: (err) => console.error('Error al cargar tipos de atención', err)
        });
    }

    openCreateModal(): void {
        this.isEditMode = false;
        this.formData = { codigo: '', nombre: '', orden: this.tiposAtencion.length + 1, estado: 1 };
        this.showModal = true;
    }

    openEditModal(tipo: TipoAtencion): void {
        this.isEditMode = true;
        this.formData = { ...tipo };
        this.showModal = true;
    }

    closeModal(): void {
        this.showModal = false;
    }

    save(): void {
        if (!this.formData.nombre || !this.formData.codigo) {
            this.alertasService.error('Error', 'Todos los campos son obligatorios');
            return;
        }

        if (this.isEditMode && this.formData.id) {
            this.tipoAtencionService.actualizar(this.formData.id, this.formData).subscribe({
                next: () => {
                    this.handleSuccess('Actualizado correctamente');
                },
                error: (err) => this.handleError(err)
            });
        } else {
            this.tipoAtencionService.guardar(this.formData).subscribe({
                next: () => {
                    this.handleSuccess('Creado correctamente');
                },
                error: (err) => this.handleError(err)
            });
        }
    }

    delete(tipo: TipoAtencion): void {
        this.alertasService.deleteConfirm(
            '¿Estás seguro?', 
            `Se inactivará el tipo "${tipo.nombre}"`
        ).then((confirmed) => {
            if (confirmed && tipo.id) {
                this.tipoAtencionService.eliminar(tipo.id).subscribe({
                    next: () => {
                        this.handleSuccess('Inactivado correctamente');
                    }
                });
            }
        });
    }

    private handleSuccess(msg: string): void {
        // 1. Cerramos el modal inmediatamente
        this.closeModal();
        
        // 2. Forzamos la detección de cambios para sincronizar 'showModal = false' con la vista
        this.cdr.detectChanges();

        // 3. Disparamos la alerta premium y refrescamos los datos
        this.alertasService.success('¡Éxito!', msg);
        this.cargarTipos();
    }

    private handleError(err: any): void {
        const msg = err.error?.message || 'Error al procesar la solicitud';
        this.alertasService.error('Error', msg);
    }
}

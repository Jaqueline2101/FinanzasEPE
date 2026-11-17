import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { Sucursal } from '../../../models/sucursal.model';
import { SucursalService } from '../../../core/services/sucursal.service';
import { AlertasService } from '../../../services/alertas.service';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-modal-sucursal',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-sucursal.component.html'
})
export class ModalSucursalComponent implements OnChanges {
    @Input() isVisible = false;
    @Input() sucursal: Sucursal | null = null;
    @Output() modalClose = new EventEmitter<boolean>();

    private sucursalService = inject(SucursalService);
    private alertasService = inject(AlertasService);
    private cd = inject(ChangeDetectorRef);

    formData: any = {
        nombreSucursal: '',
        direccion: '',
        telefono: '',
        celular: '',
        email: '',
        estado: 1, // 1: Activo, 0: Inactivo
        cantidadPersonal: 0
    };

    isEditMode = false;
    isLoading = false;

    get modalTitle(): string {
        return (this.isEditMode ? 'Editar' : 'Nueva') + ' Sucursal';
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isVisible']?.currentValue) {
            this.initModal();
        }
    }

    private initModal(): void {
        if (this.sucursal) {
            this.isEditMode = true;
            this.formData = { ...this.sucursal };
        } else {
            this.isEditMode = false;
            this.resetForm();
        }
        this.cd.detectChanges();
    }

    resetForm(): void {
        this.formData = {
            nombreSucursal: '',
            direccion: '',
            telefono: '',
            celular: '',
            email: '',
            estado: 1, // 1: Activo, 0: Inactivo
            cantidadPersonal: 0
        };
    }

    save(): void {
        if (!this.formData.nombreSucursal || !this.formData.direccion) {
            this.alertasService.error('Campos obligatorios', 'Nombre y dirección son obligatorios para continuar.');
            return;
        }

        this.isLoading = true;
        this.cd.detectChanges();

        const request = (this.isEditMode && this.sucursal?.idSucursal)
            ? this.sucursalService.update(this.sucursal.idSucursal, this.formData)
            : this.sucursalService.create(this.formData);

        request.pipe(
            finalize(() => {
                this.isLoading = false;
                this.cd.detectChanges();
            })
        ).subscribe({
            next: () => {
                const action = this.isEditMode ? 'Actualizada' : 'Creada';
                this.alertasService.success('Éxito', `Sucursal ${action} correctamente.`);
                this.modalClose.emit(true);
            },
            error: (err) => {
                console.error('Error al guardar sucursal', err);
                this.alertasService.error('Error', err.error?.message || 'No se pudo procesar la solicitud.');
            }
        });
    }

    close(): void {
        this.modalClose.emit(false);
    }
}

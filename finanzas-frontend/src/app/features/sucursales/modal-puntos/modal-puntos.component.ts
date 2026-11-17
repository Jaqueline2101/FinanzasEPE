import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { Sucursal } from '../../../models/sucursal.model';
import { Punto } from '../../../models/punto.model';
import { PuntoService } from '../../../core/services/punto.service';
import { AlertasService } from '../../../services/alertas.service';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-modal-punto',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-puntos.component.html'
})
export class ModalPuntoComponent implements OnChanges {
    @Input() isVisible = false;
    @Input() sucursal: Sucursal | null = null;
    @Input() punto: Punto | null = null;
    @Output() modalClose = new EventEmitter<boolean>();

    private puntoService = inject(PuntoService);
    private alertasService = inject(AlertasService);
    private cd = inject(ChangeDetectorRef);

    formData: Partial<Punto> = this.getEmptyForm();
    isEditMode = false;
    isLoading = false;

    get modalTitle(): string {
        return (this.isEditMode ? 'Editar' : 'Nuevo') + ' Punto de Atención';
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isVisible']?.currentValue) {
            this.initModal();
        }
    }

    private initModal(): void {
        if (this.punto) {
            this.isEditMode = true;
            this.formData = { ...this.punto };
        } else {
            this.isEditMode = false;
            this.formData = this.getEmptyForm();
        }
        this.cd.detectChanges();
    }

    getEmptyForm(): Partial<Punto> {
        return {
            punto: '',
            nombre: '',
            tipo: 'F',
            valido: '1',
            imprimeComp: '1',
            agrupaServ: '1',
            buscaXIniciales: '1',
            cargaMedico: '1',
            cargaCuenta: 1,
            cobra: '0',
            ipAccesoModulo: '0.0.0.0',
            modulo: 50,
            arqueo: '0',
            fe: '0'
        };
    }

    save(): void {
        if (!this.formData.nombre || !this.formData.punto) {
            this.alertasService.error('Campos obligatorios', 'El código y nombre son requeridos.');
            return;
        }

        if (!this.sucursal) {
            this.alertasService.error('Error', 'No hay una sucursal seleccionada.');
            return;
        }

        this.isLoading = true;
        this.cd.detectChanges();

        const puntoData = { 
            ...this.formData, 
            idSucursal: this.sucursal.idSucursal 
        };

        const request = (this.isEditMode && this.punto?.idPunto) 
            ? this.puntoService.actualizar(this.punto.idPunto, puntoData)
            : this.puntoService.crear(puntoData);

        request.pipe(
            finalize(() => {
                this.isLoading = false;
                this.cd.detectChanges();
            })
        ).subscribe({
            next: () => {
                const action = this.isEditMode ? 'actualizado' : 'registrado';
                this.alertasService.success('Completado', `Punto de atención ${action} con éxito.`);
                this.modalClose.emit(true);
            },
            error: (err) => {
                console.error('Error al guardar punto', err);
                this.alertasService.error('Error', err.error?.message || 'No se pudo procesar la solicitud.');
            }
        });
    }

    close(): void {
        this.modalClose.emit(false);
    }
}

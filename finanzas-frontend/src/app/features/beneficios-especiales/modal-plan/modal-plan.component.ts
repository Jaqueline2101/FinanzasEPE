import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BeneficiosService, BeneficioItem, BeneficioRequest } from '../../../services/beneficios.service';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-plan',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './modal-plan.component.html'
})
export class ModalPlanComponent {
    /** Tipo actualmente seleccionado en el padre — define el label del modal (solo modo crear) */
    @Input() tipoSeleccionado: 'GENERAL' | 'FAMILIAR' = 'GENERAL';

    /** Emite el item recién creado */
    @Output() planCreado = new EventEmitter<BeneficioItem>();
    /** Emite el item actualizado */
    @Output() planActualizado = new EventEmitter<BeneficioItem>();
    /** Emite cuando el usuario cierra el modal sin guardar */
    @Output() cerrado = new EventEmitter<void>();

    visible = false;
    guardando = false;
    modoEdicion = false;
    idEditando: number | null = null;

    form: {
        codigo: string;
        nombre: string;
        fechaInicio: string;
        fechaFin: string;
        estado: string;
    } = { codigo: '', nombre: '', fechaInicio: '', fechaFin: '', estado: '1' };

    constructor(
        private beneficiosService: BeneficiosService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) { }

    /** Abre en modo CREAR */
    abrir() {
        this.modoEdicion = false;
        this.idEditando = null;
        this.form = { codigo: '', nombre: '', fechaInicio: '', fechaFin: '', estado: '1' };
        this.guardando = false;
        this.visible = true;
        this.cd.detectChanges();
    }

    /** Abre en modo EDITAR pre-cargando los datos del item */
    abrirEditar(item: BeneficioItem) {
        this.modoEdicion = true;
        this.idEditando = item.idPlan;
        this.form = {
            codigo: item.codigo,
            nombre: item.nombre,
            fechaInicio: item.fechaInicio || '',
            fechaFin: item.fechaFin || '',
            estado: item.estado
        };
        this.guardando = false;
        this.visible = true;
        this.cd.detectChanges();
    }

    cerrar() {
        this.visible = false;
        this.guardando = false;
        this.cerrado.emit();
        this.cd.detectChanges();
    }

    formatearCodigo(event: Event) {
        const input = event.target as HTMLInputElement;
        const soloLetrasNumeros = input.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
        const letras = soloLetrasNumeros.slice(0, 3).replace(/[^A-Z]/g, '');
        const numeros = soloLetrasNumeros.replace(/[^0-9]/g, '').slice(0, 3);
        const valor = letras.length === 3 && numeros.length > 0
            ? `${letras}-${numeros}`
            : letras;
        this.form.codigo = valor;
        input.value = valor;
    }

    codigoValido(): boolean {
        return /^[A-Z]{3}-\d{3}$/.test(this.form.codigo);
    }

    formularioValido(): boolean {
        return this.codigoValido() && this.form.nombre.trim().length > 0;
    }

    guardar() {
        if (!this.formularioValido() || this.guardando) return;

        this.guardando = true;
        this.cd.detectChanges();

        const payload: BeneficioRequest = {
            codigo: this.form.codigo,
            nombre: this.form.nombre.trim(),
            tipoPlan: this.tipoSeleccionado,
            fechaInicio: this.form.fechaInicio || null,
            fechaFin: this.form.fechaFin || null,
            estado: this.form.estado
        };

        if (this.modoEdicion && this.idEditando !== null) {
            // ── ACTUALIZAR ──
            this.beneficiosService.actualizar(this.idEditando, payload).subscribe({
                next: (actualizado) => {
                    this.alertasService.success('Plan Actualizado', 'El plan se ha actualizado correctamente');
                    this.guardando = false;
                    this.visible = false;
                    this.planActualizado.emit(actualizado);
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error actualizando beneficio', err);
                    this.alertasService.error('Error', 'No se pudo actualizar el plan');
                    this.guardando = false;
                    this.cd.detectChanges();
                }
            });
        } else {
            // ── CREAR ──
            this.beneficiosService.crear(payload).subscribe({
                next: (nuevo) => {
                    this.alertasService.success('Plan Creado', 'El plan se ha creado correctamente');
                    this.guardando = false;
                    this.visible = false;
                    this.planCreado.emit(nuevo);
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error creando beneficio', err);
                    this.alertasService.error('Error', 'No se pudo crear el plan');
                    this.guardando = false;
                    this.cd.detectChanges();
                }
            });
        }
    }

    get titulo(): string {
        if (this.modoEdicion) return 'Actualizar Plan';
        return `Nuevo ${this.tipoSeleccionado === 'FAMILIAR' ? 'Descuento Familiar' : 'Descuento General'}`;
    }

    get labelBoton(): string {
        if (this.guardando) return this.modoEdicion ? 'Actualizando...' : 'Guardando...';
        return this.modoEdicion ? 'Actualizar' : 'Guardar';
    }
}

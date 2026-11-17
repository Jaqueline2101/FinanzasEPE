import { Component, Input, OnInit, inject, ChangeDetectorRef, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Sucursal } from '../../../models/sucursal.model';
import { Punto } from '../../../models/punto.model';
import { PuntoService } from '../../../core/services/punto.service';
import { AlertasService } from '../../../services/alertas.service';
import { ModalPuntoComponent } from '../modal-puntos/modal-puntos.component';
import { finalize } from 'rxjs';

@Component({
    selector: 'app-puntos-panel',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './puntos-panel.component.html'
})
export class PuntosPanelComponent implements OnInit, OnChanges {
    @Input() sucursal: Sucursal | null = null;
    @Output() onOpenModal = new EventEmitter<Punto | null>();
    @Output() onSaved = new EventEmitter<void>();

    private puntoService = inject(PuntoService);
    private alertasService = inject(AlertasService);
    private cd = inject(ChangeDetectorRef);

    puntos: Punto[] = [];
    isLoading = false;

    // Public method to be called from parent if needed, 
    // but mostly we use the Save event to refresh.
    refresh(): void {
        this.loadPuntos();
    }

    ngOnInit(): void {
        if (this.sucursal) {
            this.loadPuntos();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['sucursal'] && this.sucursal) {
            this.loadPuntos();
        }
    }

    loadPuntos(): void {
        if (!this.sucursal) return;
        this.isLoading = true;
        this.puntoService.listarPorSucursal(this.sucursal.idSucursal).subscribe({
            next: (res) => {
                this.puntos = res.data || [];
                this.isLoading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error al cargar puntos', err);
                this.isLoading = false;
                this.cd.detectChanges();
            }
        });
    }

    openCreate(): void {
        this.onOpenModal.emit(null);
    }

    openEdit(punto: Punto): void {
        this.onOpenModal.emit(punto);
    }

    toggleEstado(punto: Punto): void {
        const nuevoEstado = punto.valido === '1' ? false : true;
        this.puntoService.cambiarEstado(punto.idPunto, nuevoEstado).subscribe({
            next: () => {
                this.loadPuntos();
            },
            error: (err) => {
                this.alertasService.error('Error', 'No se pudo cambiar el estado del punto.');
            }
        });
    }

    deletePunto(punto: Punto): void {
        this.alertasService.deleteConfirm(
            '¿Eliminar punto?',
            `Se desactivará el punto "${punto.nombre}"`
        ).then((confirmed) => {
            if (confirmed) {
                this.puntoService.eliminar(punto.idPunto).subscribe({
                    next: () => {
                        this.alertasService.success('Éxito', 'Punto desactivado correctamente.');
                        this.loadPuntos();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', 'Error al desactivar el punto.');
                    }
                });
            }
        });
    }
}

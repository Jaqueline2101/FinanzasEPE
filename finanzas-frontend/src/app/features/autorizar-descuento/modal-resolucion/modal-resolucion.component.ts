import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AutorizacionResponse, AutorizacionResolucion } from '../../../models/autorizar-descuento.model';
import { AutorizarDescuentoService } from '../../../services/autorizar-descuento.service';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-resolucion',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './modal-resolucion.component.html',
    styleUrls: ['./modal-resolucion.component.css']
})
export class ModalResolucionComponent {
    @Input() autorizacion: AutorizacionResponse | null = null;
    @Input() accion: 'APROBAR' | 'RECHAZAR' | 'RESOLVER' = 'APROBAR';
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    motivoRechazo: string = '';
    loading = false;
    
    constructor(
        private service: AutorizarDescuentoService,
        private alertasService: AlertasService
    ) { }

    cambiarARechazar() {
        this.accion = 'RECHAZAR';
    }

    guardarAprobacion() {
        this.accion = 'APROBAR';
        this.guardar();
    }

    guardar() {
        if (!this.autorizacion) return;

        if (this.accion === 'RECHAZAR' && !this.motivoRechazo.trim()) {
            this.alertasService.warning('Atención', 'Debe ingresar un motivo para rechazar el descuento.');
            return;
        }

        const resolucion: AutorizacionResolucion = {
            idAutorizador: this.getAuthUserId(),
            motivoRechazo: this.accion === 'RECHAZAR' ? this.motivoRechazo : undefined
        };

        this.loading = true;

        if (this.accion === 'APROBAR') {
            this.service.aprobarDescuento(this.autorizacion.idAutorizacion, resolucion).subscribe({
                next: (res: any) => {
                    this.loading = false;
                    this.alertasService.success('Éxito', res.message || 'Descuento aprobado correctamente');
                    this.onSave.emit();
                },
                error: (err: any) => {
                    this.loading = false;
                    this.alertasService.error('Error', err.error?.message || 'Error al aprobar descuento');
                }
            });
        } else {
            this.service.rechazarDescuento(this.autorizacion.idAutorizacion, resolucion).subscribe({
                next: (res: any) => {
                    this.loading = false;
                    this.alertasService.success('Éxito', res.message || 'Descuento rechazado correctamente');
                    this.onSave.emit();
                },
                error: (err: any) => {
                    this.loading = false;
                    this.alertasService.error('Error', err.error?.message || 'Error al rechazar descuento');
                }
            });
        }
    }

    private getAuthUserId(): number {
        const token = localStorage.getItem('token');
        if (!token) return 0;
        try {
            const payloadBase64 = token.split('.')[1];
            const payloadDecoded = JSON.parse(atob(payloadBase64));
            // In finanzas-backend, the claim is 'userId'
            return Number(payloadDecoded.userId || payloadDecoded.idPersonalUser || 0);
        } catch {
            return 0;
        }
    }

    cerrar() {
        this.onClose.emit();
    }
}

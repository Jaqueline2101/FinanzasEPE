import { Component, EventEmitter, Input, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompraDeuda } from '../../../models/compra-deuda.model';
import { CronogramaPago } from '../../../models/cronograma-pago.model';
import { PagosService } from '../../../services/pagos.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-pagos-modal',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent, AlertasComponent],
    templateUrl: './pagos-modal.component.html'
})
export class PagosModalComponent {
    @Input() isOpen = false;
    @Input() compra: CompraDeuda | null = null;
    @Output() onClose = new EventEmitter<void>();
    @Output() onPagoRealizado = new EventEmitter<void>();

    cuotaSeleccionada: CronogramaPago | null = null;
    selectedCuotas: Set<number> = new Set(); // IDs de cuotas seleccionadas para pago masivo
    numeroOperacion = '';
    selectedFile: File | null = null;
    loadingPago = false;
    modoLote = false; // Indica si se está pagando un lote de cuotas seleccionadas

    constructor(
        private pagosService: PagosService, 
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) { }

    close() {
        this.isOpen = false;
        this.onClose.emit();
        this.selectedCuotas.clear();
        this.modoLote = false;
        this.cuotaSeleccionada = null;
        this.numeroOperacion = '';
    }

    // --- LÓGICA DE SELECCIÓN ---
    toggleSeleccion(cuota: CronogramaPago) {
        if (cuota.estado === 'PAGADO') return;

        if (this.selectedCuotas.has(cuota.idCronograma)) {
            this.selectedCuotas.delete(cuota.idCronograma);
        } else {
            this.selectedCuotas.add(cuota.idCronograma);
        }

        // Si deseleccionamos todo lo que estábamos pagando en lote, ocultar el formulario
        if (this.selectedCuotas.size === 0 && this.modoLote) {
            this.modoLote = false;
            this.numeroOperacion = '';
        }
    }

    seleccionarTodas() {
        if (!this.compra) return;

        const pendietes = this.compra.cuotas.filter(c => c.estado !== 'PAGADO');
        if (this.selectedCuotas.size === pendietes.length) {
            this.selectedCuotas.clear();
            if (this.modoLote) {
                this.modoLote = false;
                this.numeroOperacion = '';
            }
        } else {
            pendietes.forEach(c => this.selectedCuotas.add(c.idCronograma));
        }
    }

    isSeleccionada(id: number): boolean {
        return this.selectedCuotas.has(id);
    }

    get totalSeleccionado(): number {
        if (!this.compra) return 0;
        return this.compra.cuotas
            .filter(c => this.selectedCuotas.has(c.idCronograma))
            .reduce((acc, c) => acc + c.montoCuota, 0);
    }

    todasSeleccionadas(): boolean {
        if (!this.compra) return false;
        const pendietes = this.compra.cuotas.filter(c => c.estado !== 'PAGADO');
        return pendietes.length > 0 && this.selectedCuotas.size === pendietes.length;
    }

    // Helpers visuales
    getVencimientoClass(cuota: CronogramaPago): string {
        if (cuota.estado === 'PAGADO') return 'text-slate-400 font-medium line-through';

        // Si ya pasó fecha y no está pagado
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const venc = new Date(cuota.fechaVencimiento + 'T00:00:00');

        if (venc < today) return 'text-red-600 font-bold';
        if (venc.getTime() - today.getTime() < (3 * 24 * 60 * 60 * 1000)) return 'text-amber-600 font-bold';

        return 'text-slate-700 font-medium';
    }

    isVencido(cuota: CronogramaPago): boolean {
        if (cuota.estado === 'PAGADO') return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const venc = new Date(cuota.fechaVencimiento + 'T00:00:00');
        return venc < today;
    }

    isAlerta15Dias(cuota: CronogramaPago): boolean {
        if (cuota.estado === 'PAGADO') return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const venc = new Date(cuota.fechaVencimiento + 'T00:00:00');
        
        const diffTime = venc.getTime() - today.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        return diffDays >= 0 && diffDays <= 15;
    }

    get hayCuotasCriticas(): boolean {
        return this.compra?.cuotas.some(c => this.isAlerta15Dias(c)) || false;
    }

    // Acciones de Pago
    iniciarPago(cuota: CronogramaPago) {
        this.modoLote = false;
        this.selectedCuotas.clear();
        this.cuotaSeleccionada = cuota;
        this.numeroOperacion = '';
        this.selectedFile = null;
    }

    iniciarPagoMasivo() {
        if (this.selectedCuotas.size === 0) return;
        this.modoLote = true;
        this.cuotaSeleccionada = null;
        this.numeroOperacion = '';
    }

    cancelarPago() {
        this.cuotaSeleccionada = null;
        this.numeroOperacion = '';
        this.selectedFile = null;
    }

    onFileSelected(event: any) {
        this.selectedFile = event.target.files[0] ?? null;
    }

    confirmarPago() {
        if (this.modoLote) {
            this.confirmarPagoMasivo();
        } else {
            this.confirmarPagoIndividual();
        }
    }

    private confirmarPagoIndividual() {
        if (!this.cuotaSeleccionada || !this.numeroOperacion) return;

        this.loadingPago = true;
        this.pagosService.registrarPago(this.cuotaSeleccionada.idCronograma, this.numeroOperacion, this.selectedFile || undefined)
            .subscribe({
                next: (cuotaActualizada) => {
                    this.actualizarCuotaLocal(cuotaActualizada);
                    this.alertasService.success('Pago Exitoso', `La cuota #${this.cuotaSeleccionada?.numeroCuota} ha sido pagada.`);
                    this.terminarPago();
                },
                error: (err) => this.handleErrorPago(err)
            });
    }

    private confirmarPagoMasivo() {
        const ids = Array.from(this.selectedCuotas);
        if (ids.length === 0 || !this.numeroOperacion) return;

        this.loadingPago = true;
        this.pagosService.registrarPagosMasivos(ids, this.numeroOperacion, this.selectedFile || undefined)
            .subscribe({
                next: (resultados) => {
                    resultados.forEach(c => this.actualizarCuotaLocal(c));
                    this.alertasService.success('Pago Masivo Exitoso', `Se han pagado ${resultados.length} cuotas correctamente.`);
                    this.selectedCuotas.clear();
                    this.terminarPago();
                },
                error: (err) => this.handleErrorPago(err)
            });
    }

    private actualizarCuotaLocal(cuota: CronogramaPago) {
        if (this.compra) {
            const index = this.compra.cuotas.findIndex(c => c.idCronograma === cuota.idCronograma);
            if (index !== -1) {
                this.compra.cuotas[index] = cuota;
                this.actualizarTotalesLocales();
            }
        }
    }

    private terminarPago() {
        this.loadingPago = false;
        this.cuotaSeleccionada = null;
        this.modoLote = false;
        this.onPagoRealizado.emit();
        this.cd.detectChanges();
    }

    private handleErrorPago(err: any) {
        console.error(err);
        this.loadingPago = false;
        this.alertasService.error('Error', 'No se pudo registrar el pago.');
        this.cd.detectChanges();
    }

    private actualizarTotalesLocales() {
        if (!this.compra) return;

        let pagado = 0;
        this.compra.cuotas.forEach(c => {
            if (c.estado === 'PAGADO') {
                pagado += c.montoCuota;
            }
        });

        this.compra.montoPagado = pagado;
        this.compra.montoPendiente = Math.max(0, this.compra.montoTotal - pagado);
    }
}

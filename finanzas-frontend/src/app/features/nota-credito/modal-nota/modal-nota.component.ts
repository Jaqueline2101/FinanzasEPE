import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { VentaParaNC, MotivoSunat } from '../nota-credito.component';

@Component({
    selector: 'app-modal-nota',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-nota.component.html',
})
export class ModalNotaComponent implements OnChanges {

    /** Venta seleccionada en el padre para mostrar en el modal */
    @Input() venta: VentaParaNC | null = null;

    /** Si el padre ya está emitiendo (deshabilita el botón) */
    @Input() emitiendo = false;

    /** Emite { motivoCodigo, montoNc, detalleNc } cuando el usuario confirma */
    @Output() confirmar = new EventEmitter<{ motivoCodigo: string; montoNc: number; detalleNc: string }>();

    /** Emite cuando el usuario cancela o cierra */
    @Output() cancelar = new EventEmitter<void>();

    // ─── Estado interno del formulario ───────────────────────────────────────
    motivoCodigo = '';
    montoNc: number | null = null;
    detalleNc = '';

    motivosSunat: MotivoSunat[] = [
        { codigo: '01', descripcion: '01 — Anulación de la operación' },
        { codigo: '02', descripcion: '02 — Anulación por error en el RUC' },
        { codigo: '03', descripcion: '03 — Corrección por error en la descripción' },
        { codigo: '04', descripcion: '04 — Descuento global' },
        { codigo: '05', descripcion: '05 — Descuento por ítem' },
        { codigo: '06', descripcion: '06 — Devolución total' },
        { codigo: '07', descripcion: '07 — Devolución por ítem' },
        { codigo: '13', descripcion: '13 — Ajuste en el monto del comprobante' },
    ];

    /** Resetea el formulario cada vez que se abre con una nueva venta */
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['venta'] && this.venta) {
            this.motivoCodigo = '';
            this.montoNc = this.venta.total;
            this.detalleNc = '';
        }
    }

    get formularioValido(): boolean {
        return !!this.motivoCodigo &&
            !!this.montoNc &&
            this.montoNc > 0 &&
            !!this.venta &&
            this.montoNc <= this.venta.total;
    }

    get montoExcede(): boolean {
        return !!this.venta && !!this.montoNc && this.montoNc > this.venta.total;
    }

    onMontoChange(event?: any): void {
        if (!this.venta) return;
        
        let valor = event?.target?.value || '';
        
        // 1. Solo permitir números y un punto decimal
        valor = valor.replace(/[^0-9.]/g, '');
        
        // 2. No permitir más de un punto decimal
        const partes = valor.split('.');
        if (partes.length > 2) {
            valor = partes[0] + '.' + partes.slice(1).join('');
        }

        // 3. Convertir a número para validar el máximo
        let num = parseFloat(valor);
        
        if (!isNaN(num)) {
            if (num > this.venta.total) {
                num = this.venta.total;
                valor = this.venta.total.toString();
            }
            if (num < 0) {
                num = 0;
                valor = '0';
            }
            this.montoNc = num;
        } else {
            this.montoNc = null;
        }

        // 4. Sincronizar el DOM físicamente para que el usuario no vea "3000"
        if (event?.target) {
            event.target.value = valor;
        }
    }

    onBlurMonto(): void {
        if (!this.montoNc || !this.venta) return;
        
        // Asegurar 2 decimales y recortes finales
        if (this.montoNc > this.venta.total) {
            this.montoNc = this.venta.total;
        }
    }

    onConfirmar(): void {
        if (!this.formularioValido || this.emitiendo) return;
        this.confirmar.emit({
            motivoCodigo: this.motivoCodigo,
            montoNc: this.montoNc!,
            detalleNc: this.detalleNc,
        });
    }

    onCancelar(): void {
        if (this.emitiendo) return;
        this.cancelar.emit();
    }
}

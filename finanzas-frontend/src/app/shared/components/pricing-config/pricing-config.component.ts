import {
    Component,
    EventEmitter,
    Input,
    Output,
    OnInit,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface PricingData {
    tipo: string;
    tipoPrecio: string;
    precioCompra: number;
    margenGanancia: number;
    precioConIgv: number;
    precioSinIgv: number;
    moneda: string;
}

@Component({
    selector: 'app-pricing-config',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './pricing-config.component.html',
    styleUrls: ['./pricing-config.component.css']
})
export class PricingConfigComponent implements OnInit, OnChanges {
    @Input() tipo: string = 'SERVICIO';
    @Input() tipoPrecio: string = 'PRECIO_FIJO';
    @Input() precioCompra: number = 0;
    @Input() margenGanancia: number = 0;
    @Input() precioConIgv: number = 0;
    @Input() precioSinIgv: number = 0;
    @Input() moneda: string = 'PEN';

    @Output() onChange = new EventEmitter<Partial<PricingData>>();

    pricingMode: 'PERCENTAGE' | 'FIXED' = 'PERCENTAGE';
    metrics = {
        costoNeto: 0,
        valorVentaNeto: 0,
        igv: 0,
        gananciaReal: 0,
        margenReal: 0
    };

    ngOnInit(): void {
        this.updatePricingMode();
        this.recalculatePrices();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['tipoPrecio']) {
            this.updatePricingMode();
        }
        if (changes['precioCompra'] || changes['margenGanancia'] || changes['precioConIgv']) {
            this.recalculatePrices();
        }
    }

    updatePricingMode(): void {
        this.pricingMode = this.tipoPrecio === 'PRECIO_VARIABLE' ? 'PERCENTAGE' : 'FIXED';
    }

    togglePricingMode(mode: 'PERCENTAGE' | 'FIXED'): void {
        this.pricingMode = mode;
        const newTipoPrecio = mode === 'PERCENTAGE' ? 'PRECIO_VARIABLE' : 'PRECIO_FIJO';
        this.onChange.emit({ tipoPrecio: newTipoPrecio });
        this.recalculatePrices();
    }

    setTipo(tipo: string): void {
        this.tipo = tipo;
        this.onChange.emit({ tipo });
        this.recalculatePrices();
    }

    onInput(field: string, event: any): void {
        const val = event.target.value ? parseFloat(event.target.value) : 0;
        const update: any = {};
        update[field] = val;

        if (field === 'precioConIgv') {
            update.precioSinIgv = val > 0 ? Number((val / 1.18).toFixed(2)) : 0;
        }

        this.onChange.emit(update);
        
        // Actualizar localmente para el cálculo inmediato
        (this as any)[field] = val;
        if (field === 'precioConIgv') this.precioSinIgv = update.precioSinIgv;
        
        this.recalculatePrices();
    }

    recalculatePrices(): void {
        const costoIgv = Number(this.precioCompra) || 0;
        const costoNeto = costoIgv / 1.18;

        let valorVentaNeto = 0;
        let precioFinalIgv = 0;
        let margen = 0;

        if (this.pricingMode === 'PERCENTAGE') {
            margen = Number(this.margenGanancia) || 0;
            if (margen < 100) {
                valorVentaNeto = costoNeto / (1 - (margen / 100));
            } else {
                valorVentaNeto = 0;
            }
            precioFinalIgv = valorVentaNeto * 1.18;
            
            // Emitir cambios calculados
            this.onChange.emit({
                precioConIgv: Number(precioFinalIgv.toFixed(2)),
                precioSinIgv: Number(valorVentaNeto.toFixed(2))
            });
        } else {
            precioFinalIgv = Number(this.precioConIgv) || 0;
            valorVentaNeto = precioFinalIgv / 1.18;
            const ganancia = valorVentaNeto - costoNeto;
            margen = valorVentaNeto > 0 ? (ganancia / valorVentaNeto) * 100 : 0;
            
            // Emitir margen calculado
            this.onChange.emit({
                margenGanancia: Number(margen.toFixed(2))
            });
        }

        this.metrics = {
            costoNeto,
            valorVentaNeto,
            igv: precioFinalIgv - valorVentaNeto,
            gananciaReal: valorVentaNeto - costoNeto,
            margenReal: margen
        };
    }

    displayValue(val: number): string {
        return val > 0 ? val.toString() : '';
    }

    displayMetric(value: number): string {
        return value > 0 ? value.toFixed(2) : '-';
    }
}

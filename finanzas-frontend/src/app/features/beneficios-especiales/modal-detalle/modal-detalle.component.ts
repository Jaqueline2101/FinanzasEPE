import { Component, Input, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BeneficioItem } from '../../../services/beneficios.service';

@Component({
    selector: 'app-modal-detalle',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleComponent {
    item: BeneficioItem | null = null;
    visible = false;

    constructor(private cd: ChangeDetectorRef) { }

    abrir(item: BeneficioItem) {
        this.item = item;
        this.visible = true;
        this.cd.detectChanges();
    }

    cerrar() {
        this.visible = false;
        this.item = null;
        this.cd.detectChanges();
    }
}

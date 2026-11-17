import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CompraService } from '../../../services/compra.service';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
    selector: 'app-detalle-compra',
    standalone: true,
    imports: [
        CommonModule,
        ModalComponent
    ],
    templateUrl: './detalle-compra.component.html'
})
export class DetalleCompraComponent implements OnInit {
    compra: any = null;
    loading = true;
    error: string | null = null;
    idCompra: number | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private compraService: CompraService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            if (id) {
                this.idCompra = +id;
                this.loadCompra(this.idCompra);
            } else {
                this.error = 'No se especificó un ID de compra válido';
                this.loading = false;
            }
        });
    }

    loadCompra(id: number) {
        this.loading = true;
        this.compraService.getById(id).subscribe({
            next: (data) => {
                this.compra = data;
                this.loading = false;
                this.cd.detectChanges(); // Forzar actualización de vista
            },
            error: (err) => {
                console.error('Error al cargar compra:', err);
                this.error = 'No se pudo cargar la información de la compra';
                this.loading = false;
                this.cd.detectChanges(); // Forzar actualización de vista
            }
        });
    }

    goBack() {
        this.router.navigate(['/admin/procesos/compras']);
    }

    print() {
        window.print();
    }

    // Helpers visuales
    getEstadoClass(estado: string): string {
        switch (estado) {
            case 'REGISTRADO': return 'bg-blue-100 text-blue-800 border-blue-200';
            case 'ANULADO': return 'bg-red-100 text-red-800 border-red-200';
            default: return 'bg-gray-100 text-gray-800 border-gray-200';
        }
    }

    getTipoAfectacionLabel(tipo: string): string {
        if (!tipo) return '-';
        if (tipo.includes('GRAVADO')) return 'GRAVADO';
        if (tipo.includes('INAFECTO')) return 'INAFECTO';
        if (tipo.includes('EXONERADO')) return 'EXONERADO';
        return tipo;
    }

    getCondicionPagoLabel(): string {
        return this.compra?.condicionPago || '-';
    }

    getCuotasCount(): number {
        if (this.compra?.cronograma) {
            return this.compra.cronograma.length;
        }
        return this.compra?.condicionPago === 'CREDITO' ? 1 : 0;
    }

    getFrecuenciaLabel(): string {
        if (this.compra?.cronograma && this.compra.cronograma.length > 1) {
            // Simple logic: difference between first two dates
            const d1 = new Date(this.compra.cronograma[0].fechaVencimiento);
            const d2 = new Date(this.compra.cronograma[1].fechaVencimiento);
            const diffTime = Math.abs(d2.getTime() - d1.getTime());
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            return `Cada ${diffDays} días`;
        }
        return this.compra?.condicionPago === 'CREDITO' ? 'Mensual (Est.)' : '-';
    }

    getMonedaLabel(): string {
        if (!this.compra?.moneda) return 'PEN';
        return this.compra.moneda === 'USD' ? 'Dólares ($)' : 'Soles (S/)';
    }

    getMedioPagoLabel(): string {
        const map: any = {
            'EFECTIVO_DEMAS': 'EFECTIVO, EN LOS DEMÁS CASOS',
            'DEPOSITO': 'DEPÓSITO EN CUENTA',
            'TRANSFERENCIA': 'TRANSFERENCIA DE FONDOS'
        };
        return map[this.compra?.medioPago] || this.compra?.medioPago || '-';
    }
}

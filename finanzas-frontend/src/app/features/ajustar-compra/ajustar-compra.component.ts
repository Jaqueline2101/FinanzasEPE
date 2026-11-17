import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { CompraService } from '../../services/compra.service';
import { Router } from '@angular/router';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { ModalAjustarComponent } from './modal-ajustar/modal-ajustar.component';

@Component({
    selector: 'app-ajustar-compra',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BreadcrumbComponent,
        ModalAjustarComponent
    ],
    templateUrl: './ajustar-compra.component.html'
})
export class AjustarCompraComponent implements OnInit {
    compras: any[] = [];
    loading = false;
    showEditModal = false;
    selectedCompra: any = null;

    // Pagination
    currentPage = 0;
    pageSize = 10;
    totalElements = 0;
    totalPages = 0;

    columnas: Columna[] = [
        { field: 'index', header: '#', subField: [], tipo: 'index' },
        { field: 'fechaEmision', header: 'Fecha', subField: [], tipo: 'date' },
        { field: 'serie', header: 'Serie', subField: [] },
        { field: 'correlativo', header: 'Número', subField: [] },
        { field: 'proveedor', header: 'Proveedor', subField: ['razonSocial'], tipo: 'object' },
        { field: 'moneda', header: 'Mon', subField: [] },
        { field: 'total', header: 'Total', subField: [], tipo: 'currency' },
        {
            field: 'estado',
            header: 'Estado',
            subField: [],
            tipo: 'badge',
            badgeConfig: {
                container: 'rounded-full px-2 py-0.5 text-xs font-bold',
                from: 'REGISTRADO', to: 'ANULADO'
            }
        }
    ];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Procesos' },
        { label: 'Compras', route: '/admin/procesos/compras' },
        { label: 'Ajustar compra' }
    ];

    constructor(
        private compraService: CompraService,
        private router: Router,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading = true;
        this.compraService.listarPorSucursal(1, this.currentPage, this.pageSize).subscribe({
            next: (resp) => {
                this.compras = resp.content;
                this.totalElements = resp.totalElements;
                this.totalPages = resp.totalPages;
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.loadData();
    }

    onEdit(item: any) {
        this.selectedCompra = item;
        this.showEditModal = true;
    }

    onSaveAjuste(ajustes: any) {
        // Since we don't have a specific backend endpoint yet for partial update, 
        // we'll show a "Success" message for now as per "solo hacer que se liste" logic
        Swal.fire({
            title: 'Actualizando...',
            text: 'Guardando ajustes de serie y tipo de comprobante',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        setTimeout(() => {
            Swal.fire('¡Éxito!', 'Los ajustes han sido guardados (Simulación)', 'success');
            this.showEditModal = false;
            this.loadData();
        }, 1000);
    }
}

import { Component, OnInit, inject, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SucursalService } from '../../core/services/sucursal.service';
import { Sucursal } from '../../models/sucursal.model';
import { TablaGeneralComponent } from '../../shared/components/tabla-general/tabla-general.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { ModalSucursalComponent } from './modal-sucursal/modal-sucursal.component';
import { PuntosPanelComponent } from './puntos-panel/puntos-panel.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { AlertasService } from '../../services/alertas.service';
import { finalize } from 'rxjs';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { ModalPuntoComponent } from './modal-puntos/modal-puntos.component';
import { Punto } from '../../models/punto.model';

@Component({
    selector: 'app-sucursales',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TablaGeneralComponent,
        PrimaryButtonComponent,
        ModalSucursalComponent,
        PuntosPanelComponent,
        BreadcrumbComponent,
        PageHeaderComponent,
        PaginationComponent,
        BarraFiltrosComponent,
        ModalPuntoComponent
    ],
    templateUrl: './sucursales.component.html'
})
export class SucursalesComponent implements OnInit {
    @ViewChild(PuntosPanelComponent) puntosPanel!: PuntosPanelComponent;

    private sucursalService = inject(SucursalService);
    private cd = inject(ChangeDetectorRef);
    private alertasService = inject(AlertasService);

    sucursales: Sucursal[] = [];
    isLoading = false;

    // Pagination State
    currentPage = 0;
    pageSize = 10;
    totalElements = 0;
    totalPages = 0;

    // Filters unificados
    filterCriteria: FilterCriteria = {
        term: '',
        searchType: 'ALL',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    searchOptions = [
        { label: 'Todos', value: 'ALL' }
    ];

    allSucursales: Sucursal[] = []; // Full list for local filtering
    filteredSucursales: Sucursal[] = []; // List to display

    // Breadcrumb Config
    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuraciones', route: '/admin/configuraciones' },
        { label: 'Sucursales' }
    ];

    // Modal state
    isModalVisible = false;
    selectedSucursal: Sucursal | null = null;

    // Points Panel state
    selectedSucursalForPuntos: Sucursal | null = null;

    // Punto Modal State
    isPuntoModalVisible = false;
    selectedPuntoForEdit: Punto | null = null;

    // Table Config
    columns: any[] = [
        { field: '', header: '#', tipo: 'index', align: 'center' },
        { field: 'nombreSucursal', header: 'Nombre' },
        { field: 'direccion', header: 'Dirección' },
        { field: 'estado', header: 'Estado', tipo: 'status' }
    ];

    ngOnInit(): void {
        this.loadSucursales();
    }

    loadSucursales(): void {
        this.isLoading = true;
        this.sucursalService.getAll(0, 1000).subscribe({
            next: (res: any) => {
                this.allSucursales = res.data?.content || [];
                this.applyFilters();

                // SELECCIÓN AUTOMÁTICA DEL PRIMERO
                if (this.filteredSucursales.length > 0 && !this.selectedSucursalForPuntos) {
                    this.onRowClick(this.filteredSucursales[0]);
                }

                setTimeout(() => {
                    this.isLoading = false;
                    this.cd.detectChanges();
                });
            },
            error: (err) => {
                this.alertasService.error('Error', 'No se pudieron cargar las sucursales');
                setTimeout(() => {
                    this.isLoading = false;
                    this.cd.detectChanges();
                });
            }
        });
    }

    handleFilters(criteria: FilterCriteria): void {
        this.filterCriteria = criteria;
        this.currentPage = 0;
        this.applyFilters();

        // RE-SELECCIONAR PRIMERO TRAS FILTRAR
        if (this.filteredSucursales.length > 0) {
            this.onRowClick(this.filteredSucursales[0]);
        } else {
            this.selectedSucursalForPuntos = null;
        }

        this.cd.detectChanges();
    }

    applyFilters(): void {
        let temp = [...this.allSucursales];
        const term = (this.filterCriteria.term || '').toLowerCase();

        // 1. Filtro por Término
        if (term) {
            temp = temp.filter(s =>
                s.nombreSucursal?.toLowerCase().includes(term) ||
                s.direccion?.toLowerCase().includes(term)
            );
        }

        // 2. Filtro por Estado
        if (this.filterCriteria.estado !== 'TODOS') {
            temp = temp.filter(s => String(s.estado) === this.filterCriteria.estado);
        }

        this.totalElements = temp.length;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);

        const start = this.currentPage * this.pageSize;
        this.filteredSucursales = temp.slice(start, start + this.pageSize);
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.applyFilters();
        // Mantener o cambiar selección al cambiar de página
        if (this.filteredSucursales.length > 0) {
            this.onRowClick(this.filteredSucursales[0]);
        }
    }

    onPageSizeChange(size: number): void {
        this.pageSize = size;
        this.currentPage = 0;
        this.loadSucursales();
    }

    openCreateModal(): void {
        this.selectedSucursal = null;
        this.isModalVisible = true;
    }

    openEditModal(sucursal: Sucursal): void {
        this.selectedSucursal = sucursal;
        this.isModalVisible = true;
    }

    onModalClose(refresh: boolean): void {
        this.isModalVisible = false;
        if (refresh) {
            this.loadSucursales();
        }
    }

    onRowClick(sucursal: Sucursal): void {
        this.selectedSucursalForPuntos = sucursal;
    }

    deleteSucursal(sucursal: Sucursal): void {
        this.alertasService.deleteConfirm(
            `¿Desactivar sucursal?`,
            `Confirma si deseas desactivar ${sucursal.nombreSucursal}`
        ).then((confirmed) => {
            if (confirmed) {
                this.sucursalService.delete(sucursal.idSucursal).subscribe({
                    next: () => {
                        this.alertasService.success('Desactivada', 'Sucursal desactivada correctamente');
                        this.loadSucursales();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', err.error?.message || 'Error al desactivar');
                    }
                });
            }
        });
    }

    // handlers para Puntos
    onOpenPuntoModal(punto: Punto | null): void {
        this.selectedPuntoForEdit = punto;
        this.isPuntoModalVisible = true;
    }

    onPuntoModalClose(refresh: boolean): void {
        this.isPuntoModalVisible = false;
        if (refresh && this.puntosPanel) {
            this.puntosPanel.refresh();
        }
    }
}

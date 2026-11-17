import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Paquete } from '../../models/paquete.model';
import { PaqueteService } from '../../services/paquete.service';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../shared/components/pagination/pagination';

import { ModalPaqueteComponent } from './modal-paquete/modal-paquete.component';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-paquetes',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TablaGeneralComponent,
        PageHeaderComponent,
        PrimaryButtonComponent,
        BreadcrumbComponent,
        PaginationComponent,

        ModalPaqueteComponent,
        ModalDetalleComponent,
        BarraFiltrosComponent
    ],
    templateUrl: './paquetes.component.html',
    styleUrls: ['./paquetes.component.css']
})
export class PaquetesComponent implements OnInit {
    breadcrumbItems = [
        { label: 'Configuración', route: '/admin/configuraciones' },
        { label: 'Gestión de Paquetes' }
    ];

    paquetes: any[] = []; // transformed data
    paquetesFiltrados: any[] = [];

    loading = true;

    // Pagination
    currentPage: number = 0;
    pageSize: number = 10;
    totalElements: number = 0;
    totalPages: number = 0;

    // Filters State
    currentFilters: FilterCriteria = {
        term: '',
        searchType: 'ALL',
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Ambulatorio', value: 'AMBULATORIO' },
        { label: 'Emergencia', value: 'EMERGENCIA' },
        { label: 'Hospitalario', value: 'HOSPITALARIO' }
    ];

    showModal: boolean = false;
    showModalDetalle: boolean = false;
    isEditMode: boolean = false;
    selectedPaquete: Paquete | null = null;
    selectedPaqueteDetalle: Paquete | null = null;



    columnas: Columna[] = [
        { field: 'index', header: '#', tipo: 'index', subField: [] },
        { field: 'codigo', header: 'Código', tipo: 'text', subField: [] },
        { field: 'nombre', header: 'Nombre del paquete', tipo: 'text', subField: [] },
        { field: 'tipoAtencionNombre', header: 'Tipo Atención', tipo: 'text', subField: [] },
        { field: 'categoria', header: 'Categoría', tipo: 'text', subField: [] },
        { field: 'estadoTexto', header: 'Estado', tipo: 'status-badge', subField: ['estado'] },
        { field: 'precioOferta', header: 'Precio oferta', tipo: 'currency', subField: [] },
        { field: 'precioReal', header: 'Precio real', tipo: 'currency', subField: [] },
        { field: 'ahorroTexto', header: 'Descuento por paquete', tipo: 'text', subField: [] },
        { field: 'fechaFin', header: 'Fecha de vencimiento', tipo: 'date', subField: [] }
    ];

    constructor(
        private paqueteService: PaqueteService,
        private cd: ChangeDetectorRef,
        private router: Router,
        private alertasService: AlertasService
    ) { }

    // Category Tab State
    // Category Tab State
    currentCategory: string = ''; // Empty to show all (Paquetes & Campañas)

    ngOnInit(): void {
        this.cargarDatos();
    }

    handleFilters(filters: FilterCriteria): void {
        this.currentFilters = filters;
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
        this.cd.detectChanges();
    }

    cargarDatos(): void {
        this.loading = true;
        // Use -1 for page size to list all for client-side filtering (or implement server-side pagination properly)
        // For now, let's stick to client-side filtering if backend pagination is not fully wired up with UI
        // But PaqueteService supports categoria param now.
        this.paqueteService.listarPaquetes(0, 1000, this.currentCategory).subscribe({
            next: (data) => {
                // Adjust if backend returns { content: [...], ... } or just [...] or { data: [...] }
                const rawList = data.content || data || [];

                const processed = rawList.map((p: any) => {
                    const precioOferta = p.precioPaquete ? parseFloat(p.precioPaquete.toString()) : 0;
                    const descuento = p.descuento ? parseFloat(p.descuento.toString()) : 0;
                    const precioReal = precioOferta + descuento;

                    return {
                        ...p,
                        precioOferta,
                        precioReal,
                        estadoTexto: p.estado === '1' ? 'Activo' : 'Inactivo',
                        ahorroTexto: descuento > 0 ? `Descuento por paquete S/. ${descuento.toFixed(2)}` : 'S/. 0.00'
                    };
                });

                // Sort by idPaquete descending (newest first)
                processed.sort((a: any, b: any) => {
                    const idA = a.idPaquete || 0;
                    const idB = b.idPaquete || 0;
                    return idB - idA;
                });

                this.paquetes = processed;
                this.totalElements = processed.length;
                this.aplicarFiltrosLocal();

                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error loading packages', err);
                this.loading = false;
            }
        });
    }

    handleSearch(event: { q: string, type: string }): void {
        this.currentFilters.term = event.q;
        this.currentFilters.searchType = event.type;
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
        this.cd.detectChanges();
    }

    filtrar() {
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
    }

    restablecer() {
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
        this.cd.detectChanges();
    }

    aplicarFiltrosLocal() {
        let temp = [...this.paquetes];

        // 1. Filtro por término de búsqueda
        if (this.currentFilters.term) {
            const lower = this.currentFilters.term.toLowerCase();
            temp = temp.filter(p =>
                p.nombre?.toLowerCase().includes(lower) ||
                p.codigo?.toLowerCase().includes(lower)
            );
        }

        // 2. Filtro por tipo de atención
        if (this.currentFilters.searchType !== 'ALL') {
            temp = temp.filter(p => p.tipoAtencionNombre?.toUpperCase() === this.currentFilters.searchType);
        }

        // 3. Filtro por estado
        if (this.currentFilters.estado !== 'TODOS') {
            const estadoVal = this.currentFilters.estado === 'ACTIVO' ? '1' : '0';
            temp = temp.filter(p => p.estado == estadoVal);
        } else {
            // Por defecto ocultar los eliminados lógicamente si aplica, o mostrar todos los activos
            temp = temp.filter(p => p.estado !== '0');
        }

        // 4. Filtro por rango de fechas (Solo si se han proporcionado fechas)
        if (this.currentFilters.fechaDesde || this.currentFilters.fechaHasta) {
            const inicio = this.currentFilters.fechaDesde ? new Date(this.currentFilters.fechaDesde + 'T00:00:00') : null;
            const fin = this.currentFilters.fechaHasta ? new Date(this.currentFilters.fechaHasta + 'T23:59:59') : null;
            
            temp = temp.filter(p => {
                const fechaFin = p.fechaFin ? new Date(p.fechaFin) : null;
                if (!fechaFin) return true;
                
                if (inicio && fin) return fechaFin >= inicio && fechaFin <= fin;
                if (inicio) return fechaFin >= inicio;
                if (fin) return fechaFin <= fin;
                
                return true;
            });
        }

        this.totalElements = temp.length;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);

        const start = this.currentPage * this.pageSize;
        this.paquetesFiltrados = temp.slice(start, start + this.pageSize);
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.aplicarFiltrosLocal();
    }

    openCreateModal(): void {
        this.isEditMode = false;
        this.selectedPaquete = null;
        this.showModal = true;
    }

    openEditModal(paquete: any): void {
        this.isEditMode = true;
        this.selectedPaquete = paquete; // Original paquete object is part of the transformed object
        this.showModal = true;
    }

    deletePaquete(paquete: any): void {
        this.alertasService.deleteConfirm(
            '¿Estás seguro?', 
            `Vas a eliminar el paquete: ${paquete.nombre}`
        ).then((confirmed) => {
            if (confirmed && paquete.idPaquete) {
                this.paqueteService.eliminarPaquete(paquete.idPaquete).subscribe({
                    next: () => {
                        this.alertasService.success('Eliminado!', 'El paquete ha sido eliminado.');
                        this.cargarDatos();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', 'No se pudo eliminar el paquete');
                        console.error('Error eliminando paquete', err);
                    }
                });
            }
        });
    }

    closeModal(): void {
        this.showModal = false;
        this.selectedPaquete = null;
    }

    onSaveSuccess(): void {
        this.cargarDatos();
    }

    verDetalle(paquete: any): void {
        this.selectedPaqueteDetalle = paquete;
        this.showModalDetalle = true;
    }

    closeModalDetalle(): void {
        this.showModalDetalle = false;
        this.selectedPaqueteDetalle = null;
    }
}
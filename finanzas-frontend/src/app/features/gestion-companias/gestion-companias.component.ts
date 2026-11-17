import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// TablaGeneralComponent removed (unused)
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../shared/components/pagination/pagination';

import { ModalCompaniaComponent } from './modal-compania/modal-compania.component';
import { ModalDetalleCompaniaComponent } from './modal-detalle/modal-detalle.component';
import { SearchGenericComponent } from '../../shared/components/reusable-search-selector/reusable-search-selector';

import { CompaniaService } from '../../services/compania.service';
import { ConvenioService } from '../../services/convenio.service';
import { Compania } from '../../models/compania.model';
import { Convenio } from '../../models/convenio.model';
import { AlertasService } from '../../services/alertas.service';

import { EmpresasListComponent } from './planes-empresas/empresas-list.component';

import { BeneficiosListComponent } from '../beneficios-especiales/beneficios-list.component';

import { GestionDescuentoComponent } from './gestion-descuento/gestion-descuento.component';

@Component({
    selector: 'app-gestion-companias',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        // TablaGeneralComponent,
        PageHeaderComponent,
        // PrimaryButtonComponent,
        BreadcrumbComponent,
        // PaginationComponent,
        ModalCompaniaComponent,
        ModalDetalleCompaniaComponent,

        EmpresasListComponent,
        GestionDescuentoComponent,

        BeneficiosListComponent,
        SearchGenericComponent
    ],
    templateUrl: './gestion-companias.component.html',
    styleUrls: ['./gestion-companias.component.css']
})
export class GestionCompaniasComponent implements OnInit {
    breadcrumbItems = [
        { label: 'Configuración', route: '/admin/configuraciones' },
        { label: 'Gestión de Compañías' }
    ];



    // Data
    companias: any[] = [];
    companiasFiltradas: any[] = [];
    loading = true;

    // Pagination
    currentPage: number = 0;
    pageSize: number = 10;
    totalElements: number = 0;
    totalPages: number = 0;

    // Filters
    searchTerm = '';
    currentSearchType = 'ALL';
    filtroEstado = 'TODOS';
    fechaDesde: string = '';
    fechaHasta: string = '';

    // Modal State
    showModal: boolean = false;
    showModalDetalleCompania: boolean = false;
    selectedCompania: Compania | null = null;

    // *** NUEVAS PROPIEDADES PARA EL DISEÑO DE CONVENIOS/COMPAÑIAS ***
    selectedCompaniaView: any | null = null; // Para mostrar detalle en panel derecho (Companies)
    openMenuId: number | null = null;  // Para controlar menú contextual abierto

    // Configuración Administrativa (Tabs)
    activeInfoTab: string = 'empresas'; // 'empresas', 'planes', 'descuentos'

    searchOptions = [
        { label: 'Todo', value: 'ALL' },
        { label: 'RUC', value: 'RUC' },
        { label: 'Nombre', value: 'NOMBRE' }
    ];

    constructor(
        private companiaService: CompaniaService,
        private cd: ChangeDetectorRef,
        private router: Router,
        private alertasService: AlertasService
    ) { }

    ngOnInit() {
        // Initialize dates
        const today = new Date();
        const pastDate = new Date();
        pastDate.setDate(today.getDate() - 30);
        this.fechaDesde = this.formatDateInput(pastDate);
        this.fechaHasta = this.formatDateInput(today);

        this.cargarDatos();
    }



    cargarDatos() {
        this.cargarCompanias();
    }

    // Date Handling Methods
    onDateStartChange() {
        if (!this.fechaDesde) return;
        const start = this.parseDateString(this.fechaDesde);
        const end = new Date(start);
        end.setDate(start.getDate() + 30);
        this.fechaHasta = this.formatDateInput(end);
    }

    onDateEndChange() {
        if (!this.fechaHasta) return;
        const end = this.parseDateString(this.fechaHasta);
        const start = new Date(end);
        start.setDate(end.getDate() - 30);
        this.fechaDesde = this.formatDateInput(start);
    }

    private parseDateString(dateStr: string): Date {
        const [year, month, day] = dateStr.split('-').map(Number);
        return new Date(year, month - 1, day);
    }

    private formatDateInput(date: Date): string {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();
        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;
        return [year, month, day].join('-');
    }

    cargarCompanias() {
        this.loading = true;
        this.companiaService.getCompanias().subscribe({
            next: (data) => {
                const processed = data.map(c => ({
                    ...c,
                    tipoNombre: this.getTipoNombre(c.idTipoCia),
                    nombre: c.nombre,
                    facturarTexto: c.facturar === '1' ? 'Sí' : 'No',
                    diasPlazoTexto: c.diasPlazo ? c.diasPlazo + ' días' : '0 días',
                    diasPlazo: c.diasPlazo
                }));

                this.companias = processed;
                this.aplicarFiltrosLocal();

                // Auto-select removed as per user request


                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando compañías', err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }

    handleSearch(event: { q: string, type: string }) {
        this.searchTerm = event.q;
        this.currentSearchType = event.type;
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
        this.cd.detectChanges();
    }

    filtrar() {
        this.currentPage = 0;
        this.aplicarFiltrosLocal();
    }

    restablecer() {
        this.searchTerm = '';
        this.filtroEstado = 'TODOS';

        const today = new Date();
        const pastDate = new Date();
        pastDate.setDate(today.getDate() - 30);
        this.fechaDesde = this.formatDateInput(pastDate);
        this.fechaHasta = this.formatDateInput(today);

        this.currentPage = 0;
        this.aplicarFiltrosLocal();
    }

    aplicarFiltrosLocal() {
        let temp = [...this.companias];

        // Search Term Logic (respecting currentSearchType)
        if (this.searchTerm) {
            const lower = this.searchTerm.toLowerCase();
            temp = temp.filter(c => {
                const nameMatch = c.nombre?.toLowerCase().includes(lower) || c.razSol?.toLowerCase().includes(lower);
                const rucMatch = c.codigoIafa?.toLowerCase().includes(lower) || c.codigo?.toLowerCase().includes(lower);

                if (this.currentSearchType === 'NOMBRE') return nameMatch;
                if (this.currentSearchType === 'RUC') return rucMatch;
                
                return nameMatch || rucMatch; // Default to ALL
            });
        }

        // Status Filter
        if (this.filtroEstado === 'TODOS') {
            // Mostrar todos los registros sin filtrar por estado
        } else if (this.filtroEstado === 'ACTIVO') {
            temp = temp.filter(c => c.estado == '1');
        } else if (this.filtroEstado === 'INACTIVO') {
            // Aquí mostramos las eliminadas/inactivas
            temp = temp.filter(c => c.estado == '0');
        }

        this.totalElements = temp.length;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);

        // For split view design, we want the full scrollable list in the sidebar, not paged
        this.companiasFiltradas = temp;
    }

    getTipoNombre(idTipo: number): string {
        switch (idTipo) {
            case 1: return 'EPS';
            case 2: return 'SEGUROS';
            case 3: return 'CONVENIO';
            default: return 'OTRO';
        }
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.aplicarFiltrosLocal();
    }

    // --- Actions ---

    nuevaCompania() {
        this.selectedCompania = null;
        this.showModal = true;
    }

    editarCompania(item: any) {
        this.closeMenu(); // Close menu
        this.selectedCompania = item;
        this.showModal = true;
    }

    closeModal() {
        this.showModal = false;
        this.selectedCompania = null;
    }

    onSaveSuccess() {
        this.cargarDatos();
    }

    // --- Convenio Actions ---
    // Legacy modal methods removed. 
    // Management should be done via Compania -> Planes.

    verDetalle(item: any) {
        this.closeMenu();
        // Fetch full details to ensure we have all fields (codPlan, contacts, etc.)
        this.companiaService.getCompania(item.id).subscribe({
            next: (fullCompania) => {
                // Map calculated fields that the modal expects
                this.selectedCompania = {
                    ...fullCompania,
                    tipoNombre: this.getTipoNombre(fullCompania.idTipoCia),
                    facturarTexto: fullCompania.facturar === '1' ? 'Sí' : 'No',
                    diasPlazo: fullCompania.diasPlazo // Keep as number or string? List maps it to string 'X días'. Modal 'X días'.
                    // Modal uses {{ compania.diasPlazo || 0 }} días. If I pass number it's fine.
                };
                this.showModalDetalleCompania = true;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error fetching company details:', err);
                this.alertasService.error('Error', 'No se pudo cargar los detalles de la compañía');
                this.cd.detectChanges();
            }
        });
    }

    closeModalDetalleCompania() {
        this.showModalDetalleCompania = false;
        this.selectedCompania = null;
    }

    eliminarCompania(item: any) {
        this.closeMenu(); // Close menu
        this.confirmarEliminarCompania(item);
    }

    confirmarEliminarCompania(compania: Compania) {
        this.alertasService.deleteConfirm(
            '¿Estás seguro?', 
            'No podrás revertir esto'
        ).then((confirmed) => {
            if (confirmed) {
                this.companiaService.deleteCompania(compania.id).subscribe({
                    next: () => {
                        this.alertasService.success('Eliminado!', 'La compañía ha sido eliminada.');
                        this.cargarDatos();
                    },
                    error: (err) => {
                        this.alertasService.error('Error', 'No se pudo eliminar la compañía');
                        console.error('Error eliminando compañía', err);
                    }
                });
            }
        });
    }

    // *** NUEVOS MÉTODOS PARA EL DISEÑO ***

    /**
     * Selecciona una compañía para mostrar en el panel derecho
     */
    selectCompania(compania: any): void {
        this.selectedCompaniaView = compania;
        // Si estábamos en beneficios, mantenemos beneficios, si no, vamos a empresas
        if (this.activeInfoTab !== 'beneficios') {
            this.activeInfoTab = 'empresas';
        }
    }

    selectedPlanForDiscounts: any | null = null;

    setActiveInfoTab(tab: string): void {
        this.activeInfoTab = tab;
        // Si cambiamos a beneficios, no necesitamos limpiar la compañía, 
        // pero la vista HTML se encargará de ocultar la info.
        // Si cambiamos a otra tab (empresas/planes/descuentos) y no hay compañía,
        // la vista mostrará el placeholder "Selecciona una compañía".

        // Reset discount view if switching tabs manually
        if (tab !== 'descuentos') {
            this.selectedPlanForDiscounts = null;
        }
    }

    onShowDiscounts(plan: any): void {
        this.selectedPlanForDiscounts = plan;
        this.activeInfoTab = 'descuentos';
    }

    onHideDiscounts(): void {
        this.selectedPlanForDiscounts = null;
        this.activeInfoTab = 'empresas'; // Regresar a la lista de empresas/planes
    }

    /**
     * Alterna el menú contextual de tres puntos
     */
    toggleMenu(event: Event, id: number): void {
        event.stopPropagation();
        if (id === undefined || id === null) return;
        this.openMenuId = this.openMenuId === id ? null : id;
    }

    /**
     * Cierra el menú contextual
     */
    closeMenu(): void {
        this.openMenuId = null;
    }
}
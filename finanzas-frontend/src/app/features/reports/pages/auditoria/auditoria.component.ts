import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { AuditService } from '../../services/audit.service';
import { SessionService } from '../../services/session.service';
import { LabAuditoriaDto, AuditFilterDto, SessionDto, SessionFilterDto } from '../../models/audit.model';
import { SidebarService } from '../../../../shared/sidebar/sidebar.service';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import * as XLSX from 'xlsx';
import Swal from 'sweetalert2';

import { AuditDetailModalComponent } from './audits-modals/audit-detail-modal.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../../../shared/components/barra-filtros/barra-filtros.component';
import { TablaGeneralComponent, Columna } from '../../../../shared/components/tabla-general/tabla-general.component';

@Component({
    selector: 'app-auditoria',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, PageHeaderComponent, BreadcrumbComponent, PaginationComponent, AuditDetailModalComponent, BarraFiltrosComponent, TablaGeneralComponent],
    providers: [DatePipe],
    templateUrl: './auditoria.component.html'
})
export class AuditoriaComponent implements OnInit {

    // --- UI STATE ---
    breadcrumbItems = [
        { label: 'Reportes', route: '/admin/reportes/auditoria' },
        { label: 'Bitácora y Auditoría', route: '/admin/reportes/auditoria' }
    ];

    // Modal State
    isDetailModalOpen = false;

    openDetailModal(audit: LabAuditoriaDto) {
        this.selectedAudit = audit;
        this.isDetailModalOpen = true;
    }

    closeDetailModal() {
        this.isDetailModalOpen = false;
        this.selectedAudit = null;
    }

    // Data
    audits: LabAuditoriaDto[] = [];
    private searchSubject = new Subject<string>();

    // Filtros Auditoría
    auditFilters: AuditFilterDto = {
        page: 0,
        size: 10,
        sortBy: 'fecha',
        sortDirection: 'DESC'
    };

    // Paginación Auditoría
    auditPage = 1;
    auditTotalPages = 1;
    auditTotalElements = 0;

    // UI
    showFilters = false;
    selectedAudit: LabAuditoriaDto | null = null;
    itemsPerPage = 10;

    // Buscador General
    searchQuery = '';
    searchDateStart = '';
    searchDateEnd = '';
    selectedAction = ''; // Nuevo filtro de acción

    actionOptions = [
        { label: 'Todas', value: '' },
        { label: 'INSERT', value: 'INSERT' },
        { label: 'UPDATE', value: 'UPDATE' },
        { label: 'DELETE', value: 'DELETE' },
        { label: 'LOGIN_SUCCESS', value: 'LOGIN_SUCCESS' },
        { label: 'LOGOUT', value: 'LOGOUT' }
    ];

    auditoriaColumnas: Columna[] = [
        { field: 'nombreUsuario', header: 'Usuario', tipo: 'title-subtitle', subField: ['emailUsuario'] },
        { field: 'tabla', header: 'Módulo', tipo: 'title-subtitle', subField: ['idRegistro'] },
        { field: 'accion', header: 'Acción', tipo: 'badge' },
        { field: 'descripcionCambio', header: 'Descripción', tipo: 'text' },
        { field: 'fecha', header: 'Fecha / Hora', tipo: 'date' },
        { field: 'ip', header: 'IP', tipo: 'text' },
        { field: 'estado', header: 'Estado', tipo: 'text' }
    ];

    constructor(
        private auditService: AuditService,
        private datePipe: DatePipe,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit() {
        // Inicializar fechas (Desde: Hoy - 30 días, Hasta: Hoy)
        const today = new Date();
        const pastDate = new Date();
        pastDate.setDate(today.getDate() - 30);

        this.searchDateStart = this.formatDateInput(pastDate);
        this.searchDateEnd = this.formatDateInput(today);

        // Configurar debounce para el buscador
        this.searchSubject.pipe(
            debounceTime(500),
            distinctUntilChanged()
        ).subscribe(() => {
            this.search();
        });

        // Cargar datos iniciales
        this.loadAudits();
    }

    // ==========================================
    // LOGICA FILTROS FECHA
    // ==========================================

    handleFilter(filters: FilterCriteria) {
        this.searchQuery = filters.term;
        this.selectedAction = filters.estado;
        this.searchDateStart = filters.fechaDesde;
        this.searchDateEnd = filters.fechaHasta;
        this.search();
    }

    private parseDateString(dateStr: string): Date {
        // Parsear 'yyyy-mm-dd' explícitamente como fecha local para evitar problemas de UTC/Timezone
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

    // ==========================================
    // LOGICA AUDITORIA DE DATOS
    // ==========================================

    loadAudits() {
        const filters: AuditFilterDto = {
            ...this.auditFilters,
            page: this.auditPage - 1,
            size: this.itemsPerPage
        };

        if (this.searchQuery) {
            filters.tabla = this.searchQuery; // Búsqueda simple por tabla o usuario
        }

        // Filtro de acción
        if (this.selectedAction) {
            filters.accion = this.selectedAction;
        }

        // Usar las fechas del filtro si existen
        if (this.searchDateStart) filters.fechaInicio = this.searchDateStart + 'T00:00:00';
        if (this.searchDateEnd) filters.fechaFin = this.searchDateEnd + 'T23:59:59';

        this.auditService.searchAudits(filters).subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.audits = response.data.content;
                    this.auditTotalPages = response.data.totalPages;
                    this.auditTotalElements = response.data.totalElements;
                    this.cd.detectChanges(); // Forzar detección de cambios
                }
            },
            error: (e) => console.error(e)
        });
    }

    // ==========================================
    // UTILIDADES UI
    // ==========================================

    viewDetails(audit: LabAuditoriaDto) {
        this.selectedAudit = audit;
    }

    closeDetails() {
        this.selectedAudit = null;
    }

    formatDate(dateString: string): string {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('es-PE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    search() {
        this.auditPage = 1;
        this.loadAudits();
    }

    clean() {
        this.searchQuery = '';
        this.selectedAction = ''; // Limpiar filtro de acción

        // Reset a rango por defecto
        const today = new Date();
        const pastDate = new Date();
        pastDate.setDate(today.getDate() - 30);

        this.searchDateStart = this.formatDateInput(pastDate);
        this.searchDateEnd = this.formatDateInput(today);

        this.search();
        this.cd.detectChanges();
    }

    // Paginación Genérica
    onPageChange(page: number) {
        this.auditPage = page + 1; // Convertir 0-based a 1-based
        this.loadAudits();
    }

    onPageSizeChange(size: number) {
        this.itemsPerPage = size;
        this.auditPage = 1;
        this.loadAudits();
    }

    // Helpers de Formato
    getActionClass(action: string): string {
        const classes: { [key: string]: string } = {
            'INSERT': 'bg-green-100 text-green-800 border-green-200',
            'UPDATE': 'bg-yellow-100 text-yellow-800 border-yellow-200',
            'DELETE': 'bg-red-100 text-red-800 border-red-200',
            'LOGIN': 'bg-purple-100 text-purple-800 border-purple-200',
            'LOGOUT': 'bg-gray-100 text-gray-800 border-gray-200'
        };
        return classes[action] || 'bg-gray-100 text-gray-800 border-gray-200';
    }

    getStatusClass(estado: string): string {
        const classes: { [key: string]: string } = {
            'ACTIVA': 'bg-green-100 text-green-800',
            'CERRADA': 'bg-blue-100 text-blue-800',
            'EXPIRADA': 'bg-red-100 text-red-800'
        };
        return classes[estado] || 'bg-gray-100 text-gray-800';
    }

    formatJson(jsonString: string | null): string {
        if (!jsonString) return 'N/A';
        try {
            return JSON.stringify(JSON.parse(jsonString), null, 2);
        } catch { return jsonString; }
    }

    formatDuration(minutes: number | null): string {
        if (!minutes) return '-';
        const h = Math.floor(minutes / 60);
        const m = minutes % 60;
        return h > 0 ? `${h}h ${m}m` : `${m}m`;
    }

    exportarExcel() {
        if (!this.audits || this.audits.length === 0) {
            Swal.fire('Atención', 'No hay datos para exportar', 'warning');
            return;
        }

        const dataToExport = this.audits.map((item, index) => ({
            '#': index + 1,
            'Usuario': item.nombreUsuario || '---',
            'Módulo': item.tabla || '---',
            'Acción': item.accion || '---',
            'Descripción': item.descripcionCambio || '---',
            'Fecha / Hora': item.fecha ? this.datePipe.transform(item.fecha, 'dd/MM/yyyy HH:mm:ss') : '---',
            'IP': item.ip || '---',
            'Estado': item.estado || '---'
        }));

        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Bitácora de Auditoría');

        // Ajustar anchos de columna
        ws['!cols'] = [
            { wch: 5 },  // #
            { wch: 20 }, // Usuario
            { wch: 15 }, // Módulo
            { wch: 20 }, // Acción
            { wch: 50 }, // Descripción
            { wch: 25 }, // Fecha / Hora
            { wch: 15 }, // IP
            { wch: 15 }  // Estado
        ];

        const fileName = `Auditoria_${this.searchDateStart}_al_${this.searchDateEnd}.xlsx`;
        XLSX.writeFile(wb, fileName);
    }
}
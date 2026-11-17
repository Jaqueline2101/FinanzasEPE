import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { RepHonorariosService, RepHonorariosResumenDTO } from '../../services/rep-honorarios.service';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import Swal from 'sweetalert2';
import { ViewChild } from '@angular/core';
import * as XLSX from 'xlsx';

@Component({
    selector: 'app-rep-honorarios',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent
    ],
    providers: [DatePipe],
    templateUrl: './rep-honorarios.component.html',
    styleUrls: ['./rep-honorarios.component.css']
})
export class RepHonorariosComponent implements OnInit {

    loading = false;
    resumen: RepHonorariosResumenDTO | null = null;
    
    // Filtros
    fechaInicio: string = this.getFirstDayOfMonth();
    fechaFin: string = this.getToday();
    query: string = '';
    searchType: string = 'ALL';

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Recibo', value: 'RECIBO' },
        { label: 'Nombre de Beneficiario', value: 'BENEFICIARIO' }
    ];

    tipoPagoOptions = [
        { label: 'TODOS', value: 'TODOS' },
        { label: 'HONORARIO', value: 'HONORARIO' },
        { label: 'LIQUIDACIÓN', value: 'LIQUIDACIÓN' },
        { label: 'ADELANTO', value: 'ADELANTO' }
    ];

    // Paginación
    currentPage = 0;
    pageSize = 10;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Reportes', route: '/admin/reportes/ventas' },
        { label: 'Rep. Rec. Honorarios' }
    ];

    columnas: Columna[] = [
        { field: 'idPago', header: '#', tipo: 'index', subField: [], align: 'center' },
        { field: 'fechaEmision', header: 'FECHA', tipo: 'date', subField: [], align: 'center' },
        { field: 'comprobante', header: 'RECIBO', tipo: 'text', subField: [] },
        { field: 'beneficiario', header: 'BENEFICIARIO', tipo: 'title-subtitle', subField: ['documento'] },
        { field: 'concepto', header: 'CONCEPTO', tipo: 'text', subField: [] },
        { field: 'montoBruto', header: 'BRUTO', tipo: 'currency', subField: ['moneda'] },
        { field: 'retencion', header: 'RET.', tipo: 'currency', subField: ['moneda'] },
        { field: 'montoNeto', header: 'NETO', tipo: 'currency', subField: ['moneda'] },
        { field: 'medioPago', header: 'MEDIO', tipo: 'text', subField: [] },
        { field: 'estado', header: 'ESTADO', tipo: 'badge', subField: [], badgeConfig: { container: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200' } }
    ];

    constructor(
        private repService: RepHonorariosService,
        private cdr: ChangeDetectorRef,
        private datePipe: DatePipe
    ) { }

    ngOnInit(): void {
        this.consultar();
    }

    exportarExcel() {
        if (!this.resumen?.detallePagos?.content || this.resumen.detallePagos.content.length === 0) {
            Swal.fire('Atención', 'No hay datos para exportar', 'warning');
            return;
        }

        const dataToExport = this.resumen.detallePagos.content.map((item, index) => ({
            '#': index + 1,
            'FECHA': item.fechaEmision ? this.datePipe.transform(item.fechaEmision, 'dd/MM/yyyy') : '---',
            'RECIBO': item.comprobante || '---',
            'BENEFICIARIO': item.beneficiario || '---',
            'DOCUMENTO': item.documento || '---',
            'CONCEPTO': item.concepto || '---',
            'MONEDA': item.moneda || '---',
            'BRUTO': item.montoBruto,
            'DETRAC.': item.retencion,
            'NETO': item.montoNeto,
            'ESTADO': item.estado || '---'
        }));

        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Reporte Honorarios');

        const fileName = `Reporte_Honorarios_${this.fechaInicio}_al_${this.fechaFin}.xlsx`;
        XLSX.writeFile(wb, fileName);
    }

    consultar() {
        this.loading = true;
        const request = {
            fechaInicio: this.fechaInicio,
            fechaFin: this.fechaFin,
            query: this.query || undefined
        };

        this.repService.generarReporte(request, this.currentPage, this.pageSize).subscribe({
            next: (resp) => {
                if (resp.success && resp.data) {
                    this.resumen = {
                        ...resp.data,
                        detallePagos: {
                            ...resp.data.detallePagos,
                            content: (resp.data.detallePagos.content || []).map(item => ({
                                ...item,
                                fechaEmision: item.fechaEmision ? new Date(item.fechaEmision) : null
                            }))
                        }
                    };
                }
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error al generar reporte:', err);
                this.loading = false;
                this.cdr.detectChanges();
                Swal.fire('Error', 'No se pudo cargar el reporte', 'error');
            }
        });
    }

    onFilter(criteria: FilterCriteria) {
        this.query = criteria.term;
        this.searchType = criteria.searchType;
        this.fechaInicio = criteria.fechaDesde;
        this.fechaFin = criteria.fechaHasta;
        this.currentPage = 0;
        this.consultar();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.consultar();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.consultar();
    }


    limpiar() {
        this.fechaInicio = this.getFirstDayOfMonth();
        this.fechaFin = this.getToday();
        this.query = '';
        this.currentPage = 0;
        this.consultar();
    }

    private getToday(): string {
        return new Date().toISOString().split('T')[0];
    }

    private getFirstDayOfMonth(): string {
        const d = new Date();
        return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0];
    }
}

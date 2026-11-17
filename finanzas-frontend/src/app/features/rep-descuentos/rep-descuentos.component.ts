import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as XLSX from 'xlsx';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { RepDescuentoService } from '../../services/rep-descuento.service';
import { RepDescuentoResponse, RepDescuentoParams } from '../../models/rep-descuento.model';
import { SearchGenericComponent } from '../../shared/components/reusable-search-selector/reusable-search-selector';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-rep-descuentos',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        BreadcrumbComponent,
        TablaGeneralComponent,
        SearchGenericComponent,
        PaginationComponent
    ],
    providers: [DatePipe],
    templateUrl: './rep-descuentos.component.html'
})
export class RepDescuentosComponent implements OnInit {

    @ViewChild(SearchGenericComponent) searchSelector!: SearchGenericComponent;


    // --- UI STATE ---
    breadcrumbItems = [
        { label: 'Reportes', route: '/admin/reportes/descuentos' },
        { label: 'Reporte de Descuentos', route: '/admin/reportes/descuentos' }
    ];
    isLoading: boolean = false;

    // --- FILTERS ---
    filtros: RepDescuentoParams = {
        fechaInicio: this.getTodayLocal(),
        fechaFin: this.getTodayLocal(),
        estado: '',
        query: ''
    };
    
    searchOptions = [
        { label: 'Todos los Estados', value: '' },
        { label: 'Aceptado', value: 'APROBADO' },
        { label: 'Rechazado', value: 'RECHAZADO' }
    ];

    onSearch(event: { q: string, type: string }) {
        this.filtros.query = event.q;
        this.filtros.estado = event.type;
        this.generarReporte();
    }

    // --- DATA ---
    reporteData: RepDescuentoResponse[] = [];
    
    // --- PAGINATION ---
    currentPage: number = 0;
    pageSize: number = 10;
    totalElements: number = 0;
    totalPages: number = 0;

    onPageChange(page: number) {
        this.currentPage = page;
        this.generarReporte();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0; // Reset to first page
        this.generarReporte();
    }

    // --- TABLE COLUMNS ---
    columnasTabla: Columna[] = [
        { header: '#', field: 'idAutorizacion', tipo: 'index', align: 'center' },
        { header: 'Fecha Sol.', field: 'fechaSolicitud', tipo: 'date', align: 'center' },
        { header: 'Fecha Res.', field: 'fechaResolucion', tipo: 'date', align: 'center' },
        { header: 'Paciente', field: 'pacienteNombre', align: 'left' },
        { header: 'DNI', field: 'pacienteDni', align: 'center' },
        { header: 'Médico', field: 'doctorNombre', align: 'left' },
        { header: 'Cajero', field: 'cajeroNombre', align: 'left' },
        { header: 'Autorizador', field: 'autorizadorNombre', align: 'left' },
        { header: 'Estado', field: 'estado', tipo: 'order-status', align: 'center' },
        { header: 'Monto Orig.', field: 'montoOriginal', tipo: 'currency', align: 'right' },
        { header: 'Descuento', field: 'montoDescuento', tipo: 'currency', align: 'right' },
        { header: 'Total Final', field: 'montoFinal', tipo: 'currency', align: 'right' }
    ];

    constructor(
        private repService: RepDescuentoService,
        private datePipe: DatePipe,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.generarReporte();
    }

    private getTodayLocal(): string {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    generarReporte() {
        if (!this.filtros.fechaInicio || !this.filtros.fechaFin) {
            Swal.fire('Atención', 'Seleccione un rango de fechas válido', 'warning');
            return;
        }

        this.isLoading = true;
        this.cd.detectChanges(); // Mostrar overlay de carga inmediatamente
        this.repService.obtenerReporte(this.filtros, this.currentPage, this.pageSize).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.reporteData = res.data.content;
                    this.totalElements = res.data.totalElements;
                    this.totalPages = res.data.totalPages;
                } else {
                    Swal.fire('Error', res.message || 'Error al obtener reporte', 'error');
                }
                this.isLoading = false;
                this.cd.detectChanges(); // Actualizar tabla
            },
            error: (err) => {
                this.isLoading = false;
                this.cd.detectChanges();
                console.error('Error fetching discount report', err);
                Swal.fire('Error', 'Problema del servidor al generar el reporte', 'error');
            }
        });
    }

    limpiarFiltros() {
        this.filtros = {
            fechaInicio: this.getTodayLocal(),
            fechaFin: this.getTodayLocal(),
            estado: '',
            query: ''
        };
        if (this.searchSelector) {
            this.searchSelector.reset();
        }
        this.currentPage = 0;
        this.generarReporte();
    }


    exportarExcel() {
        if (!this.reporteData || this.reporteData.length === 0) {
            Swal.fire('Atención', 'No hay datos para exportar', 'warning');
            return;
        }

        // Mapear los datos para el formato de Excel con nombres de columnas amigables
        const dataToExport = this.reporteData.map((item, index) => ({
            '#': index + 1,
            'Fecha Solicitud': item.fechaSolicitud ? this.datePipe.transform(item.fechaSolicitud, 'dd/MM/yyyy') : '---',
            'Fecha Resolución': item.fechaResolucion ? this.datePipe.transform(item.fechaResolucion, 'dd/MM/yyyy') : '---',
            'Paciente': item.pacienteNombre || '---',
            'DNI': item.pacienteDni || '---',
            'Doctor/Médico': item.doctorNombre || '---',
            'Cajero': item.cajeroNombre || '---',
            'Autorizador': item.autorizadorNombre || '---',
            'Estado': item.estado || '---',
            'Monto Original': item.montoOriginal,
            'Descuento': item.montoDescuento,
            'Monto Final': item.montoFinal
        }));

        // Crear hoja de trabajo y libro
        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Reporte de Descuentos');

        // Definir anchos de columna básicos
        ws['!cols'] = [
            { wch: 5 },  // #
            { wch: 15 }, // Fecha Sol.
            { wch: 15 }, // Fecha Res.
            { wch: 30 }, // Paciente
            { wch: 12 }, // DNI
            { wch: 30 }, // Doctor
            { wch: 25 }, // Cajero
            { wch: 25 }, // Autorizador
            { wch: 12 }, // Estado
            { wch: 15 }, // Monto O.
            { wch: 15 }, // Descuento
            { wch: 15 }  // Monto F.
        ];

        // Descargar el archivo
        const fileName = `Reporte_Descuentos_${this.filtros.fechaInicio}_al_${this.filtros.fechaFin}.xlsx`;
        XLSX.writeFile(wb, fileName);
    }

    onVerDetalle(item: any) {
        console.log('Ver detalle de descuento:', item);
        Swal.fire({
            title: 'Detalle de la Autorización',
            html: `
                <div class="text-left text-sm space-y-3 p-4 bg-slate-50 rounded-xl border border-slate-100">
                    <div class="flex justify-between border-b border-slate-200 pb-2">
                        <span class="text-slate-500 font-medium">Paciente:</span>
                        <span class="font-bold text-slate-800">${item.pacienteNombre}</span>
                    </div>
                    <div class="flex justify-between border-b border-slate-200 pb-2">
                        <span class="text-slate-500 font-medium">Estado:</span>
                        <span class="px-2 py-0.5 rounded text-[10px] font-bold ${item.estado === 'APROBADO' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'}">${item.estado}</span>
                    </div>
                    <div class="flex justify-between border-b border-slate-200 pb-2">
                        <span class="text-slate-500 font-medium">Monto Original:</span>
                        <span class="font-bold text-slate-800">S/ ${item.montoOriginal?.toFixed(2) || '0.00'}</span>
                    </div>
                    <div class="flex justify-between border-b border-slate-200 pb-2">
                        <span class="text-slate-500 font-medium">Descuento:</span>
                        <span class="font-bold text-blue-600">S/ ${item.montoDescuento?.toFixed(2) || '0.00'}</span>
                    </div>
                    <div class="flex justify-between border-b border-slate-200 pb-2">
                        <span class="text-slate-500 font-medium">Total Final:</span>
                        <span class="font-black text-slate-900 text-lg">S/ ${item.montoFinal?.toFixed(2) || '0.00'}</span>
                    </div>
                    <div class="flex justify-between pt-1">
                        <span class="text-slate-500 font-medium">Autorizador:</span>
                        <span class="font-medium text-slate-600">${item.autorizadorNombre || '---'}</span>
                    </div>
                </div>
            `,
            icon: 'info',
            confirmButtonText: 'Entendido',
            confirmButtonColor: '#00AEEF',
            customClass: {
                container: 'rounded-3xl',
                popup: 'rounded-3xl'
            }
        });
    }
}

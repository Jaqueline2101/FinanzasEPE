import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';

import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { RepVentasService, RepVentasRequestDTO, RepVentasResumenDTO, RepVentasResponseDTO } from '../../services/rep-ventas.service';
import { VentaService } from '../../services/venta-servicios.service';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';
import * as XLSX from 'xlsx';
import Swal from 'sweetalert2';
import { ViewChild } from '@angular/core';

@Component({
    selector: 'app-rep-ventas',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        BreadcrumbComponent,

        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent,
        ModalDetalleComponent
    ],
    providers: [DatePipe],
    templateUrl: './rep-ventas.component.html'
})
export class RepVentasComponent implements OnInit {
    // Eliminamos ViewChild de SearchGenericComponent ya que ahora se maneja dentro de barra-filtros


    // --- UI STATE ---
    breadcrumbItems = [
        { label: 'Admin', route: '/admin' },
        { label: 'Reportes', route: '/admin/reportes/ventas' },
        { label: 'Ventas', route: '/admin/reportes/ventas' }
    ];
    isLoading: boolean = false;
    selectedVenta: any | null = null;

    // --- FILTERS ---
    filtros: RepVentasRequestDTO = {
        fechaInicio: this.getTodayLocal(),
        fechaFin: this.getTodayLocal(),
        estado: '',
        tipoAtencion: '',
        query: ''
    };

    searchOptions = [
        { label: 'Todos', value: '' },
        { label: 'Paquete', value: 'PAQUETE' },
        { label: 'Convenio', value: 'CONVENIO' },
        { label: 'Particular', value: 'PARTICULAR' }
    ];

    ventasEstadoOptions = [
        { label: 'Todos', value: 'TODOS' },
        { label: 'Pagado', value: '16' },
        { label: 'Anulado', value: '10' }
    ];

    handleFilter(criteria: FilterCriteria) {
        this.filtros.query = criteria.term;
        this.filtros.tipoAtencion = criteria.searchType === 'ALL' ? '' : criteria.searchType;
        this.filtros.estado = criteria.estado === 'TODOS' ? '' : criteria.estado;
        this.filtros.fechaInicio = criteria.fechaDesde || this.getTodayLocal();
        this.filtros.fechaFin = criteria.fechaHasta || this.getTodayLocal();
        this.generarReporte(true);
    }

    // --- DATA ---
    resumen: RepVentasResumenDTO = {
        totalGeneral: 0,
        totalEfectivo: 0,
        totalTarjeta: 0,
        totalAnulado: 0,
        detalleVentas: {
            content: [],
            number: 0,
            pageSize: 10,
            totalElements: 0,
            totalPages: 0,
            last: true
        }
    };

    // --- PAGINATION / SORTING ---
    paginaActual: number = 0;
    tamanoPagina: number = 10;
    sortConfig: string = 'fechaDesc';

    // --- TABLE COLUMNS ---
    columnasTabla: Columna[] = [
        { header: 'N° Comprobante', field: 'comprobante', align: 'center' },
        { header: 'Fecha', field: 'fecha', tipo: 'date', align: 'center' },
        { header: 'Paciente', field: 'paciente', align: 'left' },
        { header: 'Tipo Atención', field: 'tipoAtencion', align: 'center' },
        { header: 'Método Pago', field: 'metodoPago', align: 'center' },
        { header: 'Estado', field: 'estado', tipo: 'order-status', align: 'center' },
        { header: 'Total', field: 'total', tipo: 'currency', align: 'right' }
    ];

    constructor(
        private repVentasService: RepVentasService,
        private ventaService: VentaService,
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

    generarReporte(resetPage: boolean = true) {
        if (resetPage) {
            this.paginaActual = 0;
        }

        const payload: RepVentasRequestDTO = {
            fechaInicio: this.filtros.fechaInicio,
            fechaFin: this.filtros.fechaFin,
            estado: this.filtros.estado === '' ? undefined : this.filtros.estado,
            tipoAtencion: this.filtros.tipoAtencion === '' ? undefined : this.filtros.tipoAtencion,
            query: this.filtros.query === '' ? undefined : this.filtros.query
        };

        if (!payload.fechaInicio || !payload.fechaFin) {
            Swal.fire('Atención', 'Seleccione un rango de fechas válido', 'warning');
            return;
        }

        this.isLoading = true;
        this.repVentasService.generarReporte(payload, this.paginaActual, this.tamanoPagina, this.sortConfig).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.resumen = res.data;
                } else {
                    Swal.fire('Error', res.message || 'Error al obtener reporte', 'error');
                }
                this.isLoading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                this.isLoading = false;
                this.cd.detectChanges();
                console.error('Error fetching report', err);
                Swal.fire('Error', 'Problema del servidor al generar el reporte', 'error');
            }
        });
    }

    limpiarFiltros() {
        this.filtros = {
            fechaInicio: this.getTodayLocal(),
            fechaFin: this.getTodayLocal(),
            estado: '',
            tipoAtencion: '',
            query: ''
        };
        this.generarReporte(true);
    }

    onPageChange(page: number) {
        this.paginaActual = page;
        this.generarReporte(false);
    }

    onPageSizeChange(size: number) {
        this.tamanoPagina = size;
        this.generarReporte(true);
    }

    onSortChange(event: Event) {
        const target = event.target as HTMLSelectElement;
        this.sortConfig = target.value;
        this.generarReporte(true);
    }

    exportarExcel() {
        if (!this.resumen.detalleVentas.content || this.resumen.detalleVentas.content.length === 0) {
            Swal.fire('Atención', 'No hay datos para exportar', 'warning');
            return;
        }

        const dataToExport = this.resumen.detalleVentas.content.map((item, index) => ({
            '#': index + 1,
            'Comprobante': item.comprobante,
            'Fecha': item.fecha ? this.datePipe.transform(item.fecha, 'dd/MM/yyyy') : '---',
            'Paciente': item.paciente || '---',
            'Tipo Atención': item.tipoAtencion || '---',
            'Método Pago': item.metodoPago || '---',
            'Estado': item.estado || '---',
            'Total': item.total
        }));

        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Reporte de Ventas');

        // Ajustar anchos de columna
        ws['!cols'] = [
            { wch: 5 },  // #
            { wch: 20 }, // Comprobante
            { wch: 15 }, // Fecha
            { wch: 35 }, // Paciente
            { wch: 20 }, // Tipo Atención
            { wch: 15 }, // Método Pago
            { wch: 15 }, // Estado
            { wch: 15 }  // Total
        ];

        const fileName = `Reporte_Ventas_${this.filtros.fechaInicio}_al_${this.filtros.fechaFin}.xlsx`;
        XLSX.writeFile(wb, fileName);
    }

    onVerDetalle(item: any) {
        console.log('Ver detalle de venta:', item);
        this.isLoading = true;
        this.cd.detectChanges();

        this.ventaService.obtenerVenta(item.idVenta).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.selectedVenta = res.data;
                } else {
                    Swal.fire('Error', 'No se pudo obtener el detalle de la venta', 'error');
                }
                this.isLoading = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                this.isLoading = false;
                this.cd.detectChanges();
                console.error('Error fetching sale detail', err);
                Swal.fire('Error', 'Problema al conectar con el servidor', 'error');
            }
        });
    }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { RepComprasService, RepComprasRequestDTO, RepComprasResumenDTO } from '../../services/rep-compras.service';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';

@Component({
    selector: 'app-rep-compras',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        BreadcrumbComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent
    ],
    providers: [DatePipe],
    templateUrl: './rep-compras.component.html'
})
export class RepComprasComponent implements OnInit {

    // --- UI STATE ---
    breadcrumbItems = [
        { label: 'Reportes', route: '/admin/reportes/ventas' },
        { label: 'Reporte de Compras', route: '/admin/reportes/compras' }
    ];
    isLoading: boolean = false;

    // --- FILTERS ---
    filtros: RepComprasRequestDTO = {
        fechaInicio: this.getTodayLocal(),
        fechaFin: this.getTodayLocal(),
        estado: ''
    };

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Proveedor', value: 'PROVEEDOR' },
        { label: 'RUC', value: 'RUC' }
    ];

    estadoOptions = [
        { label: 'TODOS', value: 'TODOS' },
        { label: 'REGISTRADO', value: 'REGISTRADO' },
        { label: 'PAGADO', value: 'PAGADO' },
        { label: 'ANULADO', value: 'ANULADO' }
    ];

    // --- DATA ---
    resumen: RepComprasResumenDTO = {
        totalGravado: 0,
        totalInafecto: 0,
        totalExonerado: 0,
        totalIGV: 0,
        totalGeneral: 0,
        detalleCompras: {
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
    sortConfig: string = 'fechaEmision';
    sortDir: string = 'desc';

    // --- TABLE COLUMNS ---
    columnasTabla: Columna[] = [
        { header: 'Comprobante', field: 'comprobanteCompleto', subField: [], align: 'center' },
        { header: 'F. Emisión', field: 'fechaEmisionFormat', subField: [], align: 'center' },
        { header: 'F. Registro', field: 'fechaRegistroFormat', subField: [], align: 'center' },
        { header: 'Proveedor', field: 'proveedor', subField: [], align: 'left' },
        { header: 'RUC', field: 'rucProveedor', subField: [], align: 'center' },
        { header: 'Estado', field: 'estado', tipo: 'order-status', subField: [], align: 'center' },
        { header: 'Gravado', field: 'valorVentaGravado', tipo: 'currency', align: 'right', subField: [] },
        { header: 'IGV', field: 'igv', tipo: 'currency', align: 'right', subField: [] },
        { header: 'Total', field: 'total', tipo: 'currency', align: 'right', subField: [] }
    ];

    constructor(
        private repComprasService: RepComprasService,
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

        const currentSucursal = JSON.parse(localStorage.getItem('currentSucursal') || '{}');
        
        const payload: RepComprasRequestDTO = {
            fechaInicio: this.filtros.fechaInicio,
            fechaFin: this.filtros.fechaFin,
            estado: this.filtros.estado === '' ? undefined : this.filtros.estado,
            idProveedor: this.filtros.idProveedor,
            idSucursal: currentSucursal.id
        };

        if (!payload.fechaInicio || !payload.fechaFin) {
            Swal.fire('Atención', 'Seleccione un rango de fechas válido', 'warning');
            return;
        }

        this.isLoading = true;
        this.repComprasService.generarReporte(payload, this.paginaActual, this.tamanoPagina, this.sortConfig, this.sortDir).subscribe({
            next: (res) => {
                this.resumen = res;
                // Formatear datos para la tabla
                this.resumen.detalleCompras.content = this.resumen.detalleCompras.content.map(c => ({
                    ...c,
                    comprobanteCompleto: `${c.tipoComprobante} ${c.serie}-${c.correlativo}`,
                    fechaEmisionFormat: this.datePipe.transform(c.fechaEmision, 'dd/MM/yyyy'),
                    fechaRegistroFormat: this.datePipe.transform(c.fechaRegistro, 'dd/MM/yyyy HH:mm:ss')
                })) as any;
                
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

    onFilter(criteria: FilterCriteria) {
        this.filtros.fechaInicio = criteria.fechaDesde || this.getTodayLocal();
        this.filtros.fechaFin = criteria.fechaHasta || this.getTodayLocal();
        this.filtros.estado = criteria.estado === 'TODOS' ? '' : criteria.estado;
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

    exportarExcel() {
        const data = this.resumen.detalleCompras.content;
        if (!data || data.length === 0) {
            Swal.fire('Atención', 'No hay datos para exportar', 'warning');
            return;
        }
        const rows = data.map((c: any, i: number) => ({
            '#': i + 1,
            'COMPROBANTE': c.comprobanteCompleto || '',
            'F. EMISIÓN': c.fechaEmisionFormat || '',
            'F. REGISTRO': c.fechaRegistroFormat || '',
            'PROVEEDOR': c.proveedor || '',
            'RUC': c.rucProveedor || '',
            'ESTADO': c.estado || '',
            'GRAVADO': c.valorVentaGravado,
            'IGV': c.igv,
            'TOTAL': c.total
        }));
        const ws = XLSX.utils.json_to_sheet(rows);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Compras');
        XLSX.writeFile(wb, `Reporte_Compras_${this.filtros.fechaInicio}_al_${this.filtros.fechaFin}.xlsx`);
    }
}

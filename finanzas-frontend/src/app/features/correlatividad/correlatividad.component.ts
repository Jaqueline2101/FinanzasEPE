import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { CorrelatividadService, CorrelatividadRequestDTO, CorrelatividadResponseDTO } from '../../services/correlatividad.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { Sucursal } from '../../models/sucursal.model';
import Swal from 'sweetalert2';

import { ModalCorrelatividadDetalleComponent } from './modal-detalle/modal-detalle.component';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';

@Component({
    selector: 'app-correlatividad',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        BreadcrumbComponent,
        ModalCorrelatividadDetalleComponent,
        BarraFiltrosComponent
    ],
    providers: [DecimalPipe],
    templateUrl: './correlatividad.component.html'
})
export class CorrelatividadComponent implements OnInit {

    // --- UI STATE ---
    breadcrumbItems = [
        { label: 'Reportes', route: '/admin/reportes/ventas' },
        { label: 'Correlatividad', route: '/admin/reportes/correlatividad' }
    ];
    isLoading: boolean = false;
    tabActiva: 'MES' | 'TIPO_DOC' = 'MES';

    // --- MODAL DETALLE ---
    showModalDetalle = false;
    selectedItem: any = null;
    selectedDocs: any[] = [];

    // --- FILTERS ---
    filtros: CorrelatividadRequestDTO = {
        idSucursal: null,
        anio: new Date().getFullYear(),
        mes: new Date().getMonth() + 1
    };

    searchCriteria: any = { term: '', searchType: 'ALL' };
    searchOptions = [
        { label: 'Todos los Docs.', value: 'ALL' },
        { label: 'Boleta', value: 'B' },
        { label: 'Factura', value: 'F' }
    ];

    sucursales: Sucursal[] = [];
    anios: number[] = [];
    meses = [
        { value: null, label: 'TODOS LOS MESES' },
        { value: 1, label: 'ENERO' },
        { value: 2, label: 'FEBRERO' },
        { value: 3, label: 'MARZO' },
        { value: 4, label: 'ABRIL' },
        { value: 5, label: 'MAYO' },
        { value: 6, label: 'JUNIO' },
        { value: 7, label: 'JULIO' },
        { value: 8, label: 'AGOSTO' },
        { value: 9, label: 'SEPTIEMBRE' },
        { value: 10, label: 'OCTUBRE' },
        { value: 11, label: 'NOVIEMBRE' },
        { value: 12, label: 'DICIEMBRE' }
    ];

    get sucursalOptions() {
        return [
            { label: 'TODOS LOS ESTABLECIMIENTOS', value: 'null' },
            ...this.sucursales.map(s => ({ label: s.nombreSucursal, value: s.idSucursal.toString() }))
        ];
    }

    // --- DATA ---
    resumen: CorrelatividadResponseDTO = {
        items: [],
        totalBaseImp: 0,
        totalIgv: 0,
        totalGeneral: 0
    };

    constructor(
        private correlatividadService: CorrelatividadService,
        private sucursalService: SucursalService,
        private cd: ChangeDetectorRef
    ) {
        const currentYear = new Date().getFullYear();
        for (let i = currentYear; i >= currentYear - 5; i--) {
            this.anios.push(i);
        }
    }

    ngOnInit() {
        this.cargarSucursales();
        this.generarReporte();
    }

    cargarSucursales() {
        this.sucursalService.getAll(0, 100).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.sucursales = res.data.content;
                }
            }
        });
    }

    generarReporte() {
        this.isLoading = true;
        this.cd.detectChanges();
        
        this.correlatividadService.obtenerResumen(this.filtros).subscribe({
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
                console.error('Error fetching correlatividad report', err);
                Swal.fire('Error', 'Problema del servidor al generar el reporte', 'error');
            }
        });
    }

    handleFilter(criteria: FilterCriteria) {
        this.searchCriteria = criteria;
        // Mapear el filtro de "Estado" (que usamos para Sucursal)
        this.filtros.idSucursal = criteria.estado === 'null' || criteria.estado === 'TODOS' ? null : Number(criteria.estado);
        
        this.generarReporte();
    }

    limpiarFiltros() {
        this.filtros = {
            idSucursal: null,
            anio: new Date().getFullYear(),
            mes: new Date().getMonth() + 1
        };
        this.generarReporte();
    }

    cambiarTab(tab: 'MES' | 'TIPO_DOC') {
        this.tabActiva = tab;
    }

    getTotDocumentos(): number {
        return this.resumen.items.reduce((acc, item) => acc + item.cantidad, 0);
    }

    verDetalles(item: any) {
        this.selectedItem = item;
        this.isLoading = true;
        this.cd.detectChanges(); // Forzar spinner inicial

        this.correlatividadService.obtenerDetalle({
            idSucursal: this.filtros.idSucursal,
            anio: this.filtros.anio,
            mes: item.mes || this.filtros.mes,
            tipoDoc: item.tipoDoc,
            serie: item.serie
        }).subscribe({
            next: (res) => {
                if (res.success && res.data) {
                    this.selectedDocs = res.data;
                    this.showModalDetalle = true;
                } else {
                    Swal.fire('Error', 'No se pudieron cargar los detalles', 'error');
                }
                this.isLoading = false;
                this.cd.detectChanges(); // Forzar render del modal
            },
            error: (err) => {
                this.isLoading = false;
                console.error('Error fetching details', err);
                Swal.fire('Error', 'Error de conexión con el servidor', 'error');
                this.cd.detectChanges();
            }
        });
    }

    get groupedResumen() {
        if (!this.resumen.items || this.resumen.items.length === 0) return [];

        const isByMes = this.tabActiva === 'MES';
        
        // Filtrar localmente según criterios de búsqueda
        const filteredItems = this.resumen.items.filter(item => {
            const matchType = this.searchCriteria.searchType === 'ALL' || item.tipoDoc === this.searchCriteria.searchType;
            const matchTerm = !this.searchCriteria.term || item.serie.toLowerCase().includes(this.searchCriteria.term.toLowerCase());
            return matchType && matchTerm;
        });

        if (filteredItems.length === 0) return [];

        const map = new Map<string, any[]>();
        filteredItems.forEach(item => {
            const key = isByMes ? item.mes?.toString() || '0' : item.tipoDoc;
            const list = map.get(key) || [];
            list.push(item);
            map.set(key, list);
        });

        const sortedEntries = Array.from(map.entries()).sort((a, b) => {
            if (isByMes) return parseInt(a[0]) - parseInt(b[0]);
            return a[0].localeCompare(b[0]);
        });

        return sortedEntries.map(([key, items]) => {
            let label = '';
            let bgColor = 'bg-slate-600';
            let borderColor = 'border-slate-700';
            let subTotalColor = 'bg-slate-600 text-white';

            if (isByMes) {
                const mesNum = parseInt(key);
                label = this.meses.find(m => m.value === mesNum)?.label || `MES ${key}`;
                // Gradient colors for months
                const colors = [
                    'bg-slate-600', 'bg-blue-600', 'bg-indigo-600', 'bg-purple-600',
                    'bg-pink-600', 'bg-red-600', 'bg-orange-600', 'bg-amber-600',
                    'bg-yellow-600', 'bg-lime-600', 'bg-green-600', 'bg-emerald-600', 'bg-teal-600'
                ];
                bgColor = colors[mesNum % colors.length] || 'bg-slate-600';
                borderColor = bgColor.replace('bg-', 'border-').replace('600', '700');
                subTotalColor = bgColor + ' text-white';
            } else {
                label = key === 'B' ? 'BOLETAS' : (key === 'F' ? 'FACTURAS' : key);
                bgColor = key === 'B' ? 'bg-[#00ced1]' : (key === 'F' ? 'bg-[#003366]' : 'bg-slate-600');
                borderColor = key === 'B' ? 'border-[#008b8b]' : (key === 'F' ? 'border-blue-900' : 'border-slate-700');
                subTotalColor = key === 'B' ? 'bg-cyan-600 text-white' : (key === 'F' ? 'bg-blue-900 text-white' : 'bg-slate-600 text-white');
            }

            return {
                key,
                label,
                items,
                subTotalBase: items.reduce((acc, i) => acc + i.baseImp, 0),
                subTotalIgv: items.reduce((acc, i) => acc + i.igv, 0),
                subTotalTotal: items.reduce((acc, i) => acc + i.total, 0),
                bgColor,
                borderColor,
                subTotalColor
            };
        });
    }
}

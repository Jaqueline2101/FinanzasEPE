import { Component, OnInit } from '@angular/core';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReimpresionService } from '../../services/reimprimir.service';
import { DocumentoReimpresion, ReimpresionFilter } from '../../models/reimprimir.model';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { Columna, TablaGeneralComponent } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { FormatoImpresionHonorariosComponent, HonorarioVentaInfo } from './formato-impresion/formato-impresion.component';
import { FormatoImpresionComponent, VentaInfo } from '../venta-servicios/formato-impresion/formato-impresion.component';
import { PagoHonorariosService } from '../../services/pago-honorarios.service';
import { VentaService } from '../../services/venta-servicios.service';
import { ChangeDetectorRef } from '@angular/core';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';

@Component({
    selector: 'app-reimpresion',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        FormatoImpresionHonorariosComponent,
        FormatoImpresionComponent,
        ModalDetalleComponent,
        BarraFiltrosComponent
    ],
    templateUrl: './reimpresion.component.html',
    styleUrls: ['./reimpresion.component.css']
})
export class ReimpresionComponent implements OnInit {

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Procesos' },
        { label: 'Reimprimir' }
    ];

    columnas: Columna[] = [
        { field: 'index', header: '#', tipo: 'index', subField: [] },
        { field: 'fecha', header: 'FECHA', tipo: 'date', subField: [], align: 'left' },
        { field: 'tipoDocumento', header: 'TIPO', tipo: 'text', subField: [], align: 'left' },
        { field: 'serie', header: 'SERIE / N°', tipo: 'title-subtitle', subField: ['numero'], align: 'left' },
        { field: 'clienteBeneficiario', header: 'CLIENTE / BENEFICIARIO', tipo: 'text', subField: [], align: 'left' },
        {
            field: 'moduloOrigen',
            header: 'MÓDULO',
            tipo: 'badge',
            subField: [],
            badgeConfig: {
                from: 'from-blue-50',
                to: 'to-blue-100',
                text: 'text-blue-700',
                border: 'border-blue-200'
            }
        },
        {
            field: 'total',
            header: 'TOTAL',
            tipo: 'currency',
            subField: [],
            align: 'center'
        },
        {
            field: 'estado',
            header: 'ESTADO',
            tipo: 'badge',
            subField: [],
            badgeConfig: {
                from: 'from-emerald-50',
                to: 'to-emerald-100',
                text: 'text-emerald-700',
                border: 'border-emerald-200'
            }
        }
    ];

    moduleOptions = [
        { label: 'Todos', value: '' },
        { label: 'Ventas', value: 'VENTAS' },
        { label: 'Honorarios', value: 'HONORARIOS' },
        { label: 'Pago Personal', value: 'PAGO_PERSONAL' }
    ];

    filtros: ReimpresionFilter = {
        fechaDesde: '', // Se inicializa en el constructor
        fechaHasta: '',
        tipoDocumento: '',
        serie: '',
        numero: '',
        numDocIdentidad: '',
        moduloOrigen: '',
        query: ''
    };

    documentos: DocumentoReimpresion[] = [];
    documentosPaginados: DocumentoReimpresion[] = [];
    loading = false;
    selectedDocumento: DocumentoReimpresion | null = null;
    showPreview = false;
    showFormatModal = false;
    showFormatPreview = false;
    docToPrint: DocumentoReimpresion | null = null;
    selectedFormat: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' = 'TICKET_80';
    printHonorarioInfo: HonorarioVentaInfo | null = null;
    pagoDatosOrigen: any = null; // Para retenciones y montos completos en impresión
    printVentaInfo: VentaInfo | null = null;
    carritoVenta: any[] = [];

    // Paginación
    currentPage = 0;
    pageSize = 10;
    totalPages = 0;
    pageSizeOptions = [5, 10, 25, 50];

    private getLocalDate(): string {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    constructor(
        private reimpresionService: ReimpresionService,
        private pagoHonorariosService: PagoHonorariosService,
        private ventaService: VentaService,
        private cdr: ChangeDetectorRef
    ) {
        const today = this.getLocalDate();
        this.filtros.fechaDesde = today;
        this.filtros.fechaHasta = today;
    }

    ngOnInit(): void {
        setTimeout(() => {
            this.buscar();
        });
    }

    onFilter(criteria: FilterCriteria): void {
        this.filtros.fechaDesde = criteria.fechaDesde || '';
        this.filtros.fechaHasta = criteria.fechaHasta || '';
        this.filtros.moduloOrigen = criteria.searchType || '';
        this.filtros.query = criteria.term || '';
        this.currentPage = 0;
        this.buscar();
    }

    buscar(): void {
        this.loading = true;
        this.cdr.detectChanges(); // Prevenir NG0100 al cambiar loading antes del check
        this.reimpresionService.buscarDocumentos(this.filtros).subscribe({
            next: (res) => {
                if (res.success) {
                    this.documentos = res.data || [];
                    this.actualizarPaginacion();
                }
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error al buscar documentos', err);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    actualizarPaginacion(): void {
        this.totalPages = Math.ceil(this.documentos.length / this.pageSize);
        if (this.currentPage >= this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages - 1;
        }
        if (this.totalPages === 0) this.currentPage = 0;

        const start = this.currentPage * this.pageSize;
        const end = start + this.pageSize;
        this.documentosPaginados = this.documentos.slice(start, end);
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.actualizarPaginacion();
    }

    onPageSizeChange(size: number): void {
        this.pageSize = size;
        this.currentPage = 0;
        this.actualizarPaginacion();
    }

    limpiar(): void {
        const today = this.getLocalDate();
        this.filtros = {
            fechaDesde: today,
            fechaHasta: today,
            tipoDocumento: '',
            serie: '',
            numero: '',
            numDocIdentidad: '',
            moduloOrigen: '',
            query: ''
        };
        this.buscar();
    }

    abrirSelectorFormato(doc: DocumentoReimpresion): void {
        this.docToPrint = doc;
        this.selectedFormat = 'TICKET_80'; // Default format
        this.abrirPrevisualizacionDiseno(doc);
    }

    cambiarFormato(nuevoFormato: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5'): void {
        this.selectedFormat = nuevoFormato;
        this.cdr.detectChanges();
    }

    abrirPrevisualizacionDiseno(doc: DocumentoReimpresion): void {
        this.printHonorarioInfo = null;
        this.pagoDatosOrigen = null;
        this.printVentaInfo = null;
        this.carritoVenta = [];

        // Obtenemos los datos necesarios para el diseño si aún no están
        if (doc.moduloOrigen === 'HONORARIOS' || doc.moduloOrigen === 'PAGO_PERSONAL') {
            this.loading = true;
            this.pagoHonorariosService.obtenerPorId(doc.id).subscribe({
                next: (pago) => {
                    if (pago) {
                        this.pagoDatosOrigen = pago; // Assign pagoDatosOrigen here
                        this.printHonorarioInfo = {
                            ticketNumber: `${pago.serieComprobante || '---'}-${pago.numeroRecibo || '---'}`,
                            fecha: new Date(pago.fechaEmision),
                            caja: 'CAJA PRINCIPAL',
                            usuario: pago.usuarioRegistro || 'USUARIO',
                            tipoComprobante: pago.tipoPago,
                            metodoPago: pago.medioPago,
                            montoRecibido: pago.montoNeto,
                            vuelto: 0,
                            beneficiario: pago.nombreRazonSocial,
                            numeroDocumento: pago.numeroDocumento
                        };

                        // Usamos setTimeout para asegurar que el cambio de estado ocurra en el siguiente tick
                        // y forzamos la detección de cambios para evitar el "doble clic"
                        setTimeout(() => {
                            this.showFormatPreview = true;
                            this.loading = false;
                            this.cdr.detectChanges();
                            this.cdr.markForCheck();
                        }, 50);
                    } else {
                        this.loading = false;
                        this.cdr.detectChanges();
                    }
                },
                error: (err) => {
                    console.error('Error al obtener datos para diseño', err);
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            });
        } else if (doc.moduloOrigen === 'VENTAS') {
            this.loading = true;
            this.ventaService.obtenerVenta(doc.id).subscribe({
                next: (res) => {
                    const venta = res.data;
                    if (venta) {
                        this.printVentaInfo = {
                            ticketNumber: `${venta.serie || '---'}-${venta.numero || '---'}`,
                            fecha: new Date(venta.fechaRegistro || venta.fecha || venta.fechaEmision),
                            caja: 'CAJA PRINCIPAL',
                            usuario: venta.usuarioCrea || venta.usuarioRegistro || venta.personalNombre || 'USUARIO',
                            tipoComprobante: venta.tipoComprobante || 'TICKET',
                            metodoPago: venta.metodoPago || 'EFECTIVO',
                            montoRecibido: Number(venta.total || 0),
                            vuelto: Number(venta.vuelto || 0),
                            montoEfectivo: Number(venta.montoEfectivo || 0),
                            montoTarjeta: Number(venta.montoTarjeta || 0),
                            montoTransferencia: Number(venta.montoTransferencia || 0),
                            ruc: venta.ruc,
                            razonSocial: venta.razon
                        };
                        this.carritoVenta = (venta.detalles || []).map((det: any) => ({
                            nombre: det.nombreServicio || det.nombre || det.glosa,
                            cantidad: det.cantidad || 1,
                            precioFinal: Number(det.precioUnitario || det.precioFinal || det.precio || 0),
                            descuento: Number(det.descuento || 0)
                        }));

                        setTimeout(() => {
                            this.showFormatPreview = true;
                            this.loading = false;
                            this.cdr.detectChanges();
                            this.cdr.markForCheck();
                        }, 50);
                    } else {
                        this.loading = false;
                        this.cdr.detectChanges();
                    }
                },
                error: (err) => {
                    console.error('Error al obtener datos de venta', err);
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            });
        } else {
            this.cdr.detectChanges();
        }
    }

    cerrarPrevisualizacionDiseno(): void {
        this.showFormatPreview = false;
        this.docToPrint = null;
        this.cdr.detectChanges();
    }

    verDetalle(doc: DocumentoReimpresion): void {
        this.selectedDocumento = doc;
        this.printHonorarioInfo = null;
        this.pagoDatosOrigen = null;
        this.printVentaInfo = null;
        this.carritoVenta = [];

        if (doc.moduloOrigen === 'HONORARIOS' || doc.moduloOrigen === 'PAGO_PERSONAL') {
            this.loading = true;
            this.pagoHonorariosService.obtenerPorId(doc.id).subscribe({
                next: (pago) => {
                    if (pago) {
                        this.printHonorarioInfo = {
                            ticketNumber: `${pago.serieComprobante || '---'}-${pago.numeroRecibo || '---'}`,
                            fecha: new Date(pago.fechaEmision),
                            caja: 'CAJA PRINCIPAL',
                            usuario: pago.usuarioRegistro || 'USUARIO',
                            tipoComprobante: pago.tipoPago,
                            metodoPago: pago.medioPago,
                            montoRecibido: pago.montoNeto,
                            vuelto: 0,
                            beneficiario: pago.nombreRazonSocial,
                            numeroDocumento: pago.numeroDocumento,
                            pagoOriginal: pago
                        };
                        this.showPreview = true;
                    }
                    this.loading = false;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error al obtener detalle de honorario', err);
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            });
        } else if (doc.moduloOrigen === 'VENTAS') {
            this.loading = true;
            this.ventaService.obtenerVenta(doc.id).subscribe({
                next: (res) => {
                    const venta = res.data;
                    if (venta) {
                        this.printVentaInfo = {
                            ticketNumber: `${venta.serie || '---'}-${venta.numero || '---'}`,
                            fecha: new Date(venta.fechaRegistro || venta.fecha || venta.fechaEmision),
                            caja: 'CAJA PRINCIPAL',
                            usuario: venta.usuarioCrea || venta.usuarioRegistro || venta.personalNombre || 'USUARIO',
                            tipoComprobante: venta.tipoComprobante || 'TICKET',
                            metodoPago: venta.metodoPago || 'EFECTIVO',
                            montoRecibido: Number(venta.total || 0),
                            vuelto: Number(venta.vuelto || 0),
                            montoEfectivo: Number(venta.montoEfectivo || 0),
                            montoTarjeta: Number(venta.montoTarjeta || 0),
                            montoTransferencia: Number(venta.montoTransferencia || 0),
                            ruc: venta.ruc,
                            razonSocial: venta.razon,
                            numeroDocumento: venta.nroDni || venta.ruc || '---',
                            pagoOriginal: {
                                conceptoPago: 'Venta de Servicios / Artículos'
                            }
                        };
                        this.carritoVenta = (venta.detalles || []).map((det: any) => ({
                            nombre: det.nombreServicio || det.nombre || det.glosa,
                            cantidad: det.cantidad || 1,
                            precioFinal: Number(det.precioUnitario || det.precioFinal || det.precio || 0),
                            descuento: Number(det.descuento || 0)
                        }));
                        // Use a specific structure for modal if it needs extra stuff
                        this.printHonorarioInfo = this.printVentaInfo as any;
                        this.showPreview = true;
                    }
                    this.loading = false;
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Error al obtener detalle de venta', err);
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            });
        } else {
            this.showPreview = true;
            this.cdr.detectChanges();
        }
    }

    cerrarPreview(): void {
        this.showPreview = false;
        this.selectedDocumento = null;
        this.cdr.detectChanges();
    }

    reimprimir(doc: DocumentoReimpresion): void {
        if (doc.moduloOrigen === 'HONORARIOS' || doc.moduloOrigen === 'PAGO_PERSONAL') {
            this.verDetalle(doc);
            setTimeout(() => {
                this.ejecutarImpresion();
            }, 1000);
        } else {
            console.log('Reimprimiendo documento', doc);
        }
    }

    ejecutarImpresion(): void {
        const printContent = document.getElementById('print-section');
        if (!printContent) return;

        const WindowPrt = window.open('', '', 'left=0,top=0,width=900,height=900,toolbar=0,scrollbars=0,status=0');
        if (!WindowPrt) return;

        WindowPrt.document.write(`
            <html>
                <head>
                    <title>Reimpresión</title>
                    <style>
                        ${Array.from(document.styleSheets)
                .filter(styleSheet => {
                    try { return !!styleSheet.cssRules; } catch (e) { return false; }
                })
                .map(styleSheet => Array.from(styleSheet.cssRules).map(rule => rule.cssText).join(''))
                .join('')}
                    </style>
                </head>
                <body>
                    ${printContent.innerHTML}
                </body>
            </html>
        `);
        WindowPrt.document.close();
        WindowPrt.focus();
        setTimeout(() => {
            WindowPrt.print();
            WindowPrt.close();
        }, 500);
    }

    getModuloLabel(modulo: string): string {
        switch (modulo) {
            case 'VENTAS': return 'Ventas';
            case 'HONORARIOS': return 'Honorarios';
            case 'PAGO_PERSONAL': return 'Pago Personal';
            default: return modulo;
        }
    }
}

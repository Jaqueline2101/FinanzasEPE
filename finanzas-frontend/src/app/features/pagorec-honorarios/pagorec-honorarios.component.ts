import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { PagoHonorariosService } from '../../services/pago-honorarios.service';
import { PagoComprobante } from '../../models/pago-honorarios.model';
import { ModalHonorariosComponent } from './modal-honorarios/modal-honorarios.component';
import { ModalDetalleHonorariosComponent } from './modal-detalle/modal-detalle.component';
import { ModalComponent } from '../../shared/components/modal/modal';
import { PagorecHonorariosFormatoImpresionComponent, HonorarioVentaInfo } from './formato-impresion/formato-impresion.component';
import { animate, style, transition, trigger } from '@angular/animations';
import { AlertasService } from '../../services/alertas.service';
import { CajaChicaService, CajaChicaMovimientoRequest } from '../../services/caja-chica.service';

@Component({
    selector: 'app-pagorec-honorarios',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent,
        PrimaryButtonComponent,
        ModalHonorariosComponent,
        ModalDetalleHonorariosComponent,
        PagorecHonorariosFormatoImpresionComponent,
        ModalComponent
    ],
    templateUrl: './pagorec-honorarios.component.html',
    styleUrls: ['./pagorec-honorarios.component.css'],
    animations: [
        trigger('slideInOut', [
            transition(':enter', [
                style({ transform: 'translateX(100%)' }),
                animate('300ms ease-out', style({ transform: 'translateX(0)' }))
            ]),
            transition(':leave', [
                animate('300ms ease-in', style({ transform: 'translateX(100%)' }))
            ])
        ])
    ]
})
export class PagorecHonorariosComponent implements OnInit {
    registros: PagoComprobante[] = [];
    filteredRegistros: PagoComprobante[] = [];
    loading = false;
    isFullScreenView = false;
    showDetalleModal = false;

    // Filter state
    searchQuery: string = '';
    selectedSearchType: string = 'ALL';
    selectedType: string = 'TODOS';
    fechaDesde: string = '';
    fechaHasta: string = '';

    selectedPrintFormat: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' | null = null;
    printVentaInfo: HonorarioVentaInfo | null = null;

    // Preview
    showPreviewModal = false;
    previewFormat: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' = 'TICKET_80';

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Procesos', route: '/admin/procesos' },
        { label: 'Pago Rec. Honorarios' }
    ];

    columnas: Columna[] = [
        { field: 'idPago', header: '#', tipo: 'index', subField: [], align: 'center' },
        { field: 'nombreRazonSocial', header: 'NOMBRE / PERSONA', tipo: 'title-subtitle', subField: ['numeroDocumento'] },
        { field: 'tipoPago', header: 'TIPO', tipo: 'badge', subField: [], badgeConfig: { container: 'bg-indigo-50', text: 'text-indigo-700', border: 'border-indigo-200' } },
        { field: 'montoNeto', header: 'MONTO', tipo: 'currency', subField: ['moneda'] },
        { field: 'medioPago', header: 'MÉTODO', tipo: 'text', subField: [] },
        { field: 'conceptoPago', header: 'CONCEPTO DE PAGO', tipo: 'text', subField: [] }
    ];

    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Beneficiario', value: 'BENEFICIARIO' },
        { label: 'Concepto', value: 'CONCEPTO' },
        { label: 'Documento', value: 'DOCUMENTO' }
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

    selectedPago?: PagoComprobante;

    constructor(
        private registroService: PagoHonorariosService,
        private alertasService: AlertasService,
        private cdr: ChangeDetectorRef,
        private cajaChicaService: CajaChicaService
    ) { }

    ngOnInit(): void {
        this.cargarRegistros();
    }

    cargarRegistros() {
        this.loading = true;
        this.registroService.listar().subscribe({
            next: (data) => {
                this.registros = data;
                this.aplicarFiltros();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando registros de pago', err);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onFilter(criteria: FilterCriteria) {
        this.searchQuery = criteria.term;
        this.selectedSearchType = criteria.searchType;
        this.selectedType = criteria.estado;
        this.fechaDesde = criteria.fechaDesde;
        this.fechaHasta = criteria.fechaHasta;
        this.currentPage = 0;
        this.aplicarFiltros();
    }

    aplicarFiltros() {
        const query = this.searchQuery.toLowerCase();
        const from = this.fechaDesde ? new Date(this.fechaDesde + 'T00:00:00') : null;
        const to = this.fechaHasta ? new Date(this.fechaHasta + 'T23:59:59') : null;

        this.filteredRegistros = this.registros.filter(r => {
            // Filter by Term
            let matchesTerm = true;
            if (query) {
                const nombre = (r.nombreRazonSocial || '').toLowerCase();
                const concepto = (r.conceptoPago || '').toLowerCase();
                const doc = (r.numeroDocumento || '').toLowerCase();

                if (this.selectedSearchType === 'BENEFICIARIO') matchesTerm = nombre.includes(query);
                else if (this.selectedSearchType === 'CONCEPTO') matchesTerm = concepto.includes(query);
                else if (this.selectedSearchType === 'DOCUMENTO') matchesTerm = doc.includes(query);
                else matchesTerm = nombre.includes(query) || concepto.includes(query) || doc.includes(query);
            }

            // Filter by Type
            const matchesType = this.selectedType === 'TODOS' || r.tipoPago === this.selectedType;

            // Filter by Date
            let matchesDate = true;
            if (from && to && r.fechaPago) {
                const fechaPago = new Date(r.fechaPago);
                matchesDate = fechaPago >= from && fechaPago <= to;
            }

            return matchesTerm && matchesType && matchesDate;
        });
    }

    onPageChange(page: number) {
        this.currentPage = page;
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
    }

    get totalPages(): number {
        return Math.ceil(this.filteredRegistros.length / this.pageSize);
    }

    get paginatedRegistros() {
        const start = this.currentPage * this.pageSize;
        const end = start + this.pageSize;
        return this.filteredRegistros.slice(start, end);
    }

    abrirNuevo() {
        this.selectedPago = undefined;
        this.isFullScreenView = true;
    }

    editar(pago: PagoComprobante) {
        this.selectedPago = pago;
        this.isFullScreenView = true;
    }

    verDetalle(pago: PagoComprobante) {
        this.selectedPago = pago;
        this.showDetalleModal = true;
    }

    cerrarDetalle() {
        this.showDetalleModal = false;
        this.selectedPago = undefined;
    }

    onCancelForm() {
        this.isFullScreenView = false;
        this.selectedPago = undefined;
    }

    onPreview(pago: any) {
        const format = pago.formatoImpresion;
        if (format) {
            this.previewFormat = format;
            this.prepararImpresion(pago, format);
            this.showPreviewModal = true;
            this.cdr.detectChanges();
        }
    }

    closePreviewModal() {
        this.showPreviewModal = false;
    }

    onSave(pago: any) {
        this.loading = true;
        const format = pago.formatoImpresion;
        const cajaChicaId = pago.cajaChicaId;
        delete pago.formatoImpresion;
        delete pago.cajaChicaId;

        this.registroService.registrar(pago).subscribe({
            next: (response) => {
                const newPago = response.data;

                // If payment is EFECTIVO and we have cajaChicaId, register caja movement
                if (pago.medioPago === 'EFECTIVO' && cajaChicaId) {
                    const movimiento: CajaChicaMovimientoRequest = {
                        cajaChicaId: cajaChicaId,
                        tipo: 'EGRESO',
                        monto: newPago.montoNeto,
                        descripcion: `Pago por honorarios a ${newPago.nombreRazonSocial}`,
                        referencia: newPago.serieComprobante ? `${newPago.serieComprobante}-${newPago.numeroRecibo}` : newPago.numeroRecibo || 'N/A',
                        metodoPago: 'EFECTIVO'
                    };

                    this.cajaChicaService.registrarMovimiento(movimiento).subscribe({
                        next: () => {
                            if (format && newPago) {
                                this.prepararImpresion(newPago, format);
                                setTimeout(() => {
                                    this.imprimir();
                                    this.onSaveSuccess();
                                }, 500);
                            } else {
                                this.onSaveSuccess();
                            }
                        },
                        error: (err) => {
                            console.error('Error al registrar movimiento en caja', err);
                            this.alertasService.error('Error', 'Se registró el pago pero no se pudo actualizar la caja. Por favor verifique.');
                            if (format && newPago) {
                                this.prepararImpresion(newPago, format);
                                setTimeout(() => {
                                    this.imprimir();
                                    this.onSaveSuccess();
                                }, 500);
                            } else {
                                this.onSaveSuccess();
                            }
                        }
                    });
                } else {
                    if (format && newPago) {
                        this.prepararImpresion(newPago, format);
                        setTimeout(() => {
                            this.imprimir();
                            this.onSaveSuccess();
                        }, 500);
                    } else {
                        this.onSaveSuccess();
                    }
                }
            },
            error: (err) => {
                console.error('Error al registrar pago', err);
                this.loading = false;
                this.cdr.detectChanges();
                this.alertasService.error('Error', 'No se pudo registrar el pago por honorarios.');
            }
        });
    }

    prepararImpresion(pago: PagoComprobante, format: any) {
        this.selectedPago = pago;
        this.selectedPrintFormat = format;
        this.printVentaInfo = {
            ticketNumber: `${pago.serieComprobante || '---'}-${pago.numeroRecibo || '---'}`,
            fecha: new Date(),
            caja: 'CAJA PRINCIPAL',
            usuario: 'USUARIO ACTUAL',
            tipoComprobante: pago.tipoPago,
            metodoPago: pago.medioPago,
            montoRecibido: pago.montoNeto,
            vuelto: 0,
            beneficiario: pago.nombreRazonSocial,
            numeroDocumento: pago.numeroDocumento
        };
        this.cdr.detectChanges();
    }

    imprimir() {
        const printContent = document.getElementById('print-section-honorarios');
        if (!printContent) return;

        // Crear un iframe oculto para la impresión
        let iframe = document.getElementById('print-iframe-honorarios') as HTMLIFrameElement;
        if (!iframe) {
            iframe = document.createElement('iframe');
            iframe.id = 'print-iframe-honorarios';
            iframe.style.position = 'fixed';
            iframe.style.right = '0';
            iframe.style.bottom = '0';
            iframe.style.width = '0';
            iframe.style.height = '0';
            iframe.style.border = 'none';
            document.body.appendChild(iframe);
        }

        const doc = iframe.contentWindow?.document;
        if (!doc) return;

        // Capturar estilos actuales
        const styles = Array.from(document.styleSheets)
            .filter(styleSheet => {
                try { return !!styleSheet.cssRules; } catch (e) { return false; }
            })
            .map(styleSheet => Array.from(styleSheet.cssRules).map(rule => rule.cssText).join(''))
            .join('\n');

        // Escribir contenido en el iframe
        doc.open();
        doc.write(`
            <html>
                <head>
                    <title>Impresión de Honorario</title>
                    <style>
                        ${styles}
                        body { background: white !important; margin: 0; padding: 0; }
                        .preview-container { background: white !important; padding: 0 !important; min-height: auto !important; }
                        .paper { box-shadow: none !important; border: none !important; margin: 0 !important; width: 100% !important; max-width: none !important; }
                        @media print {
                            @page { margin: 0; }
                            body { margin: 1cm; }
                        }
                    </style>
                </head>
                <body>
                    ${printContent.innerHTML}
                </body>
            </html>
        `);
        doc.close();

        // Lanzar impresión después de que cargue el contenido
        setTimeout(() => {
            if (iframe.contentWindow) {
                iframe.contentWindow.focus();
                iframe.contentWindow.print();
            }
        }, 500);
    }

    onSaveSuccess() {
        this.alertasService.success('¡Registro Exitoso!', 'El pago por honorarios ha sido registrado correctamente.');
        this.cargarRegistros();
        this.isFullScreenView = false;
        this.selectedPago = undefined;
    }
}

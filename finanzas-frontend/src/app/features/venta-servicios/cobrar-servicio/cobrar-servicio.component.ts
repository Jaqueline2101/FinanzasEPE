import { Component, EventEmitter, Input, Output, OnInit, OnDestroy, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { ServicioService } from '../../../services/servicio.service';
import { NivelService } from '../../../services/nivel.service';
import { AuthService } from '../../auth/services/auth.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { FormatoImpresionComponent, VentaInfo } from '../formato-impresion/formato-impresion.component';
import { AlertasService } from '../../../services/alertas.service';
import { VentaService } from '../../../services/venta-servicios.service';
import { PaqueteService } from '../../../services/paquete.service';
import { UserService } from '../../../services/user.service';
import { ConvenioService } from '../../../services/convenio.service';
import { ModalSolicitarComponent } from '../modal-solicitar/modal-solicitar.component';
import { NivelSelectorComponent } from '../../../shared/components/nivel-selector/nivel-selector.component';

@Component({
    selector: 'app-cobrar-servicio',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent, FormatoImpresionComponent, ModalSolicitarComponent, NivelSelectorComponent],
    templateUrl: './cobrar-servicio.component.html',
    styleUrls: ['./cobrar-servicio.component.css']
})
export class CobrarServicioComponent implements OnInit, OnDestroy, OnChanges {
    private searchSubscription?: any;
    @Input() paciente: any | null = null;
    @Input() ordenSeleccionada: any | null = null;
    @Input() carrito: any[] = [];
    @Input() tipoCliente: string = 'PARTICULAR';
    @Input() idPlan: number | null = null;
    @Input() totalVenta: number = 0;
    @Input() cacheBeneficios: any[] = [];

    @Output() volver = new EventEmitter<void>();
    @Output() procesarPago = new EventEmitter<any>();
    @Output() carritoChange = new EventEmitter<any[]>();
    @Output() totalVentaChange = new EventEmitter<number>();
    @Output() loadOrderById = new EventEmitter<number>();
    @Output() registrarPaciente = new EventEmitter<void>();

    metodoPago: 'EFECTIVO' | 'YAPE_PLIN' | 'TRANSFERENCIA' | 'MIXTO' = 'EFECTIVO';
    formatoImpresion: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' = 'TICKET_80';

    // Payment data (Multi-method)
    montoEfectivo: number | null = null;
    montoTarjeta: number | null = null;
    montoTransferencia: number | null = null;

    // References
    refTarjeta: string = '';
    refTransferencia: string = '';

    // Document types
    tipoComprobante: 'BOLETA' | 'FACTURA' = 'BOLETA';
    ruc: string = '';
    razonSocial: string = '';
    direccionRuc: string = '';

    // Calculations
    vuelto: number = 0;
    saldoPendiente: number = 0;

    // Preview Modal
    showPreviewModal = false;
    previewFormat: 'TICKET_80' | 'TICKET_58' | 'A4' | 'A5' = 'TICKET_80';
    sampleVentaInfo: VentaInfo | null = null;

    // Proactive Search
    private rucSearch$ = new Subject<string>();
    sugerenciasRuc: any[] = [];
    showSugerencias = false;
    isSearching = false;

    // --- PATIENT SEARCH ---
    private pacienteSearch$ = new Subject<string>();
    private pacienteSearchSub?: any;
    terminoBusquedaPaciente = '';
    resultadosPacientes: any[] = [];
    showSugerenciasPaciente = false;
    isSearchingPaciente = false;
    @Output() pacienteSeleccionado = new EventEmitter<any>();

    // --- SERVICE SEARCH ---
    private serviceSearch$ = new Subject<string>();
    private svcSearchSub?: any;
    terminoBusquedaServicio = '';
    resultadosServicios: any[] = [];
    showServiciosSugerencias = false;
    isSearchingSvc = false;

    // --- LEVELS ---
    niveles: any[] = [];
    idNivelSeleccionado: number | null = null;
    nivelSeleccionado: string = 'TODO';

    // --- PLAN AUTO-DETECTION ---
    planActivo: any | null = null;
    
    // --- INSURANCE LIQUIDATION ---
    // MODAL DE DESCUENTO
    showModalSolicitarDscto = false;
    idAutorizacionDescuento: number | null = null;
    montoDescuentoAutorizado: number = 0;


    constructor(
        private authService: AuthService,
        private cd: ChangeDetectorRef,
        private ventaService: VentaService,
        private servicioService: ServicioService,
        private nivelService: NivelService,
        private paqueteService: PaqueteService,
        private userService: UserService,
        private convenioService: ConvenioService,
        private alertasService: AlertasService
    ) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['totalVenta'] && !changes['totalVenta'].firstChange) {
            this.setPagoExacto();
            this.recalcularSaldos();
        }

        if (changes['idPlan'] && this.idPlan) {
            this.cargarPlanPorId(this.idPlan);
        } else if (changes['idPlan'] && !this.idPlan) {
            this.planActivo = null;
        }
    }

    private cargarPlanPorId(id: number) {
        this.convenioService.obtenerConvenio(id).subscribe({
            next: (plan) => {
                this.planActivo = plan;
                this.onSearchServiceChange(); // Trigger search to get discounted prices
                this.cd.detectChanges();
            },
            error: (err) => console.error('Error cargando plan detectado', err)
        });
    }

    ngOnInit(): void {
        if (this.carrito.length > 0) {
            this.carrito.forEach(item => {
                if (item.selected === undefined) item.selected = true;
            });
            this.calcularTotal();
            this.saldoPendiente = this.totalVenta;
        }

        // Setup Reactive Search for RUC
        this.searchSubscription = this.rucSearch$.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            switchMap(term => {
                if (term.length < 3) {
                    this.sugerenciasRuc = [];
                    this.showSugerencias = false;
                    return of([]);
                }
                this.isSearching = true;
                return this.ventaService.buscarSugerencias(term).pipe(
                    catchError(() => {
                        this.isSearching = false;
                        return of([]);
                    })
                );
            })
        ).subscribe(results => {
            this.sugerenciasRuc = results;
            this.showSugerencias = results.length > 0;
            this.isSearching = false;

            if ((this.ruc.length === 8 || this.ruc.length === 11) && results.length === 1) {
                const first = results[0];
                if (first.ruc === this.ruc) {
                    this.seleccionarSugerencia(first);
                    this.cd.detectChanges();
                }
            }
        });

        this.setupPacienteSearch();
        this.cargarNiveles();
        this.setupServiceSearch();
    }

    private setupPacienteSearch() {
        this.pacienteSearchSub = this.pacienteSearch$.pipe(
            debounceTime(300),
            distinctUntilChanged()
        ).subscribe(term => {
            if (!term || term.trim().length === 0) {
                this.resultadosPacientes = [];
                this.showSugerenciasPaciente = false;
                this.isSearchingPaciente = false;
                this.cd.detectChanges();
                return;
            }

            this.isSearchingPaciente = true;
            this.cd.detectChanges();

            this.userService.searchByFilter(term, 'paciente', 0, 10).subscribe({
                next: (res: any) => {
                    // Extract payload correctly since `ApiResponse` contains `data` which has `content`
                    const content = res.data?.content || [];
                    this.resultadosPacientes = content.map((u: any) => ({
                        type: 'PATIENT',
                        data: u,
                        label: `${u.firstName} ${u.lastName}`,
                        subLabel: Object.keys(u).some(k => k.includes('titular') || u.esFamiliar)
                            ? `FAMILIAR - ${u.numdoc} (Tit: ${u.nombreTitular || 'Desconocido'})`
                            : `DNI: ${u.numdoc || u.username}`
                    }));
                    this.showSugerenciasPaciente = true;
                    this.isSearchingPaciente = false;
                    this.cd.detectChanges();
                },
                error: (err: any) => {
                    console.error('Error buscando pacientes', err);
                    this.resultadosPacientes = [];
                    this.isSearchingPaciente = false;
                    this.showSugerenciasPaciente = false;
                    this.cd.detectChanges();
                }
            });
        });
    }

    onPacienteSearch(term: string) {
        this.pacienteSearch$.next(term);
    }

    seleccionarPacienteBuscado(res: any) {
        this.pacienteSeleccionado.emit(res.data);
        this.terminoBusquedaPaciente = `${res.data.firstName} ${res.data.lastName}`;
        this.showSugerenciasPaciente = false;
        this.resultadosPacientes = [];
        this.cd.detectChanges();
    }

    private cargarNiveles() {
        this.nivelService.obtenerTodos(0, 500).subscribe({
            next: (res) => {
                const data = res.data?.content || [];
                this.niveles = data;                // Por defecto "TODO"
                this.idNivelSeleccionado = null;
                this.nivelSeleccionado = 'TODO';
                this.cd.detectChanges();
            }
        });
    }

    private setupServiceSearch() {
        this.svcSearchSub = this.serviceSearch$.pipe(
            debounceTime(300),
            switchMap(term => {
                const planId = this.planActivo?.idPlan;

                if (!term || term.trim().length === 0) {
                    if (planId) {
                        this.isSearchingSvc = true;
                        return this.servicioService.smartSearch(' ', planId, undefined, this.nivelSeleccionado, this.idNivelSeleccionado || undefined).pipe(
                            catchError(() => {
                                this.isSearchingSvc = false;
                                return of({ results: [] });
                            })
                        );
                    }
                    this.resultadosServicios = [];
                    this.showServiciosSugerencias = false;
                    this.isSearchingSvc = false;
                    return of({ results: [] });
                }

                this.isSearchingSvc = true;
                return this.servicioService.smartSearch(term, planId, undefined, this.nivelSeleccionado, this.idNivelSeleccionado || undefined).pipe(
                    catchError(() => {
                        this.isSearchingSvc = false;
                        return of({ results: [] });
                    })
                );
            })
        ).subscribe(res => {
            // Manejamos la nueva estructura SmartSearchResponse
            const data = (res as any).results || [];
            const planMatch = (res as any).planMatch;

            // Si hay un PLAN detectado por código exacto, lo guardamos y LIMPIAMOS buscador
            if (planMatch && (!this.planActivo || this.planActivo.idPlan !== planMatch.idPlan)) {
                // Cargar el convenio completo para tener los campos de seguro (copago, deducible, etc)
                this.convenioService.obtenerConvenio(planMatch.idPlan).subscribe({
                    next: (fullPlan) => {
                        this.planActivo = fullPlan;
                        this.terminoBusquedaServicio = '';
                        this.cd.detectChanges();
                    }
                });
                
                console.log('¡Plan detectado y activado!', planMatch);

                // Disparar búsqueda vacía para listar los servicios asignados al plan
                this.serviceSearch$.next(' ');

                this.cd.detectChanges();
                return;
            }

            this.resultadosServicios = data.map((svc: any) => {
                // El backend ya nos envía precioReal (base) y precioFinal (con descuento del plan)
                const pReal = svc.precioReal || svc.precio || 0;
                const pFinal = svc.precioFinal || pReal;
                const tieneDesc = svc.tieneDescuento || (pReal > pFinal);

                return {
                    ...svc,
                    nombre: svc.nombre,
                    precioReal: pReal,
                    precioFinal: pFinal,
                    tieneDescuento: tieneDesc,
                    label: svc.nombre,
                    subLabel: `Cód: ${svc.codigo} - ${tieneDesc ? 'PRECIO PLAN: S/ ' + pFinal : 'S/ ' + pReal}`
                };
            }).filter((svc: any) => {
                // Filtro de seguridad en el frontend
                if (this.nivelSeleccionado === 'TODO') return true;
                return svc.nivelNombre === this.nivelSeleccionado;
            });

            this.showServiciosSugerencias = this.terminoBusquedaServicio.trim().length > 0 &&
                (this.resultadosServicios.length > 0 || !!planMatch);
            this.isSearchingSvc = false;
            this.cd.detectChanges();
        });
    }

    onSearchServiceChange() {
        this.serviceSearch$.next(this.terminoBusquedaServicio);
    }

    seleccionarServicio(svc: any) {
        if (svc.tipo === 'PLAN') {
            const planDetectado = { idPlan: svc.id, nombre: svc.nombre.replace('BENEFICIO: ', ''), codigo: svc.codigo };
            this.planActivo = planDetectado;
            this.terminoBusquedaServicio = '';

            // Disparar búsqueda vacía para listar los servicios asignados al plan
            this.serviceSearch$.next(' ');
            this.cd.detectChanges();
            return;
        }

        this.terminoBusquedaServicio = '';
        this.showServiciosSugerencias = false;

        const pFinal = Number(svc.precioFinal || svc.precio || 0);
        const pOriginal = Number(svc.precioReal || svc.precio || pFinal);

        const item: any = {
            id: svc.id,
            codigo: svc.codigo,
            nombre: svc.nombre,
            precioFinal: pFinal,
            precioOriginal: pOriginal,
            precioReal: pOriginal,
            isBeneficio: svc.tieneDescuento || (pOriginal > pFinal),
            total: pFinal,
            cantidad: 1,
            selected: true,
            tipo: svc.tipo,
            nivelNombre: svc.nivelNombre || 'OTROS',
            serviciosDetalle: []
        };
        
        console.log('✅ Agregando item al carrito:', item);

        const esPaquete = (svc.tipo === 'PAQUETE' && svc.tipoCatalogo !== 'EXAMEN') ||
            (svc.nombre && svc.nombre.toUpperCase().startsWith('PAQUETE:')) ||
            svc.idPaquete;

        if (esPaquete) {
            const paqueteId = svc.idPaquete || svc.id;
            this.paqueteService.obtenerPaquete(paqueteId).subscribe({
                next: (res: any) => {
                    if (res && res.servicios) {
                        item.serviciosDetalle = res.servicios.map((s: any) => ({
                            nombre: s.nombreServicio || s.nombre,
                            cantidad: s.cantidad,
                            precioUnitario: s.precioUnitario || s.precio || 0
                        }));
                        item.tipo = 'PAQUETE';
                        this.cd.detectChanges();
                    }
                },
                error: (err) => console.error('Error cargando detalle de paquete:', err)
            });
        }

        this.carrito.push(item);
        this.calcularTotal();
    }

    desactivarPlan() {
        this.planActivo = null;
        this.terminoBusquedaServicio = '';
        this.resultadosServicios = [];
        this.showServiciosSugerencias = false;
        this.cd.detectChanges();
    }

    setFilterNivel(idNivel: number | null) {
        this.idNivelSeleccionado = idNivel;
        if (idNivel) {
            // Buscamos recursivamente en el árbol si es necesario, pero aquí ya tenemos this.niveles flat
            const nivel = this.niveles.find(n => n.idNivel === idNivel);
            this.nivelSeleccionado = nivel ? nivel.nombre : '';
        } else {
            this.nivelSeleccionado = 'TODO';
        }
        this.onSearchServiceChange();
    }

    onApplyDiscount(solicitud: any) {
        if (solicitud.idVenta && (!this.ordenSeleccionada || this.ordenSeleccionada.idOrden !== solicitud.idVenta)) {
            // Contexto diferente o vacío: Pedir al padre que cargue la orden primero
            this.loadOrderById.emit(solicitud.idVenta);
        }

        this.idAutorizacionDescuento = solicitud.idAutorizacion;
        this.montoDescuentoAutorizado = solicitud.montoDescuento;
        this.recalcularSaldos();
    }

    quitarDescuento() {
        this.idAutorizacionDescuento = null;
        this.montoDescuentoAutorizado = 0;
        this.recalcularSaldos();
    }

    onRegistrarPaciente() {
        this.registrarPaciente.emit();
    }

    ngOnDestroy(): void {
        if (this.searchSubscription) {
            this.searchSubscription.unsubscribe();
        }
        if (this.svcSearchSub) {
            this.svcSearchSub.unsubscribe();
        }
    }

    onRucChange() {
        this.rucSearch$.next(this.ruc);
    }

    seleccionarSugerencia(sug: any) {
        this.ruc = sug.ruc;
        this.razonSocial = sug.razonSocial;
        this.direccionRuc = sug.direccion || '';
        this.showSugerencias = false;
        this.cd.detectChanges();
    }

    onCancelar() {
        this.volver.emit();
    }

    volverAtras() {
        this.volver.emit();
    }

    setMetodoPago(metodo: any) {
        this.metodoPago = metodo;
        if (metodo === 'EFECTIVO') this.montoEfectivo = this.totalVenta;
        if (metodo === 'YAPE_PLIN') this.montoTarjeta = this.totalVenta;
        if (metodo === 'TRANSFERENCIA') this.montoTransferencia = this.totalVenta;

        this.recalcularSaldos();
    }

    setPagoExacto() {
        if (this.metodoPago === 'EFECTIVO' || this.metodoPago === 'MIXTO') this.montoEfectivo = this.totalVenta;
        else if (this.metodoPago === 'YAPE_PLIN') this.montoTarjeta = this.totalVenta;
        else if (this.metodoPago === 'TRANSFERENCIA') this.montoTransferencia = this.totalVenta;

        this.recalcularSaldos();
    }

    recalcularSaldos() {
        const subtotal = this.totalVenta;

        const totalAPagar = Math.max(0, subtotal - (this.montoDescuentoAutorizado || 0));

        const totalPagado = (this.montoEfectivo || 0) + (this.montoTarjeta || 0) + (this.montoTransferencia || 0);
        this.saldoPendiente = Math.max(0, totalAPagar - totalPagado);

        if ((this.montoEfectivo || 0) > 0) {
            this.vuelto = Math.max(0, totalPagado - totalAPagar);
        } else {
            this.vuelto = 0;
        }

        const metodosUsados = [this.montoEfectivo, this.montoTarjeta, this.montoTransferencia].filter(m => (m || 0) > 0).length;
        if (metodosUsados > 1) {
            this.metodoPago = 'MIXTO';
        } else if (metodosUsados === 1) {
            if ((this.montoEfectivo || 0) > 0) this.metodoPago = 'EFECTIVO';
            else if ((this.montoTarjeta || 0) > 0) this.metodoPago = 'YAPE_PLIN';
            else if ((this.montoTransferencia || 0) > 0) this.metodoPago = 'TRANSFERENCIA';
        }
    }

    get itemsSeleccionados(): any[] {
        return this.carrito.filter(item => item.selected);
    }

    get gruposImpresion(): { nivelNombre: string, items: any[] }[] {
        const seleccionados = this.itemsSeleccionados;
        const gruposMap = new Map<string, any[]>();
        
        seleccionados.forEach(item => {
            const nivel = item.nivelNombre || 'OTROS';
            if (!gruposMap.has(nivel)) {
                gruposMap.set(nivel, []);
            }
            gruposMap.get(nivel)!.push(item);
        });

        // Convertir el Map a un array de objetos
        const gruposArray = Array.from(gruposMap, ([nivelNombre, items]) => ({ nivelNombre, items }));
        
        // Si no hay grupos (carrito vacío), devolver un grupo vacío para evitar errores en UI
        return gruposArray.length > 0 ? gruposArray : [{ nivelNombre: 'Venta', items: [] }];
    }

    calcularTotal() {
        this.totalVenta = this.carrito
            .filter(i => i.selected)
            .reduce((acc, i) => acc + (i.total || i.precioFinal || 0), 0);

        this.totalVentaChange.emit(this.totalVenta);
        this.carritoChange.emit(this.carrito);
        this.recalcularSaldos();
    }


    removerItem(index: number) {
        this.carrito.splice(index, 1);
        this.calcularTotal();
    }

    confirmarCobro() {
        const totalPagado = (this.montoEfectivo || 0) + (this.montoTarjeta || 0) + (this.montoTransferencia || 0);

        if (totalPagado < this.totalVenta) {
            this.alertasService.warning('Pago Incompleto', `Aún falta cobrar S/ ${this.saldoPendiente.toFixed(2)}`);
            return;
        }

        if ((this.montoTarjeta || 0) > 0 && !this.refTarjeta) {
            this.alertasService.warning('Validación Pago', 'Ingrese el número de operación para Yape/Plin.');
            return;
        }

        if ((this.montoTransferencia || 0) > 0 && !this.refTransferencia) {
            this.alertasService.warning('Atención', 'Ingrese el número de operación para la Transferencia.');
            return;
        }

        if (this.tipoComprobante === 'FACTURA') {
            if (!this.ruc || this.ruc.length !== 11) {
                this.alertasService.warning('Atención', 'El RUC debe tener 11 dígitos.');
                return;
            }
            if (!this.razonSocial) {
                this.alertasService.warning('Atención', 'Ingrese la Razón Social.');
                return;
            }
        }

        this.procesarPago.emit({
            metodo: this.metodoPago,
            formatoImpresion: this.formatoImpresion,
            tipoComprobante: this.tipoComprobante,
            idPlan: this.planActivo?.idPlan,
            datosFactura: this.tipoComprobante === 'FACTURA' ? {
                ruc: this.ruc,
                razonSocial: this.razonSocial,
                direccionRuc: this.direccionRuc
            } : null,
            info: {
                montoEfectivo: this.montoEfectivo,
                montoTarjeta: this.montoTarjeta,
                montoTransferencia: this.montoTransferencia,
                refTarjeta: this.refTarjeta,
                refTransferencia: this.refTransferencia,
                vuelto: this.vuelto,
                enlacePdf: (this.paciente as any)?.enlacePdf, // Placeholder if needed
                enlaceXml: (this.paciente as any)?.enlaceXml,
                codigoHash: (this.paciente as any)?.codigoHash
            },
            idAutorizacionDescuento: this.idAutorizacionDescuento,
            descuentoManual: this.montoDescuentoAutorizado
        });
    }

    previsualizarFormato(formato: string, event: Event) {
        event.stopPropagation();
        this.previewFormat = formato as any;

        this.sampleVentaInfo = {
            ticketNumber: 'T001-001234',
            fecha: new Date(),
            caja: 'CAJA PRINCIPAL',
            usuario: this.authService.userFullName() || 'CAJERO',
            tipoComprobante: this.tipoComprobante === 'BOLETA' ? 'BOLETA DE VENTA' : 'FACTURA ELECTRÓNICA',
            metodoPago: this.metodoPago,
            montoRecibido: (this.montoEfectivo || 0) + (this.montoTarjeta || 0) + (this.montoTransferencia || 0),
            vuelto: this.vuelto || 0,
            montoEfectivo: this.montoEfectivo || 0,
            montoTarjeta: this.montoTarjeta || 0,
            montoTransferencia: this.montoTransferencia || 0,
            ruc: this.tipoComprobante === 'FACTURA' ? this.ruc : undefined,
            razonSocial: this.tipoComprobante === 'FACTURA' ? this.razonSocial : undefined,
            sede: this.authService.currentSucursal() || 'SEDE ACTUAL',
            puntoCaja: this.authService.currentPunto() || 'PUNTO 1',
            codigoHash: 'DEMO-HASH-PREVIEW-123456789',
            enlacePdf: 'https://www.nubefact.com/demo-pdf',
            enlaceXml: 'https://www.nubefact.com/demo-xml',
            nombrePlan: this.planActivo?.nombre
        };

        this.showPreviewModal = true;
    }

    closePreviewModal() {
        this.showPreviewModal = false;
    }
}

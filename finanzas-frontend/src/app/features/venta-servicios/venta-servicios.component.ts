import { Component, ViewChild, ChangeDetectorRef, OnDestroy, OnInit, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
// Components removed as they are integrated directly into HTML now
import { VentaService, OrdenPendienteDTO, OrdenDetalleDTO } from '../../services/venta-servicios.service';
import { ConvenioService } from '../../services/convenio.service';
import { DescuentoService } from '../../services/descuento.service';
import { ServicioService } from '../../services/servicio.service';
import { UserService } from '../../services/user.service';
import { PaqueteService } from '../../services/paquete.service';
import { CredCtaCteService } from '../../services/cred-ctacte.service';
import { AuthService } from '../auth/services/auth.service';
import { CajaChicaService } from '../../services/caja-chica.service';
import { RegistrarPacienteModalComponent } from './registrar-paciente-modal/registrar-paciente-modal.component';
import { CobrarServicioComponent } from './cobrar-servicio/cobrar-servicio.component';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-venta-servicios-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        RegistrarPacienteModalComponent,
        CobrarServicioComponent
    ],
    templateUrl: './venta-servicios.component.html'
})
export class VentaServiciosPageComponent implements OnInit, OnDestroy {

    currentView: 'CHECKOUT' = 'CHECKOUT';
    sidebarMode: 'PENDIENTES' | 'PAQUETES' = 'PENDIENTES';

    breadcrumbItems = [
        { label: 'Punto de Venta', route: '/venta-servicios' }
    ];

    ordenesPendientesGlobales: OrdenPendienteDTO[] = [];
    paquetesDisponibles: any[] = [];

    paginaActual: number = 0;
    tamanoPagina: number = 10;

    get datosPaginados(): any[] {
        const start = this.paginaActual * this.tamanoPagina;
        const end = start + this.tamanoPagina;
        return this.ordenesPendientesGlobales.slice(start, end).map(orden => ({
            ...orden,
            displayId: `#${orden.idOrden}`,
            displayFecha: orden.fecha ? new Date(orden.fecha).toLocaleDateString() + ' ' + new Date(orden.fecha).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '',
            pacienteTipoLabel: this.getTipoPacienteLabel(orden),

            medicoNombre: orden.medicoNombre || '-',
            origen: orden.origen || 'LAB',
            prioridad: orden.prioridad || 'NORMAL'
        }));
    }

    get totalPaginas(): number {
        return Math.ceil(this.ordenesPendientesGlobales.length / this.tamanoPagina);
    }

    get paquetesSidebarFiltrados(): any[] {
        if (!this.terminoBusqueda) return this.paquetesDisponibles;
        const term = this.terminoBusqueda.toLowerCase();
        return this.paquetesDisponibles.filter(p =>
            p.nombre?.toLowerCase().includes(term) ||
            p.descripcion?.toLowerCase().includes(term)
        );
    }

    onAccionTabla(item: any) {
        this.buscarYCargarOrden(item.idOrden);
    }


    paciente: any | null = null;
    ordenSeleccionada: any | null = null;
    carrito: any[] = [];
    tipoCliente: 'PARTICULAR' | 'CONVENIO' | 'DESCUENTOS' = 'PARTICULAR';

    terminoBusqueda: string = '';
    resultadosBusqueda: any[] = [];
    loadingResultados: boolean = false;
    isLoading: boolean = false;
    isSaving: boolean = false;
    private searchSubject = new Subject<string>();
    private searchSubscription: Subscription;

    private cacheServicios: any[] = [];
    public cacheBeneficios: any[] = [];
    private cacheConvenios: any[] = [];
    manualConvenioId: number | null = null;

    totalVenta = 0;
    descuentoManual = 0;
    idAutorizacion: number | null = null;
    showModalDescuento = false;
    currentSucursalId: number = 1;
    currentUserId: number = 1;

    showServiceSearchModal = false;
    serviceSearchResults: any[] = [];
    isSearchingService = false;
    terminoBusquedaServicio = '';

    accionesVenta = [
        { id: 'SERVICIOS', label: 'SERVICIOS', icon: 'fa-stethoscope' },
        { id: 'PAQUETES', label: 'PAQUETES', icon: 'fa-box-open' },
        { id: 'CDI', label: 'CDI', icon: 'fa-microscope' },
        { id: 'ARTICULOS', label: 'ARTICULOS', icon: 'fa-pills' },
        { id: 'INSUMOS', label: 'INSUMOS', icon: 'fa-vial' }
    ];
    accionSeleccionada: string = '';

    @ViewChild('actionSelect') actionSelect!: ElementRef;

    constructor(
        private ventaService: VentaService,
        private convenioService: ConvenioService,
        private descuentoService: DescuentoService,
        private servicioService: ServicioService,
        private userService: UserService,
        private paqueteService: PaqueteService,
        private credCtaCteService: CredCtaCteService,
        private authService: AuthService, // Inject AuthService
        private route: ActivatedRoute,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService,
        private cajaChicaService: CajaChicaService
    ) {
        this.searchSubscription = this.searchSubject.pipe(
            debounceTime(250),
            distinctUntilChanged()
        ).subscribe(termino => {
            this.ejecutarFiltro(termino);
        });
    }

    ngOnInit() {
        this.loadContext();
        this.loadDashboardData();

        this.route.queryParams.subscribe(params => {
            if (params['view'] === 'PACKAGES') {
                this.irAPaquetes();
            }
            if (params['idOrden']) {
                this.buscarYCargarOrden(Number(params['idOrden']));
            }
        });
    }

    buscarYCargarOrden(id: number) {
        this.isLoading = true;
        this.ventaService.getOrdenById(id).subscribe({
            next: (fullOrden: any) => {
                // The backend return from getOrdenById might differ from search results
                // and might be wrapped in ApiResponse
                const ordenData = fullOrden.data || fullOrden;
                console.log('📦 Orden vinculada cargada:', ordenData);
                this.cargarOrdenParaCobro(ordenData);
            },
            error: (err) => {
                console.error("Error cargando orden vinculada", err);
                this.isLoading = false;
                this.alertasService.error('Error', 'No se pudo cargar la orden vinculada al descuento.');
            }
        });
    }

    ngOnDestroy() {
        if (this.searchSubscription) {
            this.searchSubscription.unsubscribe();
        }
    }

    loadContext() {

        const uid = localStorage.getItem('userId');
        this.currentUserId = uid ? Number(uid) : 1;

        this.authService.getSucursalesAutorizadas(this.currentUserId).subscribe({
            next: (res) => {
                const currentSucursalName = this.authService.currentSucursal();
                if (res.success && res.data) {
                    const found = res.data.find((s: any) => s.nombreSucursal === currentSucursalName);
                    if (found) {
                        this.currentSucursalId = found.idSucursal;
                        console.log('✅ [VENTAS] Sucursal actual resuelta:', this.currentSucursalId);
                    } else if (res.data.length > 0) {
                        // Fallback
                        this.currentSucursalId = res.data[0].idSucursal;
                        console.warn('⚠️ [VENTAS] Sucursal no coincide, usando fallback:', this.currentSucursalId);
                    }
                }
            },
            error: (err) => console.error('Error cargando sucursales', err)
        });
    }

    private getTodayLocal(): string {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }


    loadDashboardData() {
        this.isLoading = true;
        this.ordenSeleccionada = null;
        this.paciente = null;
        this.carrito = [];
        this.totalVenta = 0;
        this.terminoBusqueda = '';
        this.resultadosBusqueda = [];
        this.currentView = 'CHECKOUT';

        // Solo cargamos un paciente vacío o dejamos que cobrar-servicio maneje "null" pacientemente

        this.ventaService.buscarOrdenesPendientes()
            .subscribe({
                next: (res: any) => {
                    // Si res es un array directo, lo usamos; si viene en data.content, también.
                    this.ordenesPendientesGlobales = Array.isArray(res) ? res : (res.data?.content || res.data || []);
                    this.isLoading = false;
                    this.cd.detectChanges();
                },
                error: (err: any) => {
                    console.error('Error fetching pending orders', err);
                    this.isLoading = false;
                    this.cd.detectChanges();
                }
            });

        this.paqueteService.listarPaquetesActivos().subscribe({
            next: (paquetes) => {
                this.paquetesDisponibles = paquetes;
            }
        });
    }


    onOmniboxSearch(term: string) {
        this.searchSubject.next(term);
    }

    onPacienteSeleccionado(paciente: any) {
        // Trigger the Pro-active detection logic instead of resetting to PARTICULAR
        this.setPaciente(paciente);

        // Reset checkout state since it's a new patient
        this.ordenSeleccionada = null;
        this.carrito = [];

        this.cd.detectChanges();
    }

    private ejecutarFiltro(term: string) {
        if (!term || term.trim().length < 2) {
            this.resultadosBusqueda = [];
            return;
        }

        this.loadingResultados = true;
        this.resultadosBusqueda = [];

        if (this.sidebarMode === 'PAQUETES') {
            // Busqueda es solo local
            this.resultadosBusqueda = [];
            this.loadingResultados = false;
            return;
        }

        const isNumber = /^\d+$/.test(term);
        let matchedOrdersResults: any[] = [];

        if (isNumber) {

            const matchedOrders = this.ordenesPendientesGlobales.filter(o =>
                o.idOrden.toString().includes(term) || o.pacienteDoc?.includes(term)
            );

            matchedOrdersResults = matchedOrders.map(o => ({
                type: 'ORDER',
                data: o,
                label: `Orden #${o.idOrden} - ${o.pacienteNombre || 'Sin Nombre'}`,
                subLabel: `DNI: ${o.pacienteDoc || 'N/A'} - S/ ${o.totalEstimado || '0.00'}`
            }));
        }

        console.log('[VENTA-SERVICIOS] ejecutarFiltro called with term:', term);
        this.userService.buscarPersonas(term).subscribe({
            next: (response: any) => {
                console.log('[VENTA-SERVICIOS] buscarPersonas response:', response);
                const users = response.data?.content || [];
                console.log('[VENTA-SERVICIOS] users found:', users);
                const patientResults = users.map((u: any) => ({
                    type: 'PATIENT',
                    data: u,
                    label: `${u.firstName} ${u.lastName}`,
                    subLabel: u.esFamiliar
                        ? `FAMILIAR - ${u.numdoc} (Tit: ${u.nombreTitular || 'Desconocido'})`
                        : (u.numdoc || u.username)
                }));

                this.resultadosBusqueda = [...matchedOrdersResults, ...patientResults];
                console.log('[VENTA-SERVICIOS] resultadosBusqueda set to:', this.resultadosBusqueda);
                this.loadingResultados = false;
                this.cd.detectChanges();
            },
            error: (err: any) => {
                console.error('[VENTA-SERVICIOS] buscarPersonas error:', err);
                this.resultadosBusqueda = [...matchedOrdersResults];
                this.loadingResultados = false;
            }
        });
    }

    selectResult(result: any) {
        if (result.type === 'ORDER') {
            this.cargarOrdenParaCobro(result.data);
            this.terminoBusqueda = '';
            this.resultadosBusqueda = [];
        } else if (result.type === 'PATIENT') {

            this.terminoBusqueda = `${result.data.firstName} ${result.data.lastName}`;
            this.resultadosBusqueda = [];

            setTimeout(() => {
                if (this.actionSelect) {
                    this.actionSelect.nativeElement.focus();
                }
            }, 50);

            this.setPaciente(result.data);
        }
    }

    ejecutarAccionRapida() {
        if (!this.paciente) return;

        if (this.accionSeleccionada === 'PAQUETES') {
            this.iniciarVentaPaqueteConPaciente(this.paciente);
        } else {
            this.iniciarVentaDirecta(this.paciente);
        }
    }

    onTipoVentaChange() {
        if (!this.accionSeleccionada || this.accionSeleccionada === 'PAQUETES') return;

        if (!this.paciente) {
            this.alertasService.warning('Seleccione Paciente', 'Primero debe buscar y seleccionar un paciente.');
            this.accionSeleccionada = '';
            return;
        }

        this.iniciarVentaDirecta(this.paciente);
    }

    iniciarVentaPaqueteConPaciente(paciente: any) {

        this.paciente = paciente;
        this.irAPaquetes();
    }

    volverAlDashboard() {

        if (this.carrito.length > 0) {
            this.alertasService.confirm('¿Cancelar Venta?', 'Se perderán los items agregados al carrito', 'Sí, Salir', 'Continuar').then((confirmed) => {
                if (confirmed) {
                    this.resetVentaState();
                    this.currentView = 'CHECKOUT';
                    this.cd.detectChanges();
                }
            });
        } else {
            this.resetVentaState();
            this.currentView = 'CHECKOUT';
        }
    }

    iniciarVentaDirecta(paciente: any = null) {
        this.resetVentaState();
        if (paciente) {
            this.setPaciente(paciente);
        } else {

            this.alertasService.info('Seleccione Paciente', 'Busque al paciente en el dashboard primero.');
            return;
        }
        this.currentView = 'CHECKOUT';
    }



    irAPaquetes() {
        this.sidebarMode = this.sidebarMode === 'PAQUETES' ? 'PENDIENTES' : 'PAQUETES';
        if (this.sidebarMode === 'PAQUETES' && this.paquetesDisponibles.length === 0) {
            this.isLoading = true;
            this.paqueteService.listarPaquetesActivos().subscribe({
                next: (paquetes) => {
                    this.paquetesDisponibles = paquetes;
                    this.isLoading = false;
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error actualizando paquetes', err);
                    this.isLoading = false;
                }
            });
        }
    }

    calcularPrecioRealPaquete(pkg: any): number {
        if (pkg.precioReal && pkg.precioReal > 0) return pkg.precioReal;
        if (pkg.descuento && pkg.descuento > 0) {
            return Number(pkg.precioPaquete) + Number(pkg.descuento);
        }
        if (pkg.servicios && pkg.servicios.length > 0) {
            return pkg.servicios.reduce((acc: number, s: any) => {
                let price = Number(s.precio || s.precioReal || s.precioVenta || 0);
                if (s.descuentoMonto && s.descuentoMonto > 0) {
                    price += Number(s.descuentoMonto);
                } else if (s.descuentoPorcentaje && s.descuentoPorcentaje > 0) {
                    if (s.descuentoPorcentaje < 100) {
                        price = price / (1 - (s.descuentoPorcentaje / 100));
                    }
                }
                return acc + price;
            }, 0);
        }
        return pkg.precioPaquete * 1.3;
    }

    toggleDetallePaquete(pkg: any, event: Event) {
        event.stopPropagation();
        pkg.expanded = !pkg.expanded;
        this.cd.detectChanges();
    }

    getPrecioFinalServicioPaquete(s: any): number {
        // Backend DTO returns 'precioFinal' for the total discounted price of that item line.
        // If not present, fallback to precioUnitario * cantidad.
        if (s.precioFinal !== undefined && s.precioFinal !== null) {
            return Number(s.precioFinal);
        }
        const unit = Number(s.precioUnitario || s.precio || s.precioReal || 0);
        const qty = Number(s.cantidad || 1);
        const descMonto = Number(s.descuentoMonto || 0);
        return (unit * qty) - descMonto;
    }

    getPrecioRealServicioPaquete(s: any): number {
        // The true real price without discounts for the given quantity is unitPrice * qty
        const unit = Number(s.precioUnitario || s.precio || s.precioReal || 0);
        const qty = Number(s.cantidad || 1);
        return unit * qty;
    }

    getSumaPreciosFinalesPaquete(pkg: any): number {
        if (!pkg || !pkg.servicios) return pkg.precioPaquete || 0;
        return pkg.servicios.reduce((acc: number, s: any) => acc + (this.getPrecioFinalServicioPaquete(s) * (s.cantidad || 1)), 0);
    }

    getSumaPreciosRealesPaquete(pkg: any): number {
        if (!pkg || !pkg.servicios) return pkg.precioPaquete || 0;
        return pkg.servicios.reduce((acc: number, s: any) => acc + (this.getPrecioRealServicioPaquete(s) * (s.cantidad || 1)), 0);
    }

    onPaqueteSidebarClick(item: any) {
        if (this.paciente) {
            this.iniciarVentaPaqueteSeleccionado(item, this.paciente);
        } else {
            this.alertasService.warning('Seleccione Paciente', 'Debe buscar y seleccionar un paciente para vender el paquete.');
        }
    }

    iniciarVentaPaqueteSeleccionado(paquete: any, paciente: any) {
        this.paciente = paciente;
        this.ordenSeleccionada = null; // Es venta nueva
        this.carrito = paquete.servicios.map((s: any) => {
            const qty = Number(s.cantidad || 1);
            const rawPrecioFinal = s.precioFinal !== undefined ? Number(s.precioFinal) : Number(s.precio || s.precioUnitario || 0);
            const unitPrecioFinal = rawPrecioFinal / qty;
            const total = rawPrecioFinal; // s.precioFinal effectively contains the total for the line in many DTOs, or we ensure it here

            return {
                ...s,
                id: s.idServicio, // CRITICAL: Backend expects 'idArticulo', frontend maps from 'item.id'
                idServicio: s.idServicio,
                nombre: s.nombreServicio || s.nombre,
                precioUnitario: s.precioUnitario || s.precioReal || s.precio,
                precioFinal: unitPrecioFinal,
                cantidad: qty,
                total: total, // CRITICAL: Ensure 'total' is present for the calculation
                descuentoAplicado: s.descuentoMonto || 0,
                nivelNombre: 'PAQUETES',
                selected: true
            };
        });
        this.cacheBeneficios = [];
        this.tipoCliente = 'PARTICULAR';
        this.calcularTotal();
        this.cd.detectChanges();
    }

    setPaciente(p: any) {
        if (!p) return;
        this.paciente = p;
        this.manualConvenioId = null; 
        console.log('🔍 [ANALISIS] Paciente seleccionado:', p);

        // 1. Verificación Activa de Cuentas de Convenio (Inteligencia de Negocio)
        this.credCtaCteService.listarPorPaciente(p.id).subscribe({
            next: (cuentas) => {
                const cuentaActiva = cuentas.find(c => c.estado === 'ACTIVO');
                
                if (cuentaActiva) {
                    this.triggerConvenioDetectado(cuentaActiva);
                } else if (p.numdoc) {
                    // FALLBACK: Búsqueda por DNI (Caso de duplicados o Shadow Patients)
                    console.info('⚠️ No se halló por ID, intentando vinculación por DNI:', p.numdoc);
                    this.credCtaCteService.listarPorDni(p.numdoc).subscribe({
                        next: (cuentasDni) => {
                            const cuentaDniActiva = cuentasDni.find(c => c.estado === 'ACTIVO');
                            if (cuentaDniActiva) {
                                console.log('✅ Vinculación recuperada por DNI!');
                                this.triggerConvenioDetectado(cuentaDniActiva);
                            } else {
                                this.aplicarLogicaTradicionalPaciente(p);
                            }
                        },
                        error: () => this.aplicarLogicaTradicionalPaciente(p)
                    });
                } else {
                    this.aplicarLogicaTradicionalPaciente(p);
                }
            },
            error: (err) => {
                console.error('❌ Error verificando convenios', err);
                this.aplicarLogicaTradicionalPaciente(p);
            }
        });
    }

    private triggerConvenioDetectado(cuenta: any) {
        this.tipoCliente = 'CONVENIO';
        
        // Use Plan ID if available, otherwise try to recover it from the code (RUBEN's case)
        if (cuenta.idPlan) {
            this.manualConvenioId = cuenta.idPlan;
            console.log('✅ [AUTO-DETECT] Convenio activo (ID):', cuenta.nombrePlan);
            this.mostrarAlertaYRefrescar(cuenta);
        } else if (cuenta.codigoPlan) {
            console.warn('⚠️ ID de Plan ausente, intentando recuperación por código:', cuenta.codigoPlan);
            this.convenioService.obtenerPorCodigo(cuenta.codigoPlan).subscribe({
                next: (plan) => {
                    if (plan && plan.idPlan) {
                        this.manualConvenioId = plan.idPlan;
                        console.log('✅ [RECOVERY] ID de Plan recuperado:', plan.idPlan);
                    } else {
                        this.manualConvenioId = cuenta.idCompania;
                    }
                    this.mostrarAlertaYRefrescar(cuenta);
                },
                error: () => {
                    this.manualConvenioId = cuenta.idCompania;
                    this.mostrarAlertaYRefrescar(cuenta);
                }
            });
        } else {
            this.manualConvenioId = cuenta.idCompania;
            this.mostrarAlertaYRefrescar(cuenta);
        }
    }

    private mostrarAlertaYRefrescar(cuenta: any) {
        this.alertasService.info('Convenio Detectado', `Paciente vinculado a: ${cuenta.nombrePlan || cuenta.companiaNombre}`);
        this.cd.detectChanges();
    }

    private aplicarLogicaTradicionalPaciente(p: any) {
        if (p.esFamiliar) {
            this.tipoCliente = 'DESCUENTOS';
            console.log('Cliente detectado como FAMILIAR DE TRABAJADOR');
            this.loadCacheBeneficios();
        }
        else if (p.idEmpresa != null) {
            this.tipoCliente = 'DESCUENTOS';
            console.log('Cliente detectado como TRABAJADOR DE EMPRESA');
            this.loadCacheBeneficios();
        }
        else if (p.idCompania) {
            this.tipoCliente = 'CONVENIO';
            this.manualConvenioId = p.idCompania;
            console.log('Cliente detectado como CONVENIO (Directo), idCompania:', p.idCompania);
            this.cacheBeneficios = [];
            this.loadCacheConvenios();
        }
        else {
            this.tipoCliente = 'PARTICULAR';
            console.log('Cliente detectado como PARTICULAR');
            this.cacheBeneficios = [];
            this.loadCacheServicios();
        }
        this.cd.detectChanges();
    }



    agregarItemAlCarrito(item: any) {
        this.carrito.push({ ...item, cantidad: 1, total: Number(item.precioFinal) });
        this.calcularTotal();
    }

    removerItem(index: number) {
        this.carrito.splice(index, 1);
        this.calcularTotal();
    }

    calcularTotal() {
        this.totalVenta = this.carrito
            .filter(i => i.selected)
            .reduce((acc, i) => {
                const itemTotal = i.total !== undefined ? Number(i.total) : (Number(i.precioFinal || 0) * Number(i.cantidad || 1));
                return acc + itemTotal;
            }, 0);
    }

    onProcesarPago(event: any) {
        if (!this.paciente) {
            this.alertasService.error('Error', 'No hay paciente seleccionado');
            return;
        }

        if (this.carrito.length === 0) {
            this.alertasService.error('Error', 'El carrito está vacío');
            return;
        }

        this.cajaChicaService.obtenerCajaAbierta().subscribe({
            next: (res) => {
                if (!res.data) {
                    this.alertasService.error(
                        'Caja cerrada',
                        'Debes abrir la caja en "Ingresos y Gastos" antes de procesar ventas.'
                    );
                    return;
                }
                this.ejecutarPago(event);
            },
            error: () => {
                this.alertasService.error('Error', 'No se pudo verificar el estado de la caja.');
            }
        });
    }

    private ejecutarPago(event: any) {
        const metodoPago = event?.metodo || 'EFECTIVO';
        const formatoImpresion = event?.formatoImpresion || 'TICKET_80';
        const infoPago = event?.info;
        const tipoComprobante = event?.tipoComprobante || 'BOLETA';
        const datosFactura = event?.datosFactura;

        this.isSaving = true;

        const idUser = this.currentUserId;
        const idSucursal = this.currentSucursalId;
        const idAlmacen = 1;

        const itemsVenta = this.carrito
            .filter(item => item.selected)
            .map(item => {
                let precioOriginal = item.precioOriginal && item.precioOriginal > 0 ? item.precioOriginal : item.precioFinal;
                let porcentajeDesc = 0;

                if (item.precioOriginal > item.precioFinal) {
                    porcentajeDesc = ((item.precioOriginal - item.precioFinal) / item.precioOriginal) * 100;
                    porcentajeDesc = Math.round(porcentajeDesc * 100) / 100;
                }

                return {
                    idArticulo: item.idArticulo || item.id,
                    tipoArticulo: (item.isPackageItem || (item.tipo === 'PAQUETE' && item.serviciosDetalle && item.serviciosDetalle.length > 0)) ? 'PAQUETE' : 'SERVICIO',
                    glosa: (item.nombre || item.glosa || item.nombreServicio || item.nombreExamen || 'Servicio').trim(),
                    cantidad: item.cantidad,
                    precioUnitario: precioOriginal,
                    porcentajeDescuento: porcentajeDesc,
                    porcentajeCobertura: 0,
                    idMedicoSer: undefined,
                    consultorio: undefined
                };
            });

        const isOrdenCobro = !!this.ordenSeleccionada?.idOrden;

        if (isOrdenCobro) {

            const payload = {
                metodoPago: metodoPago,
                montoEfectivo: infoPago?.montoEfectivo,
                montoTarjeta: infoPago?.montoTarjeta,
                montoTransferencia: infoPago?.montoTransferencia,
                refTarjeta: infoPago?.refTarjeta,
                refTransferencia: infoPago?.refTransferencia,
                vuelto: infoPago?.vuelto || 0,
                nroDni: this.paciente?.numdoc,
                tipoDni: this.paciente?.tipoDoc || '1',
                refMetodoPago: '',
                tipoDoc: tipoComprobante === 'BOLETA' ? 'BOLETA' : 'FACTURA',
                serie: tipoComprobante === 'BOLETA' ? 'B001' : 'F001',
                numdoc: '00000000',
                nombrePac: this.paciente.firstName + ' ' + (this.paciente.lastName || ''),
                numdocPac: this.paciente.numdoc,
                ruc: datosFactura?.ruc,
                razon: datosFactura?.razonSocial,
                direcRuc: datosFactura?.direccionRuc,
                idSucursal: this.currentSucursalId,
                idPersonal: this.currentUserId,
                items: itemsVenta,
                tipoAtencion: 'LABORATORIO',
                idAutorizacionDescuento: event.idAutorizacionDescuento
            };


            this.ventaService.pagarOrden(this.ordenSeleccionada.idOrden, payload).subscribe({
                next: (res) => this.handleVentaSuccess(res, formatoImpresion),
                error: (err) => this.handleVentaError(err)
            });

        } else {

            const nuevaVenta = {
                idPaciente: this.paciente.id,
                idCtacte: this.paciente.id,
                tipoCliente: this.tipoCliente,
                tipoPac: this.paciente.tipoPac || (this.tipoCliente === 'CONVENIO' ? 'C' : 'P'),
                idPlan: this.manualConvenioId,
                items: itemsVenta,
                metodoPago: metodoPago,
                montoEfectivo: infoPago?.montoEfectivo,
                montoTarjeta: infoPago?.montoTarjeta,
                montoTransferencia: infoPago?.montoTransferencia,
                refTarjeta: infoPago?.refTarjeta,
                refTransferencia: infoPago?.refTransferencia,
                vuelto: infoPago?.vuelto || 0,
                nroDni: this.paciente?.numdoc,
                tipoDni: this.paciente?.tipoDoc || '1',
                refMetodoPago: '',
                tipoComprobante: tipoComprobante as 'BOLETA' | 'FACTURA',
                idPersonal: idUser,
                idSucursal: idSucursal,
                idAlmacen: idAlmacen,
                fecha: this.getTodayLocal(),

                tipoDoc: tipoComprobante === 'BOLETA' ? 'BOLETA' : 'FACTURA',
                serie: tipoComprobante === 'BOLETA' ? 'B001' : 'F001',
                numdoc: 'GENERATED',

                ruc: datosFactura?.ruc,
                razon: datosFactura?.razonSocial,
                direcRuc: datosFactura?.direccionRuc,

                nombrePac: this.paciente.firstName + ' ' + this.paciente.lastName,
                aplicarDescuentoAutomatico: true,
                idAutorizacionDescuento: event.idAutorizacionDescuento
            };


            this.ventaService.crearVenta(nuevaVenta).subscribe({
                next: (res) => this.handleVentaSuccess(res, formatoImpresion),
                error: (err) => this.handleVentaError(err)
            });
        }
    }

    private handleVentaSuccess(res: any, formatoImpresion: string) {
        this.isSaving = false;
        const msg = res.message || 'La venta se registró correctamente.';
        const idVenta = res.idVenta || res.data?.idVenta || '000';
        const isInsurance = this.tipoCliente === 'CONVENIO' || !!this.manualConvenioId;

        this.alertasService.success('¡Venta Exitosa!', `
            <div>${msg}</div>
            <div class="mt-4 p-4 rounded-3xl bg-indigo-50 text-indigo-700 border border-indigo-100 flex items-center justify-center gap-3">
                <i class="fas fa-print fa-spin"></i>
                <span class="text-xs font-bold uppercase tracking-widest">Imprimiendo en <b>${formatoImpresion}</b></span>
            </div>
            ${isInsurance ? '<div class="mt-2 text-[10px] text-indigo-400 font-bold uppercase">(Generando Copia Paciente + Voucher Cobertura)</div>' : ''}
        `);

        // Move these changes to the next tick to avoid NG0100
        setTimeout(() => {
            this.triggerPrint(idVenta, formatoImpresion);
            this.resetVentaState();
            this.currentView = 'CHECKOUT';
            this.loadDashboardData();
            this.cd.detectChanges();
        }, 3000); // 3 seconds to match the alert duration
    }

    private triggerPrint(idVenta: string | number, formato: string) {
        const isInsurance = this.tipoCliente === 'CONVENIO' || !!this.manualConvenioId;
        console.log(`🖨️ IMPRIMIENDO VENTA #${idVenta} EN FORMATO: ${formato}`);
        if (isInsurance) {
            console.log(`📋 GENERANDO DOBLE COPIA: PACIENTE + VOUCHER COBERTURA`);
        }
    }

    private handleVentaError(err: any) {
        this.isSaving = false;
        console.error('Error procesando venta:', err);
        let errorMsg = 'No se pudo registrar la venta. Intente nuevamente.';

        if (err.error && err.error.message) {
            errorMsg = err.error.message;

            // If it's a validation error, try to show details
            if (err.error.data && typeof err.error.data === 'object') {
                const details = Object.entries(err.error.data)
                    .map(([field, msg]) => `• <b>${field}</b>: ${msg}`)
                    .join('<br>');
                errorMsg += `<div class="mt-4 text-left text-xs bg-red-50 p-2 rounded border border-red-100">${details}</div>`;
            }
        }

        this.alertasService.error('Error en Venta', errorMsg);
    }

    onPaqueteSeleccionado(event: { paquete: any, paciente: any }) {
        const { paquete: pkg, paciente: patient } = event;

        if (!this.paciente || this.paciente.id !== patient.id) {
            this.resetVentaState();
            this.setPaciente(patient);
        }

        this.currentView = 'CHECKOUT';

        if (pkg) {
            this.paqueteService.obtenerPaquete(pkg.idPaquete || pkg.id).subscribe({
                next: (fullPkg: any) => {
                    const mappedServices = (fullPkg.servicios || []).map((s: any) => ({
                        nombre: s.nombreServicio || s.nombre,
                        cantidad: s.cantidad,
                        precioUnitario: s.precioUnitario || s.precio || 0
                    }));

                    this.agregarItemAlCarrito({
                        id: fullPkg.idPaquete || fullPkg.id,
                        nombre: `PAQUETE: ${fullPkg.nombre}`,
                        precioFinal: fullPkg.precioPaquete,
                        total: fullPkg.precioPaquete,
                        tipo: 'PAQUETE',
                        isPackageItem: true,
                        serviciosDetalle: mappedServices
                    });
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error cargando detalle del paquete:', err);

                    this.agregarItemAlCarrito({
                        id: pkg.idPaquete || pkg.id,
                        nombre: `PAQUETE: ${pkg.nombre}`,
                        precioFinal: pkg.precioPaquete,
                        total: pkg.precioPaquete,
                        tipo: 'PAQUETE',
                        isPackageItem: true
                    });
                }
            });
        }
    }

    loadCacheServicios() {
        console.log('📋 Cargando catálogo de servicios para cliente particular');

    }

    loadCacheBeneficios() {

        console.log('🎁 Cargando beneficios especiales para trabajador/familiar');

        if (this.paciente && this.paciente.idTipoPaciente) {
            this.fetchBeneficios(this.paciente.idTipoPaciente);
        }

        else if (this.paciente && this.paciente.esFamiliar) {
            console.log('⚠️ Paciente familiar sin idTipoPaciente, buscando configuración por defecto...');
            this.descuentoService.obtenerConfiguracionFamiliar().subscribe({
                next: (config) => {
                    if (config && config.idTipoPaciente) {
                        console.log('✅ Configuración familiar encontrada, ID:', config.idTipoPaciente);
                        this.paciente.idTipoPaciente = config.idTipoPaciente;
                        this.fetchBeneficios(config.idTipoPaciente);
                    } else {
                        console.warn('❌ No se encontró configuración de familia por defecto');
                    }
                },
                error: (err) => console.error('Error buscando config familiar:', err)
            });
        }
        else {
            console.warn('No se puede cargar beneficios: paciente sin idTipoPaciente explícito');
            return;
        }
    }

    private fetchBeneficios(idTipoPaciente: number) {
        this.descuentoService.getServiciosConDescuentosEspeciales(idTipoPaciente)
            .subscribe({
                next: (servicios) => {
                    this.cacheBeneficios = servicios;
                    console.log(`✅ Cargados ${servicios.length} servicios con descuentos especiales`);
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error al cargar beneficios especiales:', err);
                    this.cacheBeneficios = [];
                }
            });
    }

    loadCacheConvenios() {

        console.log('🏥 Cargando precios de convenio');

    }

    resetVentaState() {
        this.paciente = null;
        this.ordenSeleccionada = null;
        this.carrito = [];
        this.totalVenta = 0;
        this.descuentoManual = 0;
        this.terminoBusqueda = '';
        this.resultadosBusqueda = [];
    }

    @ViewChild('modalRegistroPaciente') modalRegistroPaciente!: RegistrarPacienteModalComponent;

    abrirModalRegistroPaciente() {
        this.modalRegistroPaciente?.abrir();
    }

    onPacienteRegistrado(paciente: any) {
        console.log('Paciente registrado:', paciente);

    }

    onCerrarModal() {

    }

    getTipoPacienteLabel(orden: any): string {
        if (orden.pacienteEsFamiliar) return 'FAMILIAR / ESPECIAL';
        if (orden.pacienteIdEmpresa) return 'ESPECIAL'; // Alternative for company worker
        if (orden.pacienteIdCompania) return 'CONVENIO';
        return 'PARTICULAR';
    }

    getTipoPacienteClass(orden: any): string {
        const type = this.getTipoPacienteLabel(orden);
        switch (type) {
            case 'FAMILIAR / ESPECIAL': return 'bg-blue-100 text-blue-700 border-blue-200'; // BLUE as requested
            case 'ESPECIAL': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'CONVENIO': return 'bg-orange-100 text-orange-700 border-orange-200';
            case 'PARTICULAR': return 'bg-green-100 text-green-700 border-green-200'; // GREEN as requested
            default: return 'bg-slate-100 text-slate-600 border-slate-200';
        }
    }

    // Refactored to ensure correct state loading
    cargarOrdenParaCobro(orden: OrdenPendienteDTO) {
        this.resetVentaState();
        this.ordenSeleccionada = orden;
        this.isLoading = true;

        if (orden.idPaciente) {
            this.userService.getUserById(orden.idPaciente).subscribe({
                next: (response: any) => {
                    // Extract user data from response (ApiResponse wrapper)
                    const p = response.data || response;

                    // Force-inject info from order if user fetched is generic or missing
                    const useOrderName = !p.firstName || p.firstName === 'RIMAC' || p.firstName === '---';
                    const pacienteCompleto = {
                        ...p,
                        firstName: useOrderName ? (orden.pacienteNombre || p.firstName) : p.firstName,
                        lastName: useOrderName ? '' : (p.lastName || ''),
                        numdoc: p.numdoc || orden.pacienteDoc,
                        esFamiliar: orden.pacienteEsFamiliar || p.esFamiliar,
                        idEmpresa: orden.pacienteIdEmpresa || p.idEmpresa,
                        idCompania: orden.pacienteIdCompania || p.idCompania
                    };

                    // 1. Set Paciente (Determines Tipo Cliente)
                    this.setPaciente(pacienteCompleto);

                    // 2. Load Benefits/Agreements ASYNC, then Import Details
                    if (this.tipoCliente === 'DESCUENTOS') {
                        // We need to wait for benefits to load to apply prices
                        this.cargarBeneficiosYProceder(orden);
                    } else if (this.tipoCliente === 'CONVENIO') {
                        // Same for convenios if needed, or just proceed
                        this.loadCacheConvenios(); // Sync for now or straightforward
                        this.importarDetallesOrden(orden);
                        this.finalizarCargaOrden();
                    } else {
                        // Particular
                        this.importarDetallesOrden(orden);
                        this.finalizarCargaOrden();
                    }
                },
                error: (err) => {
                    console.error("Error fetching patient", err);
                    // Fallback to minimal patient info from order
                    const pacienteFallback = {
                        id: orden.idPaciente,
                        firstName: orden.pacienteNombre,
                        lastName: '',
                        numdoc: orden.pacienteDoc, // Add document number from order
                        // Try to use flags despite error
                        esFamiliar: orden.pacienteEsFamiliar,
                        idEmpresa: orden.pacienteIdEmpresa,
                        idCompania: orden.pacienteIdCompania
                    };
                    this.setPaciente(pacienteFallback);

                    // ERROR HANDLER ENHANCED: Try to load benefits if familiar
                    if (pacienteFallback.esFamiliar) {
                        this.tryLoadFamiliarBenefits(orden);
                    } else {
                        this.importarDetallesOrden(orden);
                        this.finalizarCargaOrden();
                    }
                }
            });
        } else {
            console.warn("Orden sin ID de paciente");
            const pacienteAnon = {
                firstName: orden.pacienteNombre,
                lastName: '',
                numdoc: orden.pacienteDoc
            };
            this.setPaciente(pacienteAnon);
            this.importarDetallesOrden(orden);
            this.finalizarCargaOrden();
        }
    }

    private cargarBeneficiosYProceder(orden: any) {
        // Logic similar to loadCacheBeneficios but returns Observable or executes callback
        // Re-using logic but making sure we wait
        let idTipo = this.paciente?.idTipoPaciente;

        // If missing ID but is Familiar, fetch config first
        if (!idTipo && this.paciente?.esFamiliar) {
            this.descuentoService.obtenerConfiguracionFamiliar().subscribe({
                next: (config) => {
                    if (config?.idTipoPaciente) {
                        this.paciente.idTipoPaciente = config.idTipoPaciente;
                        this.fetchBeneficiosAndImport(config.idTipoPaciente, orden);
                    } else {
                        // Failed config, proceed with base prices
                        this.importarDetallesOrden(orden);
                        this.finalizarCargaOrden();
                    }
                },
                error: () => {
                    this.importarDetallesOrden(orden);
                    this.finalizarCargaOrden();
                }
            });
        } else if (idTipo) {
            this.fetchBeneficiosAndImport(idTipo, orden);
        } else {
            // No info to load benefits
            this.importarDetallesOrden(orden);
            this.finalizarCargaOrden();
        }
    }

    private fetchBeneficiosAndImport(idTipo: number, orden: any) {
        this.descuentoService.getServiciosConDescuentosEspeciales(idTipo).subscribe({
            next: (servicios) => {
                this.cacheBeneficios = servicios;
                console.log(`✅ Beneficios cargados para orden: ${servicios.length}`);
                this.importarDetallesOrden(orden);
                this.finalizarCargaOrden();
            },
            error: (err) => {
                console.error("Error loading benefits for order", err);
                this.importarDetallesOrden(orden);
                this.finalizarCargaOrden();
            }
        });
    }

    private finalizarCargaOrden() {
        this.currentView = 'CHECKOUT';
        this.isLoading = false;
        this.cd.detectChanges();
    }

    // Helper to fallback logic
    private tryLoadFamiliarBenefits(orden: any) {
        console.log("⚠️ Intentando cargar beneficios por defecto para Familiar...");
        this.descuentoService.obtenerConfiguracionFamiliar().subscribe({
            next: (config) => {
                if (config && config.idTipoPaciente) {
                    console.log(`✅ Configuración Familiar encontrada: ${config.nombre} (ID: ${config.idTipoPaciente})`);
                    // Update patient ID so modal knows
                    if (this.paciente) {
                        this.paciente.idTipoPaciente = config.idTipoPaciente;
                    }
                    this.fetchBeneficiosAndImport(config.idTipoPaciente, orden);
                } else {
                    console.warn("⚠️ No se encontró configuración 'PERSONAL_REUMA'");
                    this.importarDetallesOrden(orden);
                    this.finalizarCargaOrden();
                }
            },
            error: (err) => {
                console.error("Error cargando config familiar", err);
                this.importarDetallesOrden(orden);
                this.finalizarCargaOrden();
            }
        });
    }

    importarDetallesOrden(orden: any) {
        if (!orden.detalles) return;

        console.log('📦 Importando detalles. Beneficios en cache:', this.cacheBeneficios);

        orden.detalles.forEach((d: any) => {
            let precioAUsar = d.precio || 0;
            let originalPrice = null;
            let glosa = d.nombre || d.glosa || d.nombreServicio || d.nombreExamen || 'Servicio';
            let isBeneficio = false;

            // Check if we have a special price in cache
            if (this.cacheBeneficios.length > 0) {
                // Convert to string for safe comparison
                const beneficio = this.cacheBeneficios.find(b => String(b.idServicio) === String(d.idExamen));

                if (beneficio) {
                    console.log(`🎯 MATCH! Servicio ${d.nombre} (ID: ${d.idExamen}). Precio Org: ${precioAUsar} -> Desc: ${beneficio.precioFinal}`);
                    originalPrice = precioAUsar;
                    precioAUsar = beneficio.precioFinal;
                    isBeneficio = true;
                } else {
                    console.log(`⚠️ NO MATCH for ${d.nombre} (ID: ${d.idExamen}). IDs available:`, this.cacheBeneficios.map(b => b.idServicio));
                }
            } else {
                console.log('ℹ️ No hay beneficios en cache para aplicar.');
            }

            this.agregarItemAlCarrito({
                id: d.idExamen,
                codigo: d.codigo,
                nombre: glosa,
                precioFinal: precioAUsar,
                precioOriginal: originalPrice || d.precioReal || precioAUsar,
                // Fix for CobrarModal which expects precioReal and isBeneficio
                precioReal: originalPrice || d.precioReal || precioAUsar,
                isBeneficio: isBeneficio,
                total: Number(precioAUsar),
                fromOrden: true,
                selected: true
            });
        });
    }
}

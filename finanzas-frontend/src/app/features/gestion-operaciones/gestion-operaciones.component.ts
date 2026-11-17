import { Component, OnInit, inject, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaGeneralService, CajaGeneralResponse } from '../../services/caja-general.service';
import { CuentaCorriente, CuentaCorrienteService } from '../../services/cuenta-corriente.service';
import { TransferenciaDTO, MovimientoDTO } from '../../models/finanza.model';
import { AuthService } from '../auth/services/auth.service';
import { ModalOperacionComponent } from './modal-operacion/modal-operacion.component';
import { ModalTransferenciaComponent } from './modal-transferencia/modal-transferencia.component';
import { ModalDetalleComponent } from './detalle-modal/modal-detalle.component';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { AlertasService } from '../../services/alertas.service';
import { forkJoin, catchError, of } from 'rxjs';

@Component({
    selector: 'app-gestion-operaciones',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        FormsModule,
        BreadcrumbComponent,
        PageHeaderComponent,
        PrimaryButtonComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BarraFiltrosComponent,
        ModalTransferenciaComponent,
        ModalOperacionComponent,
        ModalDetalleComponent
    ],
    templateUrl: './gestion-operaciones.component.html',
    styleUrls: ['./gestion-operaciones.component.css']
})
export class GestionOperacionesComponent implements OnInit {
    private cajaGeneralService = inject(CajaGeneralService);
    private cuentaCorrienteService = inject(CuentaCorrienteService);
    private authService = inject(AuthService);
    private alertasService = inject(AlertasService);
    private cd = inject(ChangeDetectorRef);

    cuentas: CuentaCorriente[] = []; // Usaremos esta interfaz para que los modales no se rompan
    cajaGeneralData: CajaGeneralResponse | null = null;
    
    isLoading = false;
    isLoadingHistorial = false;
    showTransferModal = false;
    currentSucursalId: number = 0;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja General', route: '/admin/caja-general' },
        { label: 'Gestión de Operaciones' }
    ];

    movimientosTabla: any[] = [];
    columnasTabla: Columna[] = [
        { field: 'index', header: '#', tipo: 'index', subField: [] },
        { field: 'fechaMovimiento', header: 'Fecha', tipo: 'date', subField: [] },
        { field: 'numeroComprobante', header: 'Ref / Doc', tipo: 'text', subField: [] },
        { field: 'banco', header: 'Método de Pago', tipo: 'text', subField: [] },
        { field: 'concepto', header: 'Descripción', tipo: 'text', subField: [] },
        { field: 'salida', header: 'Salida', tipo: 'currency-color', subField: ['moneda'] },
        { field: 'ingreso', header: 'Ingreso', tipo: 'currency-color', subField: ['moneda'] },
        { field: 'usuario', header: 'Usuario', tipo: 'text', subField: [] }
    ];

    transferencia: TransferenciaDTO = {
        idCuentaOrigen: 0,
        idCuentaDestino: 0,
        monto: 0,
        referencia: '',
        idUsuario: 0,
        idSucursal: 0,
        descripcion: ''
    };

    historialTransferencias: any[] = [];
    fechaInicioHistorial: string = '';
    fechaFinHistorial: string = '';

    currentPage: number = 0;
    pageSize: number = 10;
    totalPages: number = 0;
    totalElements: number = 0;

    searchQuery: string = '';
    searchType: string = 'ALL';
    searchOptions = [
        { label: 'Todos', value: 'ALL' },
        { label: 'Concepto', value: 'CONCEPTO' },
        { label: 'Referencia', value: 'COMPROBANTE' },
        { label: 'Método Pago', value: 'BANCO' }
    ];

    estadoOptions = [
        { label: 'TODOS', value: 'TODOS' },
        { label: 'INGRESO', value: 'INGRESO' },
        { label: 'EGRESO', value: 'EGRESO' }
    ];

    showOperacionModal = false;
    movimiento: MovimientoDTO = {
        tipo: 'INGRESO',
        monto: 0,
        concepto: '',
        idUsuario: 0,
        idSucursal: 0,
        idCuenta: 0,
        metodoPago: 'EFECTIVO',
        tipoComprobante: 'RECIBO',
        numeroComprobante: '',
        beneficiario: ''
    };

    showDetailModal = false;
    selectedMovimiento: any = null;

    ngOnInit() {
        this.initLoadingSequence();
    }

    private getSucursalId(): number {
        const user = this.authService.currentUser();
        if (user?.sucursalActualId) return user.sucursalActualId;

        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            try {
                const userObj = JSON.parse(userStr);
                return userObj.sucursalActualId || 1;
            } catch {
                return 1;
            }
        }
        return 1;
    }

    private initLoadingSequence() {
        this.currentSucursalId = this.getSucursalId();
        if (!this.currentSucursalId) return;
        this.loadDataParalela();
    }

    private loadDataParalela() {
        this.isLoadingHistorial = true;
        this.cd.markForCheck();

        const cajaGeneral$ = this.cajaGeneralService.obtenerPorSucursal(this.currentSucursalId).pipe(
            catchError(() => of({ data: { saldosPorMetodo: [] } } as any))
        );

        const movimientos$ = this.cajaGeneralService.listarMovimientos(
            this.currentSucursalId,
            undefined, // no filtramos por método desde el backend por ahora
            this.currentPage,
            this.pageSize
        ).pipe(
            catchError(() => of({ data: { content: [], totalPages: 0, totalElements: 0 } } as any))
        );

        const cuentas$ = this.cuentaCorrienteService.getActive().pipe(
            catchError(() => of([]))
        );

        forkJoin([cajaGeneral$, movimientos$, cuentas$]).subscribe({
            next: ([cajaResponse, movimientosResponse, cuentasReal]: [any, any, CuentaCorriente[]]) => {
                this.cajaGeneralData = cajaResponse.data;
                
                // 1. Mapear saldos por método de Caja General
                const cuentasMetodo = (this.cajaGeneralData?.saldosPorMetodo || []).map((s: any, index: number) => ({
                    idCuenta: (index + 1) * -1, // IDs negativos para distinguir de cuentas reales
                    alias: s.metodoPago,
                    banco: 'Caja General',
                    nombreTitular: s.metodoPago,
                    saldo: s.saldo,
                    moneda: 'PEN',
                    numeroCuenta: '-',
                    estado: 'A'
                }));

                // 2. Combinar con cuentas corrientes reales
                this.cuentas = [...cuentasMetodo, ...cuentasReal];

                const page = movimientosResponse.data || { content: [], totalPages: 0, totalElements: 0 };
                this.totalPages = page.totalPages || 0;
                this.totalElements = page.totalElements || 0;
                this.historialTransferencias = page.content || [];

                this.procesarMovimientosParaTabla();

                this.isLoadingHistorial = false;
                this.cd.markForCheck();
            },
            error: () => {
                this.isLoadingHistorial = false;
                this.cd.markForCheck();
            }
        });
    }

    loadHistorialTransferencias() {
        if (!this.currentSucursalId) return;

        this.isLoadingHistorial = true;
        this.cd.markForCheck();

        this.cajaGeneralService.listarMovimientos(
            this.currentSucursalId,
            undefined,
            this.currentPage,
            this.pageSize
        ).pipe(
            catchError(() => of({ data: { content: [], totalPages: 0, totalElements: 0 } } as any))
        ).subscribe({
            next: (response: any) => {
                const page = response.data || { content: [], totalPages: 0, totalElements: 0 };
                this.totalPages = page.totalPages || 0;
                this.totalElements = page.totalElements || 0;
                this.historialTransferencias = page.content || [];

                this.procesarMovimientosParaTabla();
                this.isLoadingHistorial = false;
                this.cd.markForCheck();
            },
            error: () => {
                this.isLoadingHistorial = false;
                this.cd.markForCheck();
            }
        });
    }

    private procesarMovimientosParaTabla() {
        const movimientosBasicos = this.historialTransferencias.map(mov => {
            const isSalida = mov.tipo === 'EGRESO' || mov.tipo === 'TRANSFERENCIA_ENTRE_METODOS';
            const isIngreso = mov.tipo === 'INGRESO' || mov.tipo === 'TRANSFERENCIA_RECIBIDA';

            return {
                ...mov,
                fechaMovimiento: mov.fecha,
                salida: isSalida ? mov.monto : 0,
                ingreso: isIngreso ? mov.monto : 0,
                numeroComprobante: mov.referencia || '-',
                banco: mov.metodoPago,
                concepto: mov.descripcion,
                moneda: 'PEN',
                usuario: mov.usuario || '-',
                nombreCuentaActual: mov.metodoPagoOrigen || mov.metodoPago || '-',
                nombreCuentaVinculada: mov.metodoPagoDestino || '-'
            };
        });

        // Aplicar filtro local si existe
        if (this.searchQuery && this.searchQuery.trim() !== '') {
            const q = this.searchQuery.toLowerCase().trim();
            this.movimientosTabla = movimientosBasicos.filter(mov => {
                if (this.searchType === 'ALL') {
                    return mov.concepto?.toLowerCase().includes(q) ||
                        mov.numeroComprobante?.toLowerCase().includes(q) ||
                        mov.banco?.toLowerCase().includes(q);
                }
                if (this.searchType === 'CONCEPTO') return mov.concepto?.toLowerCase().includes(q);
                if (this.searchType === 'COMPROBANTE') return mov.numeroComprobante?.toLowerCase().includes(q);
                if (this.searchType === 'BANCO') return mov.banco?.toLowerCase().includes(q);
                return true;
            });
        } else {
            this.movimientosTabla = movimientosBasicos;
        }
    }

    onViewDetail(movimiento: any) {
        this.selectedMovimiento = movimiento;
        this.showDetailModal = true;
        this.cd.markForCheck();
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.loadHistorialTransferencias();
    }

    onPageSizeChange(size: number) {
        this.pageSize = size;
        this.currentPage = 0;
        this.loadHistorialTransferencias();
    }

    onFilter(criteria: FilterCriteria) {
        this.searchQuery = criteria.term || '';
        this.searchType = criteria.searchType || 'ALL';
        this.currentPage = 0;
        this.loadHistorialTransferencias();
    }

    openTransferModal(isCierreCaja: boolean = false) {
        const user = this.authService.currentUser();

        this.transferencia = {
            idCuentaOrigen: 0,
            idCuentaDestino: 0,
            monto: 0,
            referencia: '',
            idUsuario: user?.id || 0,
            idSucursal: this.currentSucursalId,
            descripcion: isCierreCaja ? 'CIERRE DE CAJA' : ''
        };

        this.showTransferModal = true;
        this.cd.markForCheck();
    }

    closeModal() {
        this.showTransferModal = false;
        this.cd.markForCheck();
    }

    onSubmitTransfer() {
        if (!this.isValidTransfer()) return;

        this.isLoading = true;
        this.cd.markForCheck();

        const origen = this.cuentas.find(c => c.idCuenta === this.transferencia.idCuentaOrigen);
        const destino = this.cuentas.find(c => c.idCuenta === this.transferencia.idCuentaDestino);

        const request: any = {
            idSucursal: this.currentSucursalId,
            tipo: 'TRANSFERENCIA_ENTRE_METODOS',
            monto: this.transferencia.monto,
            descripcion: this.transferencia.descripcion || 'Transferencia',
            referencia: this.transferencia.referencia || 'S/R',
            metodoPago: origen?.alias || 'EFECTIVO',
            metodoPagoOrigen: origen?.alias || 'EFECTIVO',
            metodoPagoDestino: destino?.alias || 'EFECTIVO',
            usuario: (this.authService.currentUser()?.id || 1).toString()
        };

        this.cajaGeneralService.registrarMovimiento(request).subscribe({
            next: () => {
                this.isLoading = false;
                this.closeModal();
                this.loadDataParalela();
                this.alertasService.success('¡Éxito!', 'La transferencia se completó correctamente.');
            },
            error: (err) => {
                this.alertasService.error(
                    'Error',
                    'No se pudo realizar la transferencia: ' + (err.error?.message || 'Error desconocido')
                );
                this.isLoading = false;
                this.cd.markForCheck();
            }
        });
    }

    isValidTransfer(): boolean {
        if (this.transferencia.idCuentaOrigen === this.transferencia.idCuentaDestino) {
            this.alertasService.warning('Atención', 'El origen y destino no pueden ser iguales.');
            return false;
        }
        if (this.transferencia.monto <= 0) {
            this.alertasService.warning('Atención', 'El monto debe ser mayor a 0.');
            return false;
        }

        const cuentaOrigen = this.cuentas.find(c => c.idCuenta === this.transferencia.idCuentaOrigen);

        if (cuentaOrigen && cuentaOrigen.saldo < this.transferencia.monto) {
            this.alertasService.error(
                'Saldo Insuficiente',
                `Disponible: S/ ${cuentaOrigen.saldo.toFixed(2)}\nSolicitado: S/ ${this.transferencia.monto.toFixed(2)}`
            );
            return false;
        }

        return true;
    }

    openOperacionModal() {
        const user = this.authService.currentUser();

        this.movimiento = {
            tipo: 'INGRESO',
            monto: 0,
            concepto: '',
            idUsuario: user?.id || 0,
            idSucursal: this.currentSucursalId,
            idCuenta: 0,
            metodoPago: 'EFECTIVO',
            tipoComprobante: 'RECIBO',
            numeroComprobante: '',
            beneficiario: ''
        } as any;

        this.showOperacionModal = true;
        this.cd.markForCheck();
    }

    closeOperacionModal() {
        this.showOperacionModal = false;
        this.cd.markForCheck();
    }

    onSubmitOperacion() {
        if (!this.isValidOperacion()) return;

        this.isLoading = true;
        this.cd.markForCheck();

        const cuenta = this.cuentas.find(c => c.idCuenta === this.movimiento.idCuenta);

        const request: any = {
            idSucursal: this.currentSucursalId,
            tipo: this.movimiento.tipo,
            monto: this.movimiento.monto,
            descripcion: this.movimiento.concepto,
            referencia: this.movimiento.numeroComprobante || this.movimiento.tipoComprobante || 'S/R',
            metodoPago: cuenta?.alias || 'EFECTIVO',
            usuario: (this.authService.currentUser()?.id || 1).toString()
        };

        this.cajaGeneralService.registrarMovimiento(request).subscribe({
            next: () => {
                this.isLoading = false;
                this.closeOperacionModal();
                this.loadDataParalela();
                this.alertasService.success('¡Éxito!', `${this.movimiento.tipo} registrado correctamente.`);
            },
            error: (err) => {
                this.alertasService.error(
                    'Error',
                    'No se pudo registrar la operación: ' + (err.error?.message || 'Error desconocido')
                );
                this.isLoading = false;
                this.cd.markForCheck();
            }
        });
    }

    isValidOperacion(): boolean {
        if (!this.movimiento.idCuenta || this.movimiento.idCuenta === 0) {
            this.alertasService.warning('Atención', 'Debe seleccionar un método de pago.');
            return false;
        }
        if (this.movimiento.monto <= 0) {
            this.alertasService.warning('Monto inválido', 'El monto debe ser mayor a 0.');
            return false;
        }
        if (!this.movimiento.concepto || this.movimiento.concepto.trim() === '') {
            this.alertasService.warning('Faltan datos', 'Debe ingresar un concepto.');
            return false;
        }
        if (this.movimiento.tipo === 'EGRESO') {
            const cuenta = this.cuentas.find(c => c.idCuenta === this.movimiento.idCuenta);
            if (cuenta && cuenta.saldo < this.movimiento.monto) {
                this.alertasService.error(
                    'Saldo Insuficiente',
                    `Disponible: S/ ${cuenta.saldo.toFixed(2)}\nSolicitado: S/ ${this.movimiento.monto.toFixed(2)}`
                );
                return false;
            }
        }
        return true;
    }
}
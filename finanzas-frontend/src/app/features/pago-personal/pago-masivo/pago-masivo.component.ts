import { Component, OnInit, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PagoPersonalService } from '../../../services/pago-personal.service';
import { EmpleadoService } from '../../../services/empleado.service';
import { CuentaCorrienteService, CuentaCorriente } from '../../../services/cuenta-corriente.service';
import { PagoPersonalRequest } from '../../../models/pago-personal.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { TablaGeneralComponent, Columna } from '../../../shared/components/tabla-general/tabla-general.component';
import { SearchGenericComponent } from '../../../shared/components/reusable-search-selector/reusable-search-selector';
import { DropdownSelectorComponent, DropdownOption } from '../../../shared/components/dropdown-selector/dropdown-selector.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AlertasService } from '../../../services/alertas.service';
import { animate, style, transition, trigger } from '@angular/animations';

@Component({
    selector: 'app-pago-masivo',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        TablaGeneralComponent,
        SearchGenericComponent,
        DropdownSelectorComponent,
        PaginationComponent
    ],
    templateUrl: './pago-masivo.component.html',
    animations: [
        trigger('slideInOut', [
            transition(':enter', [
                style({ transform: 'translateX(100%)', opacity: 0 }),
                animate('400ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
            ]),
            transition(':leave', [
                animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
            ])
        ])
    ]
})
export class PagoMasivoComponent implements OnInit {
    @Output() onCancel = new EventEmitter<void>();
    @Output() onSaveSuccess = new EventEmitter<void>();

    loading = false;
    bulkItems: any[] = [];
    cuentas: CuentaCorriente[] = [];
    
    // Configuración global del pago masivo
    bulkGlobalData = {
        fechaPago: new Date().toISOString().split('T')[0],
        medioPago: 'EFECTIVO',
        glosaContable: 'PAGO DE PLANILLA - ' + new Date().toLocaleString('es-ES', { month: 'long' }).toUpperCase(),
        moneda: 'PEN',
        tipoPago: 'SUELDO',
        centroCosto: 'ADMINISTRACION',
        idCuenta: 0
    };

    bulkFilterOptions = [
        { label: 'TODOS', value: 'TODOS' },
        { label: 'NOMBRE', value: 'NOMBRE' },
        { label: 'DNI', value: 'DNI' },
        { label: 'PROFESIÓN', value: 'PROFESION' }
    ];

    medioPagoOptions: DropdownOption[] = [
        { label: 'EFECTIVO', value: 'EFECTIVO', icon: 'fas fa-money-bill-wave' },
        { label: 'TRANSFERENCIA', value: 'TRANSFERENCIA', icon: 'fas fa-university' },
        { label: 'CHEQUE', value: 'CHEQUE', icon: 'fas fa-money-check-alt' }
    ];

    tipoPagoOptions: DropdownOption[] = [
        { label: 'SUELDO MENSUAL', value: 'SUELDO', subtitle: 'Pago regular de haberes' },
        { label: 'ADELANTO DE QUINCENA', value: 'ADELANTO', subtitle: 'Pago parcial anticipado' },
        { label: 'BONIFICACIÓN', value: 'BONO', subtitle: 'Pagos extras o premios' },
        { label: 'COMISIÓN POR VENTAS', value: 'COMISIÓN', subtitle: 'Incentivos comerciales' },
        { label: 'REEMBOLSO DE GASTOS', value: 'REEMBOLSO', subtitle: 'Devolución de viáticos' }
    ];

    get cuentaOptions(): DropdownOption[] {
        return this.cuentas.map(c => ({
            label: `${c.banco} - ${c.alias || c.nombreTitular}`,
            value: c.idCuenta,
            subtitle: `${c.moneda} ${c.saldo.toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
            icon: 'fas fa-credit-card'
        }));
    }

    bulkColumnas: Columna[] = [
        { field: 'checked', header: '', tipo: 'checkbox', align: 'center' },
        { field: 'nombreCompleto', header: 'Trabajador', tipo: 'text' },
        { field: 'dni', header: 'DNI', tipo: 'text' },
        { field: 'profesion', header: 'Profesión', tipo: 'text' },
        { field: 'monto', header: 'Monto (S/)', tipo: 'input-number', align: 'center' }
    ];

    bulkSearchQuery: string = '';
    bulkSearchType: string = 'TODOS';
    bulkCurrentPage = 0;
    bulkPageSize = 10;

    public MESES = ['ENERO', 'FEBRERO', 'MARZO', 'ABRIL', 'MAYO', 'JUNIO', 'JULIO', 'AGOSTO', 'SEPTIEMBRE', 'OCTUBRE', 'NOVIEMBRE', 'DICIEMBRE'];

    constructor(
        private pagoService: PagoPersonalService,
        private empleadoService: EmpleadoService,
        private cuentaService: CuentaCorrienteService,
        private alertasService: AlertasService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.cargarDatos();
    }

    cargarDatos() {
        this.loading = true;
        this.cargarCuentas();
        
        this.empleadoService.getAll().subscribe({
            next: (data) => {
                this.bulkItems = data
                    .filter(e => e.estado !== false)
                    .map(e => ({
                        empleado: e,
                        checked: false,
                        monto: e.sueldoBase || 0,
                        nombreCompleto: `${e.nombre || ''} ${e.apepat || ''} ${e.apemat || ''}`.trim(),
                        dni: e.numdoc || '-',
                        profesion: e.nombreOcupacion || '-'
                    }));
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    cargarCuentas() {
        this.cuentaService.getActive().subscribe({
            next: (data) => {
                this.cuentas = data;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error al cargar cuentas bancarias', err);
            }
        });
    }

    handleBulkSearch(event: { q: string; type: string }) {
        this.bulkSearchQuery = event.q.toLowerCase();
        this.bulkSearchType = event.type;
        this.bulkCurrentPage = 0;
    }

    onBulkPageChange(page: number) {
        this.bulkCurrentPage = page;
        this.cdr.detectChanges();
    }

    onBulkPageSizeChange(size: number) {
        this.bulkPageSize = size;
        this.bulkCurrentPage = 0;
        this.cdr.detectChanges();
    }

    onBulkCheckboxChange(event: { item: any; checked: boolean }) {
        const item = this.bulkItems.find(i => i.empleado.id === event.item.empleado.id);
        if (item) {
            item.checked = event.checked;
            this.cdr.detectChanges();
        }
    }

    onBulkHeaderCheckboxChange(checked: boolean) {
        this.filteredBulkItems.forEach(item => item.checked = checked);
        this.cdr.detectChanges();
    }

    onBulkInputChange(event: { item: any; col: Columna; value: any }) {
        const item = this.bulkItems.find(i => i.empleado.id === event.item.empleado.id);
        if (item) {
            item.monto = event.value;
            this.cdr.detectChanges();
        }
    }

    get filteredBulkItems() {
        if (!this.bulkSearchQuery && this.bulkSearchType === 'TODOS') return this.bulkItems;

        return this.bulkItems.filter(item => {
            const emp = item.empleado;
            const nombreCompleto = `${emp.nombre || ''} ${emp.apepat || ''} ${emp.apemat || ''}`.toLowerCase();
            const dni = (emp.numdoc || '').toLowerCase();
            const profesion = (emp.nombreOcupacion || '').toLowerCase();

            switch (this.bulkSearchType) {
                case 'NOMBRE':
                    return nombreCompleto.includes(this.bulkSearchQuery);
                case 'DNI':
                    return dni.includes(this.bulkSearchQuery);
                case 'PROFESION':
                    return profesion.includes(this.bulkSearchQuery);
                default:
                    return nombreCompleto.includes(this.bulkSearchQuery) ||
                        dni.includes(this.bulkSearchQuery) ||
                        profesion.includes(this.bulkSearchQuery);
            }
        });
    }

    get bulkTotalPages(): number {
        return Math.ceil(this.filteredBulkItems.length / this.bulkPageSize);
    }

    get paginatedBulkItems() {
        const start = this.bulkCurrentPage * this.bulkPageSize;
        return this.filteredBulkItems.slice(start, start + this.bulkPageSize);
    }

    get selectedCount(): number {
        return this.bulkItems.filter(item => item.checked).length;
    }

    get totalMasivo(): number {
        return this.bulkItems
            .filter(item => item.checked)
            .reduce((acc, item) => acc + (Number(item.monto) || 0), 0);
    }

    get cuentaSeleccionada(): CuentaCorriente | undefined {
        if (!this.bulkGlobalData || !this.bulkGlobalData.idCuenta) return undefined;
        // Usamos == para evitar problemas de tipos string/number desde el select
        return this.cuentas.find(c => c.idCuenta == this.bulkGlobalData.idCuenta);
    }

    get saldoInsuficiente(): boolean {
        const cuenta = this.cuentaSeleccionada;
        if (!cuenta) return false; 
        
        const total = Number(this.totalMasivo) || 0;
        const saldo = Number(cuenta.saldo) || 0;
        
        return saldo < total;
    }

    cancelar() {
        this.onCancel.emit();
    }

    procesarPagoMasivo() {
        if (this.bulkGlobalData.idCuenta === 0) {
            this.alertasService.warning('Atención', 'Debe seleccionar una cuenta bancaria antes de proceder.');
            return;
        }

        const seleccionados = this.bulkItems.filter(item => item.checked);
        if (seleccionados.length === 0) {
            this.alertasService.warning('Atención', 'Debe seleccionar al menos un empleado para el pago.');
            return;
        }

        if (this.saldoInsuficiente) {
            this.alertasService.error(
                'Saldo Insuficiente',
                `El saldo de la cuenta (${this.cuentaSeleccionada?.moneda} ${this.cuentaSeleccionada?.saldo}) es menor al total del pago (S/ ${this.totalMasivo.toFixed(2)}).`
            );
            return;
        }

        if (!this.cuentaSeleccionada) {
            this.alertasService.warning('Atención', 'No se pudo validar la cuenta seleccionada. Intente de nuevo.');
            return;
        }

        this.alertasService.confirm(
            '¿Procesar Pago Masivo?',
            `Se registrarán ${seleccionados.length} pagos por un total de S/ ${this.totalMasivo.toFixed(2)}`
        ).then((confirmed) => {
            if (confirmed) {
                this.ejecutarPagoMasivo(seleccionados);
            }
        });
    }

    private ejecutarPagoMasivo(seleccionados: any[]) {
        const validos = seleccionados.filter(item => item.monto > 0);
        
        if (validos.length === 0) {
            this.alertasService.warning('Atención', 'Debe seleccionar al menos un empleado con un monto mayor a 0.');
            return;
        }

        this.loading = true;
        const currentYear = new Date().getFullYear();
        const currentMonth = this.MESES[new Date().getMonth()];

        // Mapear cada empleado a un objeto de PagoPersonalRequest individual
        const requests = validos.map(item => ({
            tipoPago: this.bulkGlobalData.tipoPago,
            periodoMes: currentMonth,
            periodoAnio: currentYear,
            fechaPago: this.bulkGlobalData.fechaPago,
            conceptoPago: `PAGO ${this.bulkGlobalData.tipoPago} - ${currentMonth} ${currentYear} - ${item.nombreCompleto}`.toUpperCase(),
            numeroDocumento: item.dni,
            nombreRazonSocial: item.nombreCompleto,
            cargoTrabajador: item.profesion,
            centroCosto: this.bulkGlobalData.centroCosto,
            montoBruto: Number(item.monto),
            montoNeto: Number(item.monto),
            moneda: this.bulkGlobalData.moneda,
            medioPago: this.bulkGlobalData.medioPago,
            idCuenta: Number(this.bulkGlobalData.idCuenta),
            idSucursal: 1, 
            detalles: [{
                nombreConcepto: `${this.bulkGlobalData.tipoPago} - ${item.nombreCompleto}`.toUpperCase(),
                tipoConcepto: 'INGRESO',
                monto: Number(item.monto)
            }]
        } as any));

        this.pagoService.registrarPagosMasivos(requests).subscribe({
            next: (response) => {
                this.loading = false;
                this.alertasService.success(
                    '¡Éxito!',
                    `Se han registrado exitosamente ${requests.length} pagos individuales.`
                );
                this.onSaveSuccess.emit();
            },
            error: (err) => {
                console.error('Error al registrar pago masivo:', err);
                this.loading = false;
                const errorMsg = err.error?.message || 'No se pudo completar el registro masivo.';
                this.alertasService.error('Error', errorMsg);
            }
        });
    }
}

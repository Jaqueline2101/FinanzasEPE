import { Component, OnInit, signal, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaChicaService } from '../../services/caja-chica.service';
import { CuentaCorrienteService } from '../../services/cuenta-corriente.service';
import { CuentaCorriente } from '../../models/cuenta-corriente.model';
import { Movimiento } from '../../models/movimiento.model';
import { AuthService } from '../auth/services/auth.service';

import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { ImpresionMovimientoComponent } from './impresion-movimiento/impresion-movimiento.component';
import { AlertasService } from '../../services/alertas.service';

@Component({
    selector: 'app-registro-movimientos',
    standalone: true,
    imports: [CommonModule, FormsModule, BreadcrumbComponent, ImpresionMovimientoComponent],
    templateUrl: './registro-movimientos.component.html',
})
export class RegistroMovimientosComponent implements OnInit {

    // --- FORM DATA ONLY ---
    cuentas = signal<CuentaCorriente[]>([]);
    tiposMovimiento = [
        { value: 'EGRESO', label: 'Salida de dinero' },
        { value: 'INGRESO', label: 'Entrada de dinero' }
    ];
    tiposComprobante = ['SIN COMPROBANTE', 'TICKET', 'FACTURA', 'BOLETA', 'RECIBO', 'OTROS'];

    breadcrumbItems = [
        { label: 'Caja Chica', route: '/admin/caja-chica' },
        { label: 'Registro de Movimientos' }
    ];

    // Accounting Codes Configuration
    planContableOptions = [
        { code: '10.1', label: 'Caja Chica' },
        { code: '60.1', label: 'Suministros diversos' },
        { code: '60.2', label: 'Útiles de oficina' },
        { code: '60.3', label: 'Materiales de limpieza' },
        { code: '60.5', label: 'Material médico menor' },
        { code: '63.1', label: 'Alimentación' },
        { code: '63.2', label: 'Movilidad' },
        { code: '63.3', label: 'Mantenimiento' },
        { code: '63.4', label: 'Servicios de terceros' },
        { code: '63.7', label: 'Otros gastos operativos' }
    ];

    // Form Model
    movimientoForm: Partial<Movimiento> = {
        tipoMovimiento: 'EGRESO',
        tipoComprobante: 'TICKET',
        moneda: 'PEN',
        fechaEmision: '',
        estado: 'A'
    };

    loading = signal<boolean>(false);
    isAccountAutoSelected = signal<boolean>(false);
    cajaAbierta = signal<boolean>(false);
    cajaChicaId: number | null = null; // ID de la caja chica abierta

    constructor(
        private cajaChicaService: CajaChicaService,
        private cuentaService: CuentaCorrienteService,
        private authService: AuthService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) { }

    selectedTipoGasto: string = '';
    currentSucursalId: number = 1;

    ngOnInit() {
        this.cargarCuentas();
        this.initContext();
        this.movimientoForm.fechaEmision = this.getLocalDate();
        this.verificarCajaAbierta();
    }

    private initContext() {
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            const userId = user.id || user.userId;

            if (userId) {
                this.authService.getSucursalesAutorizadas(userId).subscribe({
                    next: (res) => {
                        const currentSucursalName = this.authService.currentSucursal();
                        if (res.success && res.data && res.data.length > 0) {
                            const found = res.data.find((s: any) => s.nombreSucursal === currentSucursalName);
                            if (found) {
                                this.currentSucursalId = found.idSucursal;
                                console.log('✅ Sucursal detectada:', this.currentSucursalId);
                                this.updateCorrelative();
                            }
                        }
                    }
                });
            }
        }
    }

    private verificarCajaAbierta() {
        this.cajaChicaService.obtenerCajaAbierta().subscribe({
            next: (res) => {
                if (res.data && res.data.id) {
                    this.cajaChicaId = res.data.id;
                    this.cajaAbierta.set(true);
                } else {
                    this.cajaAbierta.set(false);
                }
            },
            error: (err) => {
                console.error('Error al verificar caja:', err);
                this.cajaAbierta.set(false);
            }
        });
    }

    cargarCuentas() {
        this.cuentaService.getActive().subscribe({
            next: (data) => {
                this.cuentas.set(data);

                // AUTO-SELECT Petty Cash / Ventanilla
                const pettyCash = data.find(c => {
                    const alias = c.alias?.toLowerCase() || '';
                    const titular = c.nombreTitular?.toLowerCase() || '';
                    const banco = c.banco?.toLowerCase() || '';

                    return alias.includes('chica') ||
                        alias.includes('ventanilla') ||
                        titular.includes('chica') ||
                        titular.includes('ventanilla') ||
                        banco.includes('caja chica');
                });

                if (pettyCash) {
                    this.movimientoForm.cuenta = pettyCash;
                    this.isAccountAutoSelected.set(true);
                    console.log('✅ Auto-seleccionada cuenta de Caja Chica/Ventanilla:', pettyCash.alias || pettyCash.nombreTitular);
                }
            },
            error: (err) => console.error('Error loading accounts', err)
        });
    }

    onTipoGastoChange() {
        if (this.selectedTipoGasto) {
            this.movimientoForm.codigoContable = this.selectedTipoGasto;
        }
    }

    onTipoComprobanteChange() {
        setTimeout(() => {
            this.updateCorrelative();
        }, 0);
    }

    updateCorrelative() {
        if (this.isAutoGeneratedType && this.currentSucursalId) {
            if (!this.movimientoForm.tipoComprobante) return;
            // Correlative generation would ideally be moved to Caja Chica backend as well
            // For now, we simulate a mock correlative to avoid breaking the design UI
            this.movimientoForm.numeroComprobante = `${this.movimientoForm.tipoComprobante?.substring(0, 1)}001-${Math.floor(Math.random() * 10000).toString().padStart(6, '0')}`;
            this.cd.detectChanges();
        } else if (this.movimientoForm.tipoComprobante === 'SIN COMPROBANTE') {
            this.movimientoForm.numeroComprobante = '';
        }
    }

    onCodigoChange() {
        this.selectedTipoGasto = this.movimientoForm.codigoContable || '';
    }

    showPrintModal = false;
    savedMovimiento: any = null;

    guardar() {
        if (!this.cajaAbierta()) {
            this.alertasService.warning('Caja Cerrada', 'Debe abrir una caja desde Ingresos y Gastos antes de registrar movimientos');
            return;
        }
        if (!this.validarFormulario()) return;

        this.loading.set(true);
        
        const request = {
            cajaChicaId: this.cajaChicaId,
            tipo: this.movimientoForm.tipoMovimiento as 'INGRESO' | 'EGRESO',
            monto: Number(this.movimientoForm.monto),
            descripcion: this.movimientoForm.descripcion || '',
            referencia: this.movimientoForm.numeroComprobante || this.movimientoForm.tipoComprobante || 'S/R',
            metodoPago: 'EFECTIVO'
        };

        this.cajaChicaService.registrarMovimiento(request).subscribe({
            next: (res) => {
                this.loading.set(false);

                // Save for printing
                this.savedMovimiento = { ...this.movimientoForm, ...res.data };

                this.updateCorrelative();

                this.alertasService.success(
                    'Registrado',
                    `Movimiento registrado correctamente en Caja Chica`
                );

                setTimeout(() => {
                    this.showPrintModal = true;
                    this.cd.detectChanges();
                }, 800); 

                this.limpiarFormulario();
            },
            error: (err) => {
                this.loading.set(false);
                console.error(err);
                this.alertasService.error('Error', 'No se pudo registrar el movimiento en Caja Chica');
            }
        });
    }

    closePrintModal() {
        this.showPrintModal = false;
        this.savedMovimiento = null;
    }

    validarFormulario(): boolean {
        const monto = Number(this.movimientoForm.monto);
        
        if (!monto || monto <= 0) {
            this.alertasService.warning('Atención', 'Ingresa un monto válido');
            return false;
        }

        if (!this.movimientoForm.descripcion) {
            this.alertasService.warning('Atención', 'Ingresa una descripción/glosa');
            return false;
        }

        return true;
    }

    limpiarFormulario() {
        const currentAccount = this.movimientoForm.cuenta;
        this.movimientoForm = {
            tipoMovimiento: 'EGRESO',
            tipoComprobante: 'TICKET',
            moneda: 'PEN',
            fechaEmision: this.getLocalDate(),
            estado: 'A',
            cuenta: this.isAccountAutoSelected() ? currentAccount : undefined
        };
        this.updateCorrelative();
    }

    private getLocalDate(): string {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    get isAutoGeneratedType(): boolean {
        const type = this.movimientoForm.tipoComprobante;
        return type === 'TICKET' || type === 'BOLETA' || type === 'FACTURA' || type === 'RECIBO';
    }
}

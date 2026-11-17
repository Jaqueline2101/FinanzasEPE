import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { Columna, TablaGeneralComponent } from '../../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { ProveedorService } from '../../../services/proveedor.service';
import { AuthService } from '../../auth/services/auth.service';
import { CuentaProveedorService } from './cuenta-proveedor.service';
import { AlertService } from './alert.service';
import { AlertasService } from '../../../services/alertas.service';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { FormsModule } from '@angular/forms';


@Component({
    selector: 'app-cuenta-proveedor-list',
    standalone: true,
    imports: [
        CommonModule,
        PageHeaderComponent,
        TablaGeneralComponent,
        PaginationComponent,
        BreadcrumbComponent,
        ModalComponent,
        AlertasComponent,
        FormsModule
    ],
    templateUrl: './cuenta-proveedor-list.component.html'
})
export class CuentaProveedorListComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private cuentaService = inject(CuentaProveedorService);
    private proveedorService = inject(ProveedorService);
    private alertService = inject(AlertService);
    private alertasService = inject(AlertasService);
    private authService = inject(AuthService);

    idProveedor = signal<number | null>(null);
    proveedor = signal<any>(null);
    cuenta = signal<any>(null);
    movimientos = signal<any[]>([]);
    loading = signal(false);

    currentPage = signal(0);
    totalPages = signal(0);
    totalElements = signal(0);
    pageSize = signal(10);

    breadcrumbItems = signal<BreadcrumbItem[]>([]);

    // Modal properties
    showModal = false;
    modalTitle = '';
    modalType: 'ABONO' | 'CARGO' = 'ABONO';
    modalMonto: number | null = null;
    modalDescripcion = '';
    pagarDesdeCaja = false;

    columns: Columna[] = [
        { field: 'fechaRegistro', header: 'Fecha', tipo: 'date' },
        {
            field: 'tipo', header: 'Tipo', tipo: 'badge', badgeConfig: {
                from: 'from-blue-100', to: 'to-blue-200', text: 'text-blue-800', border: 'border-blue-300'
            }
        },
        { field: 'monto', header: 'Monto', tipo: 'currency-color', subField: ['tipoMovimiento'] },
        { field: 'descripcion', header: 'Descripción', tipo: 'text' }
    ];

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.idProveedor.set(+id);
            this.cargarDatos();
        }
    }

    cargarDatos(): void {
        if (!this.idProveedor()) return;

        this.proveedorService.getById(this.idProveedor()!).subscribe(res => {
            console.log(res);
            if (res) {
                this.proveedor.set(res);
                this.updateBreadcrumbs();
            }
        });

        this.cuentaService.obtenerCuenta(this.idProveedor()!).subscribe(res => {
            if (res.success) {
                this.cuenta.set(res.data);
            }
        });

        this.cargarMovimientos();
    }

    cargarMovimientos(): void {
        this.loading.set(true);
        this.cuentaService.listarMovimientos(this.idProveedor()!, this.currentPage(), this.pageSize())
            .subscribe({
                next: (res) => {
                    if (res.success) {
                        const mappedContent = res.data.content.map((m: any) => ({
                            ...m,
                            tipoMovimiento: m.tipo === 'ABONO' ? 'INGRESO' : 'EGRESO'
                        }));
                        this.movimientos.set(mappedContent);
                        this.totalPages.set(res.data.totalPages);
                        this.totalElements.set(res.data.totalElements);
                    }
                    this.loading.set(false);
                },
                error: () => this.loading.set(false)
            });
    }

    updateBreadcrumbs(): void {
        this.breadcrumbItems.set([
            { label: 'Compras', route: '/compra' },
            { label: 'Proveedores', route: '/compra/proveedores' },
            { label: `Cuenta: ${this.proveedor()?.razonSocial || '...'}` }
        ]);
    }

    abrirModalAbono(): void {
        this.modalTitle = 'Registrar Abono (Pago)';
        this.modalType = 'ABONO';
        this.modalMonto = null;
        this.modalDescripcion = '';
        this.pagarDesdeCaja = false;
        this.showModal = true;
    }

    abrirModalCargo(): void {
        this.modalTitle = 'Registrar Cargo (Deuda)';
        this.modalType = 'CARGO';
        this.modalMonto = null;
        this.modalDescripcion = '';
        this.pagarDesdeCaja = false;
        this.showModal = true;
    }

    confirmarMovimiento(): void {
        if (!this.modalMonto || this.modalMonto <= 0) {
            this.alertasService.warning('Monto Inválido', 'Debe ingresar un monto mayor a cero para continuar.');
            return;
        }

        const accion = this.modalType === 'ABONO' ? 'Abono (Pago)' : 'Cargo (Deuda)';

        this.alertasService.confirm(
            `Confirmar ${accion}`,
            `¿Está seguro de que desea registrar este ${accion.toLowerCase()} por un monto de S/ ${this.modalMonto}?`
        ).then(confirmed => {
            if (confirmed) {
                this.ejecutarRegistro();
            }
        });
    }

    private ejecutarRegistro(): void {
        this.loading.set(true);
        this.cuentaService.registrarMovimiento(this.idProveedor()!, {
            tipo: this.modalType,
            monto: this.modalMonto!,
            descripcion: this.modalDescripcion || 'Sin descripción',
            idSucursal: this.authService.getSucursalIdFromToken(),
            pagarDesdeCajaGeneral: this.modalType === 'ABONO' ? this.pagarDesdeCaja : false,
            idUser: this.authService.getUsuarioIdFromToken()
        }).subscribe({
            next: (res) => {
                this.loading.set(false);
                if (res.success) {
                    this.alertasService.success('Registro Completado', 'El movimiento se ha registrado con éxito en el estado de cuenta.');
                    this.showModal = false;
                    this.cargarDatos();
                }
            },
            error: () => {
                this.loading.set(false);
                this.alertasService.error('Error de Servidor', 'No se pudo procesar el registro. Inténtelo de nuevo más tarde.');
            }
        });
    }

    cerrarModal(): void {
        this.showModal = false;
    }

    cambiarPagina(page: number): void {
        this.currentPage.set(page);
        this.cargarMovimientos();
    }

    cambiarTamanoPagina(size: number): void {
        this.pageSize.set(size);
        this.currentPage.set(0);
        this.cargarMovimientos();
    }

    regresar(): void {
        this.router.navigate(['/compra/proveedores']);
    }
}

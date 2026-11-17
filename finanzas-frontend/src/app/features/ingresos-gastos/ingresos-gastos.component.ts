import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaChicaService, CajaChica, CajaResumenDTO } from '../../services/caja-chica.service';
import { AuthService } from '../auth/services/auth.service';
import { AlertasService } from '../../services/alertas.service';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { AbrirCajaModalComponent } from './abrir-caja-modal/abrir-caja-modal.component';
import { CerrarCajaModalComponent } from './cerrar-caja-modal/cerrar-caja-modal.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { BarraFiltrosComponent, FilterCriteria } from '../../shared/components/barra-filtros/barra-filtros.component';

@Component({
    selector: 'app-ingresos-gastos',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, BreadcrumbComponent, TablaGeneralComponent, PaginationComponent, AbrirCajaModalComponent, CerrarCajaModalComponent, BarraFiltrosComponent],
    templateUrl: './ingresos-gastos.component.html',
    styleUrls: ['./ingresos-gastos.component.css']
})
export class IngresosGastosComponent implements OnInit {
    private cajaChicaService = inject(CajaChicaService);
    private authService = inject(AuthService);
    private alertasService = inject(AlertasService);

    // State Signals
    cajaAbierta = signal<CajaChica | null>(null);
    movimientos = signal<any[]>([]);
    resumen = signal<CajaResumenDTO | null>(null);
    loading = signal<boolean>(false);

    // Modals State
    isAbrirCajaModalOpen = false;
    isCerrarCajaModalOpen = false;
    saldoTeoricoCierre: number = 0;

    // Table Configuration
    columnas: Columna[] = [
        { field: 'fecha', header: 'Fecha', tipo: 'date', subField: [], align: 'left' },
        { field: 'usuario', header: 'Usuario', tipo: 'text', subField: [], align: 'left' },
        { field: 'descripcion', header: 'Glosa / Detalle', tipo: 'text', subField: [], align: 'left' },
        { field: 'referencia', header: 'Referencia / Doc', tipo: 'text', subField: [], align: 'left' },
        { field: 'metodoPago', header: 'Método Pago', tipo: 'text', subField: [], align: 'center' },
        { field: 'monto', header: 'Monto', tipo: 'amount-color', subField: ['tipo'], align: 'right' }
    ];

    // Search Configuration
    searchQuery: string = '';
    searchType: string = 'TODO';
    searchOptions = [
        { label: 'Todos', value: 'TODO' },
        { label: 'Nro Comprobante / Ref', value: 'COMPROBANTE' },
        { label: 'Concepto / Glosa', value: 'CONCEPTO' }
    ];
    originalMovimientos: any[] = [];

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja Chica', route: '/admin/caja-chica' },
        { label: 'Ingresos y Gastos' }
    ];

    // Pagination
    currentPage = 0;
    pageSize = 10;
    totalPages = 0;
    totalElements = 0;

    // Context
    currentSucursalId: number | null = null;
    currentPuntoId: number = 1; // Ajustar según lógica de puntos de venta del frontend
    currentUserId: number | null = null;

    ngOnInit() {
        this.initContext();
    }

    private initContext() {
        const user = this.authService.currentUser();
        if (user) {
            this.currentUserId = user.id;
            this.currentSucursalId = user.sucursalActualId || 1;
        } else {
            const userStr = localStorage.getItem('currentUser');
            if (userStr) {
                try {
                    const userObj = JSON.parse(userStr);
                    this.currentUserId = userObj.id || userObj.userId;
                    this.currentSucursalId = userObj.sucursalActualId || 1;
                } catch (e) {
                    this.currentSucursalId = 1;
                }
            }
        }

        this.verificarCajaAbierta();
    }

    verificarCajaAbierta() {
        this.loading.set(true);
        this.cajaChicaService.obtenerCajaAbierta().subscribe({
            next: (res) => {
                if (res.data) {
                    this.cajaAbierta.set(res.data);
                    this.loadData(res.data.id);
                } else {
                    this.cajaAbierta.set(null);
                    this.loading.set(false);
                }
            },
            error: (err) => {
                console.error('Error al obtener caja abierta', err);
                this.loading.set(false);
            }
        });
    }

    abrirCaja() {
        this.isAbrirCajaModalOpen = true;
    }

    onConfirmarAbrirCaja(saldoInicial: number) {
        this.isAbrirCajaModalOpen = false;
        this.loading.set(true);
        const request = {
            nombre: 'CAJA CHICA - ' + new Date().toLocaleDateString(),
            idSucursal: this.currentSucursalId || 1,
            idPuntoVenta: this.currentPuntoId,
            saldoInicial
        };

        this.cajaChicaService.crearCaja(request).subscribe({
            next: (res) => {
                this.alertasService.success('Caja Abierta', 'La caja chica ha sido abierta correctamente.');
                this.cajaAbierta.set(res.data);
                this.loadData(res.data.id);
            },
            error: () => {
                this.alertasService.error('Error', 'No se pudo abrir la caja.');
                this.loading.set(false);
            }
        });
    }

    loadData(cajaId: number) {
        this.loading.set(true);

        // Cargar Resumen
        this.cajaChicaService.obtenerResumen(cajaId).subscribe({
            next: (res) => this.resumen.set(res.data),
            error: (err) => console.error('Error cargando resumen', err)
        });

        // Cargar Movimientos
        this.cajaChicaService.listarMovimientos(cajaId, this.currentPage, this.pageSize).subscribe({
            next: (res) => {
                const page = res.data;
                const processed = page.content.map((m: any) => ({
                    ...m,
                    montoIngreso: m.tipo === 'INGRESO' ? m.monto : 0,
                    montoEgreso: m.tipo === 'EGRESO' ? m.monto : 0
                }));
                this.originalMovimientos = processed;
                this.aplicarFiltroBusqueda();
                this.totalElements = page.totalElements;
                this.totalPages = page.totalPages;
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Error cargando movimientos', err);
                this.loading.set(false);
            }
        });
    }

    cerrarCaja() {
        const cajaId = this.cajaAbierta()?.id;
        if (!cajaId) return;

        this.saldoTeoricoCierre = this.resumen()?.saldoFinalTeorico || 0;
        this.isCerrarCajaModalOpen = true;
    }

    onConfirmarCerrarCaja(data: { saldoReal: number; transferir: boolean }) {
        const cajaId = this.cajaAbierta()?.id;
        if (!cajaId) return;

        this.isCerrarCajaModalOpen = false;
        this.loading.set(true);
        this.cajaChicaService.cerrarCaja(cajaId, data.saldoReal, data.transferir).subscribe({
            next: () => {
                this.alertasService.success('Caja Cerrada', 'El turno ha sido cerrado correctamente.');
                this.cajaAbierta.set(null);
                this.resumen.set(null);
                this.movimientos.set([]);
                this.loading.set(false);
            },
            error: () => {
                this.alertasService.error('Error', 'No se pudo cerrar la caja.');
                this.loading.set(false);
            }
        });
    }

    async anular(id: number) {
        this.alertasService.error('Acción no permitida', 'En Caja Chica no se borran registros. Por favor, registre un movimiento contrario (Egreso/Ingreso) para cuadrar la caja y agregue en la descripción "Extorno del movimiento X".');
    }

    onFilter(criteria: FilterCriteria) {
        setTimeout(() => {
            this.searchQuery = criteria.term || '';
            this.searchType = criteria.searchType || 'TODO';
            this.currentPage = 0;
            if (this.cajaAbierta()) this.loadData(this.cajaAbierta()!.id);
        });
    }

    onPageChange(page: number) {
        this.currentPage = page;
        if (this.cajaAbierta()) this.loadData(this.cajaAbierta()!.id);
    }

    onSearch(event: { q: string, type: string }) {
        this.searchQuery = event.q.trim().toLowerCase();
        this.searchType = event.type;
        this.aplicarFiltroBusqueda();
    }

    aplicarFiltroBusqueda() {
        if (!this.searchQuery) {
            this.movimientos.set(this.originalMovimientos);
            return;
        }

        const filtrados = this.originalMovimientos.filter(m => {
            const desc = m.descripcion?.toLowerCase() || '';
            const ref = m.referencia?.toLowerCase() || '';
            if (this.searchType === 'CONCEPTO') return desc.includes(this.searchQuery);
            if (this.searchType === 'COMPROBANTE') return ref.includes(this.searchQuery);
            return desc.includes(this.searchQuery) || ref.includes(this.searchQuery);
        });
        this.movimientos.set(filtrados);
    }

    restablecer() {
        this.searchQuery = '';
        this.searchType = 'TODO';
        this.currentPage = 0;
        if (this.cajaAbierta()) this.loadData(this.cajaAbierta()!.id);
    }

    onPageSizeChange(newSize: number) {
        this.pageSize = newSize;
        this.currentPage = 0;
        if (this.cajaAbierta()) this.loadData(this.cajaAbierta()!.id);
    }

}
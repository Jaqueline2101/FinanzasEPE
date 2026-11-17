import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth/services/auth.service';
import { AlertasService } from '../../services/alertas.service';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { ArqueoCajaService, ArqueoCajaDTO } from '../../services/arqueo-caja.service';
import { ModalDetalleArqueoComponent } from './modal-detalle/modal-detalle-arqueo.component';

@Component({
    selector: 'app-arqueo-caja',
    standalone: true,
    imports: [CommonModule, FormsModule, PageHeaderComponent, BreadcrumbComponent, TablaGeneralComponent, PaginationComponent, ModalDetalleArqueoComponent],
    templateUrl: './arqueo-caja.component.html'
})
export class ArqueoCajaComponent implements OnInit {
    private arqueoService = inject(ArqueoCajaService);
    private authService = inject(AuthService);
    private alertasService = inject(AlertasService);

    // State
    historial = signal<ArqueoCajaDTO[]>([]);
    loading = signal<boolean>(false);
    saldoEsperado = signal<number>(0);
    saldoFisico: number | null = null;
    observaciones: string = '';

    // Table Configuration
    columnas: Columna[] = [
        { field: '', header: '#', tipo: 'index', subField: [] },
        { field: 'fechaHora', header: 'Fecha y Hora', tipo: 'date', subField: [] },
        { field: 'nombreUsuario', header: 'Usuario', tipo: 'text', subField: [] },
        { field: 'nombrePunto', header: 'Punto', tipo: 'text', subField: [] },
        { field: 'nombreSucursal', header: 'Sucursal', tipo: 'text', subField: [] },
        { field: 'diferencia', header: 'Diferencia', tipo: 'currency', subField: [], align: 'right' },
        { field: 'estado', header: 'Estado', tipo: 'badge', subField: [] },
        { field: '', header: 'Acciones', tipo: 'custom-button-action', subField: [], actionIcon: 'fa-eye', actionLabel: '', actionClass: 'text-emerald-600 border-emerald-100 hover:bg-emerald-50' }
    ];

    showDetailModal = false;
    selectedArqueo: ArqueoCajaDTO | null = null;

    // Pagination
    currentPage = 0;
    pageSize = 20;
    totalElements = 0;
    totalPages = 0;

    // Filters
    fechaInicio: string = this.getLocalDate();
    fechaFin: string = this.getLocalDate();

    // Context
    currentSucursalId: number | null = null;
    currentUserId: number | null = null;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Caja Chica', icon: 'fas fa-cash-register' },
        { label: 'Arqueo de Caja' } // Removed isActive: true as per Interface definition
    ];

    ngOnInit() {
        this.initContext();
    }

    private initContext() {
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            this.currentUserId = user.id || user.userId;
        }

        if (this.currentUserId) {
            this.authService.getSucursalesAutorizadas(this.currentUserId).subscribe({
                next: (res) => {
                    const currentSucursalName = this.authService.currentSucursal();
                    if (res.success && res.data && res.data.length > 0) {
                        const found = res.data.find((s: any) => s.nombreSucursal === currentSucursalName);
                        if (found) {
                            this.currentSucursalId = found.idSucursal;
                        } else {
                            this.currentSucursalId = res.data[0].idSucursal;
                        }
                    } else {
                        this.currentSucursalId = 1;
                    }
                    this.loadSaldoEsperado();
                    this.loadHistorial();
                },
                error: (err) => {
                    console.error('Error cargando sucursales', err);
                    this.currentSucursalId = 1;
                    this.loadSaldoEsperado();
                    this.loadHistorial();
                }
            });
        }
    }

    private getLocalDate(): string {
        const today = new Date();
        const yyyy = today.getFullYear();
        const mm = String(today.getMonth() + 1).padStart(2, '0');
        const dd = String(today.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    loadSaldoEsperado() {
        this.arqueoService.obtenerSaldoEsperado().subscribe({
            next: (saldo: number) => {
                this.saldoEsperado.set(saldo);
            },
            error: (error: any) => {
                console.error('Error cargando saldo esperado', error);
            }
        });
    }

    loadHistorial() {
        this.loading.set(true);
        this.arqueoService.listarArqueos(
            this.fechaInicio,
            this.fechaFin,
            this.currentPage,
            this.pageSize
        ).subscribe({
            next: (response: any) => {
                this.historial.set(response.content);
                this.totalElements = response.totalElements;
                this.totalPages = response.totalPages;
                this.loading.set(false);
            },
            error: (error: any) => {
                console.error('Error cargando historial de arqueos', error);
                this.loading.set(false);
            }
        });
    }

    onDateChange() {
        this.currentPage = 0;
        this.loadHistorial();
    }

    restablecer() {
        this.fechaInicio = this.getLocalDate();
        this.fechaFin = this.getLocalDate();
        this.currentPage = 0;
        this.loadHistorial();
    }

    onDetailView(arqueo: any) {
        this.selectedArqueo = arqueo;
        this.showDetailModal = true;
    }

    closeDetailModal() {
        this.showDetailModal = false;
        this.selectedArqueo = null;
    }

    onPageChange(page: number) {
        this.currentPage = page;
        this.loadHistorial();
    }

    onPageSizeChange(newSize: number) {
        this.pageSize = newSize;
        this.currentPage = 0;
        this.loadHistorial();
    }

    get diferencia(): number {
        return (this.saldoFisico || 0) - this.saldoEsperado();
    }

    registrarArqueo() {
        if (this.saldoFisico === null || this.saldoFisico === undefined || this.saldoFisico < 0) {
            this.alertasService.warning('Dato inválido', 'Ingrese un monto físico válido mayor o igual a 0.');
            return;
        }

        if (!this.currentSucursalId || !this.currentUserId) return;

        const isMismatch = this.diferencia !== 0;
        const msg = `Se registrará un saldo físico de S/ ${this.saldoFisico.toFixed(2)}. ${isMismatch ? '⚠️ SE DETECTÓ UNA DIFERENCIA DE S/ ' + this.diferencia.toFixed(2) : 'Caja cuadrada correctamente.'}`;

        this.alertasService.confirm(
            isMismatch ? '¿Confirmar Arqueo con Diferencia?' : '¿Confirmar Arqueo?',
            msg,
            'Sí, registrar',
            'Cancelar',
            isMismatch ? 'bg-amber-600 hover:bg-amber-700 shadow-amber-600/30' : 'bg-blue-600 hover:bg-blue-700 shadow-blue-600/30'
        ).then((confirmado) => {
            if (confirmado) {
                const dto: ArqueoCajaDTO = {
                    idUsuario: this.currentUserId!,
                    idSucursal: this.currentSucursalId!,
                    idPunto: this.authService.getPuntoIdFromToken() || undefined,
                    saldoEsperado: this.saldoEsperado(),
                    saldoFisico: this.saldoFisico || 0,
                    diferencia: this.diferencia,
                    observaciones: this.observaciones
                };

                this.arqueoService.registrarArqueo(dto).subscribe({
                    next: () => {
                        this.alertasService.success('¡Arqueo Registrado!', 'El arqueo se guardó correctamente.');
                        this.saldoFisico = null;
                        this.observaciones = '';
                        this.loadSaldoEsperado();
                        this.loadHistorial();
                    },
                    error: (error: any) => {
                        console.error(error);
                        this.alertasService.error('Error', 'Hubo un problema registrando el arqueo.');
                    }
                });
            }
        });
    }
}

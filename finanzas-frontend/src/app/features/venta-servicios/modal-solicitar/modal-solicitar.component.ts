import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { DropdownSelectorComponent, DropdownOption } from '../../../shared/components/dropdown-selector/dropdown-selector.component';
import { AutorizarDescuentoService } from '../../../services/autorizar-descuento.service';
import { AutorizacionRequest, AutorizacionResponse } from '../../../models/autorizar-descuento.model';
import { AlertasService } from '../../../services/alertas.service';
import { NotificationService } from '../../notificaciones-service/notification.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-modal-solicitar',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent, DropdownSelectorComponent],
    templateUrl: './modal-solicitar.component.html',
    styleUrls: []
})
export class ModalSolicitarComponent implements OnInit, OnDestroy {

    @Input() montoBase: number = 0;
    @Input() carrito: any[] = [];
    @Input() idOrden: number | null = null;
    @Input() paciente: any | null = null;
    @Input() doctorNombre: string | null = null;


    
    @Output() cancel = new EventEmitter<void>();

    @Output() confirm = new EventEmitter<void>();
    @Output() applyDiscount = new EventEmitter<AutorizacionResponse>();

    activeTab: 'NUEVA' | 'LISTA' = 'NUEVA';
    
    // Lógica bidi de descuento
    pctDescuento: number = 0;
    montoDescuento: number = 0;
    montoFinal: number = 0;
    
    // Base de cálculo (solo particulares)
    montoParticular: number = 0;
    itemsElegibles: any[] = [];

    motivoSolicitud: string = '';
    isSubmitting = false;
    private requestSent = false;

    // Listado de solicitudes
    solicitudes: AutorizacionResponse[] = [];
    isLoadingList = false;

    // Opciones del select
    opcionesPct = [3, 5, 10, 15, 20];
    opcionesPctDropdown: DropdownOption[] = [];

    private notificationSub?: Subscription;

    constructor(
        private autorizacionService: AutorizarDescuentoService,
        private alertasService: AlertasService,
        private notificationService: NotificationService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.inicializarOpciones();
        this.calcularBaseElegible();
        this.actualizarTotales();
        if (this.activeTab === 'LISTA') {
            this.cargarSolicitudes();
        }

        // Escuchar notificaciones de Kafka (solo del modulo AUTORIZACION_DESCUENTO)
        this.notificationSub = this.notificationService.notifications$.subscribe(notifs => {
            if (notifs.length > 0) {
                const ultimaNotificacion = notifs[0];
                if (ultimaNotificacion.modulo === 'AUTORIZACION_DESCUENTO') {
                    if (this.activeTab === 'LISTA') {
                        this.cargarSolicitudes();
                    }
                }
            }
        });
    }

    private inicializarOpciones() {
        this.opcionesPctDropdown = this.opcionesPct.map(p => ({
            label: `${p}% Descuento`,
            value: p,
            subtitle: p <= 5 ? 'Aprobación Local' : 'Requiere Gerencia',
            icon: p <= 5 ? 'fas fa-shield-alt text-emerald-500' : 'fas fa-crown text-amber-500'
        }));
    }

    setTab(tab: 'NUEVA' | 'LISTA') {
        this.activeTab = tab;
        if (tab === 'LISTA') {
            this.cargarSolicitudes();
        }
    }

    cargarSolicitudes() {
        this.isLoadingList = true;
        const idCajero = this.getAuthUserId();
        this.autorizacionService.listarPorCajero(idCajero).subscribe({
            next: (res: any) => {
                // Backend returns ApiResponse<AutorizacionResponse[]>
                this.solicitudes = res.data || [];
                this.isLoadingList = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando solicitudes:', err);
                this.isLoadingList = false;
                this.cd.detectChanges();
            }
        });
    }

    private calcularBaseElegible() {
        this.itemsElegibles = this.carrito.filter(item => 
            item.tipo === 'SERVICIO' && !item.idPlan && !item.isPackageItem
        );
        this.montoParticular = this.itemsElegibles.reduce((acc, item) => acc + (Number(item.total) || 0), 0);
        if (this.montoParticular === 0) {
            this.montoParticular = this.montoBase;
        }
    }

    onPctChange(valor: number) {
        if (valor > 20) valor = 20;
        if (valor < 0) valor = 0;
        this.pctDescuento = valor;
        this.montoDescuento = Number(((this.montoParticular * valor) / 100).toFixed(2));
        this.actualizarTotales();
    }

    onMontoChange(valor: number) {
        if (valor < 0) valor = 0;
        const maxMonto = (this.montoParticular * 20) / 100;
        if (valor > maxMonto) {
            valor = Number(maxMonto.toFixed(2));
        }
        this.montoDescuento = valor;
        if (this.montoParticular > 0) {
            this.pctDescuento = Number(((valor / this.montoParticular) * 100).toFixed(2));
        } else {
            this.pctDescuento = 0;
        }
        this.actualizarTotales();
    }

    actualizarTotales() {
        this.montoFinal = Number((this.montoBase - this.montoDescuento).toFixed(2));
        // Usar markForCheck en lugar de detectChanges para ser más suave con el ciclo de vida
        this.cd.markForCheck();
    }

    cerrar() {
        // Solo mostrar mensaje de cancelación si NO se ha enviado una solicitud con éxito
        if (!this.requestSent && (this.pctDescuento > 0 || this.motivoSolicitud.length > 0)) {
            this.alertasService.info('Operación Cancelada', 'No se enviaron solicitudes de descuento.');
        }
        this.cancel.emit();
    }

    aplicar(solicitud: AutorizacionResponse) {
        if (solicitud.estado !== 'APROBADO') return;
        this.applyDiscount.emit(solicitud);
        this.confirm.emit();
    }

    enviarSolicitud() {
        if (this.montoDescuento <= 0) {
            this.alertasService.warning('Atención', 'El monto de descuento debe ser mayor a 0.');
            return;
        }
        if (!this.motivoSolicitud || this.motivoSolicitud.trim().length < 3) {
            this.alertasService.warning('Atención', 'Debe especificar un motivo válido (mínimo 3 caracteres).');
            return;
        }

        this.isSubmitting = true;
        this.cd.markForCheck(); // Notificamos el cambio suavemente

        const payload: AutorizacionRequest = {
            idVenta: this.idOrden || undefined,
            montoOriginal: this.montoBase,
            montoDescuento: this.montoDescuento,
            montoFinal: this.montoFinal,
            motivoSolicitud: `[MANUAL ${this.pctDescuento}%] ${this.motivoSolicitud.trim()}`,
            idCajero: this.getAuthUserId(),
            pacienteNombre: this.paciente ? `${this.paciente.firstName} ${this.paciente.lastName || ''}` : undefined,
            pacienteDni: this.paciente?.numdoc,
            doctorNombre: this.doctorNombre || undefined
        };

        this.autorizacionService.solicitarDescuento(payload).subscribe({
            next: () => {
                this.isSubmitting = false;
                this.requestSent = true;
                this.alertasService.success('¡Solicitud Enviada!', 'Se ha notificado a gerencia correctamente.');
                this.setTab('LISTA');
                this.cd.markForCheck();
            },
            error: (err) => {
                console.error('Error enviando solicitud:', err);
                this.isSubmitting = false;
                this.alertasService.error('Error', 'No se pudo enviar la solicitud de autorización.');
                this.cd.markForCheck();
            }
        });
    }

    private getAuthUserId(): number {
        const token = localStorage.getItem('token');
        if (!token) return 0;
        try {
            const payloadBase64 = token.split('.')[1];
            const payloadDecoded = JSON.parse(atob(payloadBase64));
            return Number(payloadDecoded.userId || payloadDecoded.idPersonalUser || 0);
        } catch {
            return 0;
        }
    }

    ngOnDestroy(): void {
        if (this.notificationSub) {
            this.notificationSub.unsubscribe();
        }
    }
}

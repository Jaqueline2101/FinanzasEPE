import {
    Component,
    EventEmitter,
    Input,
    Output,
    OnInit,
    OnChanges,
    ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Servicio } from '../../../models/servicio.model';
import { ServicioService } from '../../../services/servicio.service';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { NivelSelectorComponent } from '../../../shared/components/nivel-selector/nivel-selector.component';
import { PricingConfigComponent, PricingData } from '../../../shared/components/pricing-config/pricing-config.component';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-servicio',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ModalComponent,
        NivelSelectorComponent,
        PricingConfigComponent
    ],
    templateUrl: './modal-servicio.component.html',
    styleUrls: ['./modal-servicio.component.css']
})
export class ModalServicioComponent implements OnInit, OnChanges {

    @Input() isVisible: boolean = false;
    @Input() servicio: Servicio | null = null;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    // ===== FORM DATA =====
    formData = {
        nombreServicio: '',
        codigo: '',
        descripcion: '',
        unidad: 1,
        tipoUnidad: '',
        precioSinIgv: 0,
        precioConIgv: 0,
        precioCompra: 0,
        margenGanancia: 0,
        honorariosMedicos: false,
        idNivel: null as number | null,
        idCategoria: null as number | null,
        tipo: 'SERVICIO',
        idTipoAtencion: null as number | null,
        tipoPrecio: 'PRECIO_FIJO',
        estado: 1,
        codigoSeus: '',
        regSanitario: '',
        moneda: 'PEN'
    };

    categoryMode: 'SEUS' | 'DIGEMID' = 'SEUS';
    isEditMode: boolean = false;

    categorias: any[] = [
        { id: 1, nombre: 'Antibióticos' },
        { id: 2, nombre: 'Material de Curación' },
        { id: 3, nombre: 'Equipos Médicos' },
        { id: 4, nombre: 'Insumos Médicos' },
        { id: 5, nombre: 'Material Odontológico' },
        { id: 6, nombre: 'Reactivos de Laboratorio' }
    ];

    tiposAtencion: any[] = [];

    get modalTitle(): string {
        return this.isEditMode ? 'Actualizar Servicio' : 'Nuevo Servicio';
    }

    constructor(
        private servicioService: ServicioService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit(): void {
        this.loadInitialData();
    }

    ngOnChanges(): void {
        if (this.isVisible) {
            if (this.servicio) {
                this.isEditMode = true;
                this.loadServicioData(this.servicio);
            } else {
                this.isEditMode = false;
                this.resetForm();
            }
        }
    }

    loadInitialData(): void {
        this.cargarTiposAtencion();
    }

    cargarTiposAtencion(): void {
        this.servicioService.getTiposAtencion().subscribe({
            next: (tipos: any[]) => {
                this.tiposAtencion = tipos;
                this.cd.detectChanges();
            },
            error: (err: any) => console.error('Error loading tipos atencion', err)
        });
    }

    onPricingChange(update: Partial<PricingData>): void {
        this.formData = { ...this.formData, ...update };
    }

    loadServicioData(servicio: Servicio): void {
        this.formData = {
            nombreServicio: servicio.nombreServicio,
            codigo: servicio.codigo || '',
            descripcion: servicio.descripcion || '',
            unidad: servicio.unidad !== undefined ? servicio.unidad : 1,
            tipoUnidad: servicio.tipoUnidad || '',
            precioSinIgv: servicio.precioSinIgv || 0,
            precioConIgv: servicio.precioConIgv || 0,
            precioCompra: servicio.precioCompra || 0,
            margenGanancia: servicio.margenGanancia || 0,
            honorariosMedicos: servicio.honorariosMedicos !== undefined ? servicio.honorariosMedicos : false,
            idNivel: servicio.nivel?.idNivel || (servicio as any).idNivel || null,
            idCategoria: (servicio as any).idCategoria || null,
            tipo: servicio.tipo || 'SERVICIO',
            idTipoAtencion: servicio.idTipoAtencion || null,
            tipoPrecio: (servicio as any).tipoPrecio || 'PRECIO_FIJO',
            estado: servicio.estado !== undefined ? Number(servicio.estado) : 1,
            codigoSeus: servicio.codigoSeus || '',
            regSanitario: (servicio as any).regSanitario || '',
            moneda: servicio.moneda || 'PEN'
        };

        if (this.formData.regSanitario && !this.formData.codigoSeus) {
            this.categoryMode = 'DIGEMID';
        } else {
            this.categoryMode = 'SEUS';
        }
    }

    resetForm(): void {
        this.formData = {
            nombreServicio: '',
            codigo: '',
            descripcion: '',
            unidad: 1,
            tipoUnidad: '',
            precioSinIgv: 0,
            precioConIgv: 0,
            precioCompra: 0,
            margenGanancia: 0,
            honorariosMedicos: false,
            idNivel: null,
            idCategoria: null,
            tipo: 'SERVICIO',
            idTipoAtencion: null,
            tipoPrecio: 'PRECIO_FIJO',
            estado: 1,
            codigoSeus: '',
            regSanitario: '',
            moneda: 'PEN'
        };
        this.categoryMode = 'SEUS';
    }

    close(): void {
        this.onClose.emit();
    }

    save(): void {
        if (!this.validarFormulario()) return;

        const request: any = {
            ...this.formData,
            estado: this.formData.estado ? 1 : 0,
            idNivel: Number(this.formData.idNivel),
            idCategoria: this.formData.idCategoria ? Number(this.formData.idCategoria) : null,
            idTipoAtencion: this.formData.idTipoAtencion ? Number(this.formData.idTipoAtencion) : null,
            unidad: Number(this.formData.unidad) || 1,
            precioConIgv: Number(this.formData.precioConIgv) || 0,
            precioSinIgv: Number(this.formData.precioSinIgv) || 0,
            precioCompra: Number(this.formData.precioCompra) || 0,
            margenGanancia: Number(this.formData.margenGanancia) || 0,
            codDigemid: this.categoryMode === 'DIGEMID' ? this.formData.regSanitario : '',
        };

        if (this.isEditMode && this.servicio?.id) {
            this.servicioService.update(this.servicio.id, request).subscribe({
                next: () => this.handleSuccess('Actualizado', 'Servicio actualizado correctamente'),
                error: (err: any) => this.handleError('Error', err.error?.message || 'No se pudo actualizar el servicio')
            });
        } else {
            this.servicioService.create(request).subscribe({
                next: () => this.handleSuccess('Creado', 'Servicio creado correctamente'),
                error: (err: any) => this.handleError('Error', err.error?.message || 'No se pudo crear el servicio')
            });
        }
    }

    validarFormulario(): boolean {
        const missingFields: string[] = [];
        if (!this.formData.nombreServicio) missingFields.push('Nombre del Catalogo');
        if (!this.formData.codigo) missingFields.push('Codigo');
        if (!this.formData.idNivel) missingFields.push('Nivel');
        if (!this.formData.idCategoria) missingFields.push('Categoría (Familia)');
        if (this.categoryMode === 'SEUS' && !this.formData.codigoSeus) missingFields.push('Codigo SEUS');
        if (this.categoryMode === 'DIGEMID' && !this.formData.regSanitario) missingFields.push('Registro Sanitario (DIGEMID)');
        if (this.formData.precioConIgv <= 0) missingFields.push('Precio Venta Final');
        if (!this.formData.unidad || this.formData.unidad <= 0) missingFields.push('Unidad');
        if (!this.formData.tipoUnidad) missingFields.push('Tipo Unidad');

        if (missingFields.length > 0) {
            this.alertasService.warning(
                'Campos Incompletos', 
                `Por favor, complete los siguientes campos obligatorios: ${missingFields.join(', ')}`
            );
            return false;
        }
        return true;
    }

    handleSuccess(title: string, text: string): void {
        this.alertasService.success(title, text);
        this.onSave.emit();
        this.close();
    }

    handleError(title: string, text: string): void {
        this.alertasService.error(title, text);
    }
}
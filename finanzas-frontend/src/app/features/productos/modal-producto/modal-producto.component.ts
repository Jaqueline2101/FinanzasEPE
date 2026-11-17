import { Component, EventEmitter, Input, OnInit, Output, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { AlertasService } from '../../../services/alertas.service';
import { Producto } from '../../../models/producto.model';
import { ModalComponent } from '../../../shared/components/modal/modal';

import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button';
import { ProductoService } from '../../../services/producto.service';
import { AuthService } from '../../auth/services/auth.service';

@Component({
    selector: 'app-modal-producto',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, ModalComponent, PrimaryButtonComponent],
    templateUrl: './modal-producto.component.html'
})
export class ModalProductoComponent implements OnInit {
    @Input() producto: Producto | null = null;
    @Input() allProducts: Producto[] = [];
    @Input() isOpen: boolean = false;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<any>();

    productoForm: FormGroup;
    isEditMode = false;
    productFound = false;
    currentStockTotal = 0;
    tiposTemperatura: any[] = [];
    private destroy$ = new Subject<void>();

    constructor(
        private fb: FormBuilder,
        private productoService: ProductoService,
        private authService: AuthService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) {
        this.productoForm = this.fb.group({
            idProducto: [null],
            idSucursal: [this.authService.currentUser()?.sucursalActualId || 1, Validators.required],
            idCatalogo: [null],

            codigo: ['', Validators.required],
            nombreProducto: ['', Validators.required],
            categoria: ['', Validators.required],
            codigoBarra: [''],
            marcaLaboratorio: [''],
            modelo: [''],
            unidadMedida: ['Unidad', Validators.required],
            moneda: ['PEN'],

            cantCompra: [1, [Validators.min(1)]],
            factorCompra: [1, [Validators.min(1)]],

            stockMinimo: [0, [Validators.required, Validators.min(0)]],
            stockMaximo: [0, [Validators.required, Validators.min(0)]],
            stockReal: [0, [Validators.required, Validators.min(0)]],
            manejaUnidad: [true],
            ubicacion: [''],

            precioCompra: [0, [Validators.required, Validators.min(0)]],

            fechaVencimiento: [''],
            registroInvimaLote: [''],
            regSanitario: [''],
            almacenamiento: ['Normal'],
            diasAlertaVencimiento: [30],

            tipoAfectacionCompra: ['GRAVADO_ONEROSO'],

            tipo: ['PRODUCTO']
        });
    }

    ngOnInit(): void {
        this.loadTiposTemperatura();
        this.setupSearchListeners();
        this.productoForm.valueChanges.pipe(
            takeUntil(this.destroy$)
        ).subscribe(val => {
            const cant = Number(val.cantCompra) || 1;
            const factor = Number(val.factorCompra) || 1; // Corrected from factorCaja to factorCompra
            const stockCalc = cant * factor;

            if (this.productoForm.get('stockReal')?.value !== stockCalc) {
                this.productoForm.get('stockReal')?.patchValue(stockCalc, { emitEvent: false });
            }
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isOpen'] && changes['isOpen'].currentValue === true && !changes['isOpen'].firstChange) {

            if (this.producto) {
                this.patchForm();
            } else {
                this.resetForm();
            }
        }
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    setupSearchListeners(): void {
        if (this.isEditMode) return;

        this.productoForm.get('codigo')?.valueChanges.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        ).subscribe(val => {
            if (!this.isEditMode) this.searchProduct(val, 'codigo');
        });

        this.productoForm.get('nombreProducto')?.valueChanges.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        ).subscribe(val => {
            if (!this.isEditMode) this.searchProduct(val, 'nombre');
        });
    }

    private searchProduct(value: string, type: 'codigo' | 'nombre'): void {
        if (!value || value.length < 3) {
            if (this.productFound) this.resetMasterData();
            return;
        }

        const match = this.allProducts.find(p => {
            if (type === 'codigo') {
                return (p.codigo?.toLowerCase() === value.toLowerCase() ||
                    p.codigoBarra?.toLowerCase() === value.toLowerCase() ||
                    p.catalogo?.codigo?.toLowerCase() === value.toLowerCase());
            } else {
                return (p.nombreProducto?.toLowerCase() === value.toLowerCase() ||
                    p.catalogo?.nombreServicio?.toLowerCase() === value.toLowerCase());
            }
        });

        if (match) {
            this.handleMatchFound(match);
        } else if (this.productFound) {
            this.resetMasterData();
        }
    }

    private handleMatchFound(match: Producto): void {
        this.productFound = true;
        this.currentStockTotal = this.allProducts
            .filter(p => p.idCatalogo === match.idCatalogo)
            .reduce((sum, p) => sum + (p.stockReal || 0), 0);

        const cat = match.catalogo;

        this.productoForm.patchValue({
            idCatalogo: match.idCatalogo,
            nombreProducto: match.nombreProducto || cat?.nombreServicio,
            codigo: match.codigo || cat?.codigo,
            categoria: match.categoria || cat?.tipoMedicamento,
            modelo: match.modelo || cat?.modelo,
            regSanitario: match.regSanitario || cat?.regSanitario,
            moneda: match.moneda || cat?.moneda || 'PEN',
            factorCompra: match.factorCompra || 1
        }, { emitEvent: false });

        this.lockMasterFields();
        this.cd.detectChanges();

        console.log('Match encontrado y campos bloqueados:', match);
    }

    private extractMarca(detalle: string | undefined): string {
        if (!detalle || !detalle.startsWith("Marca: ")) return '';
        const end = detalle.indexOf(" | Ubicación:");
        return end > 7 ? detalle.substring(7, end) : detalle.substring(7);
    }

    private lockMasterFields(): void {
        const fields = ['nombreProducto', 'categoria', 'modelo', 'marcaLaboratorio', 'unidadMedida'];
        fields.forEach(f => this.productoForm.get(f)?.disable());
    }

    private unlockMasterFields(): void {
        const fields = ['nombreProducto', 'categoria', 'modelo', 'marcaLaboratorio', 'unidadMedida'];
        fields.forEach(f => this.productoForm.get(f)?.enable());
    }

    public resetMasterData(): void {
        this.productFound = false;
        this.currentStockTotal = 0;
        this.productoForm.patchValue({ idCatalogo: null });
        this.unlockMasterFields();
    }

    loadTiposTemperatura(): void {
        // Mantenemos la estructura por compatibilidad con el HTML anterior si se desea, 
        // pero ahora usamos almacenamiento hardcodeado para inmediatez.
        this.tiposTemperatura = [
            { id: 1, nombre: 'Normal' },
            { id: 2, nombre: 'Refrigerado' },
            { id: 3, nombre: 'Congelado' }
        ];
    }

    private patchForm(): void {

        this.isEditMode = !!this.producto?.idProducto;
        const formValue = {
            ...this.producto,
            idProducto: this.producto?.idProducto,
            idCatalogo: this.producto?.idCatalogo,
            codigo: this.producto?.codigo || this.producto?.codigoBarra || this.producto?.catalogo?.codigo || '',
            nombreProducto: this.producto?.nombreProducto || this.producto?.catalogo?.nombreServicio || '',
            marcaLaboratorio: this.producto?.marcaLaboratorio || this.producto?.catalogo?.modelo || '',
            modelo: this.producto?.modelo || this.producto?.catalogo?.modelo || '',
            categoria: this.producto?.categoria || this.producto?.catalogo?.tipoMedicamento || '',
            codigoBarra: this.producto?.codigoBarra || this.producto?.catalogo?.codigoBarra || '',
            unidadMedida: this.producto?.unidadMedida || this.producto?.catalogo?.presentacion || 'Unidad',
            moneda: this.producto?.moneda || this.producto?.catalogo?.moneda || 'PEN',
            regSanitario: this.producto?.regSanitario || this.producto?.catalogo?.regSanitario || '',
            almacenamiento: this.producto?.almacenamiento || 'Normal',
            cantCompra: 1,
            factorCompra: this.producto?.factorCompra || 1
        };
        this.productoForm.patchValue(formValue);

        console.log('Formulario parchado con:', formValue);
        this.cd.detectChanges();

        if (!this.isEditMode && this.producto?.idCatalogo) {
            this.handleMatchFound(this.producto);
        }
    }

    private resetForm(): void {
        this.isEditMode = false;
        this.productoForm.reset({
            idSucursal: this.authService.currentUser()?.sucursalActualId || 1,
            stockReal: 0,
            stockMinimo: 5,
            stockMaximo: 100,
            tipoAfectacionCompra: 'GRAVADO_ONEROSO',
            manejaUnidad: true,
            moneda: 'PEN',
            unidadMedida: 'Unidad',
            tipo: 'PRODUCTO',
            almacenamiento: 'Normal',
            cantCompra: 1,
            factorCompra: 1
        });
    }

    get selectedTempRange(): string {
        const id = this.productoForm.get('idTipoTemperatura')?.value;
        if (!id) return '';
        const tipo = this.tiposTemperatura.find(t => t.id === Number(id));
        return tipo ? `${tipo.tempMin}°C a ${tipo.tempMax}°C` : '';
    }

    get totalStockDisplay(): number {
        const entry = this.productoForm.get('stockReal')?.value || 0;
        return this.currentStockTotal + entry;
    }

    close(): void {
        this.unlockMasterFields();
        this.onClose.emit();
    }

    save(): void {
        const rawValue = this.productoForm.getRawValue();

        // Limpieza de espacios y normalización de tipos
        const sanitizedValue = {
            ...rawValue,
            nombreProducto: rawValue.nombreProducto?.trim(),
            codigo: rawValue.codigo?.trim(),
            codigoBarra: rawValue.codigoBarra?.trim(),
            marcaLaboratorio: rawValue.marcaLaboratorio?.trim(),
            modelo: rawValue.modelo?.trim(),
            stockReal: Number(rawValue.stockReal) || 0,
            precioCompra: Number(rawValue.precioCompra) || 0
        };

        if (this.productoForm.valid || (this.productFound && this.isPartialValid())) {
            this.onSave.emit(sanitizedValue);
        } else {
            this.productoForm.markAllAsTouched();
            this.alertasService.warning(
                'Campos Obligatorios',
                'Por favor, complete todos los campos obligatorios para continuar.'
            );
        }
    }

    private isPartialValid(): boolean {
        return (this.productoForm.get('stockReal')?.valid ?? false) &&
            (this.productoForm.get('precioCompra')?.valid ?? false);
    }
}

import { ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormBuilder, Validators, FormArray } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header';
import { OrdenCompra, OrdenCompraDetalle } from '../../../models/orden-compra.model';
import { OrdenCompraService } from '../../../services/orden-compra.service';
import { ProveedorService } from '../../../services/proveedor.service';
import { Proveedor } from '../../../models/proveedor.model';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-orden-compra',
    standalone: true,
    imports: [CommonModule, FormsModule, ReactiveFormsModule],
    templateUrl: './modal-orden-compra.component.html',
    styleUrls: ['./modal-orden-compra.component.css']
})
export class ModalOrdenCompraComponent implements OnInit {
    @Input() orden?: OrdenCompra;
    @Output() onClose = new EventEmitter<void>();
    @Output() onSave = new EventEmitter<void>();

    form: FormGroup;
    loading = false;

    // ← NUEVO: control de secciones colapsables (ahora siempre desplegados por defecto)
    mostrarLogistica = true;
    mostrarControlInterno = true;

    // Búsqueda de Proveedores
    proveedores: Proveedor[] = [];
    buscandoProveedor = false;
    mostrarDropdownProveedores = false;
    queryBusqueda = '';
    private searchSubject = new Subject<string>();

    @ViewChild('containerProveedor') containerProveedor!: ElementRef;

    tiposCompra = ['INSUMOS', 'SERVICIOS', 'EQUIPOS', 'ARTÍCULOS'];
    condicionesPago = ['CONTADO', '30 DÍAS', '60 DÍAS', '90 DÍAS'];
    monedas = [
        { code: 'PEN', symbol: 'S/' },
        { code: 'USD', symbol: '$' }
    ];

    constructor(
        private fb: FormBuilder,
        private ordenService: OrdenCompraService,
        private proveedorService: ProveedorService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) {
        this.form = this.initForm();
    }

    ngOnInit(): void {
        if (this.orden) {
            this.patchForm(this.orden);
        }
        this.setupProveedorSearch();
    }

    initForm(): FormGroup {
        return this.fb.group({
            numeroOrden: ['', Validators.required],
            fechaEmision: [new Date().toISOString().split('T')[0], Validators.required],
            fechaEstimadaEntrega: [''],
            tipoCompra: ['INSUMOS'],
            centroCosto: [''],
            areaSolicitante: [''],
            responsableCompra: [''],
            moneda: ['PEN', Validators.required],
            tipoCambio: [1, Validators.required],
            observaciones: [''],

            // Adelanto
            permiteAdelanto: [false],
            tipoAdelanto: ['PORCENTAJE'],
            porcentajeAdelanto: [0],
            montoAdelanto: [0],
            fechaAdelanto: [''],

            // Proveedor
            idProveedor: [null, Validators.required],
            rucProveedor: [{ value: '', disabled: true }],
            razonSocialProveedor: [{ value: '', disabled: true }],
            nombreComercial: [{ value: '', disabled: true }],
            direccionFiscal: [{ value: '', disabled: true }],
            condicionPago: ['CONTADO'],
            plazoDias: [0],

            // Logística
            lugarEntrega: [''],
            direccionEntrega: [''],
            fechaEntregaAcordada: [''],
            responsableRecepcion: [''],
            observacionesLogisticas: [''],

            // Control Interno
            numCotizacionRelacionada: [''],
            numRequerimientoInterno: [''],

            estado: ['PENDIENTE'],

            // Detalle
            detalles: this.fb.array([])
        });
    }

    get detallesFormArray(): FormArray {
        return this.form.get('detalles') as FormArray;
    }

    patchForm(orden: OrdenCompra) {
        this.form.patchValue(orden);
        // Limpiar y cargar detalles
        this.detallesFormArray.clear();
        orden.detalles.forEach(d => this.agregarItem(d));
        this.calcularTotales();
    }

    private setupProveedorSearch() {
        this.searchSubject.pipe(
            debounceTime(300),
            distinctUntilChanged()
        ).subscribe(query => {
            this.buscandoProveedor = true;
            if (query.trim().length === 0) {
                this.cargarTodosProveedores();
            } else {
                this.proveedorService.buscarPorNombre(query).subscribe({
                    next: (res: Proveedor[]) => {
                        this.proveedores = res.filter(p => p.estado !== 'Inhabilitado');
                        this.buscandoProveedor = false;
                        this.cd.detectChanges();
                    },
                    error: () => {
                        this.buscandoProveedor = false;
                        this.cd.detectChanges();
                    }
                });
            }
        });
    }

    cargarTodosProveedores() {
        this.buscandoProveedor = true;
        this.proveedorService.getAll().subscribe({
            next: (res: Proveedor[]) => {
                this.proveedores = res.filter(p => p.estado !== 'Inhabilitado');
                this.buscandoProveedor = false;
                this.cd.detectChanges();
            },
            error: () => {
                this.buscandoProveedor = false;
                this.cd.detectChanges();
            }
        });
    }

    toggleDropdown() {
        this.mostrarDropdownProveedores = !this.mostrarDropdownProveedores;
        if (this.mostrarDropdownProveedores && this.proveedores.length === 0) {
            this.cargarTodosProveedores();
        }
        this.cd.detectChanges();
    }

    onInputSearch(event: Event) {
        const query = (event.target as HTMLInputElement).value;
        this.queryBusqueda = query;
        this.searchSubject.next(query);
        this.mostrarDropdownProveedores = true;
        this.cd.detectChanges();
    }

    limpiarProveedor() {
        this.queryBusqueda = '';
        this.form.patchValue({
            idProveedor: null,
            rucProveedor: '',
            razonSocialProveedor: '',
            nombreComercial: '',
            direccionFiscal: '',
            plazoDias: 0
        });
        this.proveedores = [];
        this.mostrarDropdownProveedores = false;
    }

    @HostListener('document:click', ['$event'])
    onClickOutside(event: MouseEvent) {
        if (this.containerProveedor && !this.containerProveedor.nativeElement.contains(event.target)) {
            this.mostrarDropdownProveedores = false;
            this.cd.detectChanges();
        }
    }

    seleccionarProveedor(p: Proveedor) {
        this.form.patchValue({
            idProveedor: p.id,
            rucProveedor: p.numDocIdent,
            razonSocialProveedor: p.razonSocial,
            nombreComercial: p.nombreComercial,
            direccionFiscal: p.direccion || p.sedes || '-',
            plazoDias: p.plazo || 0
        });
        this.mostrarDropdownProveedores = false;
    }

    agregarItem(detalle?: OrdenCompraDetalle) {
        const item = this.fb.group({
            codigoItem: [detalle?.codigoItem || ''],
            descripcion: [detalle?.descripcion || '', Validators.required],
            unidadMedida: [detalle?.unidadMedida || 'UND'],
            cantidad: [detalle?.cantidad || 1, [Validators.required, Validators.min(0.001)]],
            precioUnitario: [detalle?.precioUnitario || 0, [Validators.required, Validators.min(0)]],
            descuento: [detalle?.descuento || 0],
            afectoIgv: [detalle?.afectoIgv ?? true],
            subtotal: [{ value: detalle?.subtotal || 0, disabled: true }],
            igv: [{ value: detalle?.igv || 0, disabled: true }],
            total: [{ value: detalle?.total || 0, disabled: true }]
        });

        // Suscribirse a cambios para recalcular fila
        item.valueChanges.subscribe(() => this.recalcularFila(item));
        this.detallesFormArray.push(item);
        this.calcularTotales();
    }

    removerItem(index: number) {
        this.detallesFormArray.removeAt(index);
        this.calcularTotales();
    }

    recalcularFila(group: FormGroup) {
        const cant = group.get('cantidad')?.value || 0;
        const precio = group.get('precioUnitario')?.value || 0;
        const desc = group.get('descuento')?.value || 0;
        const afecto = group.get('afectoIgv')?.value;

        const sub = (cant * precio) - desc;
        const igv = afecto ? sub * 0.18 : 0;
        const total = sub + igv;

        group.patchValue({
            subtotal: sub,
            igv: igv,
            total: total
        }, { emitEvent: false });

        this.calcularTotales();
    }

    totales = {
        subtotal: 0,
        descuento: 0,
        igv: 0,
        total: 0
    };

    calcularTotales() {
        this.totales = this.detallesFormArray.controls.reduce((acc, ctrl: any) => {
            const v = ctrl.getRawValue(); // IMPORTANTE: getRawValue() incluye campos disabled
            acc.subtotal += (v.cantidad * v.precioUnitario);
            acc.descuento += (v.descuento || 0);
            acc.igv += (v.igv || 0);
            acc.total += (v.total || 0);
            return acc;
        }, { subtotal: 0, descuento: 0, igv: 0, total: 0 });

        // Si permite adelanto y es porcentaje, calcular monto
        const permite = this.form.get('permiteAdelanto')?.value;
        const tipo = this.form.get('tipoAdelanto')?.value;
        if (permite && tipo === 'PORCENTAJE') {
            const perc = this.form.get('porcentajeAdelanto')?.value || 0;
            this.form.patchValue({
                montoAdelanto: (this.totales.total * perc) / 100
            }, { emitEvent: false });
        }
    }

    guardar() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const data = {
            ...this.form.getRawValue(),
            subtotal: this.totales.subtotal - this.totales.descuento,
            totalDescuento: this.totales.descuento,
            totalIgv: this.totales.igv,
            totalGeneral: this.totales.total,
            usuarioRegistro: 'ADMIN' // Temporal
        };

        this.loading = true;
        const action = this.orden?.id
            ? this.ordenService.actualizar(this.orden.id, data)
            : this.ordenService.registrar(data);

        action.subscribe({
            next: () => {
                this.loading = false;
                this.alertasService.success('¡Éxito!', `Orden de compra ${this.orden?.id ? 'actualizada' : 'registrada'} correctamente`);
                this.onSave.emit();
            },
            error: (err) => {
                console.error('Error al guardar orden', err);
                this.loading = false;
                this.alertasService.error('Error', 'No se pudo guardar la orden de compra');
            }
        });
    }

    aprobar() {
        if (!this.orden?.id) return;

        this.alertasService.confirm(
            '¿Confirmar aprobación?',
            'Una vez aprobada, la orden no podrá ser editada.'
        ).then((confirmed) => {
            if (confirmed) {
                this.loading = true;
                this.ordenService.aprobar(this.orden!.id!, 'ADMIN').subscribe({
                    next: () => {
                        this.loading = false;
                        this.alertasService.success('Aprobado', 'La orden ha sido aprobada correctamente');
                        this.onSave.emit();
                    },
                    error: (err) => {
                        this.loading = false;
                        console.error('Error al aprobar orden', err);
                        this.alertasService.error('Error', 'No se pudo aprobar la orden');
                    }
                });
            }
        });
    }

    getEstadoClass(): string {
        const estado = this.form.get('estado')?.value;
        switch (estado) {
            case 'PENDIENTE':
                return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'APROBADO':
                return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            case 'RECHAZADO':
                return 'bg-rose-100 text-rose-700 border-rose-200';
            case 'BORRADOR':
                return 'bg-slate-100 text-slate-700 border-slate-200';
            default:
                return 'bg-slate-50 text-slate-500 border-slate-100';
        }
    }

    close() {
        this.onClose.emit();
    }
}
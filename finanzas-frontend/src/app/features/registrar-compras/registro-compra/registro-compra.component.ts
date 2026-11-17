import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Observable, of, tap } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BreadcrumbComponent, BreadcrumbItem } from '../../../shared/components/breadcrumb/breadcrumb';
import { CompraService } from '../../../services/compra.service';
import { ProveedorService } from '../../../services/proveedor.service';
import { ProductoService } from '../../../services/producto.service';
import { AuthService } from '../../auth/services/auth.service';
import { AlertasService } from '../../../services/alertas.service';
import Swal from 'sweetalert2';

import { ModalProveedorComponent } from '../../proveedores/modal-proveedor/modal-proveedor.component';
import { AlertasComponent } from '../../../shared/components/alertas/alertas';

@Component({
    selector: 'app-registro-compra',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        RouterModule,
        BreadcrumbComponent,
        ModalProveedorComponent,
        AlertasComponent
    ],
    templateUrl: './registro-compra.component.html'
})
export class RegistroCompraComponent implements OnInit {
    form: FormGroup;
    loading = false;
    proveedores$: Observable<any[]> = of([]);
    proveedoresList: any[] = [];
    rucProveedor: string = '';
    productosDisponibles: any[] = [];
    showProveedorModal = false;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Inicio', route: '/admin/dashboard' },
        { label: 'Compras', route: '/admin/procesos/compras' },
        { label: 'Registrar' }
    ];

    constructor(
        private fb: FormBuilder,
        private compraService: CompraService,
        private proveedorService: ProveedorService,
        private productoService: ProductoService,
        private authService: AuthService,
        private alertasService: AlertasService,
        private router: Router,
        private cd: ChangeDetectorRef
    ) {
        const today = new Date().toISOString().substring(0, 10);
        const sucursalId = this.authService.currentUser()?.sucursalActualId || 1;

        this.form = this.fb.group({
            idSucursal: [sucursalId],
            idProveedor: [null, [Validators.required]],
            tipoComprobante: ['FACTURA', [Validators.required]],
            serie: ['', [Validators.required]],
            correlativo: ['', [Validators.required]],
            fechaEmision: [today, [Validators.required]],
            fechaVencimiento: [today],
            condicionPago: ['CONTADO', [Validators.required]],
            cuotas: [1],
            diasFrecuencia: [30],
            moneda: ['PEN', [Validators.required]],
            tipoCambio: [{ value: null, disabled: true }],
            impDolar: [{ value: 0, disabled: true }],
            impBruto: [0],
            descuento: [0],
            otrosTributos: [0],
            detraccion: [false],
            retencion: ['No'],
            medioPago: ['EFECTIVO_DEMAS'],

            valorVentaGravado: [0],
            valorVentaInafecto: [0],
            subtotal: [0],
            igv: [0],
            total: [0],
            percepcion: [0],
            totalPagar: [0],
            preciosIncluyenIgv: [false],
            porcentajeIgv: [18],

            detalles: this.fb.array([])
        });
    }

    ngOnInit(): void {
        this.loadCatalogos();
        this.setupMonedaLogic();
        this.setupTaxLogic();
    }

    setupTaxLogic() {
        this.form.get('porcentajeIgv')?.valueChanges.subscribe(() => {
            this.recalcularTodo();
        });
    }

    setupMonedaLogic() {

        this.form.get('moneda')?.valueChanges.subscribe(moneda => {
            if (moneda === 'USD') {
                this.form.get('impDolar')?.enable();
                this.form.get('tipoCambio')?.enable();
                this.form.get('impBruto')?.disable();
            } else {
                this.form.get('impDolar')?.disable();
                this.form.get('impDolar')?.setValue(0);
                this.form.get('tipoCambio')?.disable();
                this.form.get('tipoCambio')?.setValue(null);
                this.form.get('impBruto')?.enable();
            }
        });

        this.form.get('impDolar')?.valueChanges.subscribe(() => this.calcularBrutoUSD());
        this.form.get('tipoCambio')?.valueChanges.subscribe(() => this.calcularBrutoUSD());
    }

    calcularBrutoUSD() {
        if (this.form.get('moneda')?.value === 'USD') {
            const dolar = this.form.get('impDolar')?.value || 0;
            const tc = this.form.get('tipoCambio')?.value || 0;
            const brutoSoles = dolar * tc;

            this.form.get('impBruto')?.setValue(parseFloat(brutoSoles.toFixed(2)));
        }
    }

    get detalles(): FormArray {
        return this.form.get('detalles') as FormArray;
    }

    proveedoresFiltrados: any[] = [];
    mostrarDropdownProveedores: boolean = false;
    busquedaProveedor: string = '';

    busquedaProductoMaestro: string = '';
    productosFiltradosMaestro: any[] = [];
    mostrarDropdownMaestro: boolean = false;

    loadCatalogos() {
        this.proveedorService.getAll().pipe(
            tap(data => {
                this.proveedoresList = data;
                this.proveedoresFiltrados = data;
            })
        ).subscribe();

        this.productoService.getAll().subscribe(data => {
            const mapUnique = new Map();
            data.forEach(p => {
                if (p) {
                    const trueKey = p.idCatalogo || p.codigo || p.idProducto;
                    if (trueKey && !mapUnique.has(trueKey)) {
                        mapUnique.set(trueKey, p);
                    }
                }
            });
            const uniqueProducts = Array.from(mapUnique.values());

            this.productosDisponibles = uniqueProducts;
            this.productosFiltradosMaestro = uniqueProducts;
        });
    }

    agregarDetalle(producto?: any) {
        const detalle = this.fb.group({
            idProducto: [producto ? producto.idProducto : null],
            productoNombre: [producto ? producto.nombreProducto : ''],
            busquedaProducto: [producto ? `[${producto.codigo || ''}] ${producto.nombreProducto}` : ''],
            cantidad: [1, [Validators.required, Validators.min(0.01)]],
            unidadMedida: [producto ? (producto.unidadMedida || 'UNI') : 'UNI'],
            factorConversion: [producto ? (producto.factorCompra || 1) : 1],
            lote: [''],
            fechaVencimiento: [null],
            precioUnitario: [producto ? (producto.precioCompra || 0) : 0, [Validators.required, Validators.min(0)]],
            porcentajeDescuento: [0],
            esBonificacion: [false],
            tipoAfectacion: [producto ? (producto.tipoAfectacionCompra || 'GRAVADO_ONEROSO') : 'GRAVADO_ONEROSO', Validators.required],
            valorVenta: [0],
            descripcion: ['']
        });

        this.detalles.push(detalle);
        if (producto) {
            this.calcularFila(this.detalles.length - 1);
        }
    }

    filtrarProductosMaestro(term: string) {
        this.busquedaProductoMaestro = term;
        this.mostrarDropdownMaestro = true;

        if (!term) {
            this.productosFiltradosMaestro = this.productosDisponibles;
            return;
        }

        const lowerTerm = term.toLowerCase();
        this.productosFiltradosMaestro = this.productosDisponibles.filter(p =>
            p.nombreProducto.toLowerCase().includes(lowerTerm) ||
            (p.codigo && p.codigo.toLowerCase().includes(lowerTerm)) ||
            (p.codigoBarra && p.codigoBarra.toLowerCase().includes(lowerTerm))
        );
    }

    seleccionarProductoMaestro(producto: any) {
        this.agregarDetalle(producto);
        this.busquedaProductoMaestro = '';
        this.mostrarDropdownMaestro = false;
        this.cd.detectChanges();
    }

    limpiarBusquedaMaestro() {
        this.busquedaProductoMaestro = '';
        this.productosFiltradosMaestro = this.productosDisponibles;
        this.mostrarDropdownMaestro = false;
    }

    onMaestroSearchFocus() {
        if (this.productosDisponibles.length > 0) {
            this.mostrarDropdownMaestro = true;
        }
    }

    onMaestroSearchBlur() {
        setTimeout(() => {
            this.mostrarDropdownMaestro = false;
        }, 200);
    }


    eliminarDetalle(index: number) {
        this.detalles.removeAt(index);
        this.calcularTotales();
    }

    filtrarProveedores(term: string) {
        this.busquedaProveedor = term;
        this.mostrarDropdownProveedores = true;

        if (!term) {
            this.proveedoresFiltrados = this.proveedoresList;
            return;
        }

        const lowerTerm = term.toLowerCase();
        this.proveedoresFiltrados = this.proveedoresList.filter(p =>
            p.razonSocial.toLowerCase().includes(lowerTerm) ||
            (p.numDocIdent && p.numDocIdent.includes(lowerTerm))
        );
    }

    seleccionarProveedor(proveedor: any) {
        this.form.patchValue({ idProveedor: proveedor.id });
        this.busquedaProveedor = proveedor.razonSocial;
        this.rucProveedor = proveedor.numDocIdent || '';
        this.mostrarDropdownProveedores = false;
    }

    limpiarBusquedaProveedor() {
        this.busquedaProveedor = '';
        this.proveedoresFiltrados = [...this.proveedoresList];
        this.form.patchValue({ idProveedor: null });
        this.rucProveedor = '';
        this.mostrarDropdownProveedores = true;
    }

    onBusquedaFocus() {
        this.mostrarDropdownProveedores = true;
        this.filtrarProveedores(this.busquedaProveedor);
    }

    onBusquedaBlur() {
        setTimeout(() => {
            this.mostrarDropdownProveedores = false;
            const id = this.form.get('idProveedor')?.value;
            if (!id && this.busquedaProveedor) {
            }
        }, 200);
    }

    openNuevoProveedorModal() {
        this.showProveedorModal = true;
    }

    onProveedorSaved(newProveedor: any) {
        this.proveedorService.create(newProveedor).subscribe({
            next: (savedP) => {
                this.showProveedorModal = false;
                this.alertasService.success('Éxito', 'Proveedor registrado correctamente');

                this.proveedorService.getAll().subscribe(data => {
                    this.proveedoresList = data;
                    this.proveedoresFiltrados = data;

                    setTimeout(() => {
                        const found = data.find(p => p.numDocIdent === savedP.numDocIdent || p.id === savedP.id);
                        if (found) {
                            this.seleccionarProveedor(found);
                            this.cd.detectChanges();
                        }
                    });
                });
            },
            error: (err) => {
                console.error(err);
                Swal.fire('Error', 'No se pudo registrar el proveedor', 'error');
            }
        });
    }

    onProductoChange(index: number) {
        const row = this.detalles.at(index);
        const idProd = row.get('idProducto')?.value;
        const producto = this.productosDisponibles.find(p => p.idProducto == idProd);

        if (producto) {
            row.patchValue({
                productoNombre: producto.nombreProducto,
                unidadMedida: producto.unidadMedida || 'UNI',
                precioUnitario: producto.precioCompra || 0,
                factorConversion: producto.factorCompra || 1,
                tipoAfectacion: producto.tipoAfectacionCompra || 'GRAVADO_ONEROSO'
            });
            this.calcularFila(index);
            this.cd.detectChanges();
        }
    }

    onBonificacionChange(index: number) {
        const row = this.detalles.at(index);
        const esBoni = row.get('esBonificacion')?.value;
        const tipoAfectacionActual = row.get('tipoAfectacion')?.value || 'GRAVADO_ONEROSO';

        if (esBoni) {
            row.patchValue({
                tipoAfectacion: tipoAfectacionActual.includes('GRAVADO') ? 'GRAVADO_GRATUITO' : 'INAFECTO_GRATUITO'
            });
        } else {
            row.patchValue({
                tipoAfectacion: tipoAfectacionActual.includes('GRAVADO') ? 'GRAVADO_ONEROSO' : 'INAFECTO_ONEROSO'
            });
        }
        this.calcularFila(index);
    }

    calcularFila(index: number) {
        const row = this.detalles.at(index);
        const cant = row.get('cantidad')?.value || 0;
        const precioInput = row.get('precioUnitario')?.value || 0;
        const incluyeIgv = this.form.get('preciosIncluyenIgv')?.value;
        const esBonificacion = row.get('esBonificacion')?.value;
        const tipoAfectacion = row.get('tipoAfectacion')?.value;

        if (esBonificacion) {
            row.patchValue({ valorVenta: 0 }, { emitEvent: false });
            this.calcularTotales();
            return;
        }


        let precioBase = precioInput;
        const taxRate = this.form.get('porcentajeIgv')?.value || 18;

        if (incluyeIgv && tipoAfectacion?.startsWith('GRAVADO')) {
            precioBase = precioBase / (1 + (taxRate / 100));
        }

        const totalLinea = cant * precioBase;

        row.patchValue({ valorVenta: totalLinea }, { emitEvent: false });
        this.calcularTotales();
    }

    calcularTotales() {
        let subtotalGravado = 0;
        let subtotalInafecto = 0;

        this.detalles.controls.forEach(control => {
            const val = control.get('valorVenta')?.value || 0;
            const tipoAfectacion = control.get('tipoAfectacion')?.value;

            if (tipoAfectacion?.startsWith('GRAVADO')) {
                subtotalGravado += val;
            } else if (tipoAfectacion?.startsWith('INAFECTO') || tipoAfectacion?.startsWith('EXONERADO')) {
                subtotalInafecto += val;
            }
        });

        const taxRate = this.form.get('porcentajeIgv')?.value || 18;
        const igv = subtotalGravado * (taxRate / 100);
        const subtotal = subtotalGravado + subtotalInafecto;
        const total = subtotal + igv;

        this.form.patchValue({
            valorVentaGravado: subtotalGravado,
            valorVentaInafecto: subtotalInafecto,
            subtotal: subtotal,
            igv: igv,
            total: total,
            totalPagar: total
        }, { emitEvent: false });
    }

    recalcularTodo() {
        this.detalles.controls.forEach((_, index) => {
            this.calcularFila(index);
        });
    }

    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            let msg = 'Por favor complete los campos obligatorios (*).';

            if (this.detalles.length > 0) {
                const invalidRows = this.detalles.controls.some(c => c.invalid);
                if (invalidRows) {
                    msg = 'Hay productos en la lista sin seleccionar o con datos incompletos. Verifique las filas marcadas en rojo.';
                }
            }

            this.alertasService.warning('Faltan Datos', msg);
            return;
        }

        if (this.detalles.length === 0) {
            this.alertasService.warning('Resumen Vacío', 'Debe agregar al menos un producto');
            return;
        }

        this.loading = true;
        const formValue = this.form.getRawValue();

        const sanitizedDetails = formValue.detalles.map((det: any) => ({
            ...det,
            unidadMedida: null,
            factorConversion: det.factorConversion ? parseInt(det.factorConversion.toString()) : 1
        }));

        const finalPayload = {
            ...formValue,
            detalles: sanitizedDetails
        };

        this.compraService.registrar(finalPayload).subscribe({
            next: (resp) => {
                this.alertasService.success('Éxito', 'Compra registrada correctamente N° ' + resp.serie + '-' + resp.correlativo);
                this.router.navigate(['/admin/procesos/compras']);
            },
            error: (err) => {
                console.error('Error al registrar compra:', err);
                this.alertasService.error('Error', 'No se pudo registrar la compra. Verifique los datos.');
                this.loading = false;
            }
        });
    }
}

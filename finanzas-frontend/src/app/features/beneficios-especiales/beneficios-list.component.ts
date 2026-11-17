import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb';
import { BeneficiosService, BeneficioItem } from '../../services/beneficios.service';
import { AlertasService } from '../../services/alertas.service';
import { DescuentoGeneralComponent } from './descuento-especial/descuento-general.component';
import { ModalPlanComponent } from './modal-plan/modal-plan.component';
import { ModalDetalleComponent } from './modal-detalle/modal-detalle.component';

@Component({
    selector: 'app-beneficios-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        PageHeaderComponent,
        BreadcrumbComponent,
        DescuentoGeneralComponent,
        ModalPlanComponent,
        ModalDetalleComponent
    ],
    templateUrl: './beneficios-list.component.html',
    styles: []
})
export class BeneficiosListComponent implements OnInit, OnDestroy {
    @Input() companiaId?: number;
    @ViewChild(ModalPlanComponent) modalPlan!: ModalPlanComponent;
    @ViewChild(ModalDetalleComponent) modalDetalle!: ModalDetalleComponent;

    // Menú contextual
    menuAbierto: number | null = null;

    breadcrumbItems: BreadcrumbItem[] = [
        { label: 'Configuraciones', route: '/configuraciones' },
        { label: 'Beneficios Especiales' }
    ];

    tipoSeleccionado: 'GENERAL' | 'FAMILIAR' = 'GENERAL';
    itemSeleccionado: number | null = null;

    // Listas INDEPENDIENTES — cada tipo mantiene la suya
    itemsGeneral: BeneficioItem[] = [];
    itemsFamiliar: BeneficioItem[] = [];

    cargandoGeneral = false;
    cargandoFamiliar = false;
    errorCarga: string | null = null;

    constructor(
        private beneficiosService: BeneficiosService,
        private alertasService: AlertasService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.cargarGeneral();
        this.cargarFamiliar();
    }

    ngOnDestroy(): void { /* cleanup si hace falta */ }

    /** Cierra el menú al hacer clic fuera de cualquier item */
    @HostListener('document:click')
    onDocumentClick() {
        if (this.menuAbierto !== null) {
            this.menuAbierto = null;
            this.cd.detectChanges();
        }
    }

    /** Lista activa según tipo seleccionado */
    itemsActuales(): BeneficioItem[] {
        return this.tipoSeleccionado === 'FAMILIAR' ? this.itemsFamiliar : this.itemsGeneral;
    }

    get cargando(): boolean {
        return this.tipoSeleccionado === 'FAMILIAR' ? this.cargandoFamiliar : this.cargandoGeneral;
    }

    seleccionarTipo(tipo: 'GENERAL' | 'FAMILIAR') {
        this.tipoSeleccionado = tipo;
        this.itemSeleccionado = null;
        this.cd.detectChanges();
    }

    seleccionarItem(id: number) {
        this.itemSeleccionado = id;
        this.cd.detectChanges();
    }

    /** Devuelve el objeto completo del item seleccionado para pasarlo al componente hijo */
    itemSeleccionadoObj(): BeneficioItem | null {
        if (this.itemSeleccionado === null) return null;
        return this.itemsActuales().find(i => i.idPlan === this.itemSeleccionado) || null;
    }

    abrirModal() {
        this.modalPlan.abrir();
    }

    /** Recibe el plan recién creado desde ModalPlanComponent y lo prepende a la lista correcta */
    onPlanCreado(nuevo: BeneficioItem) {
        if (this.tipoSeleccionado === 'FAMILIAR') {
            this.itemsFamiliar = [nuevo, ...this.itemsFamiliar];
        } else {
            this.itemsGeneral = [nuevo, ...this.itemsGeneral];
        }
        this.cd.detectChanges();
    }

    // ── Menú contextual ────────────────────────────────

    toggleMenu(idPlan: number, event: Event) {
        event.stopPropagation();
        this.menuAbierto = this.menuAbierto === idPlan ? null : idPlan;
        this.cd.detectChanges();
    }

    cerrarMenu() {
        this.menuAbierto = null;
        this.cd.detectChanges();
    }

    verDetalle(item: BeneficioItem) {
        this.seleccionarItem(item.idPlan);
        this.modalDetalle.abrir(item);
    }

    editarItem(item: BeneficioItem) {
        this.modalPlan.abrirEditar(item);
    }

    /** Actualiza el item en la lista cuando el modal de edición confirma el guardado */
    onPlanActualizado(actualizado: BeneficioItem) {
        const actualizar = (lista: BeneficioItem[]) =>
            lista.map(i => i.idPlan === actualizado.idPlan ? actualizado : i);

        this.itemsGeneral = actualizar(this.itemsGeneral);
        this.itemsFamiliar = actualizar(this.itemsFamiliar);
        this.cd.detectChanges();
    }

    eliminarItem(item: BeneficioItem) {
        this.alertasService.deleteConfirm(
            '¿Eliminar plan?',
            `¿Estás seguro de que deseas eliminar "${item.nombre}" (${item.codigo})? Esta acción no se puede deshacer.`
        ).then(confirmed => {
            if (!confirmed) return;

            this.beneficiosService.eliminar(item.idPlan).subscribe({
                next: () => {
                    // Eliminar de ambas listas
                    this.itemsGeneral = this.itemsGeneral.filter(i => i.idPlan !== item.idPlan);
                    this.itemsFamiliar = this.itemsFamiliar.filter(i => i.idPlan !== item.idPlan);
                    if (this.itemSeleccionado === item.idPlan) this.itemSeleccionado = null;
                    
                    this.alertasService.success('Eliminado', 'El plan ha sido eliminado correctamente');
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error eliminando', err);
                    this.alertasService.error('Error', 'No se pudo eliminar el plan');
                }
            });
        });
    }

    cargarGeneral() {
        this.cargandoGeneral = true;
        this.errorCarga = null;
        this.beneficiosService.listarPorTipo('GENERAL').subscribe({
            next: (data) => {
                this.itemsGeneral = data;
                this.cargandoGeneral = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando generales', err);
                this.cargandoGeneral = false;
                this.cd.detectChanges();
            }
        });
    }

    cargarFamiliar() {
        this.cargandoFamiliar = true;
        this.beneficiosService.listarPorTipo('FAMILIAR').subscribe({
            next: (data) => {
                this.itemsFamiliar = data;
                this.cargandoFamiliar = false;
                this.cd.detectChanges();
            },
            error: (err) => {
                console.error('Error cargando familiares', err);
                this.cargandoFamiliar = false;
                this.cd.detectChanges();
            }
        });
    }

    // Compat
    abrirDescuentoGeneral() { this.seleccionarTipo('GENERAL'); }
    abrirDescuentoFamiliar() { this.seleccionarTipo('FAMILIAR'); }
    crearNuevo() { this.abrirModal(); }
}
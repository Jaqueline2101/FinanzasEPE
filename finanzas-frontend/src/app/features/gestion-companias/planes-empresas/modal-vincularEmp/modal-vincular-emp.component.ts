import { Component, EventEmitter, Input, Output, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmpresaService, Empresa } from '../../../../services/empresa.service';
import { ConvenioService } from '../../../../services/convenio.service';
import { ModalComponent } from '../../../../shared/components/modal/modal';
import { AlertasService } from '../../../../services/alertas.service';

@Component({
    selector: 'app-modal-vincular-emp',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-vincular-emp.component.html',
    styles: [`
    @keyframes fadeIn {
      from { opacity: 0; transform: scale(0.95); }
      to { opacity: 1; transform: scale(1); }
    }
    .animate-fadeIn {
      animation: fadeIn 0.2s ease-out;
    }
  `]
})
export class ModalVincularEmpComponent {
    @Input() companiaId: number | null = null;
    @Input() existingLinkedIds: Set<any> = new Set();
    @Output() onClose = new EventEmitter<void>();
    @Output() onVinculacionExitosa = new EventEmitter<void>();

    allEmpresas: Empresa[] = [];
    filteredEmpresas: Empresa[] = [];
    searchTerm: string = '';
    loading: boolean = false;
    isSaving: boolean = false;
    selectedEmpresas: Set<number> = new Set();

    planForm: any = {
        nombreBase: '',
        fechaInicio: new Date().toISOString().split('T')[0],
        fechaFin: '',
        estado: '1',
        idOrigenClon: null,
        // Programador Senior: Iniciamos con valores base razonables por si falla la carga
        copago: 55,
        deducible: 0,
        porcentajeCoaseguro: 80
    };

    codigoBaseAuto: string = 'Cargando...';

    constructor(
        private empresaService: EmpresaService,
        private convenioService: ConvenioService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit() {
        this.loadAllEmpresas();
        this.loadCodigoBase();
        this.loadExistingPlansAndSetDefaults();
    }

    /**
     * Programador Senior: Lógica de Herencia Inteligente.
     * Recuperamos el historial de la compañía para pre-cargar los valores más probables.
     */
    private loadExistingPlansAndSetDefaults(): void {
        if (!this.companiaId) return;

        this.convenioService.listarPlanesPorCompania(this.companiaId).subscribe({
            next: (planes: any[]) => {
                if (planes && planes.length > 0) {
                    // Ordenamos por ID descendente para obtener el más reciente
                    const ultimoPlan = planes.sort((a, b) => b.idPlan - a.idPlan)[0];
                    
                    console.log('Programador Senior: Aplicando herencia de último plan:', ultimoPlan);
                    
                    // Aplicamos los valores del último plan con fallback de seguridad
                    this.planForm.copago = ultimoPlan.copago ?? 55;
                    this.planForm.deducible = ultimoPlan.deducible ?? 0;
                    this.planForm.porcentajeCoaseguro = ultimoPlan.porcentajeCoaseguro ?? 80;
                    this.cd.detectChanges();
                }
            },
            error: (err) => {
                console.error('Programador Senior: Error al cargar historial para defaults:', err);
                // Si falla, los valores base ya definidos en planForm actúan como red de seguridad
            }
        });
    }

    loadCodigoBase(): void {
        if (this.companiaId) {
            this.convenioService.generarCodigo(this.companiaId).subscribe({
                next: (codigo) => {
                    this.codigoBaseAuto = codigo;
                },
                error: (err) => {
                    console.error('Error fetching codigo base', err);
                    this.codigoBaseAuto = 'ERROR';
                }
            });
        }
    }

    loadAllEmpresas(): void {
        this.loading = true;
        this.empresaService.getEmpresas().subscribe({
            next: (data: Empresa[]) => {

                this.allEmpresas = data
                    .sort((a, b) => b.idEmpresa - a.idEmpresa);

                this.filteredEmpresas = [...this.allEmpresas];
                this.loading = false;
                this.cd.detectChanges();
            },
            error: (err: any) => {
                console.error('Error loading empresas list', err);
                this.loading = false;
                this.cd.detectChanges();
            }
        });
    }


    filterEmpresas(): void {
        if (!this.searchTerm) {
            this.filteredEmpresas = [...this.allEmpresas];
            return;
        }
        const term = this.searchTerm.toLowerCase();
        this.filteredEmpresas = this.allEmpresas.filter(e =>
            e.nombre.toLowerCase().includes(term) ||
            (e.ruc && e.ruc.includes(term))
        );
    }

    get previewNombrePlan(): string {
        const prefix = (this.planForm.nombreBase && this.planForm.nombreBase.trim().length > 0)
            ? this.planForm.nombreBase.trim().toUpperCase()
            : 'PLAN';

        return `${prefix} ${this.codigoBaseAuto !== 'Cargando...' && this.codigoBaseAuto !== 'ERROR' ? this.codigoBaseAuto : '[Cod]'} - [Nombre Empresa]`;
    }

    toggleEmpresa(id: number): void {
        if (this.selectedEmpresas.has(id)) {
            this.selectedEmpresas.delete(id);
        } else {
            this.selectedEmpresas.add(id);
        }
    }

    toggleSelectAll(): void {
        if (this.areAllSelected()) {
            this.selectedEmpresas.clear();
        } else {
            this.filteredEmpresas.forEach(e => this.selectedEmpresas.add(e.idEmpresa));
        }
    }

    areAllSelected(): boolean {
        return this.filteredEmpresas.length > 0 &&
            this.filteredEmpresas.every(e => this.selectedEmpresas.has(e.idEmpresa));
    }

    clearSelection(): void {
        this.selectedEmpresas.clear();
    }

    guardar(): void {
        if (!this.companiaId || this.selectedEmpresas.size === 0) return;

        this.isSaving = true;
        const ids = Array.from(this.selectedEmpresas);

        const request = {
            idCompania: this.companiaId,
            idEmpresas: ids,
            fechaInicio: this.planForm.fechaInicio,
            fechaFin: this.planForm.fechaFin,
            estado: this.planForm.estado,
            nombreBase: this.planForm.nombreBase,
            // Insurance fields
            copago: Number(this.planForm.copago || 0),
            deducible: Number(this.planForm.deducible || 0),
            porcentajeCoaseguro: Number(this.planForm.porcentajeCoaseguro || 0)
        };

        this.convenioService.vincularEmpresasMasivo(request).subscribe({
            next: (planesNuevos: any[]) => {
                this.isSaving = false;
                this.alertasService.success('Vinculación Exitosa', `${planesNuevos.length} planes creados correctamente.`);
                this.onVinculacionExitosa.emit();
                this.closeModal();
            },
            error: (err: any) => {
                console.error('Error al crear planes masivamente', err);
                this.isSaving = false;
                this.alertasService.error('Error', 'No se pudo procesar la vinculación masiva.');
            }
        });
    }


    closeModal(): void {
        this.onClose.emit();
    }
}

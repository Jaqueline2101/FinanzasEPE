import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { ModalComponent } from '../../../shared/components/modal/modal';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CredCtaCte } from '../../../models/cred-ctacte.model';
import { CredCtaCteService } from '../../../services/cred-ctacte.service';
import { UserService } from '../../../services/user.service';
import { CompaniaService } from '../../../services/compania.service';
import { EmpresaService, Empresa } from '../../../services/empresa.service';
import { ConvenioService } from '../../../services/convenio.service';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-modal-cuenta-convenio',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-cuenta-convenio.component.html',
    styleUrls: ['./modal-cuenta-convenio.component.css']
})
export class ModalCuentaConvenioComponent implements OnInit {
    @Input() cuenta: CredCtaCte | null = null;
    @Output() onSave = new EventEmitter<void>();
    @Output() onClose = new EventEmitter<void>();

    formData: CredCtaCte = {
        dni: '',
        idCompania: 0,
        estado: 'ACTIVO'
    };

    companias: any[] = [];
    empresas: Empresa[] = [];
    loading = false;

    planesData: any[] = [];
    nroCuentaPreview: string | null = null;
    isLoadingPreview: boolean = false;

    constructor(
        private service: CredCtaCteService,
        private userService: UserService,
        private companiaService: CompaniaService,
        private empresaService: EmpresaService,
        private convenioService: ConvenioService,
        private cd: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnInit() {
        this.cargarSelectores();
        if (this.cuenta) {
            this.formData = { ...this.cuenta };
            const cmp = (this.cuenta as any).compania;
            if (!this.formData.idCompania && cmp) {
                if (typeof cmp === 'number' || typeof cmp === 'string') {
                    this.formData.idCompania = Number(cmp);
                } else if (cmp.id) {
                    this.formData.idCompania = cmp.id;
                }
            }
        } else {
            this.fetchNroCuentaPreview();
        }
    }

    cargarSelectores() {
        this.companiaService.getCompanias().subscribe(res => {
            this.companias = res;
        });

        this.empresaService.getEmpresas().subscribe(res => {
            this.empresas = res;
        });

        this.convenioService.listarConvenios().subscribe(res => {
            this.planesData = res;
        });
    }

    onDniChange() {
        this.updateCodAsegurado();
        if (this.formData.dni && this.formData.dni.length >= 8) {
            this.userService.searchByFilter(this.formData.dni, 'ALL').subscribe(res => {
                const page = (res as any).data || res; // Handle both wrapped and unwrapped for safety
                if (page.content && page.content.length > 0) {
                    const p = page.content[0];
                    this.formData.nombre = p.firstName || p.nombre;
                    this.formData.apellidoPaterno = p.paternalSurname || p.lastName || p.apepat;
                    this.formData.apellidoMaterno = p.maternalSurname || p.apemat;
                    this.formData.id = p.id; // Crucial for backend linking
                    this.cd.detectChanges();
                }
            });
        }
    }

    onPlanCodeChange() {
        const plan = this.planesData.find(p => p.codigo === this.formData.codigoPlan);
        if (plan) {
            this.updatePlanFields(plan);
        }
    }

    onPlanNameChange() {
        const plan = this.planesData.find(p => p.nombre === this.formData.nombrePlan);
        if (plan) {
            this.updatePlanFields(plan);
        }
    }

    private updatePlanFields(plan: any) {
        this.formData.codigoPlan = plan.codigo;
        this.formData.nombrePlan = plan.nombre;
        this.formData.planSeguro = plan.nombre;
        this.formData.idPlan = plan.id;

        if (plan.idCompania) {
            this.formData.idCompania = plan.idCompania;
            this.fetchNroCuentaPreview();
        }
        if (plan.idEmpresa) this.formData.idEmpresa = plan.idEmpresa;

        this.updateCodAsegurado();
    }

    private updateCodAsegurado() {
        if (!this.formData.codAsegurado) {
            this.formData.codAsegurado = this.formData.dni || '';
        }
    }

    private fetchNroCuentaPreview() {
        const idCompania = this.formData.idCompania || (this.cuenta as any)?.idCompania || (this.cuenta as any)?.compania?.id;
        if (idCompania && !this.cuenta?.nroCtacte) {
            this.isLoadingPreview = true;
            this.service.previsualizarNroCtacte(idCompania).subscribe({
                next: (res) => {
                    this.nroCuentaPreview = res.nroCtacte;
                    this.isLoadingPreview = false;
                    this.cd.detectChanges();
                },
                error: (err) => {
                    console.error('Error previsualizando nro cuenta', err);
                    this.nroCuentaPreview = '[Error de conexión]';
                    this.isLoadingPreview = false;
                    this.cd.detectChanges();
                }
            });
        }
    }

    getNombreEmpresa(): string {
        const anyData = this.formData as any;
        const e = this.empresas.find(x => x.idEmpresa == this.formData.idEmpresa);
        if (e) return e.nombre;
        return anyData.empresa?.nombre || anyData.empresaNombre || 'No asignado';
    }

    getNombreCompania(): string {
        const anyData = this.formData as any;
        const c = this.companias.find(x => x.id == this.formData.idCompania);
        if (c) return (c.nombre || c.razSol);
        return anyData.compania?.nombre || anyData.companiaNombre || 'No asignado';
    }

    getNroCuentaPreview(): string {
        if (this.formData.nroCtacte) {
            return this.formData.nroCtacte;
        }
        if (this.isLoadingPreview) {
            return 'Calculando...';
        }
        if (this.nroCuentaPreview) {
            return this.nroCuentaPreview;
        }

        return '[Generado automáticamente]';
    }

    guardar() {
        // Validación básica
        let cmp = (this.cuenta as any)?.compania;
        let idCompaniaFallback = typeof cmp === 'number' || typeof cmp === 'string' ? Number(cmp) : cmp?.id;
        let idCompania = this.formData.idCompania || (this.cuenta as any)?.idCompania || idCompaniaFallback;
        if (idCompania) this.formData.idCompania = idCompania; // Ensure it goes to the payload

        const nombre = this.formData.nombre || (this.cuenta as any)?.nombre;
        const dni = this.formData.dni;

        if (!dni || !idCompania || !nombre) {
            this.alertasService.error('Campos obligatorios', 'DNI, Nombre y Compañía son obligatorios');
            return;
        }

        this.loading = true;
        const request = this.cuenta
            ? this.service.actualizar(this.cuenta.idCtacte!, this.formData)
            : this.service.crear(this.formData);

        request.subscribe({
            next: () => {
                const action = this.cuenta ? 'Actualizado' : 'Guardado';
                const message = this.cuenta ? 'La cuenta se ha actualizado correctamente' : 'La cuenta se ha guardado correctamente';
                this.alertasService.success(action, message);
                this.onSave.emit();
            },
            error: (err) => {
                console.error('Error guardando cuenta', err);
                this.alertasService.error('Error', 'No se pudo guardar la cuenta de convenio');
                this.loading = false;
            }
        });
    }

    cerrar() {
        this.onClose.emit();
    }
}

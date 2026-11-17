import { Component, EventEmitter, Input, OnInit, Output, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmpleadoDTO, EmpleadoService } from '../../../services/empleado.service';
import { GrupoFamiliarService } from '../../../services/grupo-familiar.service';
import { GrupoFamiliar } from '../../../models/grupo-familiar.model';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-modal-detalle',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './modal-detalle.component.html'
})
export class ModalDetalleComponent implements OnInit {
  @Input() isOpen = false;
  @Input() empleadoId: number | null = null;
  @Output() closeModal = new EventEmitter<void>();

  private empleadoService = inject(EmpleadoService);
  private grupoFamiliarService = inject(GrupoFamiliarService);
  private cd = inject(ChangeDetectorRef);

  empleado: EmpleadoDTO | null = null;
  familiares: GrupoFamiliar[] = [];
  loading = false;
  formaciones: { grado: string; profesion: string }[] = [];

  ngOnInit(): void {
    if (this.empleadoId) {
      this.loadDetails(this.empleadoId);
    }
  }

  loadDetails(id: number): void {
    this.loading = true;
    this.empleadoService.getById(id).subscribe({
      next: (data) => {
        this.empleado = data;
        this.loading = false;
        this.parseFormaciones(data.nombreOcupacion || '');
        if (data.numdoc) {
          this.loadFamiliares(data.numdoc);
        }
        this.cd.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar detalles', err);
        this.loading = false;
      }
    });
  }

  loadFamiliares(dni: string): void {
    this.grupoFamiliarService.obtenerFamiliaresPorTitular(dni).subscribe({
      next: (data) => {
        this.familiares = data;
        this.cd.detectChanges();
      },
      error: (err) => console.error('Error al cargar familiares', err)
    });
  }

  parseFormaciones(ocupacion: string): void {
    if (ocupacion.includes(' - ')) {
      const entries = ocupacion.split('; ');
      this.formaciones = entries.map(entry => {
        const parts = entry.split(' - ');
        return { 
          grado: parts[0] || 'N/A', 
          profesion: parts[1] || 'N/A' 
        };
      });
    } else {
      this.formaciones = [{ grado: 'General', profesion: ocupacion || 'Sin asignar' }];
    }
  }

  close(): void {
    this.closeModal.emit();
  }
}

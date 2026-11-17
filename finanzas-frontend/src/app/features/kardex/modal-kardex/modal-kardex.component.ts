import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-modal-kardex',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  providers: [DatePipe],
  templateUrl: './modal-kardex.component.html',
  styleUrls: ['./modal-kardex.component.css']
})
export class ModalKardexComponent {
  @Input() visible: boolean = false;
  @Input() movimiento: any = null;
  @Output() onClose = new EventEmitter<void>();

  constructor(private datePipe: DatePipe) {}

  closeModal() {
    this.onClose.emit();
  }

  getDaysToVenc(fechaVenc: string): number {
    if (!fechaVenc) return 999;
    const today = new Date();
    const venc = new Date(fechaVenc);
    return Math.ceil((venc.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  }

  getVencBadgeClass(fechaVenc: string): string {
    const days = this.getDaysToVenc(fechaVenc);
    if (days <= 0) return 'bg-rose-100 text-rose-700 border-rose-200';
    if (days <= 30) return 'bg-orange-100 text-orange-700 border-orange-200';
    if (days <= 90) return 'bg-amber-100 text-amber-700 border-amber-200';
    return 'bg-emerald-100 text-emerald-700 border-emerald-200';
  }

  getVencLabel(fechaVenc: string): string {
    const days = this.getDaysToVenc(fechaVenc);
    if (days <= 0) return 'Lote Vencido';
    if (days <= 30) return 'Crítico (Próx. Vencer)';
    if (days <= 90) return 'Por Vencer';
    return 'Lote Vigente';
  }
}

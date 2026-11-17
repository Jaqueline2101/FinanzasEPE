import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal';

@Component({
  selector: 'app-modal-ajustar',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './modal-ajustar.component.html'
})
export class ModalAjustarComponent implements OnInit {
  @Input() isOpen: boolean = false;
  @Input() compra: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<any>();

  editForm = {
    serie: '',
    tipoDocumento: '' // FACTURA or BOLETA
  };

  ngOnInit() {
    if (this.compra) {
      this.editForm = {
        serie: this.compra.serie || '',
        tipoDocumento: this.compra.tipoDocumento || 'FACTURA'
      };
    }
  }

  onSave() {
    this.save.emit(this.editForm);
  }

  onClose() {
    this.close.emit();
  }
}

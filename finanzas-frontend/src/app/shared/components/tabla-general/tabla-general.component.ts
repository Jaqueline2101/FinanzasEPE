import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface Columna {
  field: string;
  header: string;
  tipo?:
  | 'order-status'
  | 'rango-critico'
  | 'rango-normal'
  | 'date'
  | 'text'
  | 'badge'
  | 'index'
  | 'area-badge'
  | 'nombre-exam'
  | 'area-info'
  | 'currency'
  | 'tiempo-entrega'
  | 'nombre_nivel'
  | 'status'
  | 'status-badge'
  | 'multi-date'
  | 'object'
  | 'list-badges'
  | 'custom-button-action'
  | 'action-button'
  | 'semaphore'
  | 'amount-color'
  | 'currency-color'
  | 'title-subtitle'
  | 'input-number'
  | 'checkbox';
  subField?: string[];
  badgeConfig?: {
    container?: string;
    from?: string; // Color inicial
    to?: string; // Color final
    text?: string; // Color de texto
    border?: string; // Color de borde
  };
  actionLabel?: string;
  actionIcon?: string;
  actionClass?: string;
  showViewDetails?: boolean;
  align?: 'left' | 'center' | 'right';
  disabledField?: string;
}

@Component({
  selector: 'app-tabla-general',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tabla-general.component.html',
})
export class TablaGeneralComponent implements OnChanges {
  @Input() autoSort: boolean = true;
  @Input() sortField: string = '';
  @Input() sortOrder: 'asc' | 'desc' = 'desc';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['datos'] && this.datos && this.autoSort) {
      this.aplicarOrdenamiento();
    }
  }

  private aplicarOrdenamiento(): void {
    if (!this.datos || this.datos.length === 0) return;

    // Si hay un campo de orden específico, usarlo
    if (this.sortField) {
      this.ordenarPor(this.sortField);
      return;
    }

    // Si no, intentar detectar un campo ID o Fecha de creación
    const campos = Object.keys(this.datos[0]);
    const priorityField = campos.find(f =>
      f.toLowerCase() === 'createdat' ||
      f.toLowerCase() === 'id' ||
      f.toLowerCase().startsWith('id')
    );

    if (priorityField) {
      this.ordenarPor(priorityField);
    }
  }

  private ordenarPor(field: string): void {
    this.datos.sort((a, b) => {
      const valA = a[field];
      const valB = b[field];

      if (valA < valB) return this.sortOrder === 'desc' ? 1 : -1;
      if (valA > valB) return this.sortOrder === 'desc' ? -1 : 1;
      return 0;
    });
  }

  @Input() datos: any[] = [];
  @Input() columnas: Columna[] = [];
  @Input() tituloAcciones: string = 'Acciones';
  @Input() showLinkAction: boolean = false;
  @Input() linkActionLabel: string = 'Ver Detalles';
  @Input() linkActionIcon: string = 'fa-eye';
  @Input() currentPage: number = 0;
  @Input() pageSize: number = 10;
  @Input() showEdit: boolean = true;
  @Input() showDelete: boolean = true;
  @Input() showSubExams: boolean = false;
  @Input() showDetallesExamenes: boolean = false;
  @Input() showViewDetails: boolean = false;
  @Input() showCreateNivel: boolean = false;
  @Input() showConfig: boolean = false;
  @Input() showPrint: boolean = false;
  @Input() showDownloadPdf: boolean = false;
  @Input() onlyPrintValidated: boolean = false;
  @Input() showCustom: boolean = false;
  @Input() customIcon: string = 'fa-circle-info';
  @Input() customTooltip: string = 'Acción';
  @Input() loading: boolean = false;
  @Input() showIndex: boolean = false; // Nueva propiedad para numeración automática
  @Input() showActions: boolean = true; // New input to toggle the entire Actions column
  @Input() selectedItem: any = null; // New input to highlight the selected row

  // Eventos para avisar al padre
  @Output() onRowClick = new EventEmitter<any>();
  @Output() onNivel = new EventEmitter<any>();
  @Output() onConfig = new EventEmitter<any>();
  @Output() onEdit = new EventEmitter<any>();
  @Output() onDelete = new EventEmitter<any>();
  @Output() onView = new EventEmitter<any>();
  @Output() viewSubExams = new EventEmitter<any>();
  @Output() viewdetalles = new EventEmitter<any>();
  @Output() onLinkAction = new EventEmitter<any>();
  @Output() onLinkClick = new EventEmitter<any>();
  @Output() viewDetails = new EventEmitter<any>();
  @Output() onPrint = new EventEmitter<any>();
  @Output() onDownloadPdf = new EventEmitter<any>();
  @Output() onCheckboxChange = new EventEmitter<{ item: any; checked: boolean }>();
  @Output() onHeaderCheckboxChange = new EventEmitter<boolean>();
  @Output() onInputChange = new EventEmitter<{ item: any; col: Columna; value: any }>();
  @Output() onCustom = new EventEmitter<any>();
  @Output() onActionButton = new EventEmitter<{item: any, col: Columna}>();

  // Helpers para multi-date
  isMultiDate(text: string): boolean {
    return !!(text && text.includes(' | '));
  }

  getLastDate(text: string): string {
    if (!text) return '';
    const parts = text.split(' | ');
    return parts[parts.length - 1];
  }

  getExtraCount(text: string): number {
    if (!text) return 0;
    return text.split(' | ').length - 1;
  }

  getAllDates(text: string): string[] {
    if (!text) return [];
    return text.split(' | ');
  }
}

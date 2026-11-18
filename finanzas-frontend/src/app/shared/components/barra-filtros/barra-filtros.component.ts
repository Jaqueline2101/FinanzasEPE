import {
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
    OnDestroy,
    ElementRef,
    HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SearchGenericComponent } from '../reusable-search-selector/reusable-search-selector';
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

export interface FilterCriteria {
    term: string;
    searchType: string;
    estado: string;
    fechaDesde: string;
    fechaHasta: string;
}

@Component({
    selector: 'app-barra-filtros',
    standalone: true,
    imports: [CommonModule, FormsModule, SearchGenericComponent],
    templateUrl: './barra-filtros.component.html'
})
export class BarraFiltrosComponent implements OnInit, OnDestroy {
    @Input() placeholder: string = 'Buscar...';
    @Input() searchOptions: any[] = [];
    @Input() estadoOptions: any[] = [
        { label: 'Todas', value: 'TODOS' },
        { label: 'Activas', value: 'ACTIVO' },
        { label: 'Inactivas', value: 'INACTIVO' }
    ];

    @Input() statusLabel: string = 'Estado';
    @Input() showStatus: boolean = true;
    @Input() showDates: boolean = true;
    @Input() initialDesde: string = '';
    @Input() initialHasta: string = '';

    @Output() onFilter = new EventEmitter<FilterCriteria>();

    filters: FilterCriteria = {
        term: '',
        searchType: '', // Cambiado de 'ALL' a '' para buscar todo por defecto
        estado: 'TODOS',
        fechaDesde: '',
        fechaHasta: ''
    };

    showEstadoDropdown: boolean = false;
    private searchSubject = new Subject<void>();
    private searchSubscription!: Subscription;

    constructor(private elementRef: ElementRef) { }

    ngOnInit(): void {
        this.initDates();
        
        // Inicializar searchType con la primera opción si existe
        if (this.searchOptions && this.searchOptions.length > 0) {
            this.filters.searchType = this.searchOptions[0].value;
        }

        // Configurar debounce para aplicar filtros automáticamente al escribir (500ms)
        this.searchSubscription = this.searchSubject.pipe(
            debounceTime(500)
        ).subscribe(() => {
            this.emitFilters();
        });
    }

    ngOnDestroy(): void {
        if (this.searchSubscription) {
            this.searchSubscription.unsubscribe();
        }
        this.searchSubject.complete();
    }

    private initDates(): void {
        // Usar fechas iniciales si se proporcionan, de lo contrario vacío
        this.filters.fechaDesde = this.initialDesde || '';
        this.filters.fechaHasta = this.initialHasta || '';
    }

    onSearchEvent(event: { q: string; type: string }): void {
        this.filters.term = event.q;
        this.filters.searchType = event.type;
        this.searchSubject.next();
    }

    onDateStartChange(): void {
        if (!this.filters.fechaDesde) return;

        const start = this.parseDateString(this.filters.fechaDesde);
        const end = new Date(start);
        end.setDate(start.getDate() + 30);
        this.filters.fechaHasta = this.formatDateInput(end);

        this.emitFilters();
    }

    onDateEndChange(): void {
        if (!this.filters.fechaHasta) return;

        this.emitFilters();
    }

    // ===== CUSTOM DROPDOWN LOGIC =====

    toggleEstadoDropdown(event: Event): void {
        event.stopPropagation();
        this.showEstadoDropdown = !this.showEstadoDropdown;
    }

    selectEstado(value: string): void {
        this.filters.estado = value;
        this.showEstadoDropdown = false;
        this.emitFilters();
    }

    getEstadoLabel(): string {
        const option = this.estadoOptions.find(opt => opt.value === this.filters.estado || (opt.value === 'null' && (this.filters.estado === 'TODOS' || !this.filters.estado)));
        if (option) return option.label;
        if (this.estadoOptions && this.estadoOptions.length > 0) {
            return this.estadoOptions[0].label;
        }
        return this.statusLabel || 'Seleccionar';
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (!this.elementRef.nativeElement.contains(event.target)) {
            this.showEstadoDropdown = false;
        }
    }

    // ================================

    emitFilters(): void {
        this.onFilter.emit({ ...this.filters });
    }

    resetFilters(): void {
        this.filters.term = '';
        this.filters.estado = 'TODOS';
        this.filters.fechaDesde = '';
        this.filters.fechaHasta = '';
        this.emitFilters();
    }

    private parseDateString(dateStr: string): Date {
        const [year, month, day] = dateStr.split('-').map(Number);
        return new Date(year, month - 1, day);
    }

    private formatDateInput(date: Date): string {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();
        if (month.length < 2) month = '0' + month;
        if (day.length < 2) day = '0' + day;
        return [year, month, day].join('-');
    }
}

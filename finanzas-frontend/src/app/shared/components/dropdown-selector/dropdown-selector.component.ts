import { 
    Component, 
    EventEmitter, 
    Input, 
    Output, 
    OnInit, 
    OnChanges, 
    SimpleChanges, 
    ChangeDetectorRef, 
    HostListener, 
    ElementRef, 
    ViewChild,
    signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface DropdownOption {
    label: string;
    value: any;
    subtitle?: string;
    icon?: string;
}

@Component({
    selector: 'app-dropdown-selector',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="relative w-full" #container>
        <!-- Trigger Button -->
        <button type="button" (click)="toggleDropdown($event)"
            [disabled]="disabled"
            class="w-full h-12 flex justify-between items-center bg-white border border-slate-200 rounded-2xl px-4 text-sm outline-none transition-all hover:bg-slate-50 focus:ring-4 focus:ring-indigo-500/10 focus:border-indigo-400 group disabled:opacity-50 disabled:cursor-not-allowed">
            
            <div class="flex flex-col items-start overflow-hidden">
                <span *ngIf="!value && value !== 0" class="text-slate-400 font-medium italic">
                    {{ placeholder }}
                </span>
                <span *ngIf="value || value === 0" class="text-slate-700 font-bold truncate">
                    {{ getSelectedLabel() }}
                </span>
                <span *ngIf="getSelectedSubtitle()" class="text-[9px] font-black text-indigo-500 uppercase tracking-tighter -mt-1 truncate">
                    {{ getSelectedSubtitle() }}
                </span>
            </div>

            <i class="fas fa-chevron-down text-slate-400 text-[10px] transition-transform duration-300"
                [class.rotate-180]="showDropdown()"></i>
        </button>

        <!-- Dropdown Panel (Fixed to escape containers) -->
        <div *ngIf="showDropdown()"
            class="bg-white border border-slate-200 rounded-2xl shadow-[0_15px_50px_-12px_rgba(0,0,0,0.15)] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-200"
            [ngStyle]="dropdownStyles" (click)="$event.stopPropagation()">
            
            <!-- Search field if many options -->
            <div *ngIf="options.length > 6" class="p-4 border-b border-slate-50 bg-slate-50/30">
                <div class="relative">
                    <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xs"></i>
                    <input type="text" [(ngModel)]="searchTerm" placeholder="Filtrar opciones..."
                        class="w-full h-9 pl-9 pr-4 bg-white border border-slate-200 rounded-xl text-xs outline-none focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-300 font-semibold"
                        (click)="$event.stopPropagation()">
                </div>
            </div>

            <!-- Options List -->
            <div class="overflow-y-auto max-h-[250px] p-2 custom-scrollbar">
                <button type="button" *ngFor="let opt of filteredOptions"
                    (click)="selectOption(opt)"
                    class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-all group"
                    [class.bg-indigo-50]="value === opt.value"
                    [class.hover:bg-slate-50]="value !== opt.value">
                    
                    <div class="flex items-center gap-3 overflow-hidden text-left">
                        <div *ngIf="opt.icon" class="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-500 group-hover:text-indigo-600 transition-colors">
                            <i [class]="opt.icon"></i>
                        </div>
                        <div class="flex flex-col min-w-0">
                            <span class="text-xs font-bold leading-tight" 
                                [class.text-indigo-700]="value === opt.value"
                                [class.text-slate-700]="value !== opt.value">
                                {{ opt.label }}
                            </span>
                            <span *ngIf="opt.subtitle" class="text-[9px] font-medium text-slate-400 group-hover:text-slate-500 transition-colors">
                                {{ opt.subtitle }}
                            </span>
                        </div>
                    </div>

                    <div *ngIf="value === opt.value" class="w-5 h-5 bg-indigo-500 rounded-full flex items-center justify-center animate-in zoom-in duration-200">
                        <i class="fas fa-check text-[8px] text-white"></i>
                    </div>
                </button>

                <!-- Empty state -->
                <div *ngIf="filteredOptions.length === 0" class="py-10 text-center px-4">
                    <div class="w-12 h-12 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-3 text-slate-300">
                        <i class="fas fa-search"></i>
                    </div>
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Sin resultados</p>
                </div>
            </div>
        </div>
    </div>
    `,
    styles: [`
        .custom-scrollbar::-webkit-scrollbar { width: 4px; }
        .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
        .custom-scrollbar::-webkit-scrollbar-thumb { 
            background: #e2e8f0; 
            border-radius: 10px; 
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #cbd5e1; }
    `]
})
export class DropdownSelectorComponent implements OnInit, OnChanges {
    @Input() options: DropdownOption[] = [];
    @Input() value: any = null;
    @Input() placeholder: string = 'Seleccionar...';
    @Input() disabled: boolean = false;
    @Output() valueChange = new EventEmitter<any>();

    @ViewChild('container') container!: ElementRef;

    showDropdown = signal(false);
    searchTerm = '';
    dropdownStyles: { [key: string]: string } = {};

    constructor(private cd: ChangeDetectorRef) {}

    ngOnInit(): void {}

    ngOnChanges(changes: SimpleChanges): void {}

    toggleDropdown(e: Event): void {
        if (this.disabled) return;
        e.stopPropagation();
        this.showDropdown.set(!this.showDropdown());
        if (this.showDropdown()) {
            this.searchTerm = '';
            setTimeout(() => this.calculateDropdownPosition(), 0);
        }
    }

    private calculateDropdownPosition(): void {
        if (!this.container) return;
        const trigger = this.container.nativeElement.querySelector('button');
        if (!trigger) return;

        const rect = trigger.getBoundingClientRect();
        const viewportHeight = window.innerHeight;
        const dropdownMaxHeight = 280;
        const margin = 8;

        const spaceBelow = viewportHeight - rect.bottom - margin;
        const spaceAbove = rect.top - margin;

        let top: number;
        let maxHeight: number;

        if (spaceBelow >= 200 || spaceBelow >= spaceAbove) {
            top = rect.bottom + margin;
            maxHeight = Math.min(dropdownMaxHeight, spaceBelow);
        } else {
            maxHeight = Math.min(dropdownMaxHeight, spaceAbove);
            top = rect.top - maxHeight - margin;
        }

        this.dropdownStyles = {
            'position': 'fixed',
            'top': `${top}px`,
            'left': `${rect.left}px`,
            'width': `${rect.width}px`,
            'max-height': `${maxHeight}px`,
            'z-index': '9999'
        };
        this.cd.detectChanges();
    }

    selectOption(opt: DropdownOption): void {
        this.value = opt.value;
        this.valueChange.emit(this.value);
        this.showDropdown.set(false);
    }

    getSelectedLabel(): string {
        const option = this.options.find(opt => opt.value === this.value);
        return option ? option.label : '';
    }

    getSelectedSubtitle(): string {
        const option = this.options.find(opt => opt.value === this.value);
        return option?.subtitle || '';
    }

    get filteredOptions(): DropdownOption[] {
        if (!this.searchTerm) return this.options;
        const term = this.searchTerm.toLowerCase();
        return this.options.filter(opt => 
            opt.label.toLowerCase().includes(term) || 
            (opt.subtitle && opt.subtitle.toLowerCase().includes(term))
        );
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (!this.showDropdown()) return;
        if (!this.container.nativeElement.contains(event.target)) {
            this.showDropdown.set(false);
        }
    }

    @HostListener('window:scroll')
    @HostListener('window:resize')
    onWindowChange(): void {
        if (this.showDropdown()) {
            this.calculateDropdownPosition();
        }
    }
}

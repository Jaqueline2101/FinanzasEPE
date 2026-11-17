import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, OnInit, OnDestroy, signal, ElementRef, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-search-generic',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="relative z-50 w-full group">
        <div class="flex items-center bg-white dark:bg-slate-900 h-11.5 px-3 rounded-xl border border-slate-200 dark:border-slate-700 transition-all hover:border-slate-300 dark:hover:border-slate-600 shadow-sm focus-within:ring-2 focus-within:ring-indigo-500/10 focus-within:border-indigo-400">
            <!-- Icono Lupa (Fijo) -->
            <svg class="w-4 h-4 text-slate-400 dark:text-slate-500 flex-shrink-0" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>

            <!-- Input (Flexible y con min-w-0 para no romper el flex) -->
            <input type="text"
                [(ngModel)]="query"
                (input)="onInput()"
                class="min-w-0 flex-1 bg-transparent border-none text-slate-700 dark:text-slate-200 text-sm focus:ring-0 outline-none placeholder-slate-400 dark:placeholder-slate-500 px-3 py-1"
                [placeholder]="placeholder">

            <!-- Botón Limpiar -->
            <button type="button" *ngIf="query" 
                (click)="clearSearch()"
                class="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full transition-colors flex-shrink-0 mr-1">
                <svg class="w-3.5 h-3.5 text-slate-400 hover:text-slate-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>

            <!-- Separador (Fijo) -->
            <div class="h-5 w-[1px] bg-slate-200 dark:bg-slate-700 flex-shrink-0"></div>

            <!-- Selector de Tipo (Flexible con truncado) -->
            <button type="button" (click)="toggleDropdown($event)"
                class="flex items-center pl-3 pr-1 text-[11px] font-black text-slate-500 dark:text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors uppercase tracking-wider max-w-[120px] flex-shrink-0">
                <span class="truncate mr-1.5">{{ getCurrentLabel() }}</span>
                <svg class="w-3.5 h-3.5 transition-transform duration-300 flex-shrink-0"
                    [class.rotate-180]="isOpen()" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7"></path>
                </svg>
            </button>
        </div>

        <!-- Dropdown Menu -->
        <div *ngIf="isOpen()"
            class="absolute right-0 top-full mt-2 w-48 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-2xl z-[100] overflow-hidden animate-fadeIn">
            <div class="py-1">
                <button type="button"
                    *ngFor="let opt of options"
                    (click)="changeType(opt.value); isOpen.set(false)"
                    class="w-full text-left px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 hover:text-slate-900 dark:hover:text-white transition-colors flex items-center justify-between group">
                    {{ opt.label }}
                    <svg *ngIf="currentType === opt.value" class="w-4 h-4 text-blue-500 opacity-100" fill="none"
                        viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M5 13l4 4L19 7" />
                    </svg>
                </button>
            </div>
        </div>
    </div>
  `,
})
export class SearchGenericComponent implements OnInit, OnDestroy {
  @Input() options: { label: string; value: string }[] = [];
  @Input() placeholder: string = 'Buscar...';
  @Output() onSearch = new EventEmitter<{ q: string; type: string }>();

  query: string = '';
  currentType: string = 'ALL';
  isOpen = signal(false);
  private searchSubject = new Subject<string>();
  private sub?: Subscription;

  constructor(private eRef: ElementRef) { }

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.isOpen.set(!this.isOpen());
  }

  ngOnInit() {
    if (this.options.length > 0) this.currentType = this.options[0].value;

    this.sub = this.searchSubject
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe((q) => this.onSearch.emit({ q, type: this.currentType }));
  }

  onInput() {
    this.searchSubject.next(this.query);
  }

  changeType(type: string) {
    this.currentType = type;
    this.onSearch.emit({ q: this.query, type: this.currentType });
  }

  getCurrentLabel(): string {
    const option = this.options.find(opt => opt.value === this.currentType);
    return option ? option.label : 'Filtrar por';
  }


  clearSearch() {
    this.query = '';
    this.onInput();
  }

  reset() {
    this.query = '';
    if (this.options.length > 0) this.currentType = this.options[0].value;
    this.isOpen.set(false);
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }
}
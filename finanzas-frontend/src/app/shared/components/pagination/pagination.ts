import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex flex-col sm:flex-row justify-between items-center mt-2 gap-2">
      
      <div *ngIf="showPageSizeOptions" class="flex items-center gap-1 bg-slate-50 px-2 py-1 rounded-lg border border-slate-200">
        <label class="text-[10px] font-semibold text-slate-600">Mostrar</label>
        <select 
          [ngModel]="pageSize" 
          (ngModelChange)="onPageSizeChange($event)"
          class="border border-slate-300 rounded px-1.5 py-0.5 text-[10px] font-bold focus:outline-none focus:ring-2 focus:ring-[#1E293B] focus:border-[#1E293B] bg-white text-[#1E293B] cursor-pointer">
          <option *ngFor="let size of pageSizeOptions" [ngValue]="size">{{ size }}</option>
        </select>
        <span class="text-[10px] font-semibold text-slate-600">{{ recordsLabel }}</span>
      </div>

      <nav class="flex items-center gap-1">
        
        <button type="button"
          (click)="onPageChange(currentPage - 1)" 
          [disabled]="currentPage === 0"
          class="px-2 py-1 text-[10px] font-bold border border-slate-300 rounded-lg hover:bg-slate-100 transition-all disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed flex items-center gap-0.5 text-[#1E293B]">
          <svg class="w-3 h-3 text-[#1E293B]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7" />
          </svg>
          {{ previousLabel }}
        </button>

        <div class="hidden sm:flex items-center gap-0.5">
          <button
            *ngFor="let page of getVisiblePages()"
            (click)="onPageChange(page)" 
            [class.active-page]="page === currentPage"
            [class.inactive-page]="page !== currentPage"
            class="min-w-8 px-2 py-1 text-[10px] font-bold rounded-lg border transition-all duration-200">
            {{ page + 1 }}
          </button>
        </div>

        <button type="button"
          (click)="onPageChange(currentPage + 1)" 
          [disabled]="currentPage >= totalPages - 1"
          class="px-2 py-1 text-[10px] font-bold border border-slate-300 rounded-lg hover:bg-slate-100 transition-all disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed flex items-center gap-0.5 text-[#1E293B]">
          {{ nextLabel }}
          <svg class="w-3 h-3 text-[#1E293B]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </nav>
    </div>
  `,
  styles: [`
    .active-page {
      background-color: #1E293B;
      border-color: #1E293B;
      color: white;
      box-shadow: 0 4px 6px -1px rgb(30 41 59 / 0.2);
    }
    .inactive-page {
      background-color: white;
      border-color: #E2E8F0; /* slate-200 */
      color: #475569; /* slate-600 */
    }
    .inactive-page:hover {
      background-color: #F8FAFC; /* slate-50 */
      border-color: #CBD5E1; /* slate-300 */
      color: #1E293B;
    }
  `]
})
export class PaginationComponent {
  @Input() currentPage: number = 0;
  @Input() totalPages: number = 0;
  @Input() pageSize: number = 10;
  @Input() pageSizeOptions: number[] = [10, 25, 50, 100];
  @Input() maxVisiblePages: number = 5;
  @Input() showPageSizeOptions: boolean = true;

  @Input() recordsLabel: string = 'registros';
  @Input() previousLabel: string = 'Anterior';
  @Input() nextLabel: string = 'Siguiente';

  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.pageChange.emit(page);
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSizeChange.emit(newSize);
  }

  getVisiblePages(): number[] {
    if (this.totalPages <= 0) return [];

    const halfVisible = Math.floor(this.maxVisiblePages / 2);
    let startPage = Math.max(0, this.currentPage - halfVisible);
    let endPage = Math.min(this.totalPages - 1, startPage + this.maxVisiblePages - 1);

    if (endPage - startPage < this.maxVisiblePages - 1) {
      startPage = Math.max(0, endPage - this.maxVisiblePages + 1);
    }

    const pages: number[] = [];
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }
}
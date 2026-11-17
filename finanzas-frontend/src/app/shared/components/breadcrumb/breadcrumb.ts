import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, Location } from '@angular/common'; // Import Location
import { RouterModule } from '@angular/router';

export interface BreadcrumbItem {
  label: string;
  route?: string;
  icon?: string;
  queryParams?: any;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="mb-4 w-full flex items-center justify-between px-4 py-2 bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 rounded-lg shadow-sm">
      <div class="flex items-center gap-2">
        <a routerLink="/dashboard" class="text-slate-500 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
          <svg *ngIf="showIcon" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
          </svg>
        </a>

        <ng-container *ngFor="let item of items; let last = last">
          <span class="text-gray-300 dark:text-slate-600">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
            </svg>
          </span>

          <a *ngIf="item.route && !last" 
             [routerLink]="item.route"
             [queryParams]="item.queryParams"
             class="text-sm font-medium text-slate-600 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
            {{ item.label }}
          </a>
          
          <span *ngIf="!item.route && !last" class="text-sm font-medium text-slate-600 dark:text-slate-400">
            {{ item.label }}
          </span>

          <span *ngIf="last" class="text-sm font-bold text-slate-900 dark:text-slate-200">
            {{ item.label }}
          </span>
        </ng-container>
      </div>

      <!-- Volver Button -->
      <button *ngIf="showBackButton" 
              (click)="goBack()" 
              class="flex items-center gap-1 text-sm font-medium text-blue-600 hover:text-blue-800 transition-colors focus:outline-none">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        Volver
      </button>
    </nav>
  `
})
export class BreadcrumbComponent {
  @Input() items: BreadcrumbItem[] = [];
  @Input() showIcon: boolean = true;
  @Input() showBackButton: boolean = true;

  @Output() back = new EventEmitter<void>();

  constructor(private location: Location) { }

  goBack(): void {
    if (this.back.observed) {
      this.back.emit();
    } else {
      this.location.back();
    }
  }
}
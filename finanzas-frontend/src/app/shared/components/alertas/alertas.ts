import { Component, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertasService } from '../../../services/alertas.service';

@Component({
    selector: 'app-alertas',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './alertas.html',
    styleUrl: './alertas.css'
})
export class AlertasComponent {
    private service = inject(AlertasService);
    alert = this.service.alert;

    @ViewChild('inputPrompt') inputElement?: ElementRef<HTMLInputElement>;

    close() {
        this.service.close();
    }

    onConfirm() {
        const inputValue = this.inputElement?.nativeElement?.value;
        this.service.handleConfirm(true, inputValue);
    }

    onCancel() {
        this.service.handleConfirm(false);
    }

    onBackdropClick() {
        // En confirmaciones, obligamos a usar los botones
        if (this.alert().type === 'confirm') return;
        
        if (this.alert().type !== 'error' && this.alert().type !== 'warning') {
            this.close();
        }
    }

    isConfirm() {
        return this.alert().type === 'confirm';
    }

    isError() {
        return this.alert().type === 'error';
    }

    getIconContainerClass() {
        const type = this.alert().type;
        const iconType = type === 'confirm' ? (this.alert().confirmIcon || 'warning') : type;
        switch (iconType) {
            case 'success': return 'bg-green-50 dark:bg-green-500/10 border-4 border-green-100 dark:border-green-500/20';
            case 'error': return 'bg-rose-50 dark:bg-rose-500/10 border-4 border-rose-100 dark:border-rose-500/20';
            case 'warning': return 'bg-amber-50 dark:bg-amber-500/10 border-4 border-amber-100 dark:border-amber-500/20';
            case 'info': return 'bg-blue-50 dark:bg-blue-500/10 border-4 border-blue-100 dark:border-blue-500/20';
            default: return '';
        }
    }

    getButtonClass() {
        switch (this.alert().type) {
            case 'success': return 'bg-green-500 hover:bg-green-600 shadow-green-500/30';
            case 'error': return 'bg-rose-500 hover:bg-rose-600 shadow-rose-500/30';
            case 'warning': return 'bg-amber-500 hover:bg-amber-600 shadow-amber-500/30';
            case 'info': return 'bg-blue-500 hover:bg-blue-600 shadow-blue-500/30';
            case 'confirm': return 'bg-blue-600 hover:bg-blue-700 shadow-blue-600/30';
            default: return 'bg-slate-800';
        }
    }
}

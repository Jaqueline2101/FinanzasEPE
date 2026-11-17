import { Injectable, signal } from '@angular/core';

export type AlertaType = 'success' | 'error' | 'warning' | 'info' | 'confirm';

export interface AlertaConfig {
    show: boolean;
    type: AlertaType;
    title: string;
    message: string;
    autoClose?: boolean;
    duration?: number;
    confirmText?: string;
    cancelText?: string;
    confirmButtonClass?: string;
    confirmIcon?: 'success' | 'warning' | 'info' | 'error';
    showInput?: boolean;
    inputPlaceholder?: string;
}

@Injectable({
    providedIn: 'root'
})
export class AlertasService {
    private alertState = signal<AlertaConfig>({
        show: false,
        type: 'info',
        title: '',
        message: '',
        autoClose: true,
        duration: 2500
    });

    alert = this.alertState.asReadonly();

    // Referencias para las promesas
    private confirmResolve?: (value: boolean) => void;
    private promptResolve?: (value: string | null) => void;

    constructor() {}

    success(title: string, message: string, autoClose: boolean = true) {
        this.showAlert('success', title, message, autoClose);
    }

    error(title: string, message: string, autoClose: boolean = false) {
        this.showAlert('error', title, message, autoClose);
    }

    warning(title: string, message: string, autoClose: boolean = false) {
        this.showAlert('warning', title, message, autoClose);
    }

    info(title: string, message: string, autoClose: boolean = true) {
        this.showAlert('info', title, message, autoClose);
    }

    // Método para confirmaciones genéricas
    confirm(
        title: string, 
        message: string, 
        confirmText: string = 'Confirmar', 
        cancelText: string = 'Cancelar',
        confirmButtonClass: string = 'bg-blue-600 hover:bg-blue-700 shadow-blue-600/30',
        confirmIcon: 'success' | 'warning' | 'info' | 'error' = 'warning'
    ): Promise<boolean> {
        return new Promise((resolve) => {
            this.confirmResolve = resolve;
            // Defer update to avoid NG0100 error
            setTimeout(() => {
                this.alertState.set({
                    show: true,
                    type: 'confirm',
                    title,
                    message,
                    autoClose: false,
                    confirmText,
                    cancelText,
                    confirmButtonClass,
                    confirmIcon,
                    showInput: false
                });
            }, 0);
        });
    }

    // Nuevo método para capturar texto (Prompt)
    prompt(
        title: string,
        message: string,
        placeholder: string = 'Escriba aquí...',
        confirmText: string = 'Aceptar',
        cancelText: string = 'Cancelar'
    ): Promise<string | null> {
        return new Promise((resolve) => {
            this.promptResolve = resolve;
            setTimeout(() => {
                this.alertState.set({
                    show: true,
                    type: 'confirm',
                    title,
                    message,
                    autoClose: false,
                    confirmText,
                    cancelText,
                    showInput: true,
                    inputPlaceholder: placeholder
                });
            }, 0);
        });
    }

    // Nuevo método para confirmaciones (ej: eliminar) - Ahora usa el método confirm genérico
    deleteConfirm(title: string = '¿Estás seguro?', message: string = 'No podrás revertir esto'): Promise<boolean> {
        return this.confirm(title, message, 'Sí, eliminar', 'Cancelar', 'bg-rose-500 hover:bg-rose-600 shadow-rose-500/30', 'warning');
    }

    // Métodos para ser llamados desde el componente Alertas
    handleConfirm(result: boolean, inputValue?: string) {
        if (this.confirmResolve) {
            this.confirmResolve(result);
            this.confirmResolve = undefined;
        }

        if (this.promptResolve) {
            this.promptResolve(result ? (inputValue || '') : null);
            this.promptResolve = undefined;
        }

        this.close();
    }

    private showAlert(type: AlertaType, title: string, message: string, autoClose: boolean) {
        // Defer update to avoid NG0100 error
        setTimeout(() => {
            this.alertState.set({
                show: true,
                type,
                title,
                message,
                autoClose,
                duration: type === 'success' ? 2500 : 4000
            });

            if (autoClose) {
                setTimeout(() => {
                    this.close();
                }, this.alert().duration);
            }
        }, 0);
    }

    close() {
        setTimeout(() => {
            this.alertState.update(state => ({ ...state, show: false }));
        }, 0);
    }
}

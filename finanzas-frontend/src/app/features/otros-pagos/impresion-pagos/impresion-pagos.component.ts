import { Component, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
    selector: 'app-impresion-pagos',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div *ngIf="visible && !printing" class="modal-overlay">
      <div class="modal-container">
        <div class="modal-header">
            <div class="flex items-center gap-3">
                 <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
                    </svg>
                 </div>
                 <div>
                     <h2 class="text-lg font-bold text-gray-800">Vista Previa de Impresión</h2>
                     <p class="text-xs text-gray-500">Seleccione el formato deseado</p>
                 </div>
            </div>
            <button (click)="close()" class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>
        
        <div class="modal-content">
            <div class="previews-container">
                
                <div class="preview-card">
                    <div class="paper-80mm">
                         <div class="ticket-80mm" [innerHTML]="safeTicket80"></div>
                    </div>
                    <button (click)="print('80mm')" class="btn-print-80">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
                        </svg>
                        Imprimir 80mm
                    </button>
                </div>

                <div class="preview-card">
                    <div class="paper-58mm">
                         <div class="ticket-58mm" [innerHTML]="safeTicket58"></div>
                    </div>
                     <button (click)="print('58mm')" class="btn-print-58">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                           <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        Imprimir 58mm
                    </button>
                </div>

            </div>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .modal-overlay { position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center; padding: 1rem; background: rgba(0, 0, 0, 0.5); backdrop-filter: blur(4px); }
    .modal-container { background: #f3f4f6; border-radius: 0.75rem; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); width: 100%; max-width: 80rem; height: 90vh; display: flex; flex-direction: column; overflow: hidden; }
    .modal-header { background: white; padding: 1rem; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1); flex-shrink: 0; }
    .modal-content { flex: 1; overflow: auto; padding: 1.5rem; background: #f3f4f6; }
    .previews-container { display: flex; flex-wrap: wrap; justify-content: center; gap: 2rem; align-items: flex-start; }
    .preview-card { display: flex; flex-direction: column; align-items: center; gap: 1rem; }
    .paper-80mm, .paper-58mm { background: white; border-radius: 2px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); border: 1px solid #e5e7eb; }
    .paper-80mm { width: 80mm; padding: 12px; }
    .paper-58mm { width: 58mm; padding: 8px; }
    .btn-print-80, .btn-print-58 { display: flex; align-items: center; gap: 0.5rem; padding: 0.75rem 1.5rem; font-weight: bold; border-radius: 0.5rem; transition: all 0.2s; width: 100%; justify-content: center; border: none; cursor: pointer; color: white; }
    .btn-print-80 { background: #2563eb; }
    .btn-print-80:hover { background: #1d4ed8; }
    .btn-print-58 { background: #334155; }
    .btn-print-58:hover { background: #1e293b; }
    .ticket-80mm, .ticket-58mm { font-family: 'Courier New', monospace; color: #000; font-size: 11px; line-height: 1.4; }
    .ticket-58mm { font-size: 9px; line-height: 1.3; }
  `]
})
export class ImpresionPagosComponent implements OnDestroy {
    @Input() visible = false;
    @Input() data: any = null;
    @Output() onClose = new EventEmitter<void>();

    printing = false;
    fechaHora = new Date();
    userDisplay = 'Administrador';

    safeTicket80: SafeHtml = '';
    safeTicket58: SafeHtml = '';

    constructor(private sanitizer: DomSanitizer) {
        window.onafterprint = () => {
            this.printing = false;
        };
    }

    ngOnChanges() {
        // Update safe HTML whenever data changes
        this.safeTicket80 = this.sanitizer.bypassSecurityTrustHtml(this.getTicket80());
        this.safeTicket58 = this.sanitizer.bypassSecurityTrustHtml(this.getTicket58());
    }

    ngOnDestroy() {
        window.onafterprint = null;
    }

    print(type: '80mm' | '58mm') {
        this.printing = true;

        setTimeout(() => {
            const iframe = document.createElement('iframe');
            iframe.style.position = 'absolute';
            iframe.style.width = '0';
            iframe.style.height = '0';
            iframe.style.border = 'none';
            document.body.appendChild(iframe);

            const iframeDoc = iframe.contentWindow!.document;
            iframeDoc.open();
            iframeDoc.write(`
                <html>
                <head>
                    <style>
                        @page { size: ${type} auto; margin: 0; }
                        body { margin: 0; padding: ${type === '80mm' ? '10px' : '6px'}; font-family: 'Courier New', monospace; }
                    </style>
                </head>
                <body>${type === '80mm' ? this.getTicket80() : this.getTicket58()}</body>
                </html>
            `);
            iframeDoc.close();

            iframe.contentWindow!.focus();
            iframe.contentWindow!.print();

            setTimeout(() => {
                document.body.removeChild(iframe);
                this.printing = false;
            }, 1000);
        }, 100);
    }

    getTicket80(): string {
        const fecha = this.formatDate(this.data?.fecha);
        const hora = this.formatTime(this.fechaHora);
        const monto = this.formatMonto(this.data?.monto);

        return `
            <div style="font-size: 11px; line-height: 1.4; color: #000;">
                <div>================================</div>
                <div style="text-align: center; font-weight: bold;">CLÍNICA ADVENTISTA</div>
                <div style="text-align: center;">RUC: 20123456789</div>
                <div style="text-align: center;">Av. Ejemplo 123, Lima</div>
                <div>================================</div>
                <div style="text-align: center; font-weight: bold;">COMPROBANTE DE DONACIÓN</div>
                <div>================================</div>
                <div style="height: 8px;"></div>
                <div>N°: ${this.data?.numeroComprobante || 'REC-001-000015'}</div>
                <div>Fecha: ${fecha}</div>
                <div>Hora: ${hora}</div>
                <div style="height: 8px;"></div>
                <div>--------------------------------</div>
                <div style="font-weight: bold;">DONANTE:</div>
                <div>${this.data?.beneficiario || 'Anónimo'}</div>
                ${this.data?.dni ? `<div>DNI/RUC: ${this.data.dni}</div>` : ''}
                ${this.data?.tipoPersona ? `<div>Tipo: ${this.data.tipoPersona}</div>` : ''}
                <div>--------------------------------</div>
                <div style="height: 8px;"></div>
                <div style="text-align: center; font-weight: bold;">MONTO DONADO</div>
                <div style="height: 8px;"></div>
                <div style="text-align: center; font-size: 20px; font-weight: bold;">S/ ${monto}</div>
                <div style="height: 8px;"></div>
                <div>--------------------------------</div>
                <div>Forma Pago: ${this.data?.metodoPago || 'EFECTIVO'}</div>
                ${this.data?.nombreCampana ? `<div>Destino: ${this.data.nombreCampana}</div>` : ''}
                ${this.data?.concepto ? `<div>Concepto: "${this.data.concepto}"</div>` : ''}
                <div>--------------------------------</div>
                <div style="height: 8px;"></div>
                <div style="text-align: center;">¡Gracias por su generosa</div>
                <div style="text-align: center;">donación!</div>
                <div style="height: 8px;"></div>
                <div style="text-align: center;">Tu aporte ayudará a muchas</div>
                <div style="text-align: center;">familias</div>
                <div style="height: 8px;"></div>
                <div>________________________________</div>
                <div style="text-align: center;">Firma</div>
                <div style="height: 8px;"></div>
                <div style="text-align: center;">Recibido por: ${this.userDisplay}</div>
                <div>================================</div>
            </div>
        `;
    }

    getTicket58(): string {
        const fecha = this.formatDate(this.data?.fecha);
        const hora = this.formatTime(this.fechaHora);
        const monto = this.formatMonto(this.data?.monto);

        return `
            <div style="font-size: 9px; line-height: 1.3; color: #000;">
                <div>============================</div>
                <div style="text-align: center; font-weight: bold;">CLÍNICA ADVENTISTA</div>
                <div>============================</div>
                <div style="text-align: center; font-weight: bold;">DONACIÓN</div>
                <div>============================</div>
                <div>N°: ${this.data?.numeroComprobante || 'REC-001-000015'}</div>
                <div>F: ${fecha} H: ${hora}</div>
                <div>----------------------------</div>
                <div style="font-weight: bold;">DONANTE:</div>
                <div>${this.formatNombre(this.data?.beneficiario)}</div>
                ${this.data?.dni ? `<div>ID: ${this.data.dni}</div>` : ''}
                <div>----------------------------</div>
                <div style="height: 4px;"></div>
                <div style="text-align: center; font-weight: bold;">MONTO</div>
                <div style="height: 4px;"></div>
                <div style="text-align: center; font-size: 16px; font-weight: bold;">S/ ${monto}</div>
                <div style="height: 4px;"></div>
                <div>----------------------------</div>
                <div>FP: ${this.data?.metodoPago || 'EFECTIVO'}</div>
                ${this.data?.nombreCampana ? `<div>Dst: ${this.data.nombreCampana}</div>` : ''}
                <div>----------------------------</div>
                <div style="height: 4px;"></div>
                <div style="text-align: center; font-weight: bold;">¡Gracias por su apoyo!</div>
                <div style="height: 4px;"></div>
                <div style="text-align: center; font-size: 8px;">Recibido: ${this.userDisplay}</div>
                <div>============================</div>
            </div>
        `;
    }

    close() {
        this.visible = false;
        this.onClose.emit();
    }

    formatDate(date: any): string {
        if (!date) return '---';
        const d = new Date(date);
        return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
    }

    formatTime(date: Date): string {
        const hours = date.getHours();
        const minutes = date.getMinutes();
        const ampm = hours >= 12 ? 'PM' : 'AM';
        const displayHours = hours % 12 || 12;
        return `${displayHours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')} ${ampm}`;
    }

    formatMonto(monto: any): string {
        if (!monto) return '0.00';
        return parseFloat(monto).toFixed(2);
    }

    formatNombre(nombre: string): string {
        if (!nombre) return 'Anónimo';
        return nombre.length > 20 ? nombre.substring(0, 20) + '...' : nombre;
    }
}
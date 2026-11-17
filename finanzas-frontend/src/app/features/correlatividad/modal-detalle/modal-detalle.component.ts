import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import * as XLSX from 'xlsx';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-modal-correlatividad-detalle',
    standalone: true,
    imports: [CommonModule],
    providers: [DecimalPipe, DatePipe],
    template: `
    <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl overflow-hidden border border-slate-200 animate-in fade-in zoom-in duration-200">
        
        <!-- HEADER -->
        <div class="px-6 py-4 flex items-center justify-between border-b border-slate-100 bg-slate-50/50">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center text-white shadow-lg shadow-blue-200">
              <i class="fa-solid fa-file-invoice text-lg"></i>
            </div>
            <div>
              <h3 class="text-sm font-black text-slate-800 uppercase tracking-tight">
                Detalle de Correlatividad
              </h3>
              <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                {{ data?.tipoDoc === 'B' ? 'BOLETAS' : 'FACTURAS' }} - SERIE {{ data?.serie }}
              </p>
            </div>
          </div>
          <button (click)="close.emit()" class="w-8 h-8 rounded-full hover:bg-slate-200 text-slate-400 hover:text-slate-600 transition-colors flex items-center justify-center">
            <i class="fa-solid fa-times"></i>
          </button>
        </div>

        <!-- INFO BAR -->
        <div class="px-6 py-3 bg-blue-50/50 border-b border-blue-100 flex items-center gap-6">
          <div class="flex flex-col">
            <span class="text-[9px] font-black text-blue-400 uppercase">Rango solicitado</span>
            <span class="text-xs font-black text-blue-700 font-mono tracking-tighter">
              {{ data?.desde }} — {{ data?.hasta }}
            </span>
          </div>
          <div class="w-px h-6 bg-blue-100"></div>
          <div class="flex flex-col">
            <span class="text-[9px] font-black text-blue-400 uppercase">Total Items</span>
            <span class="text-xs font-black text-blue-700 capitalize">{{ data?.cantidad }} documentos</span>
          </div>
        </div>

        <!-- CONTENT -->
        <div class="p-0 overflow-hidden flex flex-col h-[500px]">
          <div class="overflow-y-auto flex-1 custom-scrollbar">
            <table class="w-full text-left border-collapse">
              <thead class="sticky top-0 bg-white z-10">
                <tr class="bg-slate-800 text-white shadow-sm">
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider">Número</th>
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider">Fecha</th>
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider">Paciente / Cliente</th>
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider text-right">Base Imp.</th>
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider text-right">Igv</th>
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider text-right">Total</th>
                  <th class="px-6 py-3 text-[10px] font-black uppercase tracking-wider text-center">Estado</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <ng-container *ngFor="let doc of docs; let i = index">
                  <!-- SI HAY UN SALTO -->
                  <tr *ngIf="hasJump(i)" class="bg-red-50/50">
                    <td colspan="7" class="px-6 py-2 text-[10px] font-black text-red-500 text-center uppercase tracking-widest italic border-y border-red-100">
                      <i class="fa-solid fa-triangle-exclamation mr-2"></i>
                      {{ getJumpMissing(i) }}
                    </td>
                  </tr>

                  <tr class="hover:bg-blue-50/30 transition-colors group">
                    <td class="px-6 py-2.5 text-[11px] font-mono font-bold text-blue-600">
                      {{ data?.serie }}-{{ doc.numdoc }}
                    </td>
                    <td class="px-6 py-2.5 text-[11px] font-medium text-slate-500 uppercase">
                      {{ doc.fecha | date:'dd/MM/yyyy HH:mm' }}
                    </td>
                    <td class="px-6 py-2.5 text-[11px] font-black text-slate-700 uppercase">
                      {{ doc.nombrePac }}
                    </td>
                    <td class="px-6 py-2.5 text-[11px] font-mono text-slate-600 text-right">
                      {{ doc.baseImp | number:'1.2-2' }}
                    </td>
                    <td class="px-6 py-2.5 text-[11px] font-mono text-slate-600 text-right">
                      {{ doc.igv | number:'1.2-2' }}
                    </td>
                    <td class="px-6 py-2.5 text-[11px] font-mono font-black text-slate-900 text-right">
                      {{ doc.total | number:'1.2-2' }}
                    </td>
                    <td class="px-6 py-2.5 text-center">
                      <span class="px-2 py-0.5 bg-green-100 text-green-700 text-[9px] font-black rounded-full uppercase tracking-tighter border border-green-200">
                        {{ doc.estado === 16 ? 'REGISTRADO' : (doc.estado === 10 ? 'ANULADO' : 'ACEPTADO') }}
                      </span>
                    </td>
                  </tr>
                </ng-container>
              </tbody>
            </table>
          </div>
        </div>

        <!-- FOOTER -->
        <div class="px-6 py-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <button (click)="exportarExcel()" class="px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white text-[10px] font-black rounded-lg transition-all shadow-md active:scale-95 flex items-center uppercase tracking-widest">
              <i class="fa-solid fa-file-excel mr-2 text-green-400"></i> Exportar Excel
            </button>
            <button class="px-4 py-2 bg-white hover:bg-slate-100 text-slate-700 text-[10px] font-black rounded-lg transition-all border border-slate-300 shadow-sm active:scale-95 flex items-center uppercase tracking-widest">
              <i class="fa-solid fa-print mr-2"></i> Imprimir
            </button>
          </div>
          <button (click)="close.emit()" class="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white text-[10px] font-black rounded-lg transition-all shadow-lg shadow-blue-200 active:scale-95 uppercase tracking-widest">
            Cerrar Ventana
          </button>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .custom-scrollbar::-webkit-scrollbar {
      width: 6px;
    }
    .custom-scrollbar::-webkit-scrollbar-track {
      background: #f1f5f9;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb {
      background: #cbd5e1;
      border-radius: 10px;
    }
    .custom-scrollbar::-webkit-scrollbar-thumb:hover {
      background: #94a3b8;
    }
  `]
})
export class ModalCorrelatividadDetalleComponent {
    @Input() data: any;
    @Input() docs: any[] = [];
    @Output() close = new EventEmitter<void>();

    constructor(private datePipe: DatePipe) {}

    ngOnInit() {
    }

    hasJump(index: number): boolean {
        if (index === 0) return false;
        const current = parseInt(this.docs[index].numdoc);
        const previous = parseInt(this.docs[index - 1].numdoc);
        return current - previous > 1;
    }

    getJumpMissing(index: number): string {
        const current = parseInt(this.docs[index].numdoc);
        const previous = parseInt(this.docs[index - 1].numdoc);
        if (current - previous <= 1) return '';
        
        const start = previous + 1;
        const end = current - 1;
        const count = end - start + 1;
        
        return `FALTAN ${count} DOCUMENTOS (${start.toString().padStart(8, '0')} AL ${end.toString().padStart(8, '0')})`;
    }

    exportarExcel() {
        if (!this.docs || this.docs.length === 0) {
            Swal.fire('Atención', 'No hay datos para exportar', 'warning');
            return;
        }

        const dataToExport: any[] = [];
        
        this.docs.forEach((doc, i) => {
            // Verificar si hay un salto antes de este documento
            if (this.hasJump(i)) {
                dataToExport.push({
                    'Número': '---',
                    'Fecha': '---',
                    'Paciente / Cliente': this.getJumpMissing(i),
                    'Base Imp.': 0,
                    'Igv': 0,
                    'Total': 0,
                    'Estado': 'FALTANTE'
                });
            }

            // Agregar el documento
            dataToExport.push({
                'Número': `${this.data?.serie}-${doc.numdoc}`,
                'Fecha': this.datePipe.transform(doc.fecha, 'dd/MM/yyyy HH:mm') || '---',
                'Paciente / Cliente': doc.nombrePac || '---',
                'Base Imp.': doc.baseImp || 0,
                'Igv': doc.igv || 0,
                'Total': doc.total || 0,
                'Estado': doc.estado === 16 ? 'REGISTRADO' : (doc.estado === 10 ? 'ANULADO' : 'ACEPTADO')
            });
        });

        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Detalle Correlatividad');

        // Ajustar anchos de columna
        ws['!cols'] = [
            { wch: 20 }, // Número
            { wch: 20 }, // Fecha
            { wch: 50 }, // Paciente
            { wch: 15 }, // Base Imp
            { wch: 15 }, // Igv
            { wch: 15 }, // Total
            { wch: 15 }  // Estado
        ];

        const fileName = `Detalle_Correlatividad_${this.data?.serie}.xlsx`;
        XLSX.writeFile(wb, fileName);
    }
}

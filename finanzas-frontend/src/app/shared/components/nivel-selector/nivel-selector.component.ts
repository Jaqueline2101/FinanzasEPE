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
    ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NivelService } from '../../../services/nivel.service';

@Component({
    selector: 'app-nivel-selector',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './nivel-selector.component.html',
    styleUrls: ['./nivel-selector.component.css']
})
export class NivelSelectorComponent implements OnInit, OnChanges {
    @Input() value: number | null = null;
    @Input() label: string = 'Nivel';
    @Input() required: boolean = false;
    @Output() valueChange = new EventEmitter<number | null>();

    @ViewChild('nivelContainer') nivelContainer!: ElementRef;

    niveles: any[] = [];
    nivelesArbol: any[] = [];
    showDropdown: boolean = false;
    selectedName: string = 'Seleccionar nivel...';
    expandedNodes: Set<number> = new Set<number>();
    searchTerm: string = '';
    dropdownStyles: { [key: string]: string } = {};

    constructor(
        private nivelService: NivelService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadNiveles();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['value'] && this.niveles.length > 0) {
            this.updateSelectedName();
        }
    }

    loadNiveles(): void {
        this.nivelService.obtenerTodos(0, 100).subscribe({
            next: (response: any) => {
                if (response.success && response.data) {
                    this.niveles = response.data.content || [];
                    this.procesarNivelesJerarquicos();
                    this.updateSelectedName();
                    this.cd.detectChanges();
                }
            },
            error: (err: any) => console.error('Error loading niveles', err)
        });
    }

    procesarNivelesJerarquicos(): void {
        this.nivelesArbol = this.construirArbol(this.niveles);
    }

    private construirArbol(niveles: any[]): any[] {
        const mapa = new Map<number, any>();
        const raices: any[] = [];

        niveles.forEach(nivel => {
            mapa.set(nivel.idNivel, { ...nivel, hijos: [] });
        });

        niveles.forEach(nivel => {
            const nivelMapeado = mapa.get(nivel.idNivel);
            if (nivel.idNivelPadre) {
                const padre = mapa.get(nivel.idNivelPadre);
                if (padre) {
                    padre.hijos.push(nivelMapeado);
                } else {
                    raices.push(nivelMapeado);
                }
            } else {
                raices.push(nivelMapeado);
            }
        });

        return raices;
    }

    toggleDropdown(e: Event): void {
        e.stopPropagation();
        this.showDropdown = !this.showDropdown;
        if (this.showDropdown) {
            this.searchTerm = '';
            setTimeout(() => this.calcularPosicionDropdown(), 0);
        }
    }

    calcularPosicionDropdown(): void {
        if (!this.nivelContainer) return;
        const trigger = this.nivelContainer.nativeElement.querySelector('button');
        if (!trigger) return;

        const rect: DOMRect = trigger.getBoundingClientRect();
        const viewportHeight = window.innerHeight;
        const dropdownMaxHeight = 280;
        const margen = 6;

        const espacioAbajo = viewportHeight - rect.bottom - margen;
        const espacioArriba = rect.top - margen;

        let top: number;
        let maxHeight: number;

        if (espacioAbajo >= 150 || espacioAbajo >= espacioArriba) {
            top = rect.bottom + margen;
            maxHeight = Math.min(dropdownMaxHeight, espacioAbajo);
        } else {
            maxHeight = Math.min(dropdownMaxHeight, espacioArriba);
            top = rect.top - maxHeight - margen;
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

    selectNivel(nivel: any): void {
        this.value = nivel.idNivel;
        this.selectedName = nivel.nombre;
        this.valueChange.emit(this.value);
        this.showDropdown = false;
    }

    toggleNode(e: Event, nodo: any, hermanos: any[]): void {
        e.stopPropagation();
        const idNivel = nodo.idNivel;
        if (this.expandedNodes.has(idNivel)) {
            this.expandedNodes.delete(idNivel);
        } else {
            // Acordeón: colapsar hermanos
            if (hermanos) {
                hermanos.forEach(h => {
                    if (h.idNivel !== idNivel) {
                        this.collapseRecursive(h);
                    }
                });
            }
            this.expandedNodes.add(idNivel);
        }
    }

    private collapseRecursive(nodo: any): void {
        this.expandedNodes.delete(nodo.idNivel);
        if (nodo.hijos) {
            nodo.hijos.forEach((h: any) => this.collapseRecursive(h));
        }
    }

    updateSelectedName(): void {
        if (this.value) {
            const nivel = this.niveles.find(n => n.idNivel === this.value);
            if (nivel) {
                this.selectedName = nivel.nombre;
            }
        } else {
            this.selectedName = 'Seleccionar nivel...';
        }
    }

    get arbolFiltrado(): any[] {
        return this.filtrarArbol(this.nivelesArbol, this.searchTerm);
    }

    private filtrarArbol(nodos: any[], term: string): any[] {
        if (!term) return nodos;
        const lowerTerm = term.toLowerCase();
        return nodos.reduce((acc: any[], nodo: any) => {
            const matchNombre = nodo.nombre.toLowerCase().includes(lowerTerm);
            let hijosFiltrados: any[] = [];
            if (nodo.hijos && nodo.hijos.length > 0) {
                hijosFiltrados = this.filtrarArbol(nodo.hijos, term);
            }
            if (matchNombre || hijosFiltrados.length > 0) {
                acc.push({ ...nodo, hijos: hijosFiltrados });
            }
            return acc;
        }, []);
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (!this.showDropdown) return;
        if (this.nivelContainer) {
            const clickedInside = this.nivelContainer.nativeElement.contains(event.target);
            if (!clickedInside) {
                this.showDropdown = false;
            }
        }
    }

    @HostListener('window:scroll')
    @HostListener('window:resize')
    onWindowChange(): void {
        if (this.showDropdown) {
            this.calcularPosicionDropdown();
        }
    }
}

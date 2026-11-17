import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Nivel, NivelTree } from '../../models/nivel.model';
import { NivelService } from '../../services/nivel.service';
import { BreadcrumbComponent } from '../../shared/components/breadcrumb/breadcrumb';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { Router, RouterModule } from '@angular/router';
import { SidebarService } from '../../shared/sidebar/sidebar.service';
import { AlertasService } from '../../services/alertas.service';

import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button';
import { TablaGeneralComponent, Columna } from '../../shared/components/tabla-general/tabla-general.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination';
import { TipoAtencionComponent } from './tipo-atencion/tipo-atencion.component';
import { ModalNivelComponent } from './modal-nivel/modal-nivel.component';
import { ModalSubnivelComponent } from './modal-subnivel/modal-subnivel.component';

@Component({
  selector: 'app-niveles',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    PageHeaderComponent,
    BreadcrumbComponent,
    FormsModule,
    PrimaryButtonComponent,
    TablaGeneralComponent,
    PaginationComponent,
    TipoAtencionComponent,
    ModalNivelComponent,
    ModalSubnivelComponent
  ],
  templateUrl: './niveles.component.html',
  styleUrls: ['./niveles.component.css'],
})
export class NivelesComponent implements OnInit {
  @ViewChild(ModalNivelComponent) modalNivel!: ModalNivelComponent;
  @ViewChild(ModalSubnivelComponent) modalSubnivel!: ModalSubnivelComponent;

  // Datos
  niveles: Nivel[] = [];
  arbolNiveles: NivelTree[] = [];
  tiposArea: any[] = [];

  // Paginación
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  totalElements: number = 0;

  // Vista
  vistaActual: 'tabla' | 'arbol' = 'tabla';
  mostrarSoloRaiz: boolean = true;

  // Breadcrumb
  breadcrumbItems = [
    { label: 'Configuración', route: '/configuraciones' },
    { label: 'Niveles' },
  ];

  columnasNiveles: Columna[] = [
    { field: 'id', header: 'N°', tipo: 'index', subField: [''] },
    {
      field: 'numNivel',
      header: 'Código',
      tipo: 'badge',
      badgeConfig: {
        from: 'from-purple-100',
        to: 'to-purple-100',
        text: 'text-purple-800',
        border: 'border-purple-200',
      },
      subField: [''],
    },
    { field: 'nombre', header: 'Nombre del Nivel', tipo: 'nombre_nivel', subField: ['totalHijos'] },
    { field: 'tipo', header: 'Tipo', tipo: 'text', subField: [''] },
    { field: 'centroCosto', header: 'Centro de Costo', tipo: 'text', subField: [''] },
    { field: 'nombrePadre', header: 'Nivel Superior', tipo: 'text', subField: [''] },
    { field: 'estado', header: 'Estado', tipo: 'status', subField: [''] },
  ];

  constructor(
    private nivelService: NivelService,
    public sidebarService: SidebarService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private alertasService: AlertasService
  ) { }

  ngOnInit(): void {
    this.cargarDatos();
    this.cargarTipos();
  }

  cargarTipos(): void {
    this.nivelService.obtenerTipos().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          // Deferir para evitar NG0100 y forzar detección
          setTimeout(() => {
            this.tiposArea = response.data;
            this.cdr.detectChanges();
          });
        }
      },
      error: (error) => console.error('Error al cargar tipos', error)
    });
  }

  // ========================================
  // CARGA DE DATOS
  // ========================================

  cargarDatos(): void {
    if (this.vistaActual === 'tabla') {
      if (this.mostrarSoloRaiz) {
        this.cargarNivelesRaiz();
      } else {
        this.cargarTodosNiveles();
      }
    } else {
      this.cargarArbol();
    }
  }

  cargarNivelesRaiz(): void {
    this.nivelService.obtenerNivelesRaiz(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          console.log('Raices: ', response.data);
          this.niveles = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.currentPage = response.data.number;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error al cargar niveles raíz:', error);
        this.mostrarError('Error al cargar los niveles');
      },
    });
  }

  cargarTodosNiveles(): void {
    this.nivelService.obtenerTodos(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.niveles = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.currentPage = response.data.number;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error al cargar niveles:', error);
        this.mostrarError('Error al cargar los niveles');
      },
    });
  }

  cargarArbol(): void {
    this.nivelService.obtenerArbolCompleto().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.arbolNiveles = response.data;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error al cargar árbol:', error);
        this.mostrarError('Error al cargar el árbol de niveles');
      },
    });
  }

  cargarHijos(nivel: NivelTree): void {
    if (nivel.expandido) {
      nivel.expandido = false;
      return;
    }

    this.nivelService.obtenerHijos(nivel.idNivel).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          nivel.hijos = response.data.map((n) => this.convertirANivelTree(n));
          nivel.expandido = true;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error al cargar hijos:', error);
      },
    });
  }

  convertirANivelTree(nivel: Nivel): NivelTree {
    return {
      idNivel: nivel.idNivel!,
      nombre: nivel.nombre,
      numNivel: nivel.numNivel,
      tipo: nivel.tipo,
      estado: nivel.estado,
      nivelJerarquia: nivel.nivelJerarquia || 1,
      rutaCompleta: nivel.rutaCompleta,
      orden: nivel.orden,
      idNivelPadre: nivel.idNivelPadre,
      tieneHijos: nivel.tieneHijos || false,
      expandido: false,
      hijos: [],
    };
  }

  // ========================================
  // CRUD
  // ========================================

  openCreateModal(nivelPadre?: any): void {
    if (nivelPadre) {
      // Si hay padre, usar el modal de subnivel
      this.modalSubnivel.open(nivelPadre);
    } else {
      // Si no hay padre, es nivel raíz
      this.modalNivel.openCreate();
    }
  }

  toggleNivel(nivel: NivelTree): void {
    if (!nivel.tieneHijos) {
      return;
    }
    this.cargarHijos(nivel);
  }

  openEditModal(nivel: Nivel): void {
    this.modalNivel.openEdit(nivel);
  }

  deleteNivel(nivel: Nivel): void {
    this.alertasService.deleteConfirm(
      '¿Estás seguro?', 
      `Vas a eliminar el nivel: ${nivel.nombre}`
    ).then((confirmed) => {
      if (confirmed) {
        this.confirmDelete(nivel.idNivel!);
      }
    });
  }

  confirmDelete(idNivel: number): void {
    this.nivelService.eliminar(idNivel).subscribe({
      next: (response) => {
        if (response.success) {
          this.mostrarExito('Nivel eliminado exitosamente');
          this.cargarDatos();
        }
      },
      error: (error) => {
        console.error('Error al eliminar:', error);
        this.mostrarError(
          error.error?.message || 'Error al eliminar el nivel'
        );
      },
    });
  }

  // ========================================
  // PAGINACIÓN
  // ========================================

  changePage(page: number): void {
    this.currentPage = page;
    this.cargarDatos();
  }

  changePageSize(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 0;
    this.cargarDatos();
  }

  // ========================================
  // VISTA
  // ========================================

  cambiarVista(vista: 'tabla' | 'arbol'): void {
    this.vistaActual = vista;
    this.cargarDatos();
  }

  toggleMostrarSoloRaiz(): void {
    this.mostrarSoloRaiz = !this.mostrarSoloRaiz;
    this.currentPage = 0;
    this.cargarDatos();
  }

  // ========================================
  // UTILIDADES
  // ========================================

  getIndentClass(nivel: number): string {
    return `ml-${(nivel - 1) * 4}`;
  }

  getColorByLevel(nivel: number): string {
    const colors = ['blue', 'green', 'purple', 'orange', 'pink'];
    return colors[(nivel - 1) % colors.length];
  }

  private mostrarExito(mensaje: string): void {
    this.alertasService.success('¡Logrado!', mensaje);
  }

  private mostrarError(mensaje: string): void {
    this.alertasService.error('Error', mensaje);
  }
}

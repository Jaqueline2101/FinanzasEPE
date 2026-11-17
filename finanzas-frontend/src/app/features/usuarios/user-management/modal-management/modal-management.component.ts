import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AlertasService } from '../../../../services/alertas.service';

import { ModalComponent } from '../../../../shared/components/modal/modal';
import { UserService } from '../../../../services/user.service';
import { CreateUserRequest, User } from '../../../../models/user.model';
import { Role } from '../../../../models/role.model';
import { Sucursal } from '../../../../models/sucursal.model';
import { EmpleadoService, EmpleadoDTO } from '../../../../services/empleado.service';

@Component({
    selector: 'app-modal-management',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './modal-management.component.html',
})
export class ModalManagementComponent implements OnChanges {
    @Input() isOpen = false;
    @Input() userToEdit: User | null = null;
    @Input() availableRoles: Role[] = [];
    @Input() availableSucursales: Sucursal[] = [];

    @Output() modalClose = new EventEmitter<void>();
    @Output() userSaved = new EventEmitter<void>();

    isEditMode = false;
    saving = false;

    // Formulario
    formData: CreateUserRequest = this.getEmptyForm();
    confirmPassword = '';

    // Selecciones
    selectedRoleIds: Set<number> = new Set();
    selectedSucursalIds: Set<number> = new Set();
    originalSucursalIds: Set<number> = new Set(); // Para calcular el diff en edición

    // Employee Search
    showEmployeeSearch = false;
    searchTerm = '';
    employees: EmpleadoDTO[] = [];
    filteredEmployees: EmpleadoDTO[] = [];
    selectedEmployeeId: number | null = null;
    isPromoteMode = false;

    constructor(
        private userService: UserService,
        private empleadoService: EmpleadoService,
        private cdRef: ChangeDetectorRef,
        private alertasService: AlertasService
    ) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['isOpen'] && this.isOpen) {
            this.initForm();
        }
    }

    private initForm(): void {
        this.selectedRoleIds.clear();
        this.selectedSucursalIds.clear();
        this.originalSucursalIds.clear();
        this.confirmPassword = '';
        this.showEmployeeSearch = false;
        this.searchTerm = '';
        this.employees = [];
        this.filteredEmployees = [];
        this.selectedEmployeeId = null;
        this.isPromoteMode = false;

        if (this.userToEdit) {
            this.isEditMode = true;
            this.formData = {
                username: this.userToEdit.username,
                email: this.userToEdit.email,
                password: '',
                firstName: this.userToEdit.firstName,
                paternalSurname: this.userToEdit.paternalSurname,
                maternalSurname: this.userToEdit.maternalSurname,
                roleIds: [],
                active: this.userToEdit.active,
            };

            // Mapear roles por nombre a IDs
            this.userToEdit.roles.forEach((roleName: string) => {
                const roleObj = this.availableRoles.find((r) => r.name === roleName);
                if (roleObj) this.selectedRoleIds.add(roleObj.id);
            });

            // Cargar sucursales del usuario
            this.loadUserSucursales(this.userToEdit.id);

        } else {
            this.isEditMode = false;
            this.formData = this.getEmptyForm();
            // Load employees for search
            this.loadEmployees();
            // Open search by default
            this.showEmployeeSearch = true;
            setTimeout(() => {
                const input = document.getElementById('employeeSearchInput');
                if (input) input.focus();
            }, 300);
        }
    }

    loadEmployees() {
        this.empleadoService.getAll().subscribe({
            next: (data) => {
                // Filter out employees who might already be users? 
                // Currently backend doesn't give us this info easily in getAll, 
                // but promoteUser handles the check.
                this.employees = data;
                this.filteredEmployees = [];
            },
            error: (err) => console.error('Error loading employees', err)
        });
    }

    toggleEmployeeSearch() {
        this.showEmployeeSearch = !this.showEmployeeSearch;
        if (this.showEmployeeSearch) {
            setTimeout(() => {
                const input = document.getElementById('employeeSearchInput');
                if (input) input.focus();
            }, 100);
        }
    }

    searchEmployees() {
        if (!this.searchTerm || this.searchTerm.trim() === '') {
            this.filteredEmployees = [];
            return;
        }
        const term = this.searchTerm.toLowerCase();
        this.filteredEmployees = this.employees.filter(e => {
            const nombre = (e.nombre || '').toLowerCase();
            const apepat = (e.apepat || '').toLowerCase();
            const apemat = (e.apemat || '').toLowerCase();
            const dni = (e.numdoc || '').toLowerCase();

            return nombre.includes(term) ||
                apepat.includes(term) ||
                apemat.includes(term) ||
                dni.includes(term);
        }).slice(0, 5); // Limit to 5 results
    }

    selectEmployee(employee: EmpleadoDTO) {
        this.selectedEmployeeId = employee.id!;
        this.isPromoteMode = true;
        this.isEditMode = false; // We are "creating" a user from an employee
        this.showEmployeeSearch = false;
        this.searchTerm = '';

        // Fill form
        this.formData = {
            username: '', // Let them choose, or default to DNI?
            email: employee.email || '',
            password: '',
            firstName: employee.nombre,
            paternalSurname: employee.apepat || '',
            maternalSurname: employee.apemat || '',
            roleIds: [],
            active: true,
            phone: employee.fono1
        };

        // Notify
        this.alertasService.info('Empleado Seleccionado', `Se han cargado los datos de ${employee.nombre} ${employee.apepat}. Por favor complete las credenciales.`);
    }

    loadUserSucursales(userId: number): void {
        this.userService.getSucursalesAsignadas(userId).subscribe({
            next: (response) => {
                if (response.success && response.data) {
                    this.selectedSucursalIds = new Set(response.data.map((s) => s.idSucursal));
                    this.originalSucursalIds = new Set(this.selectedSucursalIds);
                    this.cdRef.detectChanges();
                } else {
                    this.selectedSucursalIds.clear();
                    this.originalSucursalIds.clear();
                }
            },
            error: (error) => {
                console.error('❌ Error al cargar sucursales del usuario:', error);
                this.selectedSucursalIds.clear();
                this.originalSucursalIds.clear();
            }
        });
    }

    toggleRole(roleId: number) {
        if (this.selectedRoleIds.has(roleId)) {
            this.selectedRoleIds.delete(roleId);
        } else {
            this.selectedRoleIds.add(roleId);
        }
    }

    toggleSucursal(sucursalId: number): void {
        const newSet = new Set(this.selectedSucursalIds);
        if (newSet.has(sucursalId)) {
            newSet.delete(sucursalId);
        } else {
            newSet.add(sucursalId);
        }
        this.selectedSucursalIds = newSet;
    }

    close(): void {
        this.modalClose.emit();
    }

    saveUser() {
        // 1. Validación local de contraseñas
        if (!this.isEditMode && this.formData.password !== this.confirmPassword) {
            this.alertasService.warning('Atención', 'Las contraseñas ingresadas no coinciden');
            return;
        }

        // 2. Validación de usuario seleccionado para edición
        if (this.isEditMode && !this.userToEdit) {
            this.alertasService.error('Error', 'No se ha seleccionado un usuario válido para editar');
            return;
        }

        // El overlay de loading se maneja mediante el flag 'saving' en el template

        this.saving = true;
        this.formData.roleIds = Array.from(this.selectedRoleIds);

        let request;
        if (this.isEditMode) {
            request = this.userService.updateUser(this.userToEdit!.id, this.formData);
        } else if (this.isPromoteMode && this.selectedEmployeeId) {
            request = this.userService.promoteUser(this.selectedEmployeeId, this.formData);
        } else {
            request = this.userService.createUser(this.formData);
        }

        request.subscribe({
            next: (res) => {
                this.saving = false;

                if (res.success) {
                    const userId = res.data.id;

                    // Preparar operaciones de sucursales
                    const operations: any[] = [];
                    const currentSucursales = Array.from(this.selectedSucursalIds);

                    if (this.isEditMode) {
                        // Modo Edición: Calcular diferencias
                        const originalSucursales = Array.from(this.originalSucursalIds);
                        const toRemove = originalSucursales.filter((id) => !this.selectedSucursalIds.has(id));
                        const toAdd = currentSucursales.filter((id) => !this.originalSucursalIds.has(id));

                        if (toRemove.length > 0) {
                            operations.push(this.userService.removeSucursales(userId, toRemove));
                        }
                        if (toAdd.length > 0) {
                            operations.push(this.userService.assignSucursales(userId, toAdd));
                        }
                    } else {
                        // Modo Creación: Asignar todas las seleccionadas
                        if (currentSucursales.length > 0) {
                            operations.push(this.userService.assignSucursales(userId, currentSucursales));
                        }
                    }

                    // Función para finalizar el proceso
                    const finalizeSuccess = () => {
                        this.alertasService.success(res.message || 'Operación exitosa', 'Usuario guardado correctamente');
                        this.userSaved.emit(); // Notificar al padre para recargar lista
                        this.close();
                    };

                    // Ejecutar operaciones adicionales si existen
                    if (operations.length > 0) {
                        forkJoin(operations).subscribe({
                            next: () => {
                                console.log('✅ Sucursales sincronizadas correctamente');
                                finalizeSuccess();
                            },
                            error: (err) => {
                                console.error('❌ Error al sincronizar sucursales:', err);
                                this.alertasService.warning('Usuario guardado parcialmente', 'El usuario se creó pero hubo un error asignando las sucursales.');
                                this.userSaved.emit();
                                this.close();
                            },
                        });
                    } else {
                        finalizeSuccess();
                    }

                } else {
                    this.alertasService.warning('Atención', res.message || 'No se pudo completar el registro del usuario');
                }
            },
            error: (err) => {
                this.saving = false;
                const errorServer = err.error?.message || 'Ocurrió un fallo inesperado en el servidor';
                this.alertasService.error('Error en la operación', errorServer);
            },
        });
    }

    private getEmptyForm(): CreateUserRequest {
        return {
            username: '',
            email: '',
            password: '',
            firstName: '',
            paternalSurname: '',
            maternalSurname: '',
            roleIds: [],
            active: true,
        };
    }
}

import { Permission } from './permission.model';
import { UserDirectPermissionResponse } from './user-permission.model';


export interface UserResponse {
    id: number;
    idPersonal: number;
    username: string;
    email: string;
    firstName: string;
    paternalSurname: string;
    maternalSurname: string;
    lastName: string;
    phone: string;
    active: boolean;
    roles: string[];
    permissions: string[];
    createdAt: string | Date;
    updatedAt: string | Date;
    lastLogin?: string | Date;
    roleId?: number;
    roleName?: string;
    useDirectPermissions: boolean;
    rolePermissions: Permission[];
    directPermissions: UserDirectPermissionResponse[];
    effectivePermissions: Permission[];
    idCompania?: number; // Added for Caja module logic
    esFamiliar?: boolean;
    nombreTitular?: string;
    numdoc?: string; // Adding for safety as it's used in Caja
    nacfec?: string | Date;
    direcc?: string;
    nomOcupacion?: string;
    fechaIng?: string | Date;
    sucursal?: string;
    fotoPerfil?: string;
}
export type User = UserResponse;
export type DatosPersonales = UserResponse; // Alias para compatibilidad con el código de Caja

export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
    firstName: string;
    paternalSurname: string;
    maternalSurname: string;
    lastName?: string;
    phone?: string;
    roleIds: number[];
    active: boolean;
}

export interface UpdateUserRequest {
    username: string;
    email: string;
    password?: string;
    firstName: string;
    paternalSurname: string;
    maternalSurname: string;
    lastName?: string;
    phone?: string;
    roleIds: number[];
    active: boolean;
}

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface UserStats {
    total: number;
    active: number;
    inactive: number;
}


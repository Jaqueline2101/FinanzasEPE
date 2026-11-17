import { Permission } from './permission.model';
import { UserResponse } from './user.model';

export interface UserDirectPermissionResponse {
    permission: Permission;
    assignedAt?: string;
    expiresAt?: string;
    reason?: string;
}

export interface UserWithPermissionsResponse extends UserResponse {
    useDirectPermissions: boolean;
    directPermissions: UserDirectPermissionResponse[];
    rolePermissions: Permission[];
    fullName?: string; // Optional helper
    // Added properties to satisfy template requirements
    permissionMode?: string;
    activeDirectPermissionsCount?: number;
    effectivePermissionsCount?: number;
}

export interface AssignDirectPermissionsRequest {
    userId: number;
    permissionIds: number[];
    reason: string;
    expiresAt?: string;
}

export interface TogglePermissionModeRequest {
    userId: number;
    useDirectPermissions: boolean;
}

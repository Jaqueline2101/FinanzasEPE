export interface PageResponse<T> {
    content: T[];            // Aquí están tus usuarios
    totalElements: number;
    totalPages: number;
    size: number;
    number: number; 
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;                 
    timestamp: string;
}
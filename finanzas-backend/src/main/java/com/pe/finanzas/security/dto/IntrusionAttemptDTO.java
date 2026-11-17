package com.pe.finanzas.security.dto;

import com.pe.finanzas.security.entity.FailureReason;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder

public class IntrusionAttemptDTO {
    private String ipAddress;
    private Integer attempts; // Cantidad de intentos actuales
    private LocalDateTime lastAttempt; // Fecha/hora del último intento
    private LocalDateTime blockedUntil; // Hasta cuándo está bloqueada la IP
    private String lastUsername; // El último nombre de usuario que intentaron usar
    private FailureReason lastFailureReason; // Por qué falló (Enum que vimos en el código anterior)
    private boolean isBlocked; // Estado booleano para el Frontend
}

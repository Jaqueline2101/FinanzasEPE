package com.pe.finanzas.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesbloqueoIPRequest {

    @NotBlank(message = "La dirección IP es requerida")
    private String ipAddress;

    @NotBlank(message = "El motivo de desbloqueo es obligatorio para la auditoría")
    private String motivo;
}
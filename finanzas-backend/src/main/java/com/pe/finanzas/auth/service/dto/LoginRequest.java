package com.pe.finanzas.auth.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El login es obligatorio")
    private String login;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String modulo; // Contexto del módulo: "Finanzas", "Laboratorio", etc.

}
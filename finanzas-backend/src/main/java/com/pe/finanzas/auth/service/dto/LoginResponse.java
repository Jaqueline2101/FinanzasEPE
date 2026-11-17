package com.pe.finanzas.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LoginResponse {

    private String token;
    private String type; // "Bearer"
    private Long userId;
    private Long idPersonal; // ID de negocio (el que se usa para vinculos)
    private String username;
    private String email;
    private String firstName;
    private String paternalSurname;
    private String maternalSurname;
    private String lastName;
    private String sexo;
    private Set<String> roles;
    private Set<String> permissions;
    private Long sessionTimeoutMs; // Timeout de sesión en milisegundos

    // Campos para selección de sucursal (Modelo Híbrido)
    private Boolean requiresSucursalSelection;
    private java.util.List<com.pe.finanzas.sucursales.dto.SucursalDto> sucursalesDisponibles;

    // Campos de sucursal seleccionada
    private Long sucursalId;
    private String sucursalNombre;
    private Long puntoId;
    private String puntoNombre;
    private String fotoPerfil;
    private boolean mustChangePassword;
}
package com.pe.finanzas.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String paternalSurname;
    private String maternalSurname;
    private String lastName;
    private Set<String> roles;
    private String message;
}

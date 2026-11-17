package com.pe.finanzas.users.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email no válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String paternalSurname;

    private String maternalSurname;

    private String lastName; // Mantener para compatibilidad temporal

    private String phone;

    private Set<Long> roleIds;

    private Boolean active;

    // Campos adicionales para pacientes
    private String numdoc;
    private String tipodoc;
    private String apepat;
    private String apemat;
    private java.time.LocalDate nacfec;
    private String sexo;
    private String direcc;
}
// Force recompile

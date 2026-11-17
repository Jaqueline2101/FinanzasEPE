package com.pe.finanzas.auth.controller;

import com.pe.finanzas.auth.service.AuthSucursalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.pe.finanzas.shared.dto.ApiResponse; // Add import

@RestController
@RequestMapping("/api/auth/current-sucursal")
@RequiredArgsConstructor
public class AuthSucursalController {

    private final AuthSucursalService authSucursalService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> selectSucursal(
            @Valid @RequestBody SucursalSelectionRequest request,
            HttpServletRequest httpRequest) {
        Map<String, Object> response = authSucursalService.seleccionarSucursal(
                request.getUserId(),
                request.getSucursalId(),
                request.getPuntoId(),
                request.getPassword(),
                httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Sucursal y punto seleccionados correctamente", response));
    }

    @Data
    public static class SucursalSelectionRequest {
        @NotNull(message = "El ID del usuario es requerido")
        private Long userId;

        @NotNull(message = "El ID de la sucursal es requerido")
        private Long sucursalId;

        @NotNull(message = "La contraseña es requerida")
        private String password;

        private Long puntoId; // Opcional por ahora, será obligatorio en frontend actualizado
    }
}

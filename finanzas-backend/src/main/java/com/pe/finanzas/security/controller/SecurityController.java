package com.pe.finanzas.security.controller;

import com.pe.finanzas.security.dto.DesbloqueoIPRequest;
import com.pe.finanzas.security.service.SecurityMonitorService;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityMonitorService securityMonitorService;

    @PostMapping("/unblock-ip")
    @PreAuthorize("hasAuthority('SECURITY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unblockIp(@Valid @RequestBody DesbloqueoIPRequest request) {
        // En una implementación real, esto limpiaría los intentos fallidos en la BD
        // O añadiría la IP a una lista blanca.
        // Por ahora, como es log-based, no hay un "estado" que resetear fácilmente
        // sin borrar logs, pero simulemos el éxito.

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("IP desbloqueada correctamente (Simulado)")
                .build());
    }

    @GetMapping("/check-ip")
    public ResponseEntity<ApiResponse<Boolean>> checkIpStatus(@RequestParam String ip) {
        boolean isBlocked = securityMonitorService.isIpBlocked(ip);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .data(isBlocked)
                .message(isBlocked ? "IP Bloqueada" : "IP Limpia")
                .build());
    }
}

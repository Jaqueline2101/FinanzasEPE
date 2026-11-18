package com.pe.finanzas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "Finanzas Backend API",
                "version", "0.0.2",
                "deployedAt", LocalDateTime.now().toString(),
                "docs", "/swagger-ui/index.html",
                "health", "/api/auth/health"
        ));
    }
}

package com.pe.finanzas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "Finanzas Backend API",
                "version", "0.0.1-SNAPSHOT",
                "docs", "/swagger-ui/index.html",
                "health", "/api/auth/health"
        ));
    }
}


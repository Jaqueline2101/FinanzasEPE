package com.pe.finanzas.dashboard.controller;

import com.pe.finanzas.dashboard.dto.DashboardResponseDTO;
import com.pe.finanzas.dashboard.service.DashboardService;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DashboardResponseDTO>> getResumen(
            @RequestParam(required = false) Long idSucursal) {
        
        DashboardResponseDTO data = dashboardService.obtenerResumenDashboard(idSucursal);
        
        return ResponseEntity.ok(ApiResponse.<DashboardResponseDTO>builder()
                .success(true)
                .message("Dashboard cargado correctamente")
                .data(data)
                .build());
    }
}

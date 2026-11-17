package com.pe.finanzas.controller;

import com.pe.finanzas.jobs.PlanExpirationJob;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-notifications")
@RequiredArgsConstructor
public class TestNotificationController {

    private final PlanExpirationJob planExpirationJob;
    private final com.pe.finanzas.paquetes.service.PaqueteService paqueteService;

    @GetMapping("/check-plans")
    public String triggerPlanCheck() {
        planExpirationJob.checkPlanExpirations();
        return "Revisión de vencimientos ejecutada. Revisa los logs y el frontend.";
    }

    @GetMapping("/deactivate-expired")
    public String triggerDeactivation() {
        planExpirationJob.deactivateExpiredPlans();
        return "Proceso de desactivación manual ejecutado. Revisa la base de datos y la tabla de planes.";
    }

    @GetMapping("/deactivate-packages")
    public String triggerPackageDeactivation() {
        paqueteService.verificarVencimientoPaquetes();
        return "Proceso de desactivación manual de PAQUETES ejecutado. Revisa el frontend.";
    }
}

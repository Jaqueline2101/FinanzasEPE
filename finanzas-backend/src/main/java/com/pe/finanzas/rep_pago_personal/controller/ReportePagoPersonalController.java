package com.pe.finanzas.rep_pago_personal.controller;

import com.pe.finanzas.rep_pago_personal.dto.DashboardPagoPersonalDto;
import com.pe.finanzas.rep_pago_personal.service.ReportePagoPersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/pagos-personal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportePagoPersonalController {

    private final ReportePagoPersonalService reporteService;

    @GetMapping("/dashboard")
    public DashboardPagoPersonalDto getDashboard(@RequestParam(required = false) Integer anio) {
        if (anio == null) {
            anio = java.time.LocalDate.now().getYear();
        }
        return reporteService.obtenerDashboard(anio);
    }
}

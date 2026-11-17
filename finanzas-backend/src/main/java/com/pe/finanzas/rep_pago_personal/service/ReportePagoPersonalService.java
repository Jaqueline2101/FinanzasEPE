package com.pe.finanzas.rep_pago_personal.service;

import com.pe.finanzas.rep_pago_personal.dto.DashboardPagoPersonalDto;

public interface ReportePagoPersonalService {
    DashboardPagoPersonalDto obtenerDashboard(Integer anio);
}

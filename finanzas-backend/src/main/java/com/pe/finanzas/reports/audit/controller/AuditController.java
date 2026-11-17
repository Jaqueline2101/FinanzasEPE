package com.pe.finanzas.reports.audit.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pe.finanzas.reports.audit.dto.AuditFilterDto;
import com.pe.finanzas.reports.audit.dto.LabAuditoriaDto;
import com.pe.finanzas.reports.audit.service.AuditService;
import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.shared.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports/audit")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<LabAuditoriaDto>>> searchLogs(@RequestBody AuditFilterDto filter) {
        Page<LabAuditoriaDto> result = auditService.searchAudits(filter);
        return ResponseEntity.ok(ApiResponse.success("Auditorías encontradas", PageResponse.of(result)));
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<ApiResponse<PageResponse<LabAuditoriaDto>>> getLogsByTable(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuditFilterDto filter = new AuditFilterDto();
        filter.setTabla(tableName);
        filter.setPage(page);
        filter.setSize(size);
        Page<LabAuditoriaDto> result = auditService.searchAudits(filter);
        return ResponseEntity.ok(ApiResponse.success("Auditorías por tabla encontradas", PageResponse.of(result)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<LabAuditoriaDto>>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuditFilterDto filter = new AuditFilterDto();
        filter.setIdPersonalUser(userId);
        filter.setPage(page);
        filter.setSize(size);
        Page<LabAuditoriaDto> result = auditService.searchAudits(filter);
        return ResponseEntity.ok(ApiResponse.success("Auditorías por usuario encontradas", PageResponse.of(result)));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<PageResponse<LabAuditoriaDto>>> getLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuditFilterDto filter = new AuditFilterDto();
        filter.setAccion(action);
        filter.setPage(page);
        filter.setSize(size);
        Page<LabAuditoriaDto> result = auditService.searchAudits(filter);
        return ResponseEntity.ok(ApiResponse.success("Auditorías por acción encontradas", PageResponse.of(result)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PageResponse<LabAuditoriaDto>>> getLatestAudits(
            @RequestParam(defaultValue = "50") int cantidad) {
        AuditFilterDto filter = new AuditFilterDto();
        filter.setPage(0);
        filter.setSize(cantidad);
        Page<LabAuditoriaDto> result = auditService.searchAudits(filter);
        return ResponseEntity.ok(ApiResponse.success("Últimas auditorías encontradas", PageResponse.of(result)));
    }
}

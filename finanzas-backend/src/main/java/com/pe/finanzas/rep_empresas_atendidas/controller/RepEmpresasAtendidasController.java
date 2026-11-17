package com.pe.finanzas.rep_empresas_atendidas.controller;

import com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO;
import com.pe.finanzas.rep_empresas_atendidas.service.RepEmpresasAtendidasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rep-empresas-atendidas")
@RequiredArgsConstructor
public class RepEmpresasAtendidasController {

    private final RepEmpresasAtendidasService repEmpresasAtendidasService;

    @GetMapping
    public ResponseEntity<List<EmpresaAtendidaDTO>> getEmpresasMasAtendidas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(repEmpresasAtendidasService.getEmpresasMasAtendidas(fechaInicio, fechaFin));
    }
}

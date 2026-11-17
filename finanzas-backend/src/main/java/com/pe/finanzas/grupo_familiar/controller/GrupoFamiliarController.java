package com.pe.finanzas.grupo_familiar.controller;

import com.pe.finanzas.grupo_familiar.dto.GrupoFamiliarRequest;
import com.pe.finanzas.grupo_familiar.dto.GrupoFamiliarResponse;
import com.pe.finanzas.grupo_familiar.dto.ValidacionTrabajadorResponse;
import com.pe.finanzas.grupo_familiar.service.GrupoFamiliarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/grupo-familiar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GrupoFamiliarController {

    private final GrupoFamiliarService grupoFamiliarService;

    @PostMapping
    public ResponseEntity<GrupoFamiliarResponse> registrarFamiliar(@RequestBody GrupoFamiliarRequest request) {
        log.info("POST /api/grupo-familiar - Registrando familiar");
        try {
            GrupoFamiliarResponse response = grupoFamiliarService.registrarFamiliar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error al registrar familiar: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/titular/{dni}")
    public ResponseEntity<List<GrupoFamiliarResponse>> obtenerFamiliaresPorTitular(@PathVariable String dni) {
        log.info("GET /api/grupo-familiar/titular/{} - Obteniendo familiares", dni);
        List<GrupoFamiliarResponse> familiares = grupoFamiliarService.obtenerFamiliaresPorTitular(dni);
        return ResponseEntity.ok(familiares);
    }

    @GetMapping("/validar-trabajador/{dni}")
    public ResponseEntity<ValidacionTrabajadorResponse> validarTrabajador(@PathVariable String dni) {
        log.info("GET /api/grupo-familiar/validar-trabajador/{} - Validando trabajador", dni);
        ValidacionTrabajadorResponse response = grupoFamiliarService.validarTrabajador(dni);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarFamiliar(@PathVariable Long id) {
        log.info("PUT /api/grupo-familiar/{}/desactivar - Desactivando familiar", id);
        grupoFamiliarService.desactivarFamiliar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/familiar/{dni}")
    public ResponseEntity<GrupoFamiliarResponse> obtenerTitularPorFamiliar(@PathVariable String dni) {
        log.info("GET /api/grupo-familiar/familiar/{} - Obteniendo titular", dni);
        GrupoFamiliarResponse response = grupoFamiliarService.obtenerTitularPorFamiliar(dni);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/es-familiar/{dni}")
    public ResponseEntity<Boolean> esFamiliarDeTrabajador(@PathVariable String dni) {
        log.info("GET /api/grupo-familiar/es-familiar/{} - Verificando si es familiar", dni);
        boolean esFamiliar = grupoFamiliarService.esFamiliarDeTrabajador(dni);
        return ResponseEntity.ok(esFamiliar);
    }
}

package com.pe.finanzas.roles.controller;

import com.pe.finanzas.roles.dto.SegregacionRequest;
import com.pe.finanzas.roles.dto.SegregacionResponse;
import com.pe.finanzas.roles.service.SegregacionFuncionesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/segregations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SegregacionFuncionesController {

    private final SegregacionFuncionesService segregacionService;

    @PostMapping
    public ResponseEntity<SegregacionResponse> createSegregation(@Valid @RequestBody SegregacionRequest request) {
        log.info("REST request to create segregation");
        SegregacionResponse response = segregacionService.createSegregation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SegregacionResponse> updateSegregation(
            @PathVariable Long id,
            @Valid @RequestBody SegregacionRequest request) {
        log.info("REST request to update segregation: {}", id);
        SegregacionResponse response = segregacionService.updateSegregation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSegregation(@PathVariable Long id) {
        log.info("REST request to delete segregation: {}", id);
        segregacionService.deleteSegregation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SegregacionResponse> getSegregationById(@PathVariable Long id) {
        log.info("REST request to get segregation: {}", id);
        SegregacionResponse response = segregacionService.getSegregationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<SegregacionResponse>> getSegregationsByRole(@PathVariable Long roleId) {
        log.info("REST request to get segregations by role: {}", roleId);
        List<SegregacionResponse> segregations = segregacionService.getSegregationsByRole(roleId);
        return ResponseEntity.ok(segregations);
    }

    @GetMapping("/conflict/{roleId1}/{roleId2}")
    public ResponseEntity<Map<String, Boolean>> checkConflict(
            @PathVariable Long roleId1,
            @PathVariable Long roleId2) {
        log.info("REST request to check conflict between roles {} and {}", roleId1, roleId2);
        boolean hasConflict = segregacionService.hasConflict(roleId1, roleId2);
        return ResponseEntity.ok(Map.of("hasConflict", hasConflict));
    }
}

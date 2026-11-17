package com.pe.finanzas.roles.controller;

import com.pe.finanzas.roles.dto.RestriccionRequest;
import com.pe.finanzas.roles.dto.RestriccionResponse;
import com.pe.finanzas.roles.dto.ValidateActionRequest;
import com.pe.finanzas.roles.service.RestriccionPermisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restrictions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RestriccionPermisoController {

    private final RestriccionPermisoService restriccionService;

    @PostMapping
    public ResponseEntity<RestriccionResponse> createRestriction(@Valid @RequestBody RestriccionRequest request) {
        log.info("REST request to create restriction");
        RestriccionResponse response = restriccionService.createRestriction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestriccionResponse> updateRestriction(
            @PathVariable Long id,
            @Valid @RequestBody RestriccionRequest request) {
        log.info("REST request to update restriction: {}", id);
        RestriccionResponse response = restriccionService.updateRestriction(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestriction(@PathVariable Long id) {
        log.info("REST request to delete restriction: {}", id);
        restriccionService.deleteRestriction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestriccionResponse> getRestrictionById(@PathVariable Long id) {
        log.info("REST request to get restriction: {}", id);
        RestriccionResponse response = restriccionService.getRestrictionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RestriccionResponse>> getRestrictionsByRole(@PathVariable Long roleId) {
        log.info("REST request to get restrictions by role: {}", roleId);
        List<RestriccionResponse> restrictions = restriccionService.getRestrictionsByRole(roleId);
        return ResponseEntity.ok(restrictions);
    }

    @GetMapping("/role/{roleId}/permission/{permissionId}")
    public ResponseEntity<List<RestriccionResponse>> getRestrictionsByRoleAndPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        log.info("REST request to get restrictions by role {} and permission {}", roleId, permissionId);
        List<RestriccionResponse> restrictions = restriccionService.getRestrictionsByRoleAndPermission(roleId,
                permissionId);
        return ResponseEntity.ok(restrictions);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateAction(@Valid @RequestBody ValidateActionRequest request) {
        log.info("REST request to validate action");
        boolean isValid = restriccionService.validateAction(request);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}

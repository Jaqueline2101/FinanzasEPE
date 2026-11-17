package com.pe.finanzas.roles.controller;

import com.pe.finanzas.roles.dto.DelegacionRequest;
import com.pe.finanzas.roles.dto.DelegacionResponse;
import com.pe.finanzas.roles.service.DelegacionTemporalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DelegacionTemporalController {

    private final DelegacionTemporalService delegacionService;

    @PostMapping
    public ResponseEntity<DelegacionResponse> createDelegation(@Valid @RequestBody DelegacionRequest request) {
        log.info("REST request to create delegation");
        DelegacionResponse response = delegacionService.createDelegation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DelegacionResponse> updateDelegation(
            @PathVariable Long id,
            @Valid @RequestBody DelegacionRequest request) {
        log.info("REST request to update delegation: {}", id);
        DelegacionResponse response = delegacionService.updateDelegation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelegation(@PathVariable Long id) {
        log.info("REST request to delete delegation: {}", id);
        delegacionService.deleteDelegation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DelegacionResponse> getDelegationById(@PathVariable Long id) {
        log.info("REST request to get delegation: {}", id);
        DelegacionResponse response = delegacionService.getDelegationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/origin/{usuarioOrigenId}")
    public ResponseEntity<List<DelegacionResponse>> getDelegationsByOriginUser(@PathVariable Long usuarioOrigenId) {
        log.info("REST request to get delegations by origin user: {}", usuarioOrigenId);
        List<DelegacionResponse> delegations = delegacionService.getDelegationsByOriginUser(usuarioOrigenId);
        return ResponseEntity.ok(delegations);
    }

    @GetMapping("/delegated/{usuarioDelegadoId}")
    public ResponseEntity<List<DelegacionResponse>> getDelegationsByDelegatedUser(
            @PathVariable Long usuarioDelegadoId) {
        log.info("REST request to get delegations by delegated user: {}", usuarioDelegadoId);
        List<DelegacionResponse> delegations = delegacionService.getDelegationsByDelegatedUser(usuarioDelegadoId);
        return ResponseEntity.ok(delegations);
    }

    @GetMapping("/active/{usuarioId}")
    public ResponseEntity<List<DelegacionResponse>> getActiveDelegationsForUser(@PathVariable Long usuarioId) {
        log.info("REST request to get active delegations for user: {}", usuarioId);
        List<DelegacionResponse> delegations = delegacionService.getActiveDelegationsForUser(usuarioId);
        return ResponseEntity.ok(delegations);
    }
}

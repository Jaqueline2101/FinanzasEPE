package com.pe.finanzas.roles.controller;

import com.pe.finanzas.roles.dto.AprobacionRequest;
import com.pe.finanzas.roles.dto.AprobacionResponse;
import com.pe.finanzas.roles.entity.EstadoAprobacion;
import com.pe.finanzas.roles.service.FlujoAprobacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FlujoAprobacionController {

    private final FlujoAprobacionService flujoService;

    @PostMapping
    public ResponseEntity<AprobacionResponse> createApprovalRequest(@Valid @RequestBody AprobacionRequest request) {
        log.info("REST request to create approval request");
        AprobacionResponse response = flujoService.createApprovalRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<AprobacionResponse> approveRequest(
            @PathVariable Long id,
            @RequestParam Long aprobadorId,
            @RequestParam(required = false) String comentario) {
        log.info("REST request to approve request: {}", id);
        AprobacionResponse response = flujoService.approveRequest(id, aprobadorId, comentario);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<AprobacionResponse> rejectRequest(
            @PathVariable Long id,
            @RequestParam Long aprobadorId,
            @RequestParam(required = false) String comentario) {
        log.info("REST request to reject request: {}", id);
        AprobacionResponse response = flujoService.rejectRequest(id, aprobadorId, comentario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AprobacionResponse> getApprovalById(@PathVariable Long id) {
        log.info("REST request to get approval: {}", id);
        AprobacionResponse response = flujoService.getApprovalById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requester/{solicitanteId}")
    public ResponseEntity<List<AprobacionResponse>> getApprovalsByRequester(@PathVariable Long solicitanteId) {
        log.info("REST request to get approvals by requester: {}", solicitanteId);
        List<AprobacionResponse> approvals = flujoService.getApprovalsByRequester(solicitanteId);
        return ResponseEntity.ok(approvals);
    }

    @GetMapping("/approver/{aprobadorId}")
    public ResponseEntity<List<AprobacionResponse>> getApprovalsByApprover(@PathVariable Long aprobadorId) {
        log.info("REST request to get approvals by approver: {}", aprobadorId);
        List<AprobacionResponse> approvals = flujoService.getApprovalsByApprover(aprobadorId);
        return ResponseEntity.ok(approvals);
    }

    @GetMapping("/approver/{aprobadorId}/pending")
    public ResponseEntity<List<AprobacionResponse>> getPendingApprovals(@PathVariable Long aprobadorId) {
        log.info("REST request to get pending approvals for approver: {}", aprobadorId);
        List<AprobacionResponse> approvals = flujoService.getPendingApprovals(aprobadorId);
        return ResponseEntity.ok(approvals);
    }

    @GetMapping("/status/{estado}")
    public ResponseEntity<List<AprobacionResponse>> getApprovalsByStatus(@PathVariable EstadoAprobacion estado) {
        log.info("REST request to get approvals by status: {}", estado);
        List<AprobacionResponse> approvals = flujoService.getApprovalsByStatus(estado);
        return ResponseEntity.ok(approvals);
    }
}

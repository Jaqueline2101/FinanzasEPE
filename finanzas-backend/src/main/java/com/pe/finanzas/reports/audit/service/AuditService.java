package com.pe.finanzas.reports.audit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.reports.audit.dto.AuditFilterDto;
import com.pe.finanzas.reports.audit.dto.LabAuditoriaDto;
import com.pe.finanzas.reports.audit.entity.AuditLog;
import com.pe.finanzas.reports.audit.repository.AuditRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String module, String action, String details,
            String username, Long userId, String ipAddress,
            String status, String userAgent, String url, String method) {
        logAction(module, action, details, username, userId, ipAddress, status, userAgent, url, method, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String module, String action, String details,
            String username, Long userId, String ipAddress,
            String status, String userAgent, String url, String method,
            String oldData, String newData) {

        AuditLog log = AuditLog.builder()
                .module(module)
                .action(action)
                .details(details)
                .username(username)
                .userId(userId)
                .ipAddress(ipAddress)
                .status(status)
                .userAgent(userAgent)
                .url(url)
                .method(method)
                .oldData(oldData)
                .newData(newData)
                .timestamp(LocalDateTime.now())
                .build();
        auditRepository.save(log);
    }

    /**
     * Registra una acción de usuario capturando automáticamente el contexto
     * (Usuario, IP, etc)
     */
    public void logUserAction(String module, String action, String details) {
        logUserAction(module, action, details, null, null);
    }

    /**
     * Registra una acción de usuario capturando automáticamente el contexto
     * (Usuario, IP, etc) con soporte para diffs (oldData, newData)
     */
    public void logUserAction(String module, String action, String details, String oldData, String newData) {
        try {
            String username = com.pe.finanzas.security.util.HttpUtils.getCurrentUsername();
            String ipAddress = com.pe.finanzas.security.util.HttpUtils.getClientIpAddress();
            String userAgent = com.pe.finanzas.security.util.HttpUtils.getUserAgent();

            // Intentar obtener URL y método si es posible
            String url = "N/A";
            String method = "N/A";
            var request = com.pe.finanzas.security.util.HttpUtils.getCurrentRequest();
            if (request != null) {
                url = request.getRequestURI();
                method = request.getMethod();
            }

            Long userId = null;

            logAction(module, action, details, username, userId, ipAddress, "Éxito", userAgent, url, method, oldData,
                    newData);
        } catch (Exception e) {
            // Silently fail to not block business logic
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }

    public Page<LabAuditoriaDto> searchAudits(AuditFilterDto filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        String sortBy = filter.getSortBy();
        if (sortBy == null || sortBy.isEmpty() || sortBy.equals("fecha")) {
            sortBy = "timestamp";
        }
        String sortDir = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getTabla() != null && !filter.getTabla().isEmpty()) {
                // Search in module or username mostly
                Predicate modulePred = cb.like(cb.lower(root.get("module")),
                        "%" + filter.getTabla().toLowerCase() + "%");
                Predicate userPred = cb.like(cb.lower(root.get("username")),
                        "%" + filter.getTabla().toLowerCase() + "%");
                predicates.add(cb.or(modulePred, userPred));
            }

            if (filter.getAccion() != null && !filter.getAccion().isEmpty()) {
                predicates.add(cb.equal(root.get("action"), filter.getAccion()));
            }

            if (filter.getIdPersonalUser() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getIdPersonalUser()));
            }

            if (filter.getFechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.getFechaInicio()));
            }

            if (filter.getFechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.getFechaFin()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        System.out.println("Searching audits with filter: " + filter);

        Page<AuditLog> logs = auditRepository.findAll(spec, pageable);
        System.out.println("Found " + logs.getTotalElements() + " logs.");

        return logs.map(this::mapToDto);
    }

    private LabAuditoriaDto mapToDto(AuditLog log) {
        return LabAuditoriaDto.builder()
                .idAuditoria(log.getId())
                .tabla(log.getModule())
                .idRegistro(log.getId() != null ? log.getId().toString() : "") // Fallback
                .accion(log.getAction())
                .idPersonalUser(log.getUserId())
                .nombreUsuario(log.getUsername())
                .emailUsuario(null) // Not stored
                .fecha(log.getTimestamp())
                .datoAnterior(log.getOldData())
                .datoNuevo(log.getNewData())
                .descripcionCambio(log.getDetails())
                .cantidadCambios(1)
                .ip(log.getIpAddress())
                .estado(log.getStatus())
                .userAgent(log.getUserAgent())
                .url(log.getUrl())
                .method(log.getMethod())
                .build();
    }
}

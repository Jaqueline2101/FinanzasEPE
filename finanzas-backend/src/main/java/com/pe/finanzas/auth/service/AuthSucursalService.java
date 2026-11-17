package com.pe.finanzas.auth.service;

import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.security.service.SecurityMonitorService;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.sucursales.repository.SucursalRepository;
import com.pe.finanzas.puntos.entity.Punto;
import com.pe.finanzas.puntos.repository.PuntoRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.pe.finanzas.reports.audit.service.AuditService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSucursalService {

        private final DatosPersonalesRepository datosPersonalesRepository;
        private final SucursalRepository sucursalRepository;
        private final PuntoRepository puntoRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuditService auditService;
        private final SecurityMonitorService securityMonitorService;

        public java.util.Map<String, Object> seleccionarSucursal(Long userId, Long sucursalId, Long puntoId,
                        String password,
                        HttpServletRequest request) {

                DatosPersonales user = datosPersonalesRepository.findByIdWithRoles(userId)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado (ID: " + userId + ")"));

                String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
                if (!user.getLogin().equals(authenticatedUsername)) {
                        String ipAddress = com.pe.finanzas.security.util.HttpUtils.getClientIpAddress(request);
                        String userAgent = com.pe.finanzas.security.util.HttpUtils.getUserAgent(request);
                        securityMonitorService.registerFailedAttempt(authenticatedUsername, ipAddress, userAgent,
                                        com.pe.finanzas.security.entity.FailureReason.UNAUTHORIZED);
                        auditService.logAction("AUTH", "SUCURSAL_CHANGE_ATTEMPT_UNAUTHORIZED",
                                        "Intentó cambiar sucursal para otro usuario ID: " + userId,
                                        authenticatedUsername, userId, ipAddress, "FAILED", userAgent,
                                        request.getRequestURI(), request.getMethod());
                        throw new RuntimeException("No estás autorizado para realizar esta acción para otro usuario.");
                }

                if (!passwordEncoder.matches(password, user.getPassword())) {
                        String ipAddress = com.pe.finanzas.security.util.HttpUtils.getClientIpAddress(request);
                        String userAgent = com.pe.finanzas.security.util.HttpUtils.getUserAgent(request);
                        securityMonitorService.registerFailedAttempt(user.getLogin(), ipAddress, userAgent,
                                        com.pe.finanzas.security.entity.FailureReason.INVALID_CREDENTIALS);
                        auditService.logAction("AUTH", "SUCURSAL_CHANGE_FAILED_PASSWORD",
                                        "Contraseña incorrecta al cambiar sucursal",
                                        user.getLogin(), user.getId(), ipAddress, "FAILED", userAgent,
                                        request.getRequestURI(), request.getMethod());
                        throw new RuntimeException("Contraseña incorrecta.");
                }

                List<Sucursal> allowedSucursales = obtenerSucursalesAutorizadas(user);
                boolean isAllowed = allowedSucursales.stream().anyMatch(s -> s.getIdSucursal().equals(sucursalId));

                if (!isAllowed) {
                        String ipAddress = com.pe.finanzas.security.util.HttpUtils.getClientIpAddress(request);
                        String userAgent = com.pe.finanzas.security.util.HttpUtils.getUserAgent(request);
                        auditService.logAction("AUTH", "SUCURSAL_CHANGE_FORBIDDEN",
                                        "Intento de acceso a sucursal no asignada ID: " + sucursalId,
                                        user.getLogin(), user.getId(), ipAddress, "FAILED", userAgent,
                                        request.getRequestURI(), request.getMethod());
                        throw new RuntimeException("No tienes permiso para acceder a esta sucursal.");
                }

                Sucursal nuevaSucursal = sucursalRepository.findById(sucursalId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Sucursal no encontrada (ID: " + sucursalId + ")"));

                Punto puntoSeleccionado = null;
                if (puntoId != null) {
                        puntoSeleccionado = puntoRepository.findById(puntoId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Punto de venta no encontrado (ID: " + puntoId + ")"));

                        if (!puntoSeleccionado.getSucursal().getIdSucursal().equals(sucursalId)) {
                                throw new RuntimeException(
                                                "El punto de venta no pertenece a la sucursal seleccionada.");
                        }

                        if (!"1".equals(puntoSeleccionado.getValido())) {
                                throw new RuntimeException("El punto de venta seleccionado no está activo.");
                        }
                }

                user.setSucursalActual(nuevaSucursal);
                user.setPuntoVentaActual(puntoSeleccionado);
                datosPersonalesRepository.save(user);

                String newToken;
                if (puntoSeleccionado != null) {
                        newToken = jwtService.generateTokenWithSucursalAndPunto(user, nuevaSucursal.getIdSucursal(),
                                        nuevaSucursal.getNombreSucursal(), puntoSeleccionado.getPunto(),
                                        puntoSeleccionado.getNombre());
                } else {
                        newToken = jwtService.generateTokenWithSucursal(user, nuevaSucursal.getIdSucursal(),
                                        nuevaSucursal.getNombreSucursal());
                }

                String ipAddress = com.pe.finanzas.security.util.HttpUtils.getClientIpAddress(request);
                String userAgent = com.pe.finanzas.security.util.HttpUtils.getUserAgent(request);

                String puntoInfo = puntoSeleccionado != null ? " - Punto: " + puntoSeleccionado.getNombre()
                                : " - Sin Punto";

                auditService.logAction("AUTH", "SUCURSAL_CHANGED",
                                "Cambió a sucursal ID: " + sucursalId + " - " + nuevaSucursal.getNombreSucursal()
                                                + puntoInfo,
                                user.getLogin(), user.getId(), ipAddress, "SUCCESS", userAgent,
                                request.getRequestURI(), request.getMethod());
                securityMonitorService.registerSuccessfulLogin(user, ipAddress, userAgent, newToken);

                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("token", newToken);
                result.put("sucursalNombre", nuevaSucursal.getNombreSucursal());
                if (puntoSeleccionado != null) {
                        result.put("puntoNombre", puntoSeleccionado.getNombre());
                }

                return result;
        }

        public List<Sucursal> obtenerSucursalesAutorizadas(DatosPersonales user) {
                boolean isAdmin = user.getRoles().stream()
                                .map(Role::getName)
                                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));

                if (isAdmin) {
                        return sucursalRepository.findAllActivas();
                } else {
                        return new ArrayList<>(user.getSucursalesAsignadas());
                }
        }
}

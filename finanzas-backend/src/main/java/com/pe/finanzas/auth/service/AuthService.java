package com.pe.finanzas.auth.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.auth.service.dto.LoginRequest;
import com.pe.finanzas.auth.service.dto.LoginResponse;
import com.pe.finanzas.auth.service.dto.RegisterRequest;
import com.pe.finanzas.auth.service.dto.RegisterResponse;
import com.pe.finanzas.auth.service.dto.TokenInfoResponse;
import com.pe.finanzas.exception.AuthException;
import com.pe.finanzas.exception.ValidationException;

import com.pe.finanzas.reports.audit.service.AuditService;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.roles.repository.RoleRepository;
import com.pe.finanzas.security.entity.FailureReason;
import com.pe.finanzas.security.service.SecurityMonitorService;
import com.pe.finanzas.security.util.HttpUtils;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

        private final DatosPersonalesRepository datosPersonalesRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final SecurityMonitorService securityMonitorService;
        private final AuditService auditService;

        public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
                String ipAddress = HttpUtils.getClientIpAddress(httpRequest);
                String userAgent = HttpUtils.getUserAgent(httpRequest);

                log.info("Login attempt for user: {} from IP: {}", request.getLogin(), ipAddress);

                // 1. VERIFICAR SI LA IP ESTÁ BLOQUEADA
                if (securityMonitorService.isIpBlocked(ipAddress)) {
                        log.warn("Login attempt from blocked IP: {}", ipAddress);

                        int remainingAttempts = securityMonitorService.getRemainingAttempts(ipAddress);
                        var unblockTime = securityMonitorService.getUnblockTime(ipAddress);

                        securityMonitorService.registerFailedAttempt(
                                        request.getLogin(),
                                        ipAddress,
                                        userAgent,
                                        FailureReason.IP_BLOCKED);

                        throw new AuthException(
                                        "IP bloqueada temporalmente por intentos sospechosos. Intente nuevamente más tarde.",
                                        remainingAttempts,
                                        true,
                                        unblockTime);
                }

                // 2. VALIDAR CREDENCIALES CON AuthenticationManager
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getLogin(),
                                                        request.getPassword()));
                } catch (AuthenticationException e) {
                        log.error("Authentication failed for user: {} from IP: {}", request.getLogin(), ipAddress);

                        // Registrar intento fallido
                        securityMonitorService.registerFailedAttempt(
                                        request.getLogin(),
                                        ipAddress,
                                        userAgent,
                                        FailureReason.INVALID_CREDENTIALS);

                        // Calcular intentos restantes
                        int remainingAttempts = securityMonitorService.getRemainingAttempts(ipAddress);

                        // Mensajes progresivos
                        String message;
                        if (remainingAttempts == 0) {
                                message = "Credenciales inválidas. Su IP ha sido bloqueada temporalmente por intentos excesivos.";
                        } else if (remainingAttempts == 1) {
                                message = "Credenciales inválidas. ⚠️ ÚLTIMO INTENTO antes del bloqueo.";
                        } else if (remainingAttempts <= 2) {
                                message = String.format("Credenciales inválidas. Le quedan %d intentos.",
                                                remainingAttempts);
                        } else {
                                message = "Credenciales inválidas. Usuario o contraseña incorrectos.";
                        }

                        // Auditoría de Fallo
                        try {
                                auditService.logAction(
                                                "LOGIN",
                                                "LOGIN_FAILED",
                                                "Intento fallido de inicio de sesión. " + message,
                                                request.getLogin(),
                                                null,
                                                ipAddress,
                                                "Fallo",
                                                userAgent,
                                                "/api/auth/login",
                                                "POST");
                        } catch (Exception ex) {
                                log.error("Error registrando auditoría de fallo de login: {}", ex.getMessage());
                        }

                        throw new AuthException(message, remainingAttempts, remainingAttempts == 0, null);
                }

                // 3. CARGAR USUARIO CON ROLES Y PERMISOS
                try {
                        DatosPersonales user = datosPersonalesRepository.findByLoginWithRoles(request.getLogin())
                                        .orElseThrow(() -> {
                                                securityMonitorService.registerFailedAttempt(
                                                                request.getLogin(),
                                                                ipAddress,
                                                                userAgent,
                                                                FailureReason.USER_NOT_FOUND);
                                                return new AuthException("Usuario no encontrado");
                                        });

                        // 4. VERIFICAR SI LA CUENTA ESTÁ ACTIVA
                        if (!user.getActive()) {
                                log.warn("Attempt to login with inactive account: {}", request.getLogin());
                                securityMonitorService.registerFailedAttempt(
                                                request.getLogin(),
                                                ipAddress,
                                                userAgent,
                                                FailureReason.ACCOUNT_BLOCKED);
                                throw new AuthException("La cuenta ha sido bloqueada. Contacte al administrador.");
                        }

                        // Actualizar último login
                        user.setLastLogin(LocalDateTime.now());
                        datosPersonalesRepository.save(user);

                        // 5. REGISTRAR SESIÓN EXITOSA
                        log.info("Generating token for user: {}", user.getLogin());

                        // Generar token con sucursal guardada (si existe)
                        // Generar token con sucursal y punto guardados (si existen)
                        String token;
                        if (user.getSucursalActual() != null) {
                                if (user.getPuntoVentaActual() != null) {
                                        token = jwtService.generateTokenWithSucursalAndPunto(
                                                        user,
                                                        user.getSucursalActual().getIdSucursal(),
                                                        user.getSucursalActual().getNombreSucursal(),
                                                        user.getPuntoVentaActual().getPunto(),
                                                        user.getPuntoVentaActual().getNombre());
                                        log.info("Token generated with saved sucursal: {} and punto: {}",
                                                        user.getSucursalActual().getNombreSucursal(),
                                                        user.getPuntoVentaActual().getNombre());
                                } else {
                                        token = jwtService.generateTokenWithSucursal(
                                                        user,
                                                        user.getSucursalActual().getIdSucursal(),
                                                        user.getSucursalActual().getNombreSucursal());
                                        log.info("Token generated with saved sucursal: {} (no punto)",
                                                        user.getSucursalActual().getNombreSucursal());
                                }
                        } else {
                                token = jwtService.generateToken(user);
                                log.info("Token generated without sucursal");
                        }

                        securityMonitorService.registerSuccessfulLogin(user, ipAddress, userAgent, token);

                        // Auditoría
                        try {
                                auditService.logAction(
                                                "LOGIN",
                                                "LOGIN_SUCCESS",
                                                "Usuario " + user.getLogin() + " ha iniciado sesión exitosamente.",
                                                user.getLogin(),
                                                user.getId(),
                                                ipAddress,
                                                "Éxito",
                                                userAgent,
                                                "/api/auth/login",
                                                "POST");
                        } catch (Exception e) {
                                log.error("Error logging audit for login", e);
                        }

                        String moduloContext = request.getModulo();
                        if (moduloContext == null || moduloContext.trim().isEmpty()
                                        || "Finanzas".equalsIgnoreCase(moduloContext)) {
                                moduloContext = "Sistema"; // Redirección total al nuevo contexto
                        }

                        Collection<? extends GrantedAuthority> authorities = user.getAuthoritiesByModulo(moduloContext);
                        log.info("Filtering permissions for context: {}", moduloContext);

                        Set<String> allPermissions = authorities.stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .filter(auth -> !auth.startsWith("ROLE_"))
                                        .collect(Collectors.toSet());
                        Set<String> roleNames = authorities.stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .filter(auth -> auth.startsWith("ROLE_"))
                                        .map(role -> role.replace("ROLE_", ""))
                                        .collect(Collectors.toSet());

                        log.info("Permisos finales enviados al Front: {}", allPermissions);

                        return LoginResponse.builder()
                                        .token(token)
                                        .userId(user.getId())
                                        .idPersonal(user.getId())
                                        .username(user.getLogin())
                                        .email(user.getEmail())
                                        .firstName(user.getNombre())
                                        .paternalSurname(user.getApepat())
                                        .maternalSurname(user.getApemat())
                                        .lastName(user.getApepat())
                                        .sexo(user.getSexo())
                                        .roles(roleNames)
                                        .permissions(allPermissions)
                                        .sucursalId(user.getSucursalActual() != null
                                                        ? user.getSucursalActual().getIdSucursal()
                                                        : null)
                                        .sucursalNombre(user.getSucursalActual() != null
                                                        ? user.getSucursalActual().getNombreSucursal()
                                                        : null)
                                        .puntoId(user.getPuntoVentaActual() != null
                                                        ? user.getPuntoVentaActual().getPunto()
                                                        : null)
                                        .puntoNombre(user.getPuntoVentaActual() != null
                                                        ? user.getPuntoVentaActual().getNombre()
                                                        : null)
                                        .fotoPerfil(user.getFotoPerfil())
                                        .mustChangePassword(user.getMustChangePassword())
                                        .build();
                } catch (Exception e) {
                        log.error("CRITICAL UNEXPECTED ERROR during login flow for user: " + request.getLogin(), e);
                        throw e;
                }
        }

        public RegisterResponse register(RegisterRequest request) {
                log.info("Register attempt for username: {}", request.getUsername());

                if (datosPersonalesRepository.findByLogin(request.getUsername()).isPresent()) {
                        throw new ValidationException("El nombre de usuario ya existe. Elija otro.");
                }

                // Email check could ideally use existsByEmail but not strictly present in your
                // repos, skipping or using simplified

                DatosPersonales newUser = new DatosPersonales();
                newUser.setLogin(request.getUsername());
                newUser.setEmail(request.getEmail());
                newUser.setPasswd(passwordEncoder.encode(request.getPassword()));
                newUser.setNombre(request.getFirstName());
                newUser.setApepat(request.getLastName());
                // newUser.setFonLocal(request.getPhone()); // Verify exist
                newUser.setActive(true);
                newUser.setRoles(new HashSet<>());

                Set<Role> assignedRoles = new HashSet<>();

                if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                        for (String roleName : request.getRoles()) {
                                Role role = roleRepository.findFirstByName(roleName.toUpperCase())
                                                .orElseThrow(() -> new ValidationException(
                                                                "Rol no encontrado: " + roleName));
                                assignedRoles.add(role);
                        }
                } else {
                        // Default role logic (requires ROLE_USER or similar to exist in UserPermission
                        // table)
                        // For Finanzas, maybe strict? Or default?
                        // Leaving logic, but be warned if PACIENTE doesn't exist
                        roleRepository.findFirstByName("PACIENTE").ifPresent(assignedRoles::add);
                }

                newUser.setRoles(assignedRoles);

                DatosPersonales savedUser = datosPersonalesRepository.save(newUser);

                Set<String> roleNames = savedUser.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet());

                log.info("User {} registered successfully with roles: {}", savedUser.getLogin(), roleNames);

                return RegisterResponse.builder()
                                .id(savedUser.getId())
                                .username(savedUser.getLogin())
                                .email(savedUser.getEmail())
                                .firstName(savedUser.getNombre())
                                .paternalSurname(savedUser.getApepat())
                                .maternalSurname(savedUser.getApemat())
                                .lastName(savedUser.getApepat())
                                .roles(roleNames)
                                .message("Usuario registrado exitosamente")
                                .build();
        }

        public void logout(String token, HttpServletRequest httpRequest) {
                try {
                        String username = jwtService.extractUsername(token);
                        Long userId = jwtService.extractUserId(token);
                        String ipAddress = HttpUtils.getClientIpAddress(httpRequest);
                        String userAgent = HttpUtils.getUserAgent(httpRequest);

                        log.info("User {} logged out successfully", username);

                        // Close session in SecurityMonitor
                        securityMonitorService.closeSession(token);

                        try {
                                auditService.logAction(
                                                "LOGOUT",
                                                "LOGOUT",
                                                "Usuario " + username + " cerró sesión.",
                                                username,
                                                userId,
                                                ipAddress,
                                                "Éxito",
                                                userAgent,
                                                "/api/auth/logout",
                                                "POST");
                        } catch (Exception e) {
                                log.error("Error logging audit for logout", e);
                        }

                } catch (Exception e) {
                        log.error("Error processing logout request", e);
                }
        }

        public TokenInfoResponse getTokenInfo(String token) {
                return TokenInfoResponse.builder()
                                .token(token)
                                .timeRemainingMs(jwtService.getTimeRemaining(token))
                                .timeRemainingSeconds(jwtService.getTimeRemainingInSeconds(token))
                                .expirationTimeMs(jwtService.getExpirationTime().getTime())
                                .isExpired(jwtService.isTokenExpired(token))
                                .build();
        }

        public TokenInfoResponse refreshToken(String oldToken) {
                String username = jwtService.extractUsername(oldToken);

                DatosPersonales user = datosPersonalesRepository.findByLoginWithRoles(username)
                                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

                if (!user.getActive()) {
                        throw new AuthException("Cuenta inactiva");
                }

                String newToken;
                if (user.getSucursalActual() != null) {
                        if (user.getPuntoVentaActual() != null) {
                                newToken = jwtService.generateTokenWithSucursalAndPunto(
                                                user,
                                                user.getSucursalActual().getIdSucursal(),
                                                user.getSucursalActual().getNombreSucursal(),
                                                user.getPuntoVentaActual().getPunto(),
                                                user.getPuntoVentaActual().getNombre());
                        } else {
                                newToken = jwtService.generateTokenWithSucursal(
                                                user,
                                                user.getSucursalActual().getIdSucursal(),
                                                user.getSucursalActual().getNombreSucursal());
                        }
                } else {
                        newToken = jwtService.generateToken(user);
                }

                return TokenInfoResponse.builder()
                                .token(newToken)
                                .timeRemainingMs(jwtService.getTimeRemaining(newToken))
                                .timeRemainingSeconds(jwtService.getTimeRemainingInSeconds(newToken))
                                .expirationTimeMs(jwtService.getExpirationTime().getTime())
                                .isExpired(false)
                                .build();
        }
}
package com.pe.finanzas.security.service;

import com.pe.finanzas.security.entity.FailureReason;
import com.pe.finanzas.security.entity.IntrusionAttempt;
import com.pe.finanzas.security.entity.Session;
import com.pe.finanzas.security.entity.SessionStatus;
import com.pe.finanzas.security.repository.IntrusionAttemptRepository;
import com.pe.finanzas.security.repository.SessionRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityMonitorService {

    private final IntrusionAttemptRepository intrusionAttemptRepository;
    private final SessionRepository sessionRepository;

    @Value("${security.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.block-duration-minutes:30}")
    private int blockDurationMinutes;

    @Transactional
    public void registerFailedAttempt(String username, String ipAddress, String userAgent, FailureReason reason) {
        IntrusionAttempt attempt = new IntrusionAttempt();
        attempt.setIpAddress(ipAddress);
        attempt.setAttempts(1); // Inicializar attempts en 1
        attempt.setLastUsername(username);
        attempt.setLastFailureReason(reason);
        attempt.setLastAttempt(LocalDateTime.now());
        intrusionAttemptRepository.save(attempt);
        log.warn("Security Event: Failed login for user {} from IP {} due to {}", username, ipAddress, reason);
    }

    @Transactional(readOnly = true)
    public boolean isIpBlocked(String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long attempts = intrusionAttemptRepository.countByIpAddressAndLastAttemptAfter(ipAddress, oneHourAgo);
        return attempts >= maxAttempts; // Assuming MAX_ATTEMPTS refers to maxAttempts field
    }

    public int getRemainingAttempts(String ipAddress) {
        LocalDateTime sinceTime = LocalDateTime.now().minusMinutes(blockDurationMinutes);
        long recentFailures = intrusionAttemptRepository.countByIpAddressAndLastAttemptAfter(ipAddress, sinceTime);
        return Math.max(0, maxAttempts - (int) recentFailures);
    }

    public LocalDateTime getUnblockTime(String ipAddress) {
        // Estimación simple: último intento + duración bloqueo
        return LocalDateTime.now().plusMinutes(blockDurationMinutes);
    }

    @Transactional
    public void registerSuccessfulLogin(DatosPersonales user, String ipAddress, String userAgent, String token) {
        // Idempotency check: If session already exists for this token, do nothing
        if (sessionRepository.findByToken(token).isPresent()) {
            return;
        }

        Session session = new Session();
        session.setUser(user);
        session.setToken(token);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setStatus(SessionStatus.ACTIVE);
        session.setLoginTime(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        // session.setExpirationTime(...) debería configurarse según JWT

        try {
            sessionRepository.save(session);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle race condition where token was inserted between check and save
            log.warn("Duplicate session token detected during registration. Providing idempotency.");
        }
    }

    @Transactional
    public void closeSession(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setStatus(SessionStatus.CLOSED);
            sessionRepository.save(session);
        });
    }
}

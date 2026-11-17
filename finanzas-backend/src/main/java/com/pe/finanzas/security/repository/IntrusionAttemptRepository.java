package com.pe.finanzas.security.repository;

import com.pe.finanzas.security.entity.FailureReason;
import com.pe.finanzas.security.entity.IntrusionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IntrusionAttemptRepository extends JpaRepository<IntrusionAttempt, Long> {

        Optional<IntrusionAttempt> findByIpAddress(String ipAddress);

        // 1. CONTEO DE FALLOS (Para lógica de bloqueo):
        // Cuenta cuántas veces falló una IP desde una hora determinada
        long countByIpAddressAndLastAttemptAfter(String ipAddress, LocalDateTime sinceTime);

        // Cuenta cuántas veces se intentó atacar a un usuario específico
        long countByLastUsernameAndLastAttemptAfter(String username, LocalDateTime sinceTime);

        // 2. CONSULTAS PARA EL PANEL DE SEGURIDAD:
        // Lista de intentos recientes por IP
        List<IntrusionAttempt> findByIpAddressAndLastAttemptAfterOrderByLastAttemptDesc(
                        String ipAddress,
                        LocalDateTime sinceTime);

        // 3. LÓGICA DE BLOQUEO AUTOMÁTICO (Query avanzada):
        // Devuelve TRUE si la IP ya superó el límite (threshold) de intentos fallidos
        @Query("SELECT COUNT(i) >= :threshold FROM IntrusionAttempt i " +
                        "WHERE i.ipAddress = :ipAddress " +
                        "AND i.lastAttempt >= :sinceTime")
        boolean isIpBlocked(
                        @Param("ipAddress") String ipAddress,
                        @Param("sinceTime") LocalDateTime sinceTime,
                        @Param("threshold") long threshold);

        // 4. ANÁLISIS DE MOTIVOS:
        // Para saber si nos están atacando con usuarios inexistentes o contraseñas
        // viejas
        List<IntrusionAttempt> findByLastFailureReasonAndLastAttemptBetween(
                        FailureReason reason,
                        LocalDateTime startTime,
                        LocalDateTime endTime);

        // 5. MANTENIMIENTO:
        // Borra intentos antiguos para que la tabla no crezca infinitamente
        @Modifying
        @Transactional
        void deleteByLastAttemptBefore(LocalDateTime beforeTime);
}
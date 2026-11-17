package com.pe.finanzas.security.repository;

import com.pe.finanzas.security.entity.Session;
import com.pe.finanzas.security.entity.SessionStatus;
import com.pe.finanzas.users.entity.DatosPersonales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // 1. BUSQUEDA POR TOKEN: Para validar cada petición en el JwtFilter
    Optional<Session> findByToken(String token);

    // 2. CONTROL DE SESIONES ACTIVAS: Para saber si un usuario tiene varias
    // sesiones
    List<Session> findByUserAndStatus(DatosPersonales user, SessionStatus status);

    // 3. AUDITORÍA DE ÚLTIMO ACCESO: Para mostrar "Tu última conexión fue..."
    @Query("SELECT s FROM Session s WHERE s.user = :user ORDER BY s.loginTime DESC LIMIT 1")
    Optional<Session> findLastSessionByUser(@Param("user") DatosPersonales user);

    // 4. DETECCIÓN DE INTRUSOS (CRÍTICO PARA FINANZAS):
    // Verifica si hay una sesión activa en una IP distinta a la actual
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.user = :user " +
            "AND s.ipAddress != :currentIp " +
            "AND s.status = 'ACTIVE' " +
            "AND s.loginTime >= :sinceTime")
    boolean existsRecentSessionFromDifferentIp(
            @Param("user") DatosPersonales user,
            @Param("currentIp") String currentIp,
            @Param("sinceTime") LocalDateTime sinceTime);

    // 5. MONITOREO DE ACTIVIDAD SOSPECHOSA: Para paneles de administración
    List<Session> findByIsSuspiciousAndLoginTimeBetween(
            boolean isSuspicious,
            LocalDateTime startTime,
            LocalDateTime endTime);

    // 6. MANTENIMIENTO: Limpiar sesiones expiradas de la base de datos
    void deleteByExpirationTimeBefore(LocalDateTime now);

    // 7. BLOQUEO POR IP: Ver todas las sesiones asociadas a una IP marcada
    List<Session> findByIpAddressAndStatus(String ipAddress, SessionStatus status);
}
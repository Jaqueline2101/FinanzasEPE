package com.pe.finanzas.audit.interceptor;

import com.pe.finanzas.audit.entity.AuditLog;
import com.pe.finanzas.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Solo auditar métodos de escritura (POST, PUT, DELETE, PATCH)
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            return;
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";

            // Obtener IP
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) ip = request.getRemoteAddr();

            AuditLog audit = AuditLog.builder()
                    .username(username)
                    .method(method)
                    .url(request.getRequestURI())
                    .responseStatus(response.getStatus())
                    .ipAddress(ip)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Guardar log de forma asíncrona sería mejor, pero por ahora directo
            auditLogRepository.save(audit);
            
        } catch (Exception e) {
            log.error("Error saving audit log", e);
        }
    }
}

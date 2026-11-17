package com.pe.finanzas.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.pe.finanzas.users.entity.DatosPersonales user;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String token;

    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    // Campo para localización (agregado para coincidir con DTO)
    private String location;

    // Campo para marcar sospechoso (agregado para coincidir con Repositorio)
    private boolean isSuspicious;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private LocalDateTime loginTime;

    private LocalDateTime lastActivity;

    private LocalDateTime expirationTime;

    @PrePersist
    public void prePersist() {
        if (this.loginTime == null) {
            this.loginTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = SessionStatus.ACTIVE;
        }
    }
}

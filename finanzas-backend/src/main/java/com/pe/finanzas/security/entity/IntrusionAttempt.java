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
@Table(name = "intrusion_attempts")
public class IntrusionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private Integer attempts;

    private LocalDateTime lastAttempt;

    private LocalDateTime blockedUntil;

    private String lastUsername;

    @Enumerated(EnumType.STRING)
    private FailureReason lastFailureReason;

    @PrePersist
    public void prePersist() {
        if (this.lastAttempt == null) {
            this.lastAttempt = LocalDateTime.now();
        }
    }
}

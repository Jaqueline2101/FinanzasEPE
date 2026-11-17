package com.pe.finanzas.security.dto;

import com.pe.finanzas.security.entity.SessionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfoDTO {
    private Long id;
    private String username;
    private String ipAddress;
    private String userAgent;
    private SessionStatus status;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private LocalDateTime lastActivity;
    private boolean isSuspicious;
    private boolean isExpired;
    private String location;
}

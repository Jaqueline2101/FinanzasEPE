package com.pe.finanzas.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfoResponse {
    private String token;
    private long timeRemainingMs;
    private long timeRemainingSeconds;
    private long expirationTimeMs;
    private boolean isExpired;
}

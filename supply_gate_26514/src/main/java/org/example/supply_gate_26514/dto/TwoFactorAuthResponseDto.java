package org.example.supply_gate_26514.dto;

import java.util.UUID;

/**
 * DTO for Two-Factor Authentication response.
 * Returned when 2FA is required after initial login.
 */
public record TwoFactorAuthResponseDto(
    String sessionId,      // Temporary session ID for 2FA verification
    UUID userId,           // User ID
    String username,       // Username
    String message         // Message to display to user
) {}


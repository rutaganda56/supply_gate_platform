package org.example.supply_gate_26514.dto;

import java.util.UUID;

/**
 * DTO for authentication responses.
 * 
 * Follows enterprise patterns where login returns:
 * - Access token (short-lived, for API calls)
 * - Refresh token (long-lived, for token renewal)
 * - User information
 * 
 * This enables secure token refresh without re-authentication.
 */
public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn, // seconds until access token expires
        UUID userId,
        String username,
        String userType
) {
    public AuthResponseDto {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        if (tokenType == null || tokenType.isEmpty()) {
            tokenType = "Bearer";
        }
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("Expires in must be positive");
        }
    }
}
















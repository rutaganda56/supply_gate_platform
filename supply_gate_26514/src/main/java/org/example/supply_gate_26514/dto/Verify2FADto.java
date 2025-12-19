package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for 2FA code verification request.
 */
public record Verify2FADto(
    @NotEmpty
    String sessionId,
    
    @NotEmpty
    @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be 6 digits")
    String code
) {}


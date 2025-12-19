package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * DTO for password reset (with token).
 * 
 * Password requirements:
 * - Minimum 8 characters
 * - Must contain at least one letter and one number
 */
public record PasswordResetDto(
    @NotEmpty
    String token,
    
    @NotEmpty
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword
) {}


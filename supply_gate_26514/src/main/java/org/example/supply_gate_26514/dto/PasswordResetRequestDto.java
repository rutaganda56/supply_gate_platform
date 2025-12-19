package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for password reset request (forgot password).
 */
public record PasswordResetRequestDto(
    @NotEmpty
    @Email
    String email
) {}


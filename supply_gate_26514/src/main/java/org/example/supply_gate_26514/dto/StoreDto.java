package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating/updating a Store.
 * 
 * SECURITY NOTE: userId is NOT included in this DTO.
 * The user ID is automatically extracted from the authentication context
 * to prevent unauthorized store creation for other users.
 * This follows enterprise security best practices.
 */
public record StoreDto(
        @NotEmpty(message = "Store name is required")
        String storeName,
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        String phoneNumber,
        @Email(message = "Invalid email format")
        String storeEmail
) {
}

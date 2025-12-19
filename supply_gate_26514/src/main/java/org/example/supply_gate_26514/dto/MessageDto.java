package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for sending a message to a supplier.
 */
public record MessageDto(
        @NotNull(message = "Supplier ID is required")
        UUID supplierId,
        
        @NotEmpty(message = "Your name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String senderName,
        
        @NotEmpty(message = "Email is required")
        @Email(message = "Invalid email format")
        String senderEmail,
        
        String senderPhone, // Optional
        
        @NotEmpty(message = "Subject is required")
        @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
        String subject,
        
        @NotEmpty(message = "Message is required")
        @Size(min = 10, max = 5000, message = "Message must be between 10 and 5000 characters")
        String messageContent,
        
        UUID productId, // Optional: Related product
        String productName // Optional: Product name
) {
}


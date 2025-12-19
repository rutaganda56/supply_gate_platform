package org.example.supply_gate_26514.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for message responses.
 */
public record MessageResponseDto(
        UUID messageId,
        UUID supplierId,
        String supplierName,
        String supplierEmail,
        String senderName,
        String senderEmail,
        String senderPhone,
        String subject,
        String messageContent,
        UUID productId,
        String productName,
        LocalDateTime createdAt,
        boolean isRead,
        LocalDateTime readAt
) {
}


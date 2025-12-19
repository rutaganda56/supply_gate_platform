package org.example.supply_gate_26514.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDto(
        UUID notificationId,
        UUID userId,
        String userName,
        String message,
        String type,
        boolean isRead,
        LocalDateTime createdAt
) {
}
















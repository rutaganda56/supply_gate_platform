package org.example.supply_gate_26514.dto;

import java.util.UUID;

public record ReviewDto(
        UUID productId,
        UUID userId,
        String message
) {
}

package org.example.supply_gate_26514.dto;

import java.util.UUID;

public record CategoryResponseDto(
        UUID categoryId,
        String categoryName
) {
}

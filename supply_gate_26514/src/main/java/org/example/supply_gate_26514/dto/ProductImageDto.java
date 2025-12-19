package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductImageDto(
        @NotEmpty
        String imageUrl,
        @NotNull
        UUID productId
) {
}

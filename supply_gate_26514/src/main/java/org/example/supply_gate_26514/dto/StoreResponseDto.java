package org.example.supply_gate_26514.dto;

import java.util.UUID;

public record StoreResponseDto(
        UUID storeId,
        String storeName,
        String phoneNumber,
        String storeEmail
) {
}

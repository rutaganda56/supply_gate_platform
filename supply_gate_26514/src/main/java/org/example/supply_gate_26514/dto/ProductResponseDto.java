package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record ProductResponseDto(
        UUID productId,
        @NotEmpty
        String productName,
        String productDescription,
        Double productPrice,
        @NotEmpty
        String quantity,
        UUID categoryId,
        String categoryName,
        UUID storeId,
        String storeName,
        List<String> imageUrls,
        // Supplier information
        UUID supplierId,              // User ID of the supplier
        String supplierName,           // Supplier's full name
        String supplierEmail,          // Supplier's email
        Boolean isSupplierVerified     // Whether supplier is verified (APPROVED status)
) {
}

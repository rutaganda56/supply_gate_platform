package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductDto(
        @NotEmpty
  UUID categoryId,
        @NotEmpty
  UUID storeId,
        @NotEmpty
  String productName,
        @NotEmpty
  String productDescription,
  @NotNull
  Double productPrice,
  @NotEmpty
  String quantity
) {
}

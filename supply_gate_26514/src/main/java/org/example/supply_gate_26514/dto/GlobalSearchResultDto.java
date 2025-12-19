package org.example.supply_gate_26514.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for global search results.
 * Contains categorized results from multiple entities.
 */
public record GlobalSearchResultDto(
        List<SearchResultItem> products,
        List<SearchResultItem> stores,
        List<SearchResultItem> categories,
        List<SearchResultItem> verifications,
        List<SearchResultItem> messages,
        int totalResults
) {
    public static record SearchResultItem(
            String id,
            String title,
            String description,
            String type, // "product", "store", "category", "verification", "message"
            String url,  // Navigation URL
            String metadata // Additional info (e.g., "Price: $100", "Status: APPROVED")
    ) {}
}

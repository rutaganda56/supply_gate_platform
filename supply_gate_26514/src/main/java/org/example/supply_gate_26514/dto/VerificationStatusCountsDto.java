package org.example.supply_gate_26514.dto;

/**
 * DTO for verification status counts.
 * Used to display statistics on the industry dashboard.
 */
public record VerificationStatusCountsDto(
    long pendingCount,
    long approvedCount,
    long rejectedCount,
    long totalCount
) {
}

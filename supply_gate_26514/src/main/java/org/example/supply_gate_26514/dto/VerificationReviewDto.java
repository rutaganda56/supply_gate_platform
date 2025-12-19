package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import org.example.supply_gate_26514.model.VerificationStatus;

import java.util.UUID;

public record VerificationReviewDto(
        @NotEmpty
        VerificationStatus status,
        String rejectionReason,
        UUID reviewedBy
) {
}
















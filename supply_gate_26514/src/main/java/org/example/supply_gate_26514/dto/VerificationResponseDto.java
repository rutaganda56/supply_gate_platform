package org.example.supply_gate_26514.dto;

import org.example.supply_gate_26514.model.VerificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VerificationResponseDto(
        UUID verificationId,
        UUID userId,
        String supplierName,
        String email,
        String businessType,
        VerificationStatus status,
        String businessLicenseUrl,
        String taxCertificateUrl,
        String bankStatementUrl,
        String identityProofUrl,
        LocalDateTime submittedDate,
        LocalDateTime lastUpdatedDate,
        String rejectionReason,
        UUID reviewedBy,
        String companyName,
        UUID assignedIndustryId,
        String assignedIndustryName
) {
}

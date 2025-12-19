package org.example.supply_gate_26514.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

public record VerificationDto(
        @NotEmpty
        UUID userId,
        String businessLicenseUrl,
        String taxCertificateUrl,
        String bankStatementUrl,
        String identityProofUrl
) {
}
















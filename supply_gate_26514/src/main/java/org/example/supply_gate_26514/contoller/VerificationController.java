package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.dto.VerificationResponseDto;
import org.example.supply_gate_26514.dto.VerificationReviewDto;
import org.example.supply_gate_26514.dto.VerificationStatusCountsDto;
import org.example.supply_gate_26514.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    /**
     * Get all verifications (paginated with search).
     * Requires authentication.
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param search Optional search term to filter verifications
     */
    @GetMapping
    public ResponseEntity<Page<VerificationResponseDto>> getAllVerifications(
            @PageableDefault(size = 20, sort = "submittedDate") Pageable pageable,
            @RequestParam(required = false) String search) {
        try {
            Page<VerificationResponseDto> verifications = verificationService.getAllVerifications(pageable, search);
            return ResponseEntity.ok(verifications);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
    
    /**
     * Gets verification status counts (pending, approved, rejected, total).
     * For industry workers, only counts verifications assigned to their industry.
     * 
     * @return VerificationStatusCountsDto with counts for each status
     */
    @GetMapping("/status-counts")
    public ResponseEntity<VerificationStatusCountsDto> getStatusCounts() {
        try {
            VerificationStatusCountsDto counts = verificationService.getStatusCounts();
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            System.err.println("ERROR in getStatusCounts endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current user's verification.
     * Requires authentication.
     */
    @GetMapping("/my-verification")
    public ResponseEntity<?> getMyVerification() {
        try {
            Optional<VerificationResponseDto> verification = verificationService.getMyVerification();
            if (verification.isPresent()) {
                return ResponseEntity.ok(verification.get());
            } else {
                // Return 404 with a clear message
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new HashMap<String, String>() {{
                            put("message", "No verification found for current user");
                            put("status", "NOT_FOUND");
                        }});
            }
        } catch (IllegalStateException e) {
            // Authentication/authorization error
            System.err.println("ERROR in getMyVerification (auth): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new HashMap<String, String>() {{
                        put("error", "Authentication required");
                        put("message", e.getMessage());
                    }});
        } catch (Exception e) {
            // Other errors
            System.err.println("ERROR in getMyVerification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to retrieve verification");
                        put("message", e.getMessage());
                    }});
        }
    }
    
    /**
     * Submit verification documents.
     * Requires authentication (supplier role).
     * 
     * @param companyName Company name (for backward compatibility)
     * @param assignedIndustryId Industry ID to assign this verification to (optional but recommended)
     * @param businessLicense Business license file
     * @param taxCertificate Tax certificate file
     * @param bankStatement Bank statement file
     * @param identityProof Identity proof file
     * @return VerificationResponseDto
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitVerification(
            @RequestParam("companyName") String companyName,
            @RequestParam(value = "assignedIndustryId", required = false) String assignedIndustryIdStr,
            @RequestParam("businessLicense") MultipartFile businessLicense,
            @RequestParam("taxCertificate") MultipartFile taxCertificate,
            @RequestParam("bankStatement") MultipartFile bankStatement,
            @RequestParam("identityProof") MultipartFile identityProof) {
        // Log immediately when method is called
        System.out.println("DEBUG: ========== submitVerification method called ==========");
        System.out.println("DEBUG: Request received at: " + java.time.LocalDateTime.now());
        
        try {
            // Debug logging
            System.out.println("DEBUG: Received verification submission:");
            System.out.println("  Company Name: " + companyName);
            System.out.println("  Assigned Industry ID: " + assignedIndustryIdStr);
            System.out.println("  Business License: " + (businessLicense != null ? businessLicense.getOriginalFilename() + " (" + businessLicense.getSize() + " bytes)" : "null"));
            System.out.println("  Tax Certificate: " + (taxCertificate != null ? taxCertificate.getOriginalFilename() + " (" + taxCertificate.getSize() + " bytes)" : "null"));
            System.out.println("  Bank Statement: " + (bankStatement != null ? bankStatement.getOriginalFilename() + " (" + bankStatement.getSize() + " bytes)" : "null"));
            System.out.println("  Identity Proof: " + (identityProof != null ? identityProof.getOriginalFilename() + " (" + identityProof.getSize() + " bytes)" : "null"));
            
            // Validate files are not empty
            if (businessLicense == null || businessLicense.isEmpty()) {
                return ResponseEntity.badRequest().body("Business license is required");
            }
            if (taxCertificate == null || taxCertificate.isEmpty()) {
                return ResponseEntity.badRequest().body("Tax certificate is required");
            }
            if (bankStatement == null || bankStatement.isEmpty()) {
                return ResponseEntity.badRequest().body("Bank statement is required");
            }
            if (identityProof == null || identityProof.isEmpty()) {
                return ResponseEntity.badRequest().body("Identity proof is required");
            }
            
            UUID assignedIndustryId = null;
            if (assignedIndustryIdStr != null && !assignedIndustryIdStr.trim().isEmpty()) {
                try {
                    assignedIndustryId = UUID.fromString(assignedIndustryIdStr);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid assignedIndustryId format: " + assignedIndustryIdStr);
                }
            }
            
            VerificationResponseDto verification = verificationService.submitVerification(
                    companyName,
                    assignedIndustryId,
                    businessLicense,
                    taxCertificate,
                    bankStatement,
                    identityProof
            );
            
            System.out.println("DEBUG: Verification submitted successfully - ID: " + verification.verificationId());
            return ResponseEntity.ok(verification);
        } catch (IOException e) {
            System.err.println("ERROR in submitVerification (IOException): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to store documents");
                        put("message", e.getMessage());
                        put("details", e.getClass().getSimpleName());
                    }});
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR in submitVerification (IllegalArgumentException): " + e.getMessage());
            return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                put("error", "Invalid request");
                put("message", e.getMessage());
            }});
        } catch (IllegalStateException e) {
            System.err.println("ERROR in submitVerification (IllegalStateException): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, String>() {{
                put("error", "Authentication error");
                put("message", e.getMessage());
            }});
        } catch (Exception e) {
            System.err.println("ERROR in submitVerification (Exception): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("error", "Failed to submit verification");
                        put("message", e.getMessage());
                        put("details", e.getClass().getSimpleName());
                    }});
        }
    }

    /**
     * Review verification (approve or reject).
     * SECURITY: Only INDUSTRY_WORKER role can review verifications.
     * 
     * @param verificationId The verification ID to review
     * @param reviewDto Review data containing status (APPROVED/REJECTED) and optional rejectionReason
     * @return Updated VerificationResponseDto
     */
    @PostMapping("/{verificationId}/review")
    public ResponseEntity<VerificationResponseDto> reviewVerification(
            @PathVariable("verificationId") java.util.UUID verificationId,
            @RequestBody VerificationReviewDto reviewDto) {
        try {
            VerificationResponseDto updated = verificationService.reviewVerification(verificationId, reviewDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            // Role mismatch or authorization failure
            String errorMessage = e.getMessage();
            if (errorMessage != null && (
                errorMessage.toLowerCase().contains("required role") ||
                errorMessage.toLowerCase().contains("does not have") && errorMessage.toLowerCase().contains("role") ||
                errorMessage.toLowerCase().contains("role assigned")
            )) {
                // Return 403 FORBIDDEN - authorization failure, not authentication failure
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Other IllegalStateException cases (e.g., verification not found)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            // Invalid status or other validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Authentication failure or other errors
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

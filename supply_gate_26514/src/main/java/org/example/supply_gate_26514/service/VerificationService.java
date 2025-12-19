package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.VerificationResponseDto;
import org.example.supply_gate_26514.dto.VerificationReviewDto;
import org.example.supply_gate_26514.dto.VerificationStatusCountsDto;
import org.example.supply_gate_26514.model.Verification;
import org.example.supply_gate_26514.model.VerificationStatus;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.model.UserEnum;
import org.example.supply_gate_26514.repository.VerificationRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.supply_gate_26514.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtils securityUtils;
    
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Gets all verifications (paginated) with optional search.
     * For industry workers, only shows verifications assigned to their industry.
     * 
     * @param pageable Pagination parameters
     * @param search Optional search term to filter verifications
     * @return Paginated verifications
     */
    public Page<VerificationResponseDto> getAllVerifications(Pageable pageable, String search) {
        Page<Verification> verifications;
        
        // Check if current user is an industry worker
        try {
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser != null && currentUser.getUserType() == UserEnum.INDUSTRY_WORKER) {
                // Industry workers only see verifications assigned to their industry
                UUID industryId = currentUser.getUserId();
                
                // Debug logging (can be removed in production)
                if (System.getProperty("java.util.logging.config.file") == null) {
                    System.out.println("DEBUG: Industry worker filtering - User ID: " + industryId + ", Email: " + currentUser.getEmail());
                }
                
                if (search != null && !search.trim().isEmpty()) {
                    verifications = verificationRepository.findByAssignedIndustryAndSearch(industryId, search.trim(), pageable);
                } else {
                    verifications = verificationRepository.findByAssignedIndustry(industryId, pageable);
                }
                
                // Debug logging
                if (System.getProperty("java.util.logging.config.file") == null) {
                    System.out.println("DEBUG: Found " + verifications.getTotalElements() + " verifications for industry worker " + industryId);
                }
            } else {
                // Admin or other roles see all verifications
                if (search != null && !search.trim().isEmpty()) {
                    verifications = verificationRepository.findBySearch(search.trim(), pageable);
                } else {
                    verifications = verificationRepository.findAll(pageable);
                }
            }
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("ERROR in getAllVerifications: " + e.getMessage());
            e.printStackTrace();
            
            // If security context fails, return all verifications (for backward compatibility)
            if (search != null && !search.trim().isEmpty()) {
                verifications = verificationRepository.findBySearch(search.trim(), pageable);
            } else {
                verifications = verificationRepository.findAll(pageable);
            }
        }
        
        return verifications.map(this::mapToResponseDto);
    }

    /**
     * Gets verification status counts (pending, approved, rejected, total).
     * For industry workers, only counts verifications assigned to their industry.
     * For other roles, counts all verifications.
     * 
     * @return VerificationStatusCountsDto with counts for each status
     */
    public VerificationStatusCountsDto getStatusCounts() {
        try {
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser != null && currentUser.getUserType() == UserEnum.INDUSTRY_WORKER) {
                // Industry workers only see counts for their assigned industry
                UUID industryId = currentUser.getUserId();
                long pendingCount = verificationRepository.countByAssignedIndustryAndStatus(industryId, VerificationStatus.PENDING);
                long approvedCount = verificationRepository.countByAssignedIndustryAndStatus(industryId, VerificationStatus.APPROVED);
                long rejectedCount = verificationRepository.countByAssignedIndustryAndStatus(industryId, VerificationStatus.REJECTED);
                long totalCount = verificationRepository.countSubmittedByAssignedIndustry(industryId);
                
                return new VerificationStatusCountsDto(pendingCount, approvedCount, rejectedCount, totalCount);
            } else {
                // Admin or other roles see counts for all verifications
                long pendingCount = verificationRepository.countByStatus(VerificationStatus.PENDING);
                long approvedCount = verificationRepository.countByStatus(VerificationStatus.APPROVED);
                long rejectedCount = verificationRepository.countByStatus(VerificationStatus.REJECTED);
                long totalCount = verificationRepository.countAllSubmitted();
                
                return new VerificationStatusCountsDto(pendingCount, approvedCount, rejectedCount, totalCount);
            }
        } catch (Exception e) {
            System.err.println("ERROR in getStatusCounts: " + e.getMessage());
            e.printStackTrace();
            
            // Return zero counts on error
            return new VerificationStatusCountsDto(0, 0, 0, 0);
        }
    }

    /**
     * Gets verification for the current authenticated user.
     * 
     * @return VerificationResponseDto or null if not found
     */
    public Optional<VerificationResponseDto> getMyVerification() {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            System.out.println("DEBUG: getMyVerification called for user ID: " + userId);
            
            // Try to find verification
            Optional<Verification> verification = verificationRepository.findByUser_UserId(userId);
            
            if (verification.isPresent()) {
                Verification v = verification.get();
                System.out.println("DEBUG: Verification found - ID: " + v.getVerificationId() + ", Status: " + v.getStatus());
                System.out.println("DEBUG: Verification user_id: " + (v.getUser() != null ? v.getUser().getUserId() : "null"));
                System.out.println("DEBUG: Verification has documents - BL: " + (v.getBusinessLicenseUrl() != null) + ", TC: " + (v.getTaxCertificateUrl() != null));
                return verification.map(this::mapToResponseDto);
            } else {
                System.out.println("DEBUG: No verification found for user ID: " + userId);
                
                // Diagnostic: Check if there are ANY verifications in the database
                long totalVerifications = verificationRepository.count();
                System.out.println("DEBUG: Total verifications in database: " + totalVerifications);
                
                // Diagnostic: Try to find by user entity directly
                try {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        System.out.println("DEBUG: User exists - Username: " + user.getUsername() + ", Email: " + user.getEmail());
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG: Error checking user: " + e.getMessage());
                }
                
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("ERROR in getMyVerification: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Submits verification documents for the current authenticated supplier.
     * 
     * @param companyName Company name (for backward compatibility)
     * @param assignedIndustryId Industry ID to assign this verification to
     * @param businessLicense Business license file
     * @param taxCertificate Tax certificate file
     * @param bankStatement Bank statement file
     * @param identityProof Identity proof file
     * @return VerificationResponseDto
     * @throws IOException if file storage fails
     */
    @Transactional
    public VerificationResponseDto submitVerification(
            String companyName,
            UUID assignedIndustryId,
            MultipartFile businessLicense,
            MultipartFile taxCertificate,
            MultipartFile bankStatement,
            MultipartFile identityProof) throws IOException {
        
        // Get current user (supplier)
        UUID userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Find or create verification
        Verification verification = verificationRepository.findByUser_UserId(userId)
                .orElse(new Verification());
        
        // If verification already exists, log it
        if (verification.getVerificationId() != null) {
            System.out.println("DEBUG: Updating existing verification - ID: " + verification.getVerificationId());
        } else {
            System.out.println("DEBUG: Creating new verification for user ID: " + userId);
        }
        
        verification.setUser(user);
        verification.setCompanyName(companyName);
        verification.setStatus(VerificationStatus.PENDING);
        
        // Ensure submittedDate is set (important for queries)
        if (verification.getSubmittedDate() == null) {
            verification.setSubmittedDate(LocalDateTime.now());
        }
        
        // Assign to industry if provided
        if (assignedIndustryId != null) {
            User assignedIndustry = userRepository.findById(assignedIndustryId)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned industry not found"));
            if (assignedIndustry.getUserType() != UserEnum.INDUSTRY_WORKER) {
                throw new IllegalArgumentException("Assigned user must be an industry worker");
            }
            verification.setAssignedIndustry(assignedIndustry);
        }
        
        // Store documents with error handling
        try {
            if (businessLicense != null && !businessLicense.isEmpty()) {
                System.out.println("DEBUG: Storing business license - Name: " + businessLicense.getOriginalFilename() + ", Size: " + businessLicense.getSize() + " bytes");
                String businessLicenseUrl = fileStorageService.storeDocument(businessLicense, "verifications");
                verification.setBusinessLicenseUrl(businessLicenseUrl);
                System.out.println("DEBUG: Business license stored at: " + businessLicenseUrl);
            } else {
                System.out.println("DEBUG: Business license is null or empty");
            }
        } catch (Exception e) {
            System.err.println("ERROR storing business license: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to store business license: " + e.getMessage(), e);
        }
        
        try {
            if (taxCertificate != null && !taxCertificate.isEmpty()) {
                System.out.println("DEBUG: Storing tax certificate - Name: " + taxCertificate.getOriginalFilename() + ", Size: " + taxCertificate.getSize() + " bytes");
                String taxCertificateUrl = fileStorageService.storeDocument(taxCertificate, "verifications");
                verification.setTaxCertificateUrl(taxCertificateUrl);
                System.out.println("DEBUG: Tax certificate stored at: " + taxCertificateUrl);
            } else {
                System.out.println("DEBUG: Tax certificate is null or empty");
            }
        } catch (Exception e) {
            System.err.println("ERROR storing tax certificate: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to store tax certificate: " + e.getMessage(), e);
        }
        
        try {
            if (bankStatement != null && !bankStatement.isEmpty()) {
                System.out.println("DEBUG: Storing bank statement - Name: " + bankStatement.getOriginalFilename() + ", Size: " + bankStatement.getSize() + " bytes");
                String bankStatementUrl = fileStorageService.storeDocument(bankStatement, "verifications");
                verification.setBankStatementUrl(bankStatementUrl);
                System.out.println("DEBUG: Bank statement stored at: " + bankStatementUrl);
            } else {
                System.out.println("DEBUG: Bank statement is null or empty");
            }
        } catch (Exception e) {
            System.err.println("ERROR storing bank statement: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to store bank statement: " + e.getMessage(), e);
        }
        
        try {
            if (identityProof != null && !identityProof.isEmpty()) {
                System.out.println("DEBUG: Storing identity proof - Name: " + identityProof.getOriginalFilename() + ", Size: " + identityProof.getSize() + " bytes");
                String identityProofUrl = fileStorageService.storeDocument(identityProof, "verifications");
                verification.setIdentityProofUrl(identityProofUrl);
                System.out.println("DEBUG: Identity proof stored at: " + identityProofUrl);
            } else {
                System.out.println("DEBUG: Identity proof is null or empty");
            }
        } catch (Exception e) {
            System.err.println("ERROR storing identity proof: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to store identity proof: " + e.getMessage(), e);
        }
        
        verification.setSubmittedDate(LocalDateTime.now());
        
        // Save verification with explicit flush to ensure it's persisted
        Verification saved = verificationRepository.save(verification);
        verificationRepository.flush(); // Force immediate database write
        
        System.out.println("DEBUG: Verification saved successfully - ID: " + saved.getVerificationId() + ", User ID: " + userId + ", Status: " + saved.getStatus());
        
        // Verify the save by immediately querying it back
        Optional<Verification> verifySave = verificationRepository.findByUser_UserId(userId);
        if (verifySave.isPresent()) {
            System.out.println("DEBUG: Verification confirmed in database - ID: " + verifySave.get().getVerificationId());
        } else {
            System.err.println("ERROR: Verification was saved but cannot be retrieved immediately!");
        }
        
        return mapToResponseDto(saved);
    }

    /**
     * Reviews a verification (approves or rejects it).
     * SECURITY: Only INDUSTRY_WORKER role can review verifications.
     * 
     * @param verificationId The verification ID to review
     * @param reviewDto Review data containing status and optional rejectionReason
     * @return Updated VerificationResponseDto
     * @throws IllegalStateException if user doesn't have INDUSTRY_WORKER role
     * @throws IllegalArgumentException if verification not found or invalid status
     */
    @Transactional
    public VerificationResponseDto reviewVerification(UUID verificationId, VerificationReviewDto reviewDto) {
        // SECURITY: Require INDUSTRY_WORKER role
        securityUtils.requireRole(UserEnum.INDUSTRY_WORKER);
        
        // Get the current reviewer (industry worker)
        User reviewer = securityUtils.getCurrentUser();
        
        // Find the verification
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found with ID: " + verificationId));
        
        // Validate status
        VerificationStatus newStatus = reviewDto.status();
        if (newStatus != VerificationStatus.APPROVED && newStatus != VerificationStatus.REJECTED) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED, got: " + newStatus);
        }
        
        // Validate that verification is in a reviewable state
        if (verification.getStatus() == VerificationStatus.NOT_SUBMITTED) {
            throw new IllegalArgumentException("Cannot review verification that has not been submitted");
        }
        
        // Update verification status atomically
        verification.setStatus(newStatus);
        verification.setReviewedBy(reviewer);
        verification.setLastUpdatedDate(LocalDateTime.now());
        
        // Set rejection reason if rejected
        if (newStatus == VerificationStatus.REJECTED) {
            String rejectionReason = reviewDto.rejectionReason();
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting a verification");
            }
            verification.setRejectionReason(rejectionReason.trim());
        } else {
            // Clear rejection reason if approved
            verification.setRejectionReason(null);
        }
        
        // Save the updated verification
        Verification saved = verificationRepository.save(verification);
        
        return mapToResponseDto(saved);
    }

    /**
     * Maps Verification entity to VerificationResponseDto.
     */
    private VerificationResponseDto mapToResponseDto(Verification verification) {
        User user = verification.getUser();
        String supplierName = "";
        String email = "";
        String businessType = null;
        
        if (user != null) {
            supplierName = ((user.getFirstName() != null ? user.getFirstName() : "") + 
                           " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
            if (supplierName.isEmpty()) {
                supplierName = user.getUsername();
            }
            email = user.getEmail();
            // businessType could be derived from user's companyName or other fields
            businessType = user.getCompanyName();
        }
        
        UUID reviewedById = null;
        if (verification.getReviewedBy() != null) {
            reviewedById = verification.getReviewedBy().getUserId();
        }
        
        UUID assignedIndustryId = null;
        String assignedIndustryName = null;
        if (verification.getAssignedIndustry() != null) {
            assignedIndustryId = verification.getAssignedIndustry().getUserId();
            assignedIndustryName = verification.getAssignedIndustry().getCompanyName();
        }
        
        return new VerificationResponseDto(
                verification.getVerificationId(),
                user != null ? user.getUserId() : null,
                supplierName,
                email,
                businessType,
                verification.getStatus(),
                verification.getBusinessLicenseUrl(),
                verification.getTaxCertificateUrl(),
                verification.getBankStatementUrl(),
                verification.getIdentityProofUrl(),
                verification.getSubmittedDate(),
                verification.getLastUpdatedDate(),
                verification.getRejectionReason(),
                reviewedById,
                verification.getCompanyName(),
                assignedIndustryId,
                assignedIndustryName
        );
    }
}

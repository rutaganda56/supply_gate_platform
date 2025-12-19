package org.example.supply_gate_26514.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Verification entity for storing supplier verification documents and status.
 */
@Entity
@Table(name = "verifications")
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID verificationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference("user-verification")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.NOT_SUBMITTED;

    private String businessLicenseUrl;
    private String taxCertificateUrl;
    private String bankStatementUrl;
    private String identityProofUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonBackReference("reviewer-verification")
    private User reviewedBy;

    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @UpdateTimestamp
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    // Company name for matching (stored as string, not foreign key)
    @Column(name = "company_name")
    private String companyName;

    // Industry assigned to review this verification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_industry_id")
    @JsonBackReference("industry-verification")
    private User assignedIndustry;

    public Verification() {
    }

    public UUID getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(UUID verificationId) {
        this.verificationId = verificationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }

    public void setBusinessLicenseUrl(String businessLicenseUrl) {
        this.businessLicenseUrl = businessLicenseUrl;
    }

    public String getTaxCertificateUrl() {
        return taxCertificateUrl;
    }

    public void setTaxCertificateUrl(String taxCertificateUrl) {
        this.taxCertificateUrl = taxCertificateUrl;
    }

    public String getBankStatementUrl() {
        return bankStatementUrl;
    }

    public void setBankStatementUrl(String bankStatementUrl) {
        this.bankStatementUrl = bankStatementUrl;
    }

    public String getIdentityProofUrl() {
        return identityProofUrl;
    }

    public void setIdentityProofUrl(String identityProofUrl) {
        this.identityProofUrl = identityProofUrl;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public User getAssignedIndustry() {
        return assignedIndustry;
    }

    public void setAssignedIndustry(User assignedIndustry) {
        this.assignedIndustry = assignedIndustry;
    }
}

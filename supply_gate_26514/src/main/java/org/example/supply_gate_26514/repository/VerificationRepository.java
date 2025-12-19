package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Verification;
import org.example.supply_gate_26514.model.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    /**
     * Finds verification by user ID.
     * Used to check if a supplier is verified.
     * Uses explicit JOIN to ensure proper query execution.
     */
    @Query("SELECT v FROM Verification v JOIN v.user u WHERE u.userId = :userId")
    Optional<Verification> findByUser_UserId(@Param("userId") UUID userId);
    
    /**
     * Finds verifications with search across multiple fields.
     * Searches in: supplier name, email, company name, status
     */
    @Query("SELECT v FROM Verification v " +
           "LEFT JOIN v.user u " +
           "WHERE (LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(v.companyName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(v.status AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Verification> findBySearch(@Param("search") String search, Pageable pageable);
    
    /**
     * Finds verifications assigned to a specific industry.
     * Used by industry workers to see only their assigned verifications.
     * Uses INNER JOIN to ensure only verifications with assignedIndustry are returned.
     */
    @Query("SELECT v FROM Verification v " +
           "LEFT JOIN v.user u " +
           "INNER JOIN v.assignedIndustry ai " +
           "WHERE ai.userId = :industryId " +
           "AND (LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(v.companyName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(v.status AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Verification> findByAssignedIndustryAndSearch(@Param("industryId") UUID industryId, @Param("search") String search, Pageable pageable);
    
    /**
     * Finds verifications assigned to a specific industry (without search).
     * Uses INNER JOIN to ensure only verifications with assignedIndustry are returned.
     */
    @Query("SELECT v FROM Verification v INNER JOIN v.assignedIndustry ai WHERE ai.userId = :industryId")
    Page<Verification> findByAssignedIndustry(@Param("industryId") UUID industryId, Pageable pageable);
    
    /**
     * Counts verifications by status for a specific industry.
     * Excludes NOT_SUBMITTED status as those are not yet submitted.
     */
    @Query("SELECT COUNT(v) FROM Verification v INNER JOIN v.assignedIndustry ai WHERE ai.userId = :industryId AND v.status = :status")
    long countByAssignedIndustryAndStatus(@Param("industryId") UUID industryId, @Param("status") VerificationStatus status);
    
    /**
     * Counts all verifications by status (for admin or non-industry users).
     * Excludes NOT_SUBMITTED status as those are not yet submitted.
     */
    long countByStatus(VerificationStatus status);
    
    /**
     * Counts all submitted verifications (excluding NOT_SUBMITTED) for a specific industry.
     */
    @Query("SELECT COUNT(v) FROM Verification v INNER JOIN v.assignedIndustry ai WHERE ai.userId = :industryId AND v.status != org.example.supply_gate_26514.model.VerificationStatus.NOT_SUBMITTED")
    long countSubmittedByAssignedIndustry(@Param("industryId") UUID industryId);
    
    /**
     * Counts all submitted verifications (excluding NOT_SUBMITTED) for all industries.
     */
    @Query("SELECT COUNT(v) FROM Verification v WHERE v.status != org.example.supply_gate_26514.model.VerificationStatus.NOT_SUBMITTED")
    long countAllSubmitted();
}

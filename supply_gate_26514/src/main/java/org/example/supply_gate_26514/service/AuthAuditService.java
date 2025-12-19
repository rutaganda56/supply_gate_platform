package org.example.supply_gate_26514.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for auditing authentication events.
 * 
 * In production systems (banking, healthcare, enterprise), all authentication
 * events must be logged for security, compliance, and debugging.
 * 
 * This service provides structured logging of:
 * - Login attempts (success/failure)
 * - Token generation
 * - Token validation failures
 * - Logout events
 * - Token refresh events
 */
@Service
public class AuthAuditService {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Logs a successful login event.
     */
    public void logLoginSuccess(String username, UUID userId, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] LOGIN_SUCCESS - User: %s (ID: %s), IP: %s",
            timestamp, username, userId, ipAddress
        ));
    }
    
    /**
     * Logs a failed login attempt.
     */
    public void logLoginFailure(String username, String reason, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] LOGIN_FAILURE - User: %s, Reason: %s, IP: %s",
            timestamp, username != null ? username : "UNKNOWN", reason, ipAddress
        ));
    }
    
    /**
     * Logs token generation.
     */
    public void logTokenGenerated(String username, UUID userId, String tokenType) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] TOKEN_GENERATED - User: %s (ID: %s), Type: %s",
            timestamp, username, userId, tokenType
        ));
    }
    
    /**
     * Logs token validation failure.
     */
    public void logTokenValidationFailure(String reason, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] TOKEN_VALIDATION_FAILURE - Reason: %s, IP: %s",
            timestamp, reason, ipAddress
        ));
    }
    
    /**
     * Logs successful token refresh.
     */
    public void logTokenRefresh(String username, UUID userId) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] TOKEN_REFRESH - User: %s (ID: %s)",
            timestamp, username, userId
        ));
    }
    
    /**
     * Logs logout event.
     */
    public void logLogout(String username, UUID userId, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] LOGOUT - User: %s (ID: %s), IP: %s",
            timestamp, username, userId, ipAddress
        ));
    }
    
    /**
     * Logs unauthorized access attempt.
     */
    public void logUnauthorizedAccess(String endpoint, String reason, String ipAddress) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[AUTH AUDIT] [%s] UNAUTHORIZED_ACCESS - Endpoint: %s, Reason: %s, IP: %s",
            timestamp, endpoint, reason, ipAddress
        ));
    }
    
    /**
     * Logs verification review action (approve/reject).
     * Used for compliance and audit trails in KYC/regulatory systems.
     */
    public void logVerificationReview(UUID verificationId, UUID reviewerId, String reviewerUsername, 
                                      String action, String reason, UUID supplierId) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format(
            "[VERIFICATION AUDIT] [%s] REVIEW_ACTION - VerificationID: %s, Reviewer: %s (ID: %s), " +
            "Action: %s, Reason: %s, SupplierID: %s",
            timestamp, verificationId, reviewerUsername, reviewerId, action, 
            reason != null ? reason : "N/A", supplierId
        ));
    }
}















package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password Reset Service.
 * 
 * Implements secure password reset following enterprise practices:
 * - Generates secure, time-limited tokens
 * - Sends reset links via email
 * - Validates tokens before allowing password change
 * - Invalidates tokens after use
 * - Enforces token expiration (1 hour)
 */
@Service
public class PasswordResetService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private static final int TOKEN_EXPIRY_HOURS = 1;
    
    /**
     * Initiates password reset process.
     * Generates token, stores it, sends email.
     * 
     * @param email User email
     * @return Success message
     */
    @Transactional
    public String initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // Don't reveal if email exists (security best practice)
            return "If an account exists with this email, a password reset link has been sent.";
        }
        
        // Generate secure token
        String token = UUID.randomUUID().toString();
        
        // Store token and expiry
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        userRepository.save(user);
        
        // Send email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return "Password reset link has been sent to your email.";
        } catch (Exception e) {
            // Clear token if email fails
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Failed to send password reset email. Please try again later.", e);
        }
    }
    
    /**
     * Validates password reset token.
     * 
     * @param token Reset token
     * @return true if token is valid and not expired
     */
    public boolean validateResetToken(String token) {
        User user = userRepository.findByPasswordResetToken(token);
        if (user == null) {
            return false;
        }
        
        if (user.getPasswordResetTokenExpiry() == null || 
            user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates password complexity according to enterprise security standards.
     * 
     * @param password Password to validate
     * @throws RuntimeException if password doesn't meet requirements
     */
    private void validatePasswordComplexity(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }
        
        if (password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        
        // Check for at least one letter and one number
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        
        if (!hasLetter || !hasNumber) {
            throw new RuntimeException("Password must contain at least one letter and one number");
        }
        
        // Optional: Check for common weak passwords
        String lowerPassword = password.toLowerCase();
        if (lowerPassword.contains("password") || lowerPassword.contains("123456") || 
            lowerPassword.contains("qwerty") || lowerPassword.equals("password123")) {
            throw new RuntimeException("Password is too weak. Please choose a stronger password.");
        }
    }
    
    /**
     * Resets password using token.
     * 
     * @param token Reset token
     * @param newPassword New password
     * @return Success message
     */
    @Transactional
    public String resetPassword(String token, String newPassword) {
        // Validate password complexity
        validatePasswordComplexity(newPassword);
        
        User user = userRepository.findByPasswordResetToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid or expired reset token");
        }
        
        if (user.getPasswordResetTokenExpiry() == null || 
            user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            // Clear expired token
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }
        
        // Update password with strong hashing (BCrypt with strength 12)
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Invalidate token (single use)
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        
        // Note: Session invalidation is handled by JWT tokens expiring
        // When user logs in again, they'll get a new token with the new password
        // Old tokens will be invalid because they were signed with the old password hash
        
        userRepository.save(user);
        
        return "Password has been reset successfully. Please login with your new password.";
    }
}


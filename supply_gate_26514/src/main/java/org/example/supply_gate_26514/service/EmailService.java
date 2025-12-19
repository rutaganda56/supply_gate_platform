package org.example.supply_gate_26514.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email Service for sending emails.
 * 
 * Handles:
 * - Password reset emails
 * - Two-factor authentication codes
 * 
 * SECURITY: Uses secure email templates and time-limited tokens.
 */
@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:valentinrutaganda04@gmail.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    /**
     * Sends a password reset email with reset link.
     * 
     * @param toEmail Recipient email
     * @param resetToken Password reset token
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (mailSender == null) {
            System.err.println("Email service not configured. Cannot send password reset email.");
            System.err.println("SOLUTION: Configure spring.mail.* properties in application.yml");
            return;
        }
        
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Supply Gate Platform");
            message.setText(
                "You requested a password reset for your Supply Gate Platform account.\n\n" +
                "Click the link below to reset your password:\n" +
                resetLink + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request this reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Supply Gate Platform Team"
            );
            
            mailSender.send(message);
            System.out.println("Password reset email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email. Please check email configuration.", e);
        }
    }
    
    /**
     * Sends a 2FA verification code via email.
     * 
     * Note: This method does not throw exceptions to avoid blocking the login response.
     * If email fails, it's logged but the 2FA code is still valid and user can request resend.
     * 
     * @param toEmail Recipient email
     * @param code 6-digit verification code
     */
    @Async
    public void send2FACode(String toEmail, String code) {
        if (mailSender == null) {
            System.err.println("Email service not configured. Cannot send 2FA code.");
            System.err.println("SOLUTION: Configure spring.mail.* properties in application.yml");
            System.err.println("NOTE: 2FA code is still valid. User can request resend.");
            // Don't throw exception - allow login to proceed, user can resend code
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Verification Code - Supply Gate Platform");
            message.setText(
                "Your verification code is: " + code + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not request this code, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Supply Gate Platform Team"
            );
            
            mailSender.send(message);
            System.out.println("2FA code sent to: " + toEmail);
        } catch (Exception e) {
            // Log error but don't throw - allow login to proceed
            // User can still use the code or request resend
            System.err.println("WARNING: Failed to send 2FA code to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            System.err.println("NOTE: 2FA code is still valid. User can request resend if email doesn't arrive.");
            System.err.println("SOLUTION: Set EMAIL_PASSWORD environment variable with a Gmail App Password");
            System.err.println("Generate app password at: https://myaccount.google.com/apppasswords");
            // Don't throw exception - code is still valid, user can request resend
        }
    }
}


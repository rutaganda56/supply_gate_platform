package org.example.supply_gate_26514.util;

import org.example.supply_gate_26514.TokenBasedAuthentication;
import org.example.supply_gate_26514.UserRelatedInfo;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for extracting authenticated user information from Spring Security context.
 * 
 * This follows enterprise best practices where user identity comes from authentication context,
 * not from client input, ensuring security and preventing unauthorized access.
 * 
 * Used by controllers to get the current authenticated user for resource ownership.
 */
@Component
public class SecurityUtils {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Gets the current authenticated user from Spring Security context.
     * 
     * SECURITY: User identity is extracted from JWT token, not from client input.
     * This method prioritizes token-based authentication to avoid database calls.
     * 
     * NOTE: This method performs a database lookup to get the full User entity.
     * If you only need userId or username, use getCurrentUserId() or getCurrentUsername() instead.
     * 
     * @return The authenticated User entity
     * @throws IllegalStateException if no user is authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        
        // Priority 1: TokenBasedAuthentication (lightweight, userId from token)
        if (authentication instanceof TokenBasedAuthentication) {
            TokenBasedAuthentication tokenAuth = (TokenBasedAuthentication) authentication;
            UUID userId = tokenAuth.getUserId();
            
            if (userId != null) {
                // User ID is in token - fetch user from database
                // This is necessary when full User entity is needed (e.g., for relationships)
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    return user;
                }
                // If user not found by ID, user may have been deleted - throw exception
                throw new IllegalStateException("User not found in database for userId from token: " + userId);
            }
            
            // Fallback to username lookup if userId not in token (old token format)
            String username = tokenAuth.getUsername();
            User user = userRepository.findByUsernameOrEmail(username);
            if (user == null) {
                throw new IllegalStateException("User not found in database: " + username);
            }
            return user;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserRelatedInfo) {
            // Extract User entity directly from UserRelatedInfo
            UserRelatedInfo userRelatedInfo = (UserRelatedInfo) principal;
            User user = userRelatedInfo.getUser();
            
            if (user == null) {
                throw new IllegalStateException("User entity is null in UserRelatedInfo for: " + userRelatedInfo.getUsername());
            }
            
            if (user.getUserId() == null) {
                throw new IllegalStateException("User ID is null for authenticated user: " + user.getUsername());
            }
            
            return user;
        } else if (principal instanceof UserDetails) {
            // Fallback for other UserDetails implementations (requires DB call)
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            
            User user = userRepository.findByUsernameOrEmail(username);
            if (user == null) {
                throw new IllegalStateException("User not found in database: " + username);
            }
            return user;
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }
    }
    
    /**
     * Gets the current authenticated user's ID.
     * 
     * SECURITY: User ID is extracted from JWT token, not from client input.
     * This method is optimized to avoid database calls when using TokenBasedAuthentication.
     * 
     * @return The authenticated user's UUID
     * @throws IllegalStateException if no user is authenticated
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        
        // Optimized path: Extract userId directly from token (no DB call)
        if (authentication instanceof TokenBasedAuthentication) {
            TokenBasedAuthentication tokenAuth = (TokenBasedAuthentication) authentication;
            UUID userId = tokenAuth.getUserId();
            if (userId != null) {
                return userId; // Fast path - no database call
            }
            // Fall through if userId not in token (old token format)
        }
        
        // Fallback: Get from User entity (requires DB call for old tokens or UserRelatedInfo)
        return getCurrentUser().getUserId();
    }
    
    /**
     * Gets the current authenticated user's username.
     * 
     * @return The authenticated user's username
     * @throws IllegalStateException if no user is authenticated
     */
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
    
    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Requires that the current user has a specific role.
     * 
     * SECURITY: User role is extracted from database after authentication.
     * This ensures role changes are reflected immediately.
     * 
     * @param requiredRole The required user type/role
     * @throws IllegalStateException if user is not authenticated, not found, or doesn't have the required role
     */
    public void requireRole(org.example.supply_gate_26514.model.UserEnum requiredRole) {
        try {
            User user = getCurrentUser();
            
            // Check if user has a userType set
            if (user.getUserType() == null) {
                throw new IllegalStateException("User does not have a role assigned. Please contact administrator.");
            }
            
            // Check if user has the required role
            if (user.getUserType() != requiredRole) {
                throw new IllegalStateException(
                    String.format("User does not have required role: %s. Current role: %s", 
                        requiredRole, user.getUserType())
                );
            }
        } catch (IllegalStateException e) {
            // Re-throw IllegalStateException as-is (it already has a descriptive message)
            throw e;
        } catch (Exception e) {
            // Wrap unexpected exceptions
            throw new IllegalStateException("Failed to verify user role: " + e.getMessage(), e);
        }
    }
}

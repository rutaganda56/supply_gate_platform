package org.example.supply_gate_26514;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Lightweight authentication token that stores user identity from JWT token.
 * 
 * This avoids database calls on every request by storing username and userId
 * directly from the token claims. This follows enterprise best practices where
 * user identity is extracted from the token, not from client input or database.
 * 
 * SECURITY: User identity comes from validated JWT token, ensuring authenticity.
 */
public class TokenBasedAuthentication extends AbstractAuthenticationToken {
    private final String username;
    private final UUID userId;
    private final String token;
    
    /**
     * Creates a token-based authentication object.
     * 
     * @param username Username extracted from token
     * @param userId User ID extracted from token (may be null for old tokens)
     * @param token The JWT token
     */
    public TokenBasedAuthentication(String username, UUID userId, String token) {
        super(Collections.emptyList()); // No authorities for now
        this.username = username;
        this.userId = userId;
        this.token = token;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
    
    @Override
    public Object getPrincipal() {
        return username;
    }
    
    /**
     * Gets the username from the token.
     * 
     * @return Username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the user ID from the token.
     * 
     * @return User ID, or null if not present in token (old token format)
     */
    public UUID getUserId() {
        return userId;
    }
    
    /**
     * Gets the JWT token.
     * 
     * @return The JWT token
     */
    public String getToken() {
        return token;
    }
}












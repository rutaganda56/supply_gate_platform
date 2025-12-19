package org.example.supply_gate_26514.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JWTService {
    // Use a fixed secret key from configuration, or generate one if not set
    // This ensures tokens remain valid across server restarts
    @Value("${jwt.secret:}")
    private String configuredSecretKey;
    
    private String secretKey;
    
    public JWTService() {
        // Initialize will be done in @PostConstruct to ensure @Value is injected first
    }
    
    @jakarta.annotation.PostConstruct
    public void init() {
        if (configuredSecretKey != null && !configuredSecretKey.isEmpty()) {
            // Use configured secret key from application.yml
            // The key should be at least 256 bits (32 bytes) for HS256
            // If it's a plain string, we'll use it directly (JWT library will handle encoding)
            // For better security, use a Base64-encoded key
            secretKey = configuredSecretKey;
            // If the key is too short, pad it or use it as-is (JWT library will handle it)
            if (secretKey.length() < 32) {
                System.out.println("WARNING: JWT secret key is shorter than recommended (32 characters). Consider using a longer key.");
            }
        } else {
            // Use a default fixed secret key so tokens remain valid across restarts
            // IMPORTANT: In production, set jwt.secret in application.yml with a strong random key
            secretKey = Base64.getEncoder().encodeToString(
                "DefaultJWTSecretKeyForDevelopmentOnlyChangeInProduction123456789012345678901234567890".getBytes()
            );
            System.out.println("WARNING: Using default JWT secret key. For production, set 'jwt.secret' in application.yml");
            System.out.println("This ensures tokens remain valid across server restarts.");
        }
    }

    /**
     * Generates a short-lived access token (30 minutes).
     * 
     * Access tokens are used for API authentication and expire quickly
     * to minimize risk if compromised. They should be refreshed using
     * refresh tokens before expiration.
     * 
     * @param username The authenticated user's username
     * @param userId The authenticated user's UUID (included in token to avoid DB calls)
     * @return JWT access token
     */
    public String generateAccessToken(String username, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString()); // Include userId in claims to avoid DB lookups
        long expirationTime = System.currentTimeMillis() + (1000L * 60 * 30); // 30 minutes
        
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expirationTime))
                .and()
                .signWith(getKey())
                .compact();
    }
    
    /**
     * Generates a short-lived access token (30 minutes) - backward compatibility.
     * 
     * @deprecated Use generateAccessToken(String username, UUID userId) instead
     * @param username The authenticated user's username
     * @return JWT access token
     */
    @Deprecated
    public String generateAccessToken(String username) {
        // For backward compatibility, generate without userId
        // This should only be used during migration
        Map<String, Object> claims = new HashMap<>();
        long expirationTime = System.currentTimeMillis() + (1000L * 60 * 30); // 30 minutes
        
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expirationTime))
                .and()
                .signWith(getKey())
                .compact();
    }
    
    /**
     * Generates a long-lived refresh token (7 days).
     * 
     * Refresh tokens are used to obtain new access tokens without
     * requiring user re-authentication. They have longer expiration
     * but should be stored securely and revoked on logout.
     * 
     * @param username The authenticated user's username
     * @param userId The authenticated user's UUID (included in token to avoid DB calls)
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh"); // Mark as refresh token
        claims.put("userId", userId.toString()); // Include userId in claims
        long expirationTime = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7); // 7 days
        
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expirationTime))
                .and()
                .signWith(getKey())
                .compact();
    }
    
    /**
     * Generates both access and refresh tokens.
     * 
     * @param username The authenticated user's username
     * @param userId The authenticated user's UUID
     * @return Array with [accessToken, refreshToken]
     */
    public String[] generateTokenPair(String username, UUID userId) {
        return new String[]{
            generateAccessToken(username, userId),
            generateRefreshToken(username, userId)
        };
    }
    
    /**
     * Generates both access and refresh tokens - backward compatibility.
     * 
     * @deprecated Use generateTokenPair(String username, UUID userId) instead
     * @param username The authenticated user's username
     * @return Array with [accessToken, refreshToken]
     */
    @Deprecated
    public String[] generateTokenPair(String username) {
        // For backward compatibility - generates tokens without userId
        // This should only be used during migration
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("type", "refresh");
        long refreshExpirationTime = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7); // 7 days
        
        String refreshToken = Jwts.builder()
                .claims()
                .add(refreshClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(refreshExpirationTime))
                .and()
                .signWith(getKey())
                .compact();
        
        return new String[]{
            generateAccessToken(username),
            refreshToken
        };
    }
    
    /**
     * @deprecated Use generateAccessToken() instead
     * Kept for backward compatibility
     */
    @Deprecated
    public String generateToken(String username) {
        return generateAccessToken(username);
    }
    
    /**
     * Validates if a token is a refresh token.
     * 
     * @param token The token to check
     * @return true if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object tokenType = claims.get("type");
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extracts expiration time from token.
     * 
     * @param token The JWT token
     * @return Expiration time in milliseconds, or null if token is invalid
     */
    public Long getTokenExpirationTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null ? expiration.getTime() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Checks if token is expired.
     * 
     * @param token The JWT token
     * @return true if token is expired or invalid
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true; // If we can't parse, consider it expired
        }
    }

    private SecretKey getKey() {
        try {
            // Try to decode as Base64 first
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // If not Base64, use the string directly as bytes
            // This allows using plain text secrets in application.yml
            byte[] keyBytes = secretKey.getBytes();
            // Ensure minimum key length (256 bits = 32 bytes for HS256)
            if (keyBytes.length < 32) {
                // Pad or repeat to reach minimum length
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
                for (int i = keyBytes.length; i < 32; i++) {
                    paddedKey[i] = keyBytes[i % keyBytes.length];
                }
                keyBytes = paddedKey;
            }
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String extractUserName(String token) {
        return extractUserName(token, Claims::getSubject);
    }
    
    /**
     * Extracts userId from token claims.
     * This avoids database calls by getting userId directly from the token.
     * 
     * @param token The JWT token
     * @return User ID as UUID, or null if not present (for backward compatibility)
     */
    public UUID extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                return UUID.fromString(userIdObj.toString());
            }
            return null; // Token doesn't have userId (old token format)
        } catch (Exception e) {
            return null; // Invalid token or missing claim
        }
    }

    private <T> T extractUserName(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a token, validating the signature in the process.
     * This method will throw an exception if the token signature is invalid.
     * 
     * @param token The JWT token
     * @return Claims from the token
     * @throws io.jsonwebtoken.security.SignatureException if signature is invalid
     * @throws io.jsonwebtoken.ExpiredJwtException if token is expired
     * @throws io.jsonwebtoken.MalformedJwtException if token is malformed
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

    /**
     * Validates a token against user details.
     * 
     * @param token The JWT token to validate
     * @param userDetails The user details to validate against
     * @return true if token is valid and not expired
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUserName(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Token is invalid
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}

package org.example.supply_gate_26514.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.supply_gate_26514.service.JWTService;
import org.example.supply_gate_26514.service.MyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Configuration
public class JWTFilter extends OncePerRequestFilter {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private JWTService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Log incoming requests for debugging (only for verification submit)
        String requestPath = request.getRequestURI();
        if (requestPath != null && requestPath.contains("/api/verification/submit")) {
            System.out.println("DEBUG: JWTFilter - Received request: " + request.getMethod() + " " + requestPath);
            System.out.println("DEBUG: JWTFilter - Content-Type: " + request.getContentType());
            System.out.println("DEBUG: JWTFilter - Content-Length: " + request.getContentLength());
        }
        
        // Skip authentication for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip authentication for public endpoints
        // Remove query parameters for matching
        String pathWithoutQuery = requestPath.contains("?") 
            ? requestPath.substring(0, requestPath.indexOf("?")) 
            : requestPath;
        if (isPublicEndpoint(pathWithoutQuery)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.extractUserName(token);
            } catch (Exception e) {
                // Invalid token - continue to authentication check
            }
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = context.getBean(MyUserService.class).loadUserByUsername(username);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Authentication failed - will be handled by Spring Security
            }
        }
        filterChain.doFilter(request, response);
    }
    
    /**
     * Checks if the endpoint is public (doesn't require authentication).
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/forgot-password") ||
               path.startsWith("/api/auth/reset-password") ||
               path.startsWith("/api/auth/validate-reset-token") ||
               path.startsWith("/api/auth/verify-2fa") ||
               path.startsWith("/api/auth/resend-2fa-code") ||
               path.startsWith("/api/auth/validate-2fa-session") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/api/auth/companies") ||
               path.startsWith("/api/location") ||
               path.startsWith("/api/images") ||
               path.startsWith("/api/products/getProducts") ||  // Public product listings
               path.startsWith("/api/messages/send") ||        // Public: Allow visitors to send messages
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/v2/api-docs");
    }
}


package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.AuthResponseDto;
import org.example.supply_gate_26514.dto.TwoFactorAuthResponseDto;
import org.example.supply_gate_26514.dto.UserDto;
import org.example.supply_gate_26514.dto.UserResponseDto;
import org.example.supply_gate_26514.mapper.UserMapper;
import org.example.supply_gate_26514.model.Location;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.model.UserEnum;
import org.example.supply_gate_26514.repository.LocationRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private AuthAuditService authAuditService;

    private BCryptPasswordEncoder passwordEncoder= new BCryptPasswordEncoder(12);

    /**
     * Authenticates a user and generates access/refresh token pair.
     * Requires 2FA verification before granting access.
     * 
     * @param user User credentials (username, password)
     * @param request HTTP request for IP address extraction
     * @return AuthResponseDto with tokens and user info, or throws TwoFactorAuthRequiredException
     * @throws AuthenticationException if credentials are invalid
     * @throws TwoFactorAuthRequiredException if 2FA is required (normal flow)
     */
    public AuthResponseDto login(User user, HttpServletRequest request) throws AuthenticationException {
        String username = null;
        try {
            username = user.getUsername();
            String password = user.getPassword();
            
            // Remove any quotes or whitespace from password if present
            if (password != null) {
                password = password.trim();
                if (password.startsWith("\"") && password.endsWith("\"")) {
                    password = password.substring(1, password.length() - 1);
                }
            }
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            if (authentication.isAuthenticated()) {
                // Get user entity for additional info
                User authenticatedUser = userRepository.findByUsernameOrEmail(username);
                if (authenticatedUser == null) {
                    // Fallback: try username or email separately
                    authenticatedUser = userRepository.findByUsername(username);
                    if (authenticatedUser == null) {
                        authenticatedUser = userRepository.findByEmail(username);
                    }
                }
                if (authenticatedUser == null) {
                    String ipAddress = getClientIpAddress(request);
                    authAuditService.logLoginFailure(username, "User not found in database", ipAddress);
                    throw new BadCredentialsException("User not found");
                }
                
                // SECURITY: Require 2FA before granting access
                String sessionId = twoFactorAuthService.initiate2FA(authenticatedUser.getUserId());
                
                // Audit 2FA initiation
                String ipAddress = getClientIpAddress(request);
                authAuditService.logLoginSuccess(username, authenticatedUser.getUserId(), ipAddress);
                
                // Throw special exception to indicate 2FA is required
                throw new TwoFactorAuthRequiredException(
                    new TwoFactorAuthResponseDto(
                        sessionId,
                        authenticatedUser.getUserId(),
                        authenticatedUser.getUsername(),
                        "Please check your email for the verification code to complete login."
                    )
                );
            }
            
            String ipAddress = getClientIpAddress(request);
            authAuditService.logLoginFailure(username, "Authentication failed", ipAddress);
            throw new BadCredentialsException("Authentication failed");
            
        } catch (TwoFactorAuthRequiredException e) {
            // 2FA is required - re-throw to be handled by controller
            throw e;
        } catch (AuthenticationException e) {
            String ipAddress = getClientIpAddress(request);
            authAuditService.logLoginFailure(username, e.getMessage(), ipAddress);
            throw e;
        }
    }
    
    /**
     * Backward compatibility method - returns only access token.
     * @deprecated Use login(User, HttpServletRequest) instead
     */
    @Deprecated
    public String login(User user) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            if (authentication.isAuthenticated()) {
                return jwtService.generateAccessToken(user.getUsername());
            }
            throw new BadCredentialsException("Authentication failed");
        } catch (AuthenticationException e) {
            throw e;
        }
    }
    
    /**
     * Completes 2FA verification and returns authentication tokens.
     * 
     * @param sessionId 2FA session ID
     * @param code 6-digit verification code
     * @param request HTTP request for IP address extraction
     * @return AuthResponseDto with tokens and user info
     */
    public AuthResponseDto complete2FALogin(String sessionId, String code, HttpServletRequest request) {
        UUID userId = twoFactorAuthService.verify2FACode(sessionId, code);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate token pair (access + refresh) with userId in claims
        String[] tokens = jwtService.generateTokenPair(user.getUsername(), user.getUserId());
        String accessToken = tokens[0];
        String refreshToken = tokens[1];
        
        // Calculate expiration time (30 minutes = 1800 seconds)
        long expiresIn = 1800L;
        
        // Audit successful 2FA completion and token generation
        String ipAddress = getClientIpAddress(request);
        authAuditService.logTokenGenerated(user.getUsername(), user.getUserId(), "access");
        authAuditService.logTokenGenerated(user.getUsername(), user.getUserId(), "refresh");
        
        return new AuthResponseDto(
            accessToken,
            refreshToken,
            "Bearer",
            expiresIn,
            user.getUserId(),
            user.getUsername(),
            user.getUserType() != null ? user.getUserType().name() : "UNKNOWN"
        );
    }
    
    /**
     * Refreshes an access token using a refresh token.
     * 
     * @param refreshToken The refresh token
     * @param request HTTP request for IP address extraction
     * @return New AuthResponseDto with fresh tokens
     * @throws AuthenticationException if refresh token is invalid
     */
    public AuthResponseDto refreshToken(String refreshToken, HttpServletRequest request) throws AuthenticationException {
        try {
            String ipAddress = getClientIpAddress(request);
            
            // Validate refresh token signature and structure
            try {
                jwtService.extractAllClaims(refreshToken);
            } catch (Exception e) {
                authAuditService.logTokenValidationFailure("Invalid refresh token signature", ipAddress);
                throw new BadCredentialsException("Invalid refresh token");
            }
            
            // Check if refresh token is expired
            if (jwtService.isTokenExpired(refreshToken)) {
                authAuditService.logTokenValidationFailure("Refresh token expired", ipAddress);
                throw new BadCredentialsException("Refresh token has expired");
            }
            
            // Verify it's actually a refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                authAuditService.logTokenValidationFailure("Token is not a refresh token", ipAddress);
                throw new BadCredentialsException("Invalid refresh token");
            }
            
            // Extract username and userId from token
            String username = jwtService.extractUserName(refreshToken);
            UUID userId = jwtService.extractUserId(refreshToken);
            
            // Validate user still exists
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
            if (user == null) {
                user = userRepository.findByUsernameOrEmail(username);
            }
            
            if (user == null) {
                authAuditService.logTokenValidationFailure("User not found for refresh token", ipAddress);
                throw new BadCredentialsException("User not found");
            }
            
            // Generate new token pair
            String[] tokens = jwtService.generateTokenPair(username, user.getUserId());
            String newAccessToken = tokens[0];
            String newRefreshToken = tokens[1];
            
            // Audit token refresh
            authAuditService.logTokenRefresh(username, user.getUserId());
            
            return new AuthResponseDto(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                1800L,
                user.getUserId(),
                user.getUsername(),
                user.getUserType() != null ? user.getUserType().name() : "UNKNOWN"
            );
            
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            String ipAddress = getClientIpAddress(request);
            authAuditService.logTokenValidationFailure("Refresh failed: " + e.getMessage(), ipAddress);
            throw new BadCredentialsException("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Extracts client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "UNKNOWN";
    }

    public UserResponseDto registerUser(UserDto userDto) {
      UserEnum userType = UserEnum.valueOf(userDto.userType());
      
      // Validate company name for industry workers
      if ((userType == UserEnum.INDUSTRY_WORKER || userType == UserEnum.CLIENT) && 
          (userDto.companyName() == null || userDto.companyName().trim().isEmpty())) {
        throw new IllegalArgumentException("Company/organization name is required for industry workers.");
      }
      
      // Validate firstName and lastName for suppliers (required)
      if (userType == UserEnum.SUPPLIER) {
        if (userDto.firstname() == null || userDto.firstname().trim().isEmpty()) {
          throw new IllegalArgumentException("First name is required for suppliers.");
        }
        if (userDto.lastname() == null || userDto.lastname().trim().isEmpty()) {
          throw new IllegalArgumentException("Last name is required for suppliers.");
        }
      }
      
      // For industry workers, firstName and lastName are optional
      // If not provided, use empty strings (companyName is the primary identifier)
      String firstname = userDto.firstname();
      String lastname = userDto.lastname();
      if ((userType == UserEnum.INDUSTRY_WORKER || userType == UserEnum.CLIENT)) {
        if (firstname == null || firstname.trim().isEmpty()) {
          firstname = "";
        }
        if (lastname == null || lastname.trim().isEmpty()) {
          lastname = "";
        }
      }
      
      // Create a new UserDto with potentially modified firstname/lastname
      UserDto modifiedDto = new UserDto(
          userDto.username(),
          userDto.password(),
          userDto.userType(),
          firstname != null ? firstname : "",
          lastname != null ? lastname : "",
          userDto.email(),
          userDto.phoneNumber(),
          userDto.locationId(),
          userDto.companyName()
      );
      
      var user= userMapper.transformUserToUserDto(modifiedDto);
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      var savedUser = userRepository.save(user);
      return userMapper.transformUserDtoToUserResponseDto(savedUser);
    }
    public List<UserResponseDto> getAllUsers(){
        return userRepository.findAll().stream().map(userMapper::transformUserDtoToUserResponseDto).collect(Collectors.toList());
    }
    public UserResponseDto updateUser(UUID userId, UserDto userDto) {
        var existingUser= userRepository.findById(userId).orElse(new User());
        var newLocation= locationRepository.findById(userDto.locationId()).orElse(new Location());
        existingUser.setLocation(newLocation);
        existingUser.setUserType(UserEnum.valueOf(userDto.userType()));
        existingUser.setFirstName(userDto.firstname());
        existingUser.setLastName(userDto.lastname());
        existingUser.setEmail(userDto.email());
        existingUser.setPhoneNumber(userDto.phoneNumber());
        existingUser.setPassword(userDto.password());
        var updatedUser = userRepository.save(existingUser);
        return userMapper.transformUserDtoToUserResponseDto(updatedUser);
    }
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
        ResponseEntity.ok().body("User deleted successfully");
    }
    
    /**
     * Gets all distinct company names from registered industry workers.
     * Used to populate the company selection dropdown for suppliers during verification.
     * 
     * @return List of distinct company names (sorted alphabetically)
     */
    public List<String> getCompanyNames() {
        return userRepository.findDistinctCompanyNamesByUserTypeIn(
            List.of(UserEnum.INDUSTRY_WORKER, UserEnum.CLIENT)
        );
    }
    
    /**
     * Gets all industry workers (users with INDUSTRY_WORKER role).
     * Used to populate industry selection dropdown for verification routing.
     * 
     * @return List of industry worker users
     */
    public List<User> getIndustryWorkers() {
        return userRepository.findByUserTypeIn(List.of(UserEnum.INDUSTRY_WORKER))
            .stream()
            .filter(user -> user.getCompanyName() != null && !user.getCompanyName().trim().isEmpty())
            .sorted((u1, u2) -> {
                String name1 = u1.getCompanyName() != null ? u1.getCompanyName() : "";
                String name2 = u2.getCompanyName() != null ? u2.getCompanyName() : "";
                return name1.compareToIgnoreCase(name2);
            })
            .collect(Collectors.toList());
    }
}

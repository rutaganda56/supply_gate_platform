package org.example.supply_gate_26514.contoller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.supply_gate_26514.dto.AuthResponseDto;
import org.example.supply_gate_26514.dto.PasswordResetDto;
import org.example.supply_gate_26514.dto.PasswordResetRequestDto;
import org.example.supply_gate_26514.dto.TwoFactorAuthResponseDto;
import org.example.supply_gate_26514.dto.UserDto;
import org.example.supply_gate_26514.dto.UserResponseDto;
import org.example.supply_gate_26514.dto.Verify2FADto;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.service.AuthAuditService;
import org.example.supply_gate_26514.service.PasswordResetService;
import org.example.supply_gate_26514.service.TwoFactorAuthRequiredException;
import org.example.supply_gate_26514.service.TwoFactorAuthService;
import org.example.supply_gate_26514.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/auth")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthAuditService authAuditService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    /**
     * Authenticates a user and returns access/refresh token pair.
     * Requires 2FA verification before granting access.
     * 
     * @param user User credentials (username, password)
     * @param request HTTP request for IP extraction
     * @return AuthResponseDto with tokens and user info, or TwoFactorAuthResponseDto if 2FA required
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletRequest request) {
        try {
            AuthResponseDto authResponse = userService.login(user, request);
            return ResponseEntity.ok(authResponse);
        } catch (TwoFactorAuthRequiredException e) {
            // 2FA is required - return 2FA response instead of tokens
            TwoFactorAuthResponseDto twoFactorResponse = e.getTwoFactorResponse();
            
            Map<String, Object> response = new HashMap<>();
            response.put("requires2FA", true);
            response.put("sessionId", twoFactorResponse.sessionId());
            response.put("userId", twoFactorResponse.userId().toString());
            response.put("username", twoFactorResponse.username());
            response.put("message", twoFactorResponse.message());
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password. Please check your credentials.");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during login. Please try again.");
        }
    }
    
    /**
     * Verifies 2FA code and completes login.
     * 
     * @param verifyDto 2FA verification data (sessionId, code)
     * @param request HTTP request for IP extraction
     * @return AuthResponseDto with tokens and user info
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@Valid @RequestBody Verify2FADto verifyDto, HttpServletRequest request) {
        try {
            AuthResponseDto authResponse = userService.complete2FALogin(
                verifyDto.sessionId(), 
                verifyDto.code(),
                request
            );
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to verify code. Please try again.");
        }
    }
    
    /**
     * Resends 2FA verification code.
     * 
     * @param requestDto Request with sessionId
     * @return Success message
     */
    @PostMapping("/resend-2fa-code")
    public ResponseEntity<?> resend2FACode(@RequestBody HashMap<String, String> requestDto) {
        try {
            String sessionId = requestDto.get("sessionId");
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Session ID is required");
            }
            
            String newSessionId = twoFactorAuthService.resend2FACode(sessionId);
            return ResponseEntity.ok(new HashMap<String, String>() {{
                put("message", "Verification code has been resent to your email");
                put("sessionId", newSessionId);
            }});
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to resend code. Please try again.");
        }
    }
    
    /**
     * Validates 2FA session.
     * 
     * @param sessionId Session ID
     * @return Validation result
     */
    @GetMapping("/validate-2fa-session")
    public ResponseEntity<?> validate2FASession(@RequestParam String sessionId) {
        try {
            boolean valid = twoFactorAuthService.isSessionValid(sessionId);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("valid", valid);
                put("status", valid ? "Session is valid" : "Session expired or invalid");
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to validate session");
        }
    }
    
    /**
     * Initiates password reset process.
     * 
     * @param requestDto Password reset request with email
     * @return Success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDto requestDto) {
        try {
            String message = passwordResetService.initiatePasswordReset(requestDto.email());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process password reset request. Please try again later.");
        }
    }
    
    /**
     * Resets password using token.
     * 
     * @param resetDto Password reset data (token, newPassword)
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        try {
            String message = passwordResetService.resetPassword(resetDto.token(), resetDto.newPassword());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to reset password. Please try again.");
        }
    }
    
    /**
     * Validates password reset token.
     * 
     * @param token Reset token
     * @return Validation result
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            boolean valid = passwordResetService.validateResetToken(token);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("valid", valid);
                put("status", valid ? "Token is valid" : "Token is invalid or expired");
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to validate token");
        }
    }
    
    /**
     * Refreshes access token using refresh token.
     * 
     * @param requestDto Request with refreshToken
     * @param request HTTP request for IP extraction
     * @return New AuthResponseDto with fresh tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody HashMap<String, String> requestDto, HttpServletRequest request) {
        try {
            String refreshToken = requestDto.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Refresh token is required");
            }
            
            AuthResponseDto authResponse = userService.refreshToken(refreshToken, request);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to refresh token. Please login again.");
        }
    }

    @PostMapping(value = "/register", consumes = "application/json" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addUser(@RequestBody UserDto userDto) {
        try {
            // Manual validation for required fields (skip @Valid to handle conditional validation)
            if (userDto.username() == null || userDto.username().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("username", "Username is required");
                        }});
            }
            if (userDto.password() == null || userDto.password().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("password", "Password is required");
                        }});
            }
            if (userDto.userType() == null || userDto.userType().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("userType", "User type is required");
                        }});
            }
            if (userDto.email() == null || userDto.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("email", "Email is required");
                        }});
            }
            // Email format validation
            if (!userDto.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("email", "Invalid email format");
                        }});
            }
            if (userDto.phoneNumber() == null || userDto.phoneNumber().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("phoneNumber", "Phone number is required");
                        }});
            }
            // Phone number format validation (exactly 10 digits)
            if (!userDto.phoneNumber().matches("^[0-9]{10}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new HashMap<String, String>() {{
                            put("phoneNumber", "Phone number must be exactly 10 digits");
                        }});
            }
            
            // For industry workers, firstname and lastname are optional
            // Validation will be done in service layer based on userType
            // For suppliers, firstname and lastname are required (validated in service)
            
            UserResponseDto response = userService.registerUser(userDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Return validation errors with 400 status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new HashMap<String, String>() {{
                        put("message", e.getMessage());
                        put("error", e.getMessage());
                    }});
        } catch (Exception e) {
            // Return other errors with 500 status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, String>() {{
                        put("message", "Registration failed: " + e.getMessage());
                        put("error", e.getMessage());
                    }});
        }
    }
    @GetMapping( value = "/getAllUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }
    @PutMapping(value = "/updateUser/{id}", consumes = "application/json" , produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponseDto updateUser(@PathVariable("id")UUID userId, @RequestBody @Valid UserDto userDto) {

      return userService.updateUser(userId, userDto);
    }
    @DeleteMapping(value = "/deleteUser/{id}")
    public void deleteUser(@PathVariable("id")UUID userId) {
        userService.deleteUser(userId);
    }
    
    /**
     * Gets all distinct company names from registered industry workers.
     * This endpoint is public so suppliers can see available companies during verification.
     * 
     * @return List of company names
     */
    @GetMapping(value = "/companies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getCompanyNames() {
        List<String> companyNames = userService.getCompanyNames();
        return ResponseEntity.ok(companyNames);
    }
    
    /**
     * Gets all industry workers with their IDs and company names.
     * Used to populate industry selection dropdown for verification routing.
     * 
     * @return List of industry users (with userId and companyName)
     */
    @GetMapping(value = "/industries", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getIndustries() {
        List<User> industries = userService.getIndustryWorkers();
        List<Map<String, Object>> industryList = industries.stream()
            .map(industry -> {
                Map<String, Object> industryMap = new HashMap<>();
                industryMap.put("userId", industry.getUserId());
                industryMap.put("companyName", industry.getCompanyName());
                industryMap.put("email", industry.getEmail());
                return industryMap;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(industryList);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMsg = error.getDefaultMessage();
            errors.put(fieldName, errorMsg);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid username or password. Please check your credentials.");
    }

}

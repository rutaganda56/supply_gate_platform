package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.TwoFactorAuthResponseDto;

/**
 * Exception thrown when 2FA is required after successful password authentication.
 * 
 * This is not an error - it's a normal part of the 2FA flow.
 * The controller catches this and returns the 2FA response instead of tokens.
 */
public class TwoFactorAuthRequiredException extends RuntimeException {
    private final TwoFactorAuthResponseDto twoFactorResponse;
    
    public TwoFactorAuthRequiredException(TwoFactorAuthResponseDto twoFactorResponse) {
        super("2FA required");
        this.twoFactorResponse = twoFactorResponse;
    }
    
    public TwoFactorAuthResponseDto getTwoFactorResponse() {
        return twoFactorResponse;
    }
}


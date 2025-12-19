package org.example.supply_gate_26514.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record UserDto(
        @NotEmpty
        String username,
        @NotEmpty
        String password,
        @NotEmpty
        String userType,
        String firstname,  // Optional for industry workers (companyName is used instead)
        String lastname,    // Optional for industry workers (companyName is used instead)
        @Email
        String email,
        @Pattern(regexp = "^[0-9]{10}$")
        String phoneNumber,
        UUID locationId,
        String companyName  // Company/organization name (required for industry workers)
) {
}

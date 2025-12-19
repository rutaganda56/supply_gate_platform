package org.example.supply_gate_26514.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private UserEnum userType;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    @Column(name = "company_name")
    private String companyName; // Company/organization name for industry workers
    
    // Password reset fields
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;
    
    // Two-factor authentication fields
    @Column(name = "two_factor_code_hash", length = 255)
    private String twoFactorCodeHash; // Hashed 2FA code (never store plain text)
    
    @Column(name = "two_factor_code_expiry")
    private LocalDateTime twoFactorCodeExpiry;
    
    @Column(name = "two_factor_attempts")
    private Integer twoFactorAttempts; // Track failed attempts
    
    @Column(name = "two_factor_session_id", length = 255)
    private String twoFactorSessionId; // Temporary session ID for 2FA flow
    
    @CreationTimestamp
    private LocalDateTime creationDate;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @ManyToOne
    @JoinColumn(name = "location_id")
    @JsonBackReference("user-location")
    private Location location;
    @OneToOne( mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-store")
    private Store store;
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-review")
    private List<Review> review;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public List<Review> getReview() {
        return review;
    }

    public void setReview(List<Review> review) {
        this.review = review;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UserEnum getUserType() {
        return userType;
    }

    public void setUserType(UserEnum userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public LocalDateTime getPasswordResetTokenExpiry() {
        return passwordResetTokenExpiry;
    }

    public void setPasswordResetTokenExpiry(LocalDateTime passwordResetTokenExpiry) {
        this.passwordResetTokenExpiry = passwordResetTokenExpiry;
    }

    public String getTwoFactorCodeHash() {
        return twoFactorCodeHash;
    }

    public void setTwoFactorCodeHash(String twoFactorCodeHash) {
        this.twoFactorCodeHash = twoFactorCodeHash;
    }

    public LocalDateTime getTwoFactorCodeExpiry() {
        return twoFactorCodeExpiry;
    }

    public void setTwoFactorCodeExpiry(LocalDateTime twoFactorCodeExpiry) {
        this.twoFactorCodeExpiry = twoFactorCodeExpiry;
    }

    public Integer getTwoFactorAttempts() {
        return twoFactorAttempts != null ? twoFactorAttempts : 0;
    }

    public void setTwoFactorAttempts(Integer twoFactorAttempts) {
        this.twoFactorAttempts = twoFactorAttempts;
    }

    public String getTwoFactorSessionId() {
        return twoFactorSessionId;
    }

    public void setTwoFactorSessionId(String twoFactorSessionId) {
        this.twoFactorSessionId = twoFactorSessionId;
    }

    public User() {
    }
}

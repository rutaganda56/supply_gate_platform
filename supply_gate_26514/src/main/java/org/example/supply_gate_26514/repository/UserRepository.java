package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.model.UserEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByUserId(UUID userId);

    User findByUsername(String username);
    
    User findByEmail(String email);
    
    User findByPasswordResetToken(String token);
    
    User findByTwoFactorSessionId(String sessionId);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    User findByUsernameOrEmail(@org.springframework.data.repository.query.Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * Finds all distinct company names from industry workers.
     * Used to populate the company selection dropdown for suppliers.
     */
    @Query("SELECT DISTINCT u.companyName FROM User u WHERE u.userType IN :userTypes AND u.companyName IS NOT NULL AND u.companyName != '' ORDER BY u.companyName ASC")
    List<String> findDistinctCompanyNamesByUserTypeIn(List<UserEnum> userTypes);
    
    /**
     * Finds industry workers by company name.
     * Used to find reviewers for a specific company.
     */
    List<User> findByCompanyNameAndUserTypeIn(String companyName, List<UserEnum> userTypes);
    
    /**
     * Finds all users with specified user types.
     * Used to get all industry workers.
     */
    List<User> findByUserTypeIn(List<UserEnum> userTypes);
}

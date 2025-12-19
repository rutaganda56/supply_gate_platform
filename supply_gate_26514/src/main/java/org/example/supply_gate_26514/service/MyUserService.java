package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.UserRelatedInfo;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserService implements UserDetailsService {
    @Autowired
    UserRepository userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user by username or email: " + username);
        // Use the combined query to find by username OR email
        User user = userRepo.findByUsernameOrEmail(username);
        
        if (user == null) {
            System.out.println("User not found by username or email: " + username);
            throw new UsernameNotFoundException("User not found with username or email: " + username);
        }
        String storedPassword = user.getPassword();
        System.out.println("User found: " + username);
        System.out.println("Stored password hash length: " + (storedPassword != null ? storedPassword.length() : 0));
        System.out.println("Stored password hash (first 30 chars): " + (storedPassword != null && storedPassword.length() > 30 ? storedPassword.substring(0, 30) + "..." : storedPassword));
        
        // Check if password has quotes or whitespace and fix it
        if (storedPassword != null) {
            boolean needsUpdate = false;
            String cleanedPassword = storedPassword;
            
            // Remove quotes if present
            if (cleanedPassword.startsWith("\"") && cleanedPassword.endsWith("\"")) {
                System.out.println("WARNING: Password hash has quotes! Removing them...");
                cleanedPassword = cleanedPassword.substring(1, cleanedPassword.length() - 1);
                needsUpdate = true;
            }
            
            // Remove whitespace if present
            String trimmedPassword = cleanedPassword.trim();
            if (!trimmedPassword.equals(cleanedPassword)) {
                System.out.println("WARNING: Password hash has whitespace! Trimming...");
                cleanedPassword = trimmedPassword;
                needsUpdate = true;
            }
            
            // Update user if password was cleaned
            if (needsUpdate) {
                System.out.println("Fixing password in database for user: " + username);
                user.setPassword(cleanedPassword);
                // Note: We can't save here because we're in UserDetailsService
                // The password will be used as-is for this login attempt
                // You may need to manually fix the database or create a migration
            }
            
            // Update the user object with cleaned password for this request
            user.setPassword(cleanedPassword);
        }
        return new UserRelatedInfo(user);
    }
}

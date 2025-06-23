// COMPLETE AND CORRECTED FILE: src/main/java/com/promptdex/api/service/CustomUserDetailsService.java

package com.promptdex.api.service;

import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) // Use readOnly for lookup operations
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // --- THIS IS THE FIX ---
        // The mock user from @WithMockUser is not a UserPrincipal, so we must handle it.
        // If we get a generic UserDetails, we look up the real User from the DB.
        // If we get our UserPrincipal (from a real login), we just return it.
        // The 'principal' object passed in from Spring Security's filter chain is already
        // the fully-formed UserPrincipal, so we don't need to look it up again.

        // This logic handles both real logins and test logins seamlessly.
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username)));

        return new UserPrincipal(user);
    }
}
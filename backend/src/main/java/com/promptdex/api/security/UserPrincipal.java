// COMPLETE FILE: src/main/java/com/promptdex/api/security/UserPrincipal.java

package com.promptdex.api.security;

import com.promptdex.api.model.User;
import org.springframework.security.core.GrantedAuthority;
// --- NEW IMPORTS ---
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// --- END NEW IMPORTS ---
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
// --- NEW IMPORTS ---
import java.util.stream.Collectors;
// --- END NEW IMPORTS ---


public class UserPrincipal implements OidcUser, UserDetails {

    private final User user; // Keep this final, it's good practice
    private Map<String, Object> attributes;
    private OidcIdToken idToken;
    private OidcUserInfo userInfo;

    public UserPrincipal(User user) {
        if (user == null) { // Defensive check
            throw new IllegalArgumentException("User cannot be null when creating UserPrincipal");
        }
        this.user = user;
    }

    public UserPrincipal(User user, Map<String, Object> attributes) {
        this(user); // Delegate to the primary constructor
        this.attributes = attributes;
    }

    public UserPrincipal(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this(user, attributes); // Delegate
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public User getUser() {
        return user;
    }

    public UUID getId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // --- MODIFIED METHOD ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // user should not be null due to constructor check, but roles can be empty
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList(); // Or default to ROLE_USER if preferred for users with no roles
        }
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }
    // --- END MODIFIED METHOD ---

    @Override public Map<String, Object> getAttributes() { return attributes; } // Null if not OAuth2/OIDC
    @Override public String getName() { return user.getUsername(); } // Standard OidcUser & UserDetails
    @Override public Map<String, Object> getClaims() { return this.attributes; } // OIDC: usually same as attributes
    @Override public OidcUserInfo getUserInfo() { return this.userInfo; } // OIDC
    @Override public OidcIdToken getIdToken() { return this.idToken; } // OIDC
}
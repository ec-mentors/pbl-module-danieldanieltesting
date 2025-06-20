package com.promptdex.api.security;

import com.promptdex.api.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class UserPrincipal implements OidcUser, UserDetails {

    private final User user;
    private Map<String, Object> attributes;
    private OidcIdToken idToken;
    private OidcUserInfo userInfo;

    // --- NEW: Constructor for local login (UserDetails) ---
    public UserPrincipal(User user) {
        this.user = user;
    }

    // Constructor for plain OAuth2 (GitHub)
    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // Constructor for OIDC (Google)
    public UserPrincipal(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    // --- NEW: Public getter for the wrapped user entity ---
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
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public String getName() { return user.getUsername(); }
    @Override public Map<String, Object> getClaims() { return this.attributes; }
    @Override public OidcUserInfo getUserInfo() { return this.userInfo; }
    @Override public OidcIdToken getIdToken() { return this.idToken; }
}
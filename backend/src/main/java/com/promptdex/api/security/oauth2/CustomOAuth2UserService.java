package com.promptdex.api.security.oauth2;

import com.promptdex.api.exception.OAuth2AuthenticationProcessingException;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set; // <<< --- ADDED IMPORT

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            // This is a general catch-all. Specific exceptions should be handled or wrapped.
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    @Transactional // This method is public so it can be called by CustomOidcUserService
    public UserPrincipal processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");

        // GitHub specific: email might be null, try to construct one from login
        if (!StringUtils.hasText(email)) {
            if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
                String username = oAuth2User.getAttribute("login"); // GitHub's username
                if (StringUtils.hasText(username)) {
                    email = username + "@users.noreply.github.com"; // Placeholder email
                } else {
                    // If login is also null, this is an issue.
                    throw new OAuth2AuthenticationProcessingException("Login and Email not found from GitHub OAuth2 provider");
                }
            } else {
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider: " + registrationId);
            }
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Optional: Update user details if they have changed with the provider
            // user.setName(oAuth2User.getAttribute("name")); // Example
            // userRepository.save(user);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2User, email, registrationId);
        }
        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    // Changed signature slightly to pass oAuth2UserRequest for more context if needed
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User, String email, String registrationId) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        user.setEmail(email);
        // User's name might be available, e.g., oAuth2User.getAttribute("name")
        // For username, ensure it's unique
        user.setUsername(generateUniqueUsername(oAuth2User, email));

        // --- MODIFICATION: Assign default role ---
        user.setRoles(Set.of("ROLE_USER"));
        // --- END MODIFICATION ---

        return userRepository.save(user);
    }

    private String generateUniqueUsername(OAuth2User oAuth2User, String email) {
        String username = null;
        // Try 'login' (GitHub), 'preferred_username' (OIDC common), or 'name' as fallbacks
        if (oAuth2User.getAttribute("login") != null) {
            username = oAuth2User.getAttribute("login");
        } else if (oAuth2User.getAttribute("preferred_username") != null) {
            username = oAuth2User.getAttribute("preferred_username");
        } else if (oAuth2User.getAttribute("name") != null) {
            username = oAuth2User.getAttribute("name");
        } else {
            username = email.split("@")[0];
        }

        // Sanitize and ensure uniqueness
        username = username.replaceAll("[^a-zA-Z0-9_.-]", "").toLowerCase();
        if (!StringUtils.hasText(username)) { // Handle cases where username becomes empty after sanitization
            username = "user" + System.currentTimeMillis(); // Fallback to a highly unique name
        }

        String baseUsername = username;
        int counter = 1;
        // Ensure the generated username isn't excessively long for DB constraints
        final int MAX_USERNAME_LENGTH = 50; // Or whatever your DB constraint is
        if (username.length() > MAX_USERNAME_LENGTH) {
            username = username.substring(0, MAX_USERNAME_LENGTH);
            baseUsername = username; // Update baseUsername if truncated
        }

        while (userRepository.existsByUsername(username)) {
            String suffix = String.valueOf(counter++);
            int availableLength = MAX_USERNAME_LENGTH - suffix.length();
            if (baseUsername.length() > availableLength) {
                username = baseUsername.substring(0, availableLength) + suffix;
            } else {
                username = baseUsername + suffix;
            }
        }
        return username;
    }
}
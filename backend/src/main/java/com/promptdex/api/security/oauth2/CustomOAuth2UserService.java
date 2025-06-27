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
import java.util.Set;

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
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    @Transactional
    public UserPrincipal processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        if (!StringUtils.hasText(email)) {
            if (registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())) {
                String username = oAuth2User.getAttribute("login");
                if (StringUtils.hasText(username)) {
                    email = username + "@users.noreply.github.com";
                } else {
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
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2User, email, registrationId);
        }
        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User, String email, String registrationId) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        user.setEmail(email);
        user.setUsername(generateUniqueUsername(oAuth2User, email));
        user.setRoles(Set.of("ROLE_USER"));
        return userRepository.save(user);
    }

    private String generateUniqueUsername(OAuth2User oAuth2User, String email) {
        String username = null;
        if (oAuth2User.getAttribute("login") != null) {
            username = oAuth2User.getAttribute("login");
        } else if (oAuth2User.getAttribute("preferred_username") != null) {
            username = oAuth2User.getAttribute("preferred_username");
        } else if (oAuth2User.getAttribute("name") != null) {
            username = oAuth2User.getAttribute("name");
        } else {
            username = email.split("@")[0];
        }
        username = username.replaceAll("[^a-zA-Z0-9_.-]", "").toLowerCase();
        if (!StringUtils.hasText(username)) {
            username = "user" + System.currentTimeMillis();
        }
        String baseUsername = username;
        int counter = 1;
        final int MAX_USERNAME_LENGTH = 50;
        if (username.length() > MAX_USERNAME_LENGTH) {
            username = username.substring(0, MAX_USERNAME_LENGTH);
            baseUsername = username;
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
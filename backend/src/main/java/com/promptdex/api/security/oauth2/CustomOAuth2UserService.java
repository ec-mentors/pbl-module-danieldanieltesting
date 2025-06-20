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
                email = username + "@users.noreply.github.com";
            } else {
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
            }
        }
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = registerNewUser(oAuth2User, email, registrationId);
        }
        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2User oAuth2User, String email, String registrationId) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        user.setEmail(email);
        user.setUsername(generateUniqueUsername(oAuth2User, email));
        return userRepository.save(user);
    }
    private String generateUniqueUsername(OAuth2User oAuth2User, String email) {
        String username;
        if (oAuth2User.getAttribute("login") != null) {
            username = oAuth2User.getAttribute("login");
        } else {
            username = email.split("@")[0];
        }
        username = username.replaceAll("[^a-zA-Z0-9_.-]", "").toLowerCase();
        String baseUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }
}
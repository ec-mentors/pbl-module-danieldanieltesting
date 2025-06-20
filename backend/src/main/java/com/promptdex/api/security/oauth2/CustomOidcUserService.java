package com.promptdex.api.security.oauth2;

import com.promptdex.api.model.User;
import com.promptdex.api.security.UserPrincipal;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error; // --- IMPORT ---
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final CustomOAuth2UserService customOAuth2UserService;

    public CustomOidcUserService(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            UserPrincipal userPrincipal = customOAuth2UserService.processOAuth2User(userRequest, oidcUser);

            // --- FIX 1: Get the User entity from the principal before calling the constructor ---
            User user = userPrincipal.getUser();

            return new UserPrincipal(
                    user,
                    oidcUser.getAttributes(),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo()
            );
        } catch (Exception ex) {
            // --- FIX 2: Correctly construct the exception with an OAuth2Error object ---
            OAuth2Error oauth2Error = new OAuth2Error("processing_error", ex.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, ex.getMessage(), ex);
        }
    }
}
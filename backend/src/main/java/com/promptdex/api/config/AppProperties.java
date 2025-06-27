package com.promptdex.api.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final OAuth2 oauth2 = new OAuth2();
    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();
        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }
        public void setAuthorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
        }
    }
    public OAuth2 getOauth2() {
        return oauth2;
    }
}
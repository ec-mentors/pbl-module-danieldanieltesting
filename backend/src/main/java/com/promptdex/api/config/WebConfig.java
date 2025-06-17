package com.promptdex.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply this rule to all API paths
                .allowedOrigins("http://localhost:5173") // Allow requests from your Vite frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Explicitly allow methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // Allow credentials (e.g., cookies, auth headers)
                .maxAge(3600); // Cache the preflight response for 1 hour
    }
}
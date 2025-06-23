package com.promptdex.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc // Important for WebMvcConfigurer to be picked up
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply this rule specifically to API paths
                // --- MODIFIED ---
                // Allow multiple origins for local development (main frontend + admin frontend)
                .allowedOrigins(
                        "http://localhost:5173", // Standard Vite port for main frontend
                        "http://localhost:5174", // Possible Vite port for admin frontend
                        "http://localhost:5175"  // Another possible Vite port
                        // Add your production frontend URLs here when you deploy
                        // e.g., "https://www.promptdex.com", "https://admin.promptdex.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Explicitly list allowed methods
                .allowedHeaders("*") // Allow all headers for simplicity here, SecurityConfig can be more specific
                .allowCredentials(true) // Allow sending credentials (cookies, auth headers)
                .maxAge(3600); // Cache preflight (OPTIONS) request for 1 hour
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 1. Create our custom ObjectMapper for consistent date handling
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Teaches Jackson about Java 8 date/time types
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Ensures dates are ISO-8601 strings

        // 2. Create a new message converter that uses our custom ObjectMapper
        MappingJackson2HttpMessageConverter customJsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        // 3. Add our custom converter to the beginning of Spring's list of converters.
        //    This ensures it's used for all JSON serialization/deserialization.
        converters.add(0, customJsonConverter);
    }
}
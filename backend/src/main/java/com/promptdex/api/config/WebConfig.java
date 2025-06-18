// src/main/java/com/promptdex/api/config/WebConfig.java
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
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    // --- THIS IS THE EXISTING, CORRECT CORS CONFIGURATION ---
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply this rule to all API paths
                .allowedOrigins("http://localhost:5173") // Allow requests from your Vite frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Explicitly allow methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // Allow credentials (e.g., cookies, auth headers)
                .maxAge(3600); // Cache the preflight response for 1 hour
    }

    // --- THIS IS THE NEW, DEFINITIVE FIX FOR JSON DATE FORMATTING ---
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 1. Create our custom ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Teaches Jackson about Java 8 dates
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Enforces ISO-8601 strings

        // 2. Create a new message converter that uses our custom ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        // 3. Add our custom converter to the list of converters Spring will use.
        //    By adding it first, we ensure it's used for all JSON operations.
        converters.add(0, converter);
    }
}
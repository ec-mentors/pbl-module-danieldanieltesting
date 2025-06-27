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
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") 
                .allowedOrigins(
                        "http://localhost:5173", 
                        "http://localhost:5174", 
                        "http://localhost:5175"  
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") 
                .allowedHeaders("*") 
                .allowCredentials(true) 
                .maxAge(3600); 
    }
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) 
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); 
        MappingJackson2HttpMessageConverter customJsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(0, customJsonConverter);
    }
}
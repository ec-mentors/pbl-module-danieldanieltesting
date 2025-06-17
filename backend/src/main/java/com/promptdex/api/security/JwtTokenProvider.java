package com.promptdex.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    // Inject the JWT secret key from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Define the token expiration time in milliseconds (e.g., 24 hours)
    private final long jwtExpirationInMs = 24 * 60 * 60 * 1000;

    // Generate a JWT for a successfully authenticated user
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // Extract the username from a given JWT
    public String getUsernameFromJWT(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Validate the token (checks signature and expiration)
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception ex) {
            // Can add more specific exception handling here later (e.g., ExpiredJwtException)
            // For now, any exception means the token is invalid.
        }
        return false;
    }

    // Helper method to extract a specific claim from the token
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        final Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
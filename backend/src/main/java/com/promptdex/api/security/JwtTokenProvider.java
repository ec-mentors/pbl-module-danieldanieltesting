package com.promptdex.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final long jwtExpirationInMs = 24 * 60 * 60 * 1000;

    public JwtTokenProvider() {}

    public String generateToken(Authentication authentication) {
        System.out.println("\n\n--- [DEBUG STEP 3] --- JwtTokenProvider: generateToken() ENTERED ---");
        System.out.println("--- [DEBUG STEP 3] --- Principal type received: " + authentication.getPrincipal().getClass().getName());
        System.out.println("--- [DEBUG STEP 3] --- Attempting to cast to UserPrincipal...\n\n");

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String username = userPrincipal.getUsername();

        return generateTokenFromUsername(username);
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // ... other methods remain the same ...
    public String getUsernameFromJWT(String token) { return getClaimFromToken(token, Claims::getSubject); }
    public boolean validateToken(String authToken) { try { SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes()); Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken); return true; } catch (Exception ex) {} return false; }
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) { SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes()); final Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); return claimsResolver.apply(claims); }
}
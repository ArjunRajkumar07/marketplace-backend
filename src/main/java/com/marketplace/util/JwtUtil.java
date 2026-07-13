package com.marketplace.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret key used to sign the JWT token
    // Must be at least 256 bits (32 characters) long
    private static final String SECRET = "marketplace_secret_key_2025_secure_enough";

    // Token expires in 24 hours (in milliseconds)
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    // Creates a Key object from our secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Generates a JWT token for a logged-in user
    public String generateToken(String email, String role, Integer userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Reads the email from inside the token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Reads the role from inside the token
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // Checks if the token is still valid (not expired)
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }

    // Decodes and reads all data inside the token
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
package org.example.project_webservice_it211.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt-secret}")
    private String secret;

    @Value("${jwt-expired}")
    private long expiration;

    @Value("${jwt-refresh-expired}")
    private long refreshExpiration;

    private SecretKey signingKey;

    @PostConstruct
    private void init() {
        byte[] keyBytes = Arrays.copyOf(
                secret.getBytes(StandardCharsets.UTF_8),
                32
        );
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateRefreshToken(String username) {
        return UUID.randomUUID().toString();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

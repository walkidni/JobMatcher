package com.walid.jobmatcher.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;
    private final long jwtExpirationMs = 86400000; // 24 hours

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            logger.error("JWT secret key is not configured");
            throw new IllegalStateException("JWT secret key is not configured");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT secret key initialized successfully");
    }

    public String generateToken(String username, String role, String fullName) {
        logger.info("Generating token for username: {}, role: {}, fullName: {}", username, role, fullName);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", fullName);
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
                
        logger.info("Token generated successfully");
        return token;
    }

    // For backward compatibility (can be removed if not needed)
    public String generateToken(String username, String role) {
        return generateToken(username, role, "");
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            logger.info("Extracted username from token: {}", username);
            return username;
        } catch (JwtException e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            throw e;
        }
    }

    public String extractRole(String token) {
        try {
            String role = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
            logger.info("Extracted role from token: {}", role);
            return role;
        } catch (JwtException e) {
            logger.error("Error extracting role from token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            logger.info("Validating token");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                logger.warn("Token is expired. Expiration: {}", claims.getExpiration());
                return false;
            }

            // Check if token has required claims
            if (claims.get("role") == null) {
                logger.warn("Token is missing role claim");
                return false;
            }

            // Check if token has subject (username)
            if (claims.getSubject() == null) {
                logger.warn("Token is missing subject (username)");
                return false;
            }

            logger.info("Token is valid. Claims: {}", claims);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}

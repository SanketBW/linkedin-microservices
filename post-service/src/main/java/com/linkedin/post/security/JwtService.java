
package com.linkedin.post.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public Long getUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"ACCESS".equals(
                claims.get("tokenType", String.class))) {
            throw new JwtException("Not an access token");
        }

        String subject = claims.getSubject();
        try {
            long userId = Long.parseLong(subject);
            if (userId <= 0) {
                throw new JwtException("Invalid user ID in token");
            }
            return userId;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new JwtException("Invalid user ID in token", ex);
        }
    }
}

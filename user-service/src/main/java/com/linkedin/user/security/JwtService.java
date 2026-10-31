package com.linkedin.user.security;

import com.linkedin.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt-secreteKey}")
    private String secreteKey;

    private SecretKey getSecreteKey(){
        return Keys.hmacShaKeyFor(secreteKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(User user){
        long expiration = System.currentTimeMillis() + (1000 * 60 * 15);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("tokenType", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .signWith(getSecreteKey())
                .compact();
    }

    public String generateRefreshToken(User user){
        long expiration = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 30);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("tokenType", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .signWith(getSecreteKey())
                .compact();
    }

    public Long getIdFromToken(String token){
        Claims claim = extractClaims(token);
        return Long.valueOf(claim.getSubject());
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public boolean isTokenValid(String token){
        try{
            extractClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException ex){
            return false;
        }
    }

    private Claims extractClaims(String token){
        return Jwts.parser()
                .verifyWith(getSecreteKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

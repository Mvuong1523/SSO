package com.example.demoBe.util;

import com.example.demoBe.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Should be in properties, but hardcoding for simplicity/demo as requested
    private static final String SECRET_KEY = "YourSecretKeyForJwtSigningMustBeLongEnoughToSecure";
    // 3 minutes Expiration for Access Token
    private static final long EXPIRATION_TIME = 1 * 60 * 1000;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserUid()))
                .claim("userId", user.getUserId())
                .claim("userType", user.getUserType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log error
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
